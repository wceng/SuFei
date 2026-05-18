package dev.wceng.sufei.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.R
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.repository.PoemRepository
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val poemRepository: PoemRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // 遵循 NiA 风格，将随机诗词展示为响应式流
    val uiState: StateFlow<HomeUiState> = poemRepository.getRandomUserPoem()
        .map { userPoem ->
            if (userPoem != null) {
                HomeUiState.Success(userPoem)
            } else {
                HomeUiState.Error(R.string.error_daily_poem_failed)
            }
        }
        .catch { emit(HomeUiState.Error(R.string.error_unknown)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleFavorite(poemId, isFavorite)
        }
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val userPoem: UserPoem) : HomeUiState
    data class Error(val messageRes: Int) : HomeUiState
}
