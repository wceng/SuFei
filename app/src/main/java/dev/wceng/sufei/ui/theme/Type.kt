package dev.wceng.sufei.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 素扉 (SuFei) 全局字体配置
 * 统一使用内置的 NotoSerifSC (思源宋体)，确保在所有 Android 版本及厂商设备上显示效果一致。
 */
val Typography = Typography(
    // 用于详情页诗词正文
    displaySmall = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    // 用于诗词标题
    headlineMedium = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    // 用于小标题（如：注释、赏析）
    titleMedium = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    // 用于标准正文、作者信息
    bodyLarge = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
