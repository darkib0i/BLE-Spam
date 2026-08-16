package com.videodownloader.app.ui.vault

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.videodownloader.app.download.vault.VaultEntry
import com.videodownloader.app.download.vault.VaultManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class VaultUiState(
    val currentDir: File,
    val breadcrumb: String,
    val atRoot: Boolean,
    val entries: List<VaultEntry> = emptyList(),
    val importing: Boolean = false,
)

class VaultViewModel(app: Application) : AndroidViewModel(app) {

    private var root: File = VaultManager.rootDir(app, decoy = false)
    private var currentDir: File = root

    private val _state = MutableStateFlow(stateFor(root))
    val state: StateFlow<VaultUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    /** Switches to the real or decoy store after the PIN is entered. */
    fun openVault(decoy: Boolean) {
        root = VaultManager.rootDir(getApplication(), decoy)
        currentDir = root
        refresh()
    }

    fun currentRootDir(): File = root

    fun refresh() {
        viewModelScope.launch { emitCurrent() }
    }

    fun openFolder(folder: File) {
        if (folder.isDirectory) {
            currentDir = folder
            refresh()
        }
    }

    /** Navigates one level up. Returns false if already at the vault root. */
    fun goUp(): Boolean {
        if (currentDir == root) return false
        currentDir = currentDir.parentFile ?: root
        refresh()
        return true
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return
        VaultManager.createFolder(currentDir, name)
        refresh()
    }

    fun importAll(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val dir = currentDir
        viewModelScope.launch {
            _state.update { it.copy(importing = true) }
            uris.forEach { uri -> VaultManager.import(getApplication(), uri, dir) }
            _state.update { it.copy(importing = false) }
            emitCurrent()
        }
    }

    fun delete(entry: VaultEntry) {
        VaultManager.delete(entry)
        refresh()
    }

    fun rename(entry: VaultEntry, newName: String) {
        if (newName.isBlank()) return
        VaultManager.rename(entry, newName)
        refresh()
    }

    fun importTree(treeUri: Uri) {
        val dir = currentDir
        viewModelScope.launch {
            _state.update { it.copy(importing = true) }
            VaultManager.importTree(getApplication(), treeUri, dir)
            _state.update { it.copy(importing = false) }
            emitCurrent()
        }
    }

    private fun emitCurrent() {
        _state.value = stateFor(currentDir).copy(importing = _state.value.importing)
    }

    private fun stateFor(dir: File): VaultUiState = VaultUiState(
        currentDir = dir,
        breadcrumb = breadcrumbFor(dir),
        atRoot = dir == root,
        entries = VaultManager.list(dir),
    )

    private fun breadcrumbFor(dir: File): String {
        val rel = dir.absolutePath.removePrefix(root.absolutePath).trim('/')
        return if (rel.isBlank()) "Vault" else "Vault / ${rel.replace("/", " / ")}"
    }
}
