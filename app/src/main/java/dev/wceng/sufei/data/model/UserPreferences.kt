package dev.wceng.sufei.data.model

/**
 * 用户偏好模型 (业务层使用)
 */
data class UserPreferences(
    val favoritePoemIds: Set<String> = emptySet(),
    val fontSizeMultiplier: Float = 1.0f,
    val lineHeightMultiplier: Float = 1.0f,
    val useDynamicColor: Boolean = true,
    val fontFamilyName: String = "Serif"
)
