package dev.wceng.sufei.ui.screens.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import dev.wceng.sufei.domain.model.GroupedUserPoem
import dev.wceng.sufei.domain.usecase.GetGroupedFavoritesUseCase
import dev.wceng.sufei.util.ChineseConverter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    getGroupedFavoritesUseCase: GetGroupedFavoritesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // 观察带用户状态的收藏列表，并按日期分组且支持搜索过滤
    val favoritePoems: StateFlow<List<GroupedUserPoem>> = combine(
        getGroupedFavoritesUseCase(),
        _searchQuery
    ) { groupedPoems, query ->
        if (query.isBlank()) return@combine groupedPoems

        val simplifiedQuery = ChineseConverter.toSimplified(query)
        groupedPoems.mapNotNull { group ->
            val filteredPoems = group.poems.filter { userPoem ->
                val poem = userPoem.poem
                ChineseConverter.toSimplified(poem.title).contains(simplifiedQuery, ignoreCase = true) ||
                        ChineseConverter.toSimplified(poem.author).contains(simplifiedQuery, ignoreCase = true) ||
                        ChineseConverter.toSimplified(poem.content).contains(simplifiedQuery, ignoreCase = true)
            }
            if (filteredPoems.isNotEmpty()) {
                group.copy(poems = filteredPoems)
            } else {
                null
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleFavorite(poemId, isFavorite)
        }
    }
}
