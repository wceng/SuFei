package dev.wceng.sufei.data.model

import dev.wceng.sufei.R

/**
 * 用户偏好模型 (业务层使用)
 */
data class UserPreferences(
    val favorites: Map<String, Long> = emptyMap(),
    val userPoems: Map<String, Long> = emptyMap(),
    val fontSizeMultiplier: Float = 1.0f,
    val lineHeightMultiplier: Float = 1.0f,
    val useDynamicColor: Boolean = true,
    val fontFamilyName: String = "Serif",
    val dailyPoemId: String = "",
    val lastUpdateMillis: Long = 0L,
    val chineseVariant: ChineseVariant = ChineseVariant.SIMPLIFIED,
    val pinnedWidgetPoems: Map<Int, String> = emptyMap()
)

enum class ChineseVariant(val labelRes: Int) {
    SIMPLIFIED(R.string.variant_simplified),
    TRADITIONAL_HK(R.string.variant_traditional_hk),
    TRADITIONAL_TW(R.string.variant_traditional_tw)
}
