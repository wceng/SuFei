package dev.wceng.sufei.ui.screens.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import dev.wceng.sufei.domain.model.GroupedUserPoem
import dev.wceng.sufei.domain.usecase.GetGroupedFavoritesUseCase
import dev.wceng.sufei.domain.usecase.GetUserCreatedPoemsUseCase
import dev.wceng.sufei.util.ChineseConverter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 枕边页展示类别：收藏 / 创作（互斥） */
enum class CollectionCategory { FAVORITES, CREATED }

@HiltViewModel
class CollectionViewModel @Inject constructor(
    getGroupedFavoritesUseCase: GetGroupedFavoritesUseCase,
    getUserCreatedPoemsUseCase: GetUserCreatedPoemsUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(CollectionCategory.FAVORITES)
    val selectedCategory = _selectedCategory.asStateFlow()

    /** 按日期分组后的列表，应用搜索过滤（标题/作者/正文，简繁转换） */
    private fun filterGroups(groups: List<GroupedUserPoem>, query: String): List<GroupedUserPoem> {
        if (query.isBlank()) return groups

        val simplifiedQuery = ChineseConverter.toSimplified(query)
        return groups.mapNotNull { group ->
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
    }

    // 收藏列表（按日期分组 + 搜索过滤）
    val favoritePoems: StateFlow<List<GroupedUserPoem>> = combine(
        getGroupedFavoritesUseCase(),
        _searchQuery
    ) { groups, query ->
        filterGroups(groups, query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 用户创作列表（按日期分组 + 搜索过滤）
    val createdPoems: StateFlow<List<GroupedUserPoem>> = combine(
        getUserCreatedPoemsUseCase(),
        _searchQuery
    ) { groups, query ->
        filterGroups(groups, query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 当前类别展示的列表（收藏 / 创作互斥）
    val displayedGroups: StateFlow<List<GroupedUserPoem>> = combine(
        favoritePoems,
        createdPoems,
        _selectedCategory
    ) { favorites, created, category ->
        when (category) {
            CollectionCategory.FAVORITES -> favorites
            CollectionCategory.CREATED -> created
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: CollectionCategory) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleFavorite(poemId, isFavorite)
        }
    }
}
