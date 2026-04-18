package dev.wceng.sufei.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val title: String, val icon: ImageVector) {
    Home("今日", Icons.Default.Home),
    Explore("万卷", Icons.Default.Search),
    Collection("枕边", Icons.Default.Favorite),
    Settings("设置", Icons.Default.Settings)
}

fun MainTab.toRoute(): Any = when (this) {
    MainTab.Home -> Home
    MainTab.Explore -> Explore
    MainTab.Collection -> Collection
    MainTab.Settings -> Settings
}
