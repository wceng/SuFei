package dev.wceng.sufei

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.AndroidEntryPoint
import dev.wceng.sufei.data.model.UserPreferences
import dev.wceng.sufei.widget.DailyPoemWidgetReceiver
import dev.wceng.sufei.data.repository.ImportState
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import dev.wceng.sufei.ui.SuFeiApp
import dev.wceng.sufei.ui.navigation.EntryProviderInstaller
import dev.wceng.sufei.ui.navigation.Detail
import dev.wceng.sufei.ui.navigation.Explore
import dev.wceng.sufei.ui.navigation.Navigator
import dev.wceng.sufei.ui.screens.splash.SplashScreen
import dev.wceng.sufei.ui.screens.splash.SplashViewModel
import dev.wceng.sufei.ui.theme.SuFeiTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

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
                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                            GlanceAppWidgetManager(this@MainActivity)
                                .setWidgetPreviews(DailyPoemWidgetReceiver::class)
                        }
                    }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        // Widget click 通过 actionStartActivity 传递 poem_id extra，不设置 action
        val poemId = intent.getStringExtra("poem_id")
        if (poemId != null) {
            navigator.goTo(Detail(id = poemId))
            return
        }

        when (intent.action) {
            Intent.ACTION_PROCESS_TEXT -> {
                val query = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
                navigator.goTo(Explore(query = query))
            }

            Intent.ACTION_VIEW -> {
                val data = intent.data
                if (data?.scheme == "sufei" && data.host == "explore") {
                    val query = data.getQueryParameter("query")
                    val tag = data.getQueryParameter("tag")
                    val tune = data.getQueryParameter("tune")
                    val dynasty = data.getQueryParameter("dynasty")
                    navigator.goTo(
                        Explore(
                            query = query,
                            tag = tag,
                            tune = tune,
                            dynasty = dynasty
                        )
                    )
                }
            }
        }
    }
}
