package dev.wceng.sufei.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import dev.wceng.sufei.R

enum class MainTab(val titleRes: Int, val icon: ImageVector) {
    Home(R.string.tab_home, Icons.Default.Home),
    Explore(R.string.tab_explore, Icons.Default.Search),
    Collection(R.string.tab_collection, Icons.Default.Favorite),
    Settings(R.string.tab_settings, Icons.Default.Settings)
}

fun MainTab.toRoute(): Any = when (this) {
    MainTab.Home -> Home
    MainTab.Explore -> Explore()
    MainTab.Collection -> Collection
    MainTab.Settings -> Settings
}
