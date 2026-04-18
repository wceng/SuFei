package dev.wceng.sufei.ui.screens.poet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.repository.PoemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/**
 * 诗人详情页 UI 状态
 * 遵循 NiA 风格，使用 sealed interface 定义状态
 */
sealed interface PoetDetailUiState {
    data object Loading : PoetDetailUiState

    data class Success(
        val poet: Poet,
        val poems: List<UserPoem>
    ) : PoetDetailUiState

    data class Error(val message: String) : PoetDetailUiState
}

@HiltViewModel(assistedFactory = PoetDetailViewModel.Factory::class)
class PoetDetailViewModel @AssistedInject constructor(
    @Assisted private val poetId: String,
    private val poemRepository: PoemRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(poetId: String): PoetDetailViewModel
    }

    /**
     * 声明式 UI 状态流
     * 1. 监听诗人详情
     * 2. 拿到诗人后，通过 flatMapLatest 动态触发该诗人的作品集检索
     * 3. 错误捕获并转换为 Error 状态
     * 4. 使用 stateIn 转化为冷启动热执行的状态流，并设置 5 秒订阅超时以优化性能
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PoetDetailUiState> = poemRepository.getPoetById(poetId)
        .flatMapLatest { poet ->
            if (poet != null) {
                // 成功获取诗人后，串联检索其作品列表
                poemRepository.getPoemsByPoet(poet.name).map { poems ->
                    PoetDetailUiState.Success(poet, poems)
                }
            } else {
                flowOf(PoetDetailUiState.Error("未找到该诗人信息"))
            }
        }
        .catch { emit(PoetDetailUiState.Error(it.message ?: "加载失败")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PoetDetailUiState.Loading
        )

    /**
     * 示例：处理用户操作（如收藏该诗人的作品）
     * 遵循 NiA 风格，ViewModel 暴露具体的方法供 UI 调用
     */
    fun togglePoemFavorite(poemId: String, isFavorite: Boolean) {
        // TODO: 调用仓库层修改收藏状态
    }
}