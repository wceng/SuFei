package dev.wceng.sufei.di

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.ui.NavDisplay
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import dev.wceng.sufei.ui.navigation.Collection
import dev.wceng.sufei.ui.navigation.Detail
import dev.wceng.sufei.ui.navigation.EntryProviderInstaller
import dev.wceng.sufei.ui.navigation.Explore
import dev.wceng.sufei.ui.navigation.Home
import dev.wceng.sufei.ui.navigation.Navigator
import dev.wceng.sufei.ui.navigation.PoetDetail
import dev.wceng.sufei.ui.navigation.PoetWorks
import dev.wceng.sufei.ui.navigation.Settings
import dev.wceng.sufei.ui.screens.collection.CollectionScreen
import dev.wceng.sufei.ui.screens.detail.DetailScreen
import dev.wceng.sufei.ui.screens.detail.DetailViewModel
import dev.wceng.sufei.ui.screens.explore.ExploreScreen
import dev.wceng.sufei.ui.screens.explore.ExploreViewModel
import dev.wceng.sufei.ui.screens.home.HomeScreen
import dev.wceng.sufei.ui.screens.poet.PoetDetailScreen
import dev.wceng.sufei.ui.screens.poet.PoetDetailViewModel
import dev.wceng.sufei.ui.screens.poetworks.PoetWorksScreen
import dev.wceng.sufei.ui.screens.poetworks.PoetWorksViewModel
import dev.wceng.sufei.ui.screens.settings.SettingsScreen

/**
 * 屏幕路由注册模块
 * 负责将各个功能屏的 Entry 注入到 Navigation 3 的全局配置中
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
object ScreensModule {

    @IntoSet
    @Provides
    fun provideHomeEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Home> {
            HomeScreen(
                onPoemClick = { poemId ->
                    navigator.goTo(Detail(poemId))
                }
            )
        }
    }

    @IntoSet
    @Provides
    fun provideExploreEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Explore> { key ->
            val viewModel = hiltViewModel<ExploreViewModel, ExploreViewModel.Factory>(
                key = "${key.query}_${key.tag}_${key.tune}_${key.dynasty}",
                creationCallback = { factory -> factory.create(key.query, key.tag, key.tune, key.dynasty) }
            )

            ExploreScreen(
                onPoemClick = { poemId ->
                    navigator.goTo(Detail(poemId))
                },
                onPoetClick = { poetId ->
                    navigator.goTo(PoetDetail(poetId))
                },
                viewModel = viewModel
            )
        }
    }

    @IntoSet
    @Provides
    fun providePoetDetailEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<PoetDetail>(
            metadata = NavDisplay.transitionSpec {
                (slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { it / 10 }) + fadeIn()) togetherWith
                        fadeOut(animationSpec = tween(400))
            } + NavDisplay.popTransitionSpec {
                fadeIn(animationSpec = tween(400)) togetherWith
                        (slideOutVertically(
                            animationSpec = tween(400),
                            targetOffsetY = { it / 10 }) + fadeOut())
            } + NavDisplay.predictivePopTransitionSpec { _ ->
                fadeIn(animationSpec = tween(400)) togetherWith
                        (slideOutVertically(
                            animationSpec = tween(400),
                            targetOffsetY = { it / 10 }) + fadeOut())
            }
        ) { key ->
            val viewModel = hiltViewModel<PoetDetailViewModel, PoetDetailViewModel.Factory>(
                key = key.id,
                creationCallback = { factory -> factory.create(key.id) }
            )
            PoetDetailScreen(
                onBack = { navigator.goBack() },
                onPoemClick = { poemId ->
                    navigator.goTo(Detail(poemId))
                },
                onAllWorksClick = { poetName ->
                    navigator.goTo(PoetWorks(poetName))
                },
                viewModel = viewModel
            )
        }
    }

    @IntoSet
    @Provides
    fun providePoetWorksEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<PoetWorks> { key ->
            val viewModel = hiltViewModel<PoetWorksViewModel, PoetWorksViewModel.Factory>(
                key = key.poetName,
                creationCallback = { factory -> factory.create(key.poetName) }
            )
            PoetWorksScreen(
                onBack = { navigator.goBack() },
                onPoemClick = { poemId ->
                    navigator.goTo(Detail(poemId))
                },
                viewModel = viewModel
            )
        }
    }

    @IntoSet
    @Provides
    fun provideCollectionEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Collection> {
            CollectionScreen(
                onPoemClick = { poemId ->
                    navigator.goTo(Detail(poemId))
                }
            )
        }
    }

    @IntoSet
    @Provides
    fun provideDetailEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Detail>(
            metadata = NavDisplay.transitionSpec {
                (slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { it / 10 }) + fadeIn()) togetherWith
                        fadeOut(animationSpec = tween(400))
            } + NavDisplay.popTransitionSpec {
                fadeIn(animationSpec = tween(400)) togetherWith
                        (slideOutVertically(
                            animationSpec = tween(400),
                            targetOffsetY = { it / 10 }) + fadeOut())
            } + NavDisplay.predictivePopTransitionSpec { _ ->
                fadeIn(animationSpec = tween(400)) togetherWith
                        (slideOutVertically(
                            animationSpec = tween(400),
                            targetOffsetY = { it / 10 }) + fadeOut())
            }
        ) { key ->
            val viewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
                key = key.id,
                creationCallback = { factory -> factory.create(key) }
            )
            DetailScreen(
                onBack = { navigator.goBack() },
                onPoetClick = { poetId ->
                    navigator.goTo(PoetDetail(poetId))
                },
                onTagClick = { tagName ->
                    navigator.goTo(Explore(tag = tagName))
                },
                onDynastyClick = { dynastyName ->
                    navigator.goTo(Explore(dynasty = dynastyName))
                },
                viewModel = viewModel
            )
        }
    }

    @IntoSet
    @Provides
    fun provideSettingsEntry(): EntryProviderInstaller = {
        entry<Settings> {
            SettingsScreen()
        }
    }
}
