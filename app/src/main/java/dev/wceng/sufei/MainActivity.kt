package dev.wceng.sufei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.model.UserPreferences
import dev.wceng.sufei.data.repository.ImportState
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import dev.wceng.sufei.ui.SuFeiApp
import dev.wceng.sufei.ui.screens.splash.SplashScreen
import dev.wceng.sufei.ui.screens.splash.SplashViewModel
import dev.wceng.sufei.ui.theme.SuFeiTheme
import dagger.hilt.android.AndroidEntryPoint
import dev.wceng.sufei.ui.navigation.EntryProviderInstaller
import dev.wceng.sufei.ui.navigation.Navigator
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 注入 Hilt 管理的全局导航器
    @Inject
    lateinit var navigator: Navigator

    // 注入所有模块注册的导航入口
    @Inject
    lateinit var entryProviderScopes: Set<@JvmSuppressWildcards EntryProviderInstaller>

    // 直接注入用户偏好仓库
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 直接从仓库收集用户偏好流
            val userPreferences by userPreferencesRepository.userPreferences
                .collectAsState(initial = UserPreferences())

            SuFeiTheme(
                dynamicColor = userPreferences.useDynamicColor
            ) {
                val splashViewModel: SplashViewModel = hiltViewModel()
                val importState by splashViewModel.importState.collectAsState()

                if (importState is ImportState.Success) {
                    // 使用注入的单例 navigator，确保全站状态同步
                    SuFeiApp(
                        navigator = navigator,
                        entryProviderScopes = entryProviderScopes
                    )
                } else {
                    SplashScreen(onInitComplete = {})
                }
            }
        }
    }
}
