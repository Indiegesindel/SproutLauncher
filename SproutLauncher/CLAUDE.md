# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SproutLauncher is an Android launcher application for customizable app tile management. Built with Kotlin, Jetpack Compose, and MVVM architecture.

- **Package**: `games.indiegesindel.sproutlauncher`
- **Min SDK**: 26 (Android 8.0) / **Target SDK**: 36
- **Language**: Kotlin with Java 11 JVM target

## Build Commands

```bash
# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "games.indiegesindel.sproutlauncher.data.AppManagerTest"

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

## Architecture

**MVVM Pattern with Compose**

### Data Layer (`data/`)
- `AppManager` - Persists app tiles to SharedPreferences with JSON serialization. Handles recursive tile storage for hierarchical groups.
- `SettingsManager` - User preferences with StateFlow for reactive updates (themes, spacing, quick actions).

### Model (`model/`)
- `AppTile` - Core data class with UUID identifier, supports nested groups for hierarchical organization.

### UI Layer (`ui/`)
- **Screens** (`ui/screens/`) - `MainScreen`, `AllAppsTab`, `AppListTab`, `SettingsScreen`, `AppTileSettingsScreen`, `ExtendedScreen`
- **Components** (`ui/components/`) - `AppGrid`, `GroupModal`, `QuickActionsBar`, etc.
- **ViewModels** (`ui/viewmodels/`) - `MainViewModel`, `ExtendedViewModel`, `AppTileSettingsViewModel`
- **Theme** (`ui/theme/`) - 7 theme implementations with Material 3 support

### Activities
- `MainActivity` - Main launcher interface (landscape, single-task)
- `ExtendedActivity` - All apps picker
- `AppTileSettingsActivity` - Tile customization
- `ShortcutHandlerActivity` - Dynamic shortcut receiver

### Utilities (`utils/`)
- `LauncherUtils` - App launching (handles shortcuts and direct intents)
- `IconUtils` - Icon extraction and processing
- `FileUtils` - File operations

## Key Patterns

- **Reactive State**: Kotlin StateFlow throughout for UI updates
- **Recursive Groups**: AppTile supports nested groups via `tiles` property
- **Custom Image Loading**: Coil with custom ResolveInfo fetcher in `LauncherApplication`
- **Focus Tracking**: `FocusedElement` enum for keyboard/D-pad navigation
- **Shortcut Handling**: Supports both legacy Uri-based and modern LauncherApps shortcuts

## Testing

Unit tests use JUnit 4, Robolectric, MockK, and Google Truth. Tests are located in `app/src/test/` mirroring the main source structure.
