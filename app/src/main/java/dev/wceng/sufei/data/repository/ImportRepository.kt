package dev.wceng.sufei.data.repository

import kotlinx.coroutines.flow.StateFlow

sealed class ImportState {
    data object Idle : ImportState()
    data class Importing(val progress: Float) : ImportState()
    data object Success : ImportState()
    data class Error(val message: String) : ImportState()
}

interface ImportRepository {
    val importState: StateFlow<ImportState>
    suspend fun startImportIfNeeded()
}
