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
import dev.wceng.sufei.data.tts.TtsManager
import dev.wceng.sufei.ui.navigation.Detail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    private val poemRepository: PoemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val ttsManager: TtsManager,
    @Assisted val detail: Detail
) : ViewModel() {

    val isTtsPlaying = ttsManager.isPlaying
    val currentSentenceIndex = ttsManager.currentSentenceIndex

    /**
     * 发射诗人 ID 的渠道
     */
    private val _poetIdChannel = Channel<String>(Channel.BUFFERED)
    val poetIdFlow: Flow<String> = _poetIdChannel.receiveAsFlow()

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

    /**
     * 通过诗人名称查询 ID，并通过 Channel 发射
     */
    fun navigateToPoetByName(poetName: String) {
        viewModelScope.launch {
            val poetId = poemRepository.getPoetIdByName(poetName)
            if (poetId != null) {
                _poetIdChannel.send(poetId)
            }
        }
    }

    fun toggleFavorite(isFavorite: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleFavorite(detail.id, isFavorite)
        }
    }

    fun toggleTts(sentences: List<String>) {
        if (isTtsPlaying.value) {
            ttsManager.stop()
        } else {
            ttsManager.speak(sentences)
        }
    }

    fun stopTts() {
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        _poetIdChannel.close()
        ttsManager.release()
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
