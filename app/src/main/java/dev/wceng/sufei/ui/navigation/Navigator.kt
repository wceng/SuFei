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
     * 切换顶级目的地（类似安卓 TopLevel 模式，避免页面状态重置）
     */
    fun navigateToTopLevelDestination(destination: Any) {
        // 1. 检查目的地是否已在返回栈中
        val index = backStack.indexOf(destination)
        
        if (index != -1) {
            // 如果已存在（如点击当前 Tab 或回到之前的 Tab），则弹出其之上的所有页面
            // 这样由于该页面一直留在 backStack 中，其 Compose 状态（如滚动位置）会被保留
            while (backStack.size > index + 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        } else {
            // 2. 如果不存在，模拟安卓底层导航行为：
            // 弹出到根页面（起始页面），然后在其之上添加新页面
            // 这确保了起始页面（如首页）始终驻留在栈底，不会被重置
            while (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
            
            // 如果栈顶不是目标页面，则添加
            if (backStack.isNotEmpty() && backStack.last() != destination) {
                backStack.add(destination)
            } else if (backStack.isEmpty()) {
                backStack.add(destination)
            }
        }
    }
}
