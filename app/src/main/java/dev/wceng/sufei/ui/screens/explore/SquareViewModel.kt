package dev.wceng.sufei.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.repository.PoemRepository
import kotlinx.coroutines.flow.*

enum class SquareType {
    TAG, TUNE
}

sealed interface SquareUiState {
    data object Loading : SquareUiState
    data class Success(val title: String, val items: List<String>) : SquareUiState
    data class Error(val message: String) : SquareUiState
}

@HiltViewModel(assistedFactory = SquareViewModel.Factory::class)
class SquareViewModel @AssistedInject constructor(
    @Assisted private val type: SquareType,
    private val poemRepository: PoemRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(type: SquareType): SquareViewModel
    }

    val uiState: StateFlow<SquareUiState> = flow {
        val (title, dataFlow) = when (type) {
            SquareType.TAG -> "标签广场" to poemRepository.getAllTags().map { list -> list.map { it.name } }
            SquareType.TUNE -> "词牌广场" to poemRepository.getAllTunes().map { list -> list.map { it.name } }
        }
        
        emitAll(dataFlow.map { SquareUiState.Success(title, it) })
    }
    .catch<SquareUiState> { e -> emit(SquareUiState.Error(e.message ?: "加载失败")) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SquareUiState.Loading
    )
}
