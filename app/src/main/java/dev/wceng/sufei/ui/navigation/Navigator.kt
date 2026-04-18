package dev.wceng.sufei.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import dagger.hilt.android.scopes.ActivityRetainedScoped

/**
 * Navigation 3 核心安装器类型定义
 */
typealias EntryProviderInstaller = EntryProviderScope<Any>.() -> Unit

/**
 * 导航管理器，管理返回栈状态
 */
class Navigator(startDestination: Any) {
    val backStack: SnapshotStateList<Any> = mutableStateListOf(startDestination)

    fun goTo(destination: Any) {
        backStack.add(destination)
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    /**
     * 切换顶级目的地（如底部导航切换）
     */
    fun navigateToTopLevelDestination(destination: Any) {
        backStack.clear()
        backStack.add(destination)
    }
}
