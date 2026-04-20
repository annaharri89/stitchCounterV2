# Stitch Counter (Android)

Solo-built Android app for tracking knitting and crochet projects. It supports single and double counters, project photo history, theme customization, and local zip backup/restore (no cloud).

This repo is a rewrite of the older [Stitch Counter](https://github.com/annaharri89/stitchCounter) project.

## Project highlights

- **Engineering focus:** CI/CD, Jetpack Compose, Room/DataStore, Hilt, unit testing
- **MVVM + repository flow:** UI in `feature/*` talks to ViewModels. Persistence flows through repositories into Room. Import/export and zip backup run through `data/backup` and domain use cases. Theme and launcher icon updates are coordinated from `feature/theme`.

## CI/CD status

[![CI](https://github.com/annaharri89/stitchCounterV2/actions/workflows/ci.yml/badge.svg)](https://github.com/annaharri89/stitchCounterV2/actions/workflows/ci.yml)
[![Play internal CD](https://github.com/annaharri89/stitchCounterV2/actions/workflows/play-internal-cd.yml/badge.svg)](https://github.com/annaharri89/stitchCounterV2/actions/workflows/play-internal-cd.yml)
[![Codecov](https://codecov.io/gh/annaharri89/stitchCounterV2/branch/main/graph/badge.svg)](https://codecov.io/gh/annaharri89/stitchCounterV2)

### Success Metrics

- **Early usage signal:** 50+ users in Google Play internal testing
- **Stability signal:** 0 crashes in Google Play Console (Android vitals) for the current early-stage release
- **Quality signal:** JVM unit tests in `app/src/test`; line coverage on [Codecov](https://codecov.io/gh/annaharri89/stitchCounterV2)

### Backup and restore reliability
- Backups complete without errors in real use
- Restore recreates project data correctly
- Photos remain preserved and readable after restore
- Works across app restarts, devices, and app versions
- Verified between iOS and Android devices
- Import/export pipeline is covered by unit tests (`ImportLibraryTest`, `ExportLibraryTest`)


## Live links

- Play open testing: [Stitch Counter open track](https://play.google.com/apps/testing/dev.harrisonsoftware.stitchCounter)
- LinkedIn: [Anna Harrison](https://www.linkedin.com/in/anna-harrison-83a38628/)
- Portfolio: [harrisonsoftware.dev](https://harrisonsoftware.dev)
- Contact: [harrisonsoftware.dev/contact](https://harrisonsoftware.dev/contact)

## Screenshots

| | |
| --- | --- |
| Counter | <img src="docs/readme/counter.jpg" alt="Counter" width="220" /> |
| Project library (light) | <img src="docs/readme/library-light.jpg" alt="Project library (light)" width="220" /> |
| Project library (dark) | <img src="docs/readme/library-dark.jpg" alt="Project library (dark)" width="220" /> |
| Theme settings | <img src="docs/readme/theme-settings.png" alt="Theme settings" width="220" /> |
| Backup & restore | <img src="docs/readme/backup-restore.jpg" alt="Backup and restore" width="220" /> |

## Requirements & how to run

- Use Android Studio Koala Feature Drop 2024.1.2+ (or any setup with JDK 17 and Android SDK 36)
- Open the project root, wait for Gradle sync, then run the `app` configuration on API 24+
- CLI debug build: `./gradlew :app:assembleDebug`
- CLI unit tests: `./gradlew :app:testDebugUnitTest`

## Key engineering decisions

- **Compose + Material 3:** UI built with modern Android patterns and responsive layouts for phone/tablet and portrait/landscape.
- **Room + DataStore:** Structured project persistence in Room, lightweight preference storage in DataStore.
- **Local backup (no cloud):** Projects can be exported to zip with metadata and images, then restored on another device. The app does not sync to cloud services; data stays on the device unless you export it yourself.
- **Hilt + feature separation:** Dependencies are injected with Hilt and features are separated by domain area for maintainability.

## Tech stack

| Area | Choices |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Min / compile / target SDK | 24 / 36 / 36 |
| DI | Hilt |
| Navigation | Compose Destinations, Navigation-Compose |
| Local data | Room (SQLite), DataStore Preferences |
| Images | Coil; JPEG compress + max dimension in `ImageStorageUtils` |
| Serialization | Kotlinx Serialization (backup/metadata) |
| Tooling | KSP, Compose compiler plugin, AGP 8.9.x, Kotlin 2.0.x (see `gradle/libs.versions.toml`) |

## Features

- Single and double counter project modes for stitches and/or rows
- Library of saved projects with Room
- Six Material 3 color themes with light/dark mode; selection is saved in DataStore and can update the launcher icon
- Responsive Compose layouts for phones and tablets, portrait and landscape
- Up to **6** photos per project; images are compressed JPEGs in app-internal storage and loaded with Coil
- Backups are zip files with metadata + images, so projects can move between devices without cloud sync or accounts
- No in-app analytics; personal data stays on device (see in-app privacy policy URL in `Constants.kt`)

## Accessibility

The Compose UI targets screen readers and system text settings:

- **TalkBack:** Interactive elements use `contentDescription` / `Modifier.semantics` with dedicated strings (`cd_*` in `strings.xml`) for navigation, counters, adjustment controls, library rows, bottom sheets, settings, and image actions. Decorative visuals use `contentDescription = null` where they add no meaning.
- **Live regions:** The main counter value uses a polite live region so count changes can be announced without searching the layout (see shared counter composables).
- **Semantics roles:** Controls use appropriate roles where it helps (e.g. adjustment chips as a radio group, switches for toggles).
- **Library rows:** Project rows in the library support **custom accessibility actions** (e.g. delete, multi-select, open details) from a single focused row. Related content is sometimes **merged** so it reads as one unit.
- **Font scaling:** Counter text scales with system font size via `sp`-based sizing (`ResizableText` and typography).
- **Theme:** Light/dark themes and Material 3 semantic colors support readable contrast in both modes.

## Project layout (high level)

Kotlin sources live under `app/src/main/java/dev/harrisonsoftware/stitchCounter/`.

```
stitchCounter/
├── feature/           # UI: library, single/double counter, project detail, settings, stats, support, navigation shell
├── data/              # Room (`ProjectDao`, entities, migrations), backup zip pipeline, repository implementations
├── domain/            # Models, validation, import/export use cases
├── di/                # Hilt modules (database, backup, etc.)
├── ui/theme/          # Material 3 theme, typography, colors
├── logging/           # File + logcat sinks, retention, bug-report packaging
├── MainActivity.kt
└── StitchCounterApp.kt
```

## Engineering guardrails

- CI runs JVM unit tests and publishes line coverage to Codecov

# Release guide

## Signed Play Store AAB

1. Create `keystore.properties` in the project root:
   - `storeFile=/absolute/path/to/upload-keystore.jks`
   - `storePassword=YOUR_STORE_PASSWORD`
   - `keyAlias=upload`
   - `keyPassword=YOUR_KEY_PASSWORD`
2. Build the signed AAB:
   - Android Studio task: `:app:buildPlayReleaseAab`
   - CLI: `./gradlew :app:buildPlayReleaseAab`

- `buildPlayReleaseAab` runs release unit tests before packaging
- AAB output path: `app/build/outputs/bundle/release/app-release.aab`

## Release automation (GitHub Actions)

Workflow: [`.github/workflows/play-internal-cd.yml`](.github/workflows/play-internal-cd.yml)

- **Push to `main`:** After [**CI**](.github/workflows/ci.yml) passes for a push to `main`, Play internal CD bumps version, builds `:app:bundleRelease`, uploads to Play internal, and commits updated `gradle/version.properties` with `[skip ci]`
- **Manual run:** GitHub -> Actions -> **Play internal CD** -> **Run workflow**
- If the version-bump push is blocked, allow GitHub Actions (or a scoped token) to push to `main`

Workflow secrets:

- `PLAY_SERVICE_ACCOUNT_JSON` - Google Play Developer API service account JSON (invited in Play Console for this app)
- `RELEASE_KEYSTORE_BASE64` - Base64 of the upload keystore `.jks` used locally
- `RELEASE_KEYSTORE_PASSWORD` - Keystore `storePassword` (matches `keystore.properties`)
- `RELEASE_KEY_PASSWORD` - Signing `keyPassword` (matches `keystore.properties`)
- `RELEASE_KEY_ALIAS` - Signing `keyAlias` (matches `keystore.properties`)

## Roadmap

- **Accessibility:** More assistive features and polish are planned for upcoming releases (this area is under active development).