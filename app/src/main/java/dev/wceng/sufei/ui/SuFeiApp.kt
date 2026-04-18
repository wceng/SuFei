package dev.wceng.sufei.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.wceng.sufei.ui.navigation.Collection
import dev.wceng.sufei.ui.navigation.EntryProviderInstaller
import dev.wceng.sufei.ui.navigation.Explore
import dev.wceng.sufei.ui.navigation.Home
import dev.wceng.sufei.ui.navigation.MainTab
import dev.wceng.sufei.ui.navigation.Navigator
import dev.wceng.sufei.ui.navigation.Settings
import dev.wceng.sufei.ui.navigation.toRoute

@Composable
fun SuFeiApp(
    navigator: Navigator,
    entryProviderScopes: Set<EntryProviderInstaller>
) {
    val currentDestination = navigator.backStack.lastOrNull() ?: Home

    val selectedTab = remember(currentDestination) {
        when (currentDestination) {
            is Home -> MainTab.Home
            is Explore -> MainTab.Explore
            is Collection -> MainTab.Collection
            is Settings -> MainTab.Settings
            else -> null
        }
    }

    // NavigationSuiteScaffold 会自动处理底部导航栏或侧边导航轨道的空间
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            MainTab.entries.forEach { tab ->
                item(
                    selected = selectedTab == tab,
                    onClick = {
                        navigator.navigateToTopLevelDestination(tab.toRoute())
                    },
                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                    label = { Text(tab.title) }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        // 直接放置 NavDisplay，不再包裹在 Scaffold 中并应用 innerPadding
        // 这样各个页面的背景可以实现真正的“沉浸式”全屏
        NavDisplay(
            backStack = navigator.backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = { navigator.goBack() },
            entryProvider = entryProvider {
                entryProviderScopes.forEach { builder -> this.builder() }
            },
//            entryDecorators = listOf(
//                rememberSaveableStateHolderNavEntryDecorator(),
//                rememberViewModelStoreNavEntryDecorator()
//            ),
        )
    }
}
