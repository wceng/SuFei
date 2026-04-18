package dev.wceng.sufei.di

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import androidx.navigation3.ui.NavDisplay
import dev.wceng.sufei.ui.navigation.Collection
import dev.wceng.sufei.ui.navigation.Detail
import dev.wceng.sufei.ui.navigation.EntryProviderInstaller
import dev.wceng.sufei.ui.navigation.Explore
import dev.wceng.sufei.ui.navigation.Home
import dev.wceng.sufei.ui.navigation.Navigator
import dev.wceng.sufei.ui.navigation.PoetDetail
import dev.wceng.sufei.ui.navigation.Settings
import dev.wceng.sufei.ui.navigation.Square
import dev.wceng.sufei.ui.screens.collection.CollectionScreen
import dev.wceng.sufei.ui.screens.detail.DetailScreen
import dev.wceng.sufei.ui.screens.detail.DetailViewModel
import dev.wceng.sufei.ui.screens.explore.ExploreScreen
import dev.wceng.sufei.ui.screens.explore.ExploreViewModel
import dev.wceng.sufei.ui.screens.explore.SquareScreen
import dev.wceng.sufei.ui.screens.explore.SquareType
import dev.wceng.sufei.ui.screens.explore.SquareViewModel
import dev.wceng.sufei.ui.screens.home.HomeScreen
import dev.wceng.sufei.ui.screens.poet.PoetDetailScreen
import dev.wceng.sufei.ui.screens.poet.PoetDetailViewModel
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
        entry<Explore> {
            ExploreScreen(
                onPoemClick = { poemId ->
                    navigator.goTo(Detail(poemId))
                },
                onPoetClick = { poetId ->
                    navigator.goTo(PoetDetail(poetId))
                },
                onAllTagsClick = {
                    navigator.goTo(Square(SquareType.TAG))
                },
                onAllTunesClick = {
                    navigator.goTo(Square(SquareType.TUNE))
                }
            )
        }
    }

    @IntoSet
    @Provides
    fun provideSquareEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Square> { key ->
            val exploreViewModel: ExploreViewModel = hiltViewModel()

            val squareViewModel = hiltViewModel<SquareViewModel, SquareViewModel.Factory>(
                key = key.type.name,
                creationCallback = { factory -> factory.create(key.type) }
            )
            SquareScreen(
                onBackClick = { navigator.goBack() },
                onItemClick = { name ->
                    when (key.type) {
                        SquareType.TAG -> {
                            exploreViewModel.onTagSelect(name)
                            navigator.goBack()
                        }
                        SquareType.TUNE -> {
                            exploreViewModel.onTuneSelect(name)
                            navigator.goBack()
                        }
                    }
                },
                viewModel = squareViewModel
            )
        }
    }

    @IntoSet
    @Provides
    fun providePoetDetailEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<PoetDetail>(
            metadata = NavDisplay.transitionSpec {
                (slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 10 }) + fadeIn()) togetherWith
                        fadeOut(animationSpec = tween(400))
            } + NavDisplay.popTransitionSpec {
                fadeIn(animationSpec = tween(400)) togetherWith
                        (slideOutVertically(animationSpec = tween(400), targetOffsetY = { it / 10 }) + fadeOut())
            } + NavDisplay.predictivePopTransitionSpec { _ ->
                fadeIn(animationSpec = tween(400)) togetherWith
                        (slideOutVertically(animationSpec = tween(400), targetOffsetY = { it / 10 }) + fadeOut())
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
                (slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 10 }) + fadeIn()) togetherWith
                        fadeOut(animationSpec = tween(400))
            } + NavDisplay.popTransitionSpec {
                fadeIn(animationSpec = tween(400)) togetherWith
                        (slideOutVertically(animationSpec = tween(400), targetOffsetY = { it / 10 }) + fadeOut())
            } + NavDisplay.predictivePopTransitionSpec { _ ->
                fadeIn(animationSpec = tween(400)) togetherWith
                        (slideOutVertically(animationSpec = tween(400), targetOffsetY = { it / 10 }) + fadeOut())
            }
        ) { key ->
            val viewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
                key = key.id,
                creationCallback = { factory -> factory.create(key) }
            )
            DetailScreen(
                onBack = { navigator.goBack() },
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
