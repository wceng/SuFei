package dev.wceng.sufei.data.model

/**
 * 混合搜索结果模型
 */
data class SearchResult(
    val poems: List<UserPoem> = emptyList(),
    val poets: List<Poet> = emptyList()
)
