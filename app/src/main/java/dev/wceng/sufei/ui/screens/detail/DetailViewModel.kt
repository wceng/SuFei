package dev.wceng.sufei.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.model.UserPreferences
import dev.wceng.sufei.data.repository.PoemRepository
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import dev.wceng.sufei.ui.navigation.Detail
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    private val poemRepository: PoemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @Assisted val detail: Detail
) : ViewModel() {

    /**
     * 遵循 Now in Android 风格：通过组合多个数据源生成 UI 状态流
     */
    val uiState: StateFlow<DetailUiState> = combine(
        poemRepository.getUserPoemById(detail.id),
        userPreferencesRepository.userPreferences
    ) { userPoem, userPrefs ->
        if (userPoem != null) {
            DetailUiState.Success(userPoem, userPrefs)
        } else {
            DetailUiState.Error("未找到该诗词")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailUiState.Loading
    )

    fun toggleFavorite(isFavorite: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleFavorite(detail.id, isFavorite)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(detail: Detail): DetailViewModel
    }
}

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val userPoem: UserPoem, val userPreferences: UserPreferences) : DetailUiState
    data class Error(val message: String) : DetailUiState
}
