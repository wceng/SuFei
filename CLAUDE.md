# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install debug APK on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run a single unit test class
./gradlew app:test --tests "dev.wceng.sufei.ExampleTest"

# Clean build
./gradlew clean
```

## Tech Stack

- **Languages**: Kotlin 2.3, Java 17
- **UI**: Jetpack Compose (BOM 2025.09), Material 3, Material Icons Extended
- **Navigation**: Navigation 3 (experimental, `androidx.navigation3`) with custom `Navigator` class
- **DI**: Hilt 2.57 with `@HiltViewModel(assistedFactory=...)` for parameterized ViewModels
- **Database**: Room 2.8 with KSP, 4 entities (Poem, Poet, Tag, Tune), 4 DAOs
- **Preferences**: Proto DataStore with custom protobuf schema
- **Chinese Conversion**: OpenCC4J for simplified/traditional Chinese variants
- **Concurrency**: Kotlin Coroutines & Flow
- **Build**: Gradle with version catalog (`gradle/libs.versions.toml`), AGP 8.13
- **Min SDK**: 23, **Target SDK**: 35, **Compile SDK**: 36
- **ProGuard**: Enabled for release builds, with rules for Protobuf, DataStore, and OpenCC4J

## Architecture Overview

### Package structure (`dev.wceng.sufei`)

```
data/
├── local/
│   ├── room/          # AppDatabase, DAOs, entities, type converters
│   └── datastore/     # Proto DataStore serializer & data source
├── model/             # Domain models (Poem, Poet, Tag, Tune, SearchResult, UserPreferences)
├── repository/        # Repository interfaces + implementations
└── tts/               # System TTS manager
di/                    # Hilt modules (Database, DataStore, Navigation, Repository, Screens, TTS)
ui/
├── components/        # Reusable composables (PoemCard, SquareScreenTemplate)
├── navigation/        # Routes (serializable), Navigator, MainTab enum
├── screens/           # Feature screens (home, explore, collection, settings, detail, poet, poetworks, splash)
├── theme/             # Color, Theme, Typography, Font
└── SuFeiApp.kt        # Root composable with NavDisplay + NavigationSuiteScaffold
util/                  # ChineseConverter
```

### Navigation

Uses **Navigation 3** (`androidx.navigation3`) — an experimental Compose-first navigation library. The custom `Navigator` class manages a `SnapshotStateList<Any>` as the back stack. Each screen is a serializable route object registered in `ScreensModule` via `@IntoSet EntryProviderInstaller` with optional `transitionSpec` metadata for custom animations.

Two transition types:
- **Global** (top-level tab switches): crossfade 500ms — defined in `SuFeiApp.kt`
- **Deep-link** (detail pages): slide up/fade in 400ms — defined per-entry in `ScreensModule.kt`

Both support Android 13+ predictive back gesture.

### Key patterns

- **ViewModels with assisted injection**: Routes with parameters (e.g. `Explore(query, tag, tune, dynasty)`) use `@HiltViewModel(assistedFactory=...)` + `@AssistedInject` to receive navigation arguments. The ViewModel key includes all params to ensure proper caching.
- **Tab state retention**: `Navigator.navigateToTopLevelDestination()` preserves existing tab pages in the back stack so their Compose state (scroll position, etc.) is not lost.
- **Data flow**: Room DAOs return `Flow<List<T>>`, combined in ViewModels via `combine()` and `flatMapLatest`, exposed as `StateFlow` with `WhileSubscribed(5000)` sharing strategy.
- **Theme**: Full Material 3 color schemes (light/dark, medium/high contrast variants) with optional dynamic color on Android 12+.
- **Intent handling**: `MainActivity` handles `ACTION_PROCESS_TEXT` and custom `sufei://explore` deep links.

### Screen list

| Route | Screen | Description |
|-------|--------|-------------|
| `Splash` | SplashScreen | Initial data import with progress |
| `Home` | HomeScreen | Daily poem recommendation |
| `Explore` | ExploreScreen | Search with dynasty/tag/tune filters |
| `Collection` | CollectionScreen | Favorited poems |
| `Detail` | DetailScreen | Full poem reading |
| `PoetDetail` | PoetDetailScreen | Poet biography & works |
| `PoetWorks` | PoetWorksScreen | All poems by a poet |
| `Settings` | SettingsScreen | Font, theme, variant preferences |