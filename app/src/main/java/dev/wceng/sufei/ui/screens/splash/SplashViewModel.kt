package dev.wceng.sufei.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.repository.ImportRepository
import dev.wceng.sufei.data.repository.ImportState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val importRepository: ImportRepository
) : ViewModel() {

    val importState: StateFlow<ImportState> = importRepository.importState

    init {
        startImport()
    }

    private fun startImport() {
        viewModelScope.launch {
            importRepository.startImportIfNeeded()
        }
    }
}
