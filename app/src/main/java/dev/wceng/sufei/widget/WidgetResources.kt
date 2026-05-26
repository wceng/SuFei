package dev.wceng.sufei.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.background
import dev.wceng.sufei.R

val LocalFeihongColor = staticCompositionLocalOf { Color(0xFFE09E87) }

@Composable
fun widgetBackgroundModifier(): GlanceModifier {
    return if (Build.VERSION.SDK_INT >= 31) {
        GlanceModifier.background(GlanceTheme.colors.background)
    } else {
        GlanceModifier.background(ImageProvider(R.drawable.widget_bg))
    }
}
