package dev.wceng.sufei.data.model

import kotlinx.serialization.Serializable

/**
 * 纯粹的诗词领域模型 (Domain Model)
 * 仅包含诗词本身的内容，不包含任何用户状态
 */
@Serializable
data class Poem(
    val id: String,              // UUID
    val sourceUrl: String,       // 原始数据的 URL
    val title: String,           // 标题
    val author: String,          // 作者
    val dynasty: String,         // 朝代
    val content: String,         // 正文
    val tags: List<String>,      // 标签
    val notes: String? = null,   // 注释
    val translation: String? = null, // 译文
    val intro: String? = null,   // 赏析
    val background: String? = null // 创作背景
)

/**
 * 结合了用户状态的诗词包装类 (类似 Now in Android 的 UserNewsResource)
 */
data class UserPoem(
    val poem: Poem,
    val isFavorite: Boolean
) {
    /**
     * 辅助构造函数：从 Poem 和 UserPreferences 中创建 UserPoem
     */
    constructor(poem: Poem, userPreferences: UserPreferences) : this(
        poem = poem,
        isFavorite = userPreferences.favoritePoemIds.contains(poem.id)
    )
}
