package com.videodownloader.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.videodownloader.app.download.DownloadEngine
import com.videodownloader.app.download.DownloadStatus
import com.videodownloader.app.download.DownloadedFile
import com.videodownloader.app.download.Quality
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class HomeUiState(
    val url: String = "",
    val quality: Quality = Quality.BEST,
    val status: DownloadStatus = DownloadStatus.Idle,
    val library: List<DownloadedFile> = emptyList(),
)

class DownloadViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var processId: String? = null
    private var job: Job? = null

    init {
        refreshLibrary()
    }

    fun onUrlChange(value: String) = _state.update { it.copy(url = value) }

    fun onQualityChange(quality: Quality) = _state.update { it.copy(quality = quality) }

    val isRunning: Boolean
        get() = _state.value.status is DownloadStatus.Preparing ||
            _state.value.status is DownloadStatus.Downloading

    fun start() {
        val current = _state.value
        val url = current.url.trim()
        if (isRunning) return
        if (!url.startsWith("http")) {
            _state.update { it.copy(status = DownloadStatus.Error("Paste a valid video link.")) }
            return
        }

        val id = UUID.randomUUID().toString()
        processId = id
        _state.update { it.copy(status = DownloadStatus.Preparing) }

        job = viewModelScope.launch {
            runCatching {
                DownloadEngine.download(
                    context = getApplication(),
                    url = url,
                    quality = current.quality,
                    processId = id,
                ) { progress, eta, line ->
                    _state.update {
                        it.copy(status = DownloadStatus.Downloading(progress, eta, line.trim()))
                    }
                }
            }.onSuccess { file ->
                _state.update { it.copy(status = DownloadStatus.Success(file)) }
                refreshLibrary()
            }.onFailure { t ->
                _state.update {
                    it.copy(status = DownloadStatus.Error(friendlyError(t)))
                }
            }
            processId = null
        }
    }

    fun cancel() {
        processId?.let { DownloadEngine.cancel(it) }
        job?.cancel()
        processId = null
        _state.update { it.copy(status = DownloadStatus.Idle) }
    }

    fun reset() = _state.update { it.copy(status = DownloadStatus.Idle) }

    fun clearUrl() = _state.update { it.copy(url = "", status = DownloadStatus.Idle) }

    fun refreshLibrary() {
        viewModelScope.launch {
            val files = runCatching { DownloadEngine.listDownloads(getApplication()) }
                .getOrDefault(emptyList())
            _state.update { it.copy(library = files) }
        }
    }

    private fun friendlyError(t: Throwable): String {
        val raw = t.message ?: "Something went wrong."
        return when {
            raw.contains("Unsupported URL", true) -> "That link isn't supported."
            raw.contains("Private", true) -> "This video is private."
            raw.contains("network", true) || raw.contains("resolve", true) ->
                "Network error — check your connection."
            raw.length > 160 -> raw.take(160) + "…"
            else -> raw
        }
    }
}
