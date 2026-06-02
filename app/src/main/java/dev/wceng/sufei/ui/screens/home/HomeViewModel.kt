package dev.wceng.sufei.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.R
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.repository.PoemRepository
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val poemRepository: PoemRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // 内存中的诗词堆栈，仅负责控制“显示哪些卡片”
    private val _userPoems = MutableStateFlow<List<UserPoem>>(emptyList())

    // 真正的 UI 状态流：将内存堆栈与数据库偏好实时合并 (NiA 风格 UDF)
    val uiState: StateFlow<HomeUiState> = combine(
        _userPoems,
        userPreferencesRepository.userPreferences
    ) { poems, prefs ->
        if (poems.isEmpty()) {
            HomeUiState.Loading
        } else {
            HomeUiState.Success(
                userPoems = poems.map { userPoem ->
                    // 利用 UserPoem 的构造函数，结合最新的 prefs 自动计算最新的收藏状态 (isFavorite)
                    UserPoem(userPoem.poem, prefs)
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    init {
        loadInitialPoems()
    }

    private fun loadInitialPoems() {
        viewModelScope.launch {
            try {
                // 直接获取 10 首高质量随机诗词作为初始列表
                val randomPoems = poemRepository.getRandomUserPoems(10).firstOrNull() ?: emptyList()
                _userPoems.value = randomPoems
            } catch (e: Exception) {
                // 异常处理：列表为空时 uiState 保持 Loading 或可扩展 Error 状态
            }
        }
    }

    fun onCardSwiped() {
        // 仅移除顶层卡片 ID，触发 uiState 的响应式重新组合
        _userPoems.update { it.drop(1) }
        checkAndPreload()
    }

    private fun checkAndPreload() {
        if (_userPoems.value.size < 4) {
            viewModelScope.launch {
                try {
                    // 补充获取高质量随机诗词
                    val morePoems = poemRepository.getRandomUserPoems(5).firstOrNull() ?: emptyList()
                    _userPoems.update { current ->
                        val existingIds = current.map { it.poem.id }.toSet()
                        current + morePoems.filter { it.poem.id !in existingIds }
                    }
                } catch (e: Exception) {
                    // 预加载失败不中断 UI
                }
            }
        }
    }

    fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            // 纯副作用操作：更新数据库中的偏好状态
            // UI 的“红心”状态会通过上面的 combine 流自动响应式地亮起或熄灭
            userPreferencesRepository.toggleFavorite(poemId, isFavorite)
        }
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val userPoems: List<UserPoem>) : HomeUiState
    data class Error(val messageRes: Int) : HomeUiState
}
