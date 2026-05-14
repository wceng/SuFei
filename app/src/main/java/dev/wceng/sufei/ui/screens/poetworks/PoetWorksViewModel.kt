package dev.wceng.sufei.ui.screens.poetworks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.repository.PoemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

sealed interface PoetWorksUiState {
    data class Success(
        val poems: List<UserPoem>,
        val poetName: String
    ) : PoetWorksUiState

    data object Loading : PoetWorksUiState
    data class Error(val message: String) : PoetWorksUiState
}

@HiltViewModel(assistedFactory = PoetWorksViewModel.Factory::class)
class PoetWorksViewModel @AssistedInject constructor(
    @Assisted private val poetName: String,
    private val poemRepository: PoemRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PoetWorksUiState> = poemRepository.getPoemsByPoet(poetName)
        .map { poems ->
            if (poems.isNotEmpty()) {
                PoetWorksUiState.Success(poems = poems, poetName = poetName)
            } else {
                PoetWorksUiState.Error("未找到该诗人的作品")
            }
        }
        .catch { emit(PoetWorksUiState.Error(it.message ?: "加载失败")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PoetWorksUiState.Loading
        )

    @AssistedFactory
    interface Factory {
        fun create(poetName: String): PoetWorksViewModel
    }
}
