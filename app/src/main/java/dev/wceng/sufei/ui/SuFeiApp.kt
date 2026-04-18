package dev.wceng.sufei.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
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

    // 根据当前是否处于顶级页面决定是否显示导航栏
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val layoutType = if (selectedTab != null) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
    } else {
        NavigationSuiteType.None
    }

    NavigationSuiteScaffold(
        layoutType = layoutType,
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
        NavDisplay(
            backStack = navigator.backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = { navigator.goBack() },

            entryProvider = entryProvider {
                entryProviderScopes.forEach { builder -> this.builder() }
            },
            // 全局转场动画：优雅的淡入淡出
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith
                        fadeOut(animationSpec = tween(500))
            },
            popTransitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith
                        fadeOut(animationSpec = tween(500))
            },
            // 支持预测性返回手势
            predictivePopTransitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith
                        fadeOut(animationSpec = tween(500))
            }
        )
    }
}
