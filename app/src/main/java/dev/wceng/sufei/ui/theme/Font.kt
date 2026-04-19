package dev.wceng.sufei.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.wceng.sufei.R

/**
 * 自定义思源宋体 (Noto Serif SC) 字体族
 * 确保在所有设备上提供一致的衬线体阅读体验
 */
val NotoSerifSC = FontFamily(
    Font(R.font.noto_serif_sc_light, FontWeight.Light),
    Font(R.font.noto_serif_sc_regular, FontWeight.Normal),
    Font(R.font.noto_serif_sc_bold, FontWeight.Bold)
)
