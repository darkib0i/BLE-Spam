package com.videodownloader.app.ui.vault

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.videodownloader.app.download.vault.VaultItem
import com.videodownloader.app.download.vault.VaultManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VaultUiState(
    val items: List<VaultItem> = emptyList(),
    val importing: Boolean = false,
)

class VaultViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(VaultUiState())
    val state: StateFlow<VaultUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(items = VaultManager.list(getApplication())) }
        }
    }

    fun importAll(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(importing = true) }
            uris.forEach { uri -> VaultManager.import(getApplication(), uri) }
            _state.update {
                it.copy(importing = false, items = VaultManager.list(getApplication()))
            }
        }
    }

    fun delete(item: VaultItem) {
        viewModelScope.launch {
            VaultManager.delete(item)
            _state.update { it.copy(items = VaultManager.list(getApplication())) }
        }
    }
}
