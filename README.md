# Stitch Counter (Android)

A modern Android app for counting stitches and rows, with multiple themes, a project library, photos per project, and offline backup/restore. This repo is a rewrite of the older [Stitch Counter](https://github.com/annaharri89/stitchCounter) project.

## Screenshots

| | |
| --- | --- |
| Counter | <img src="docs/readme/counter.jpg" alt="Counter" width="220" /> |
| Project library (light) | <img src="docs/readme/library-light.jpg" alt="Project library (light)" width="220" /> |
| Project library (dark) | <img src="docs/readme/library-dark.jpg" alt="Project library (dark)" width="220" /> |
| Theme settings | <img src="docs/readme/theme-settings.png" alt="Theme settings" width="220" /> |
| Backup & restore | <img src="docs/readme/backup-restore.jpg" alt="Backup and restore" width="220" /> |

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

## Requirements & how to run

- Use Android Studio Koala Feature Drop 2024.1.2+ (or any setup with JDK 17 and Android SDK 36).
- Open the project root, wait for Gradle sync, then run the `app` configuration on API 24+.
- CLI debug build: `./gradlew :app:assembleDebug`
- CLI unit tests: `./gradlew :app:testDebugUnitTest`

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

**Data flow (short):** UI in `feature/*` talks to ViewModels. Persistence flows through repositories into Room. Import/export and zip backup run through `data/backup` and domain use cases. Theme and launcher icon updates are coordinated from `feature/theme`.

## Features

- Single and double counter project modes for stitches and/or rows
- Library of saved projects with Room
- Six Material 3 color themes with light/dark mode; selection is saved in DataStore and can update the launcher icon
- Responsive Compose layouts for phones and tablets, portrait and landscape
- Up to **6** photos per project; images are compressed JPEGs in app-internal storage and loaded with Coil
- Backups are zip files with metadata + images, so projects can move between devices without cloud sync
- No in-app analytics; personal data stays on device (see in-app privacy policy URL in `Constants.kt`)

## Engineering guardrails

- CI runs `:app:testDebugUnitTest`.
- Install local Git hooks:

  ```bash
  bash scripts/install-git-hooks.sh
  ```

- The pre-commit hook runs `:app:testDebugUnitTest` when staged changes touch Kotlin under `app/src/main/`, `app/src/test/`, or `app/src/androidTest/` (JVM unit tests only; instrumentation tests are not run in the hook).

# Release Guide

## Signed Play Store AAB

1. Create `keystore.properties` in the project root:
   - `storeFile=/absolute/path/to/upload-keystore.jks`
   - `storePassword=YOUR_STORE_PASSWORD`
   - `keyAlias=upload`
   - `keyPassword=YOUR_KEY_PASSWORD`
2. Build the signed AAB:
   - Android Studio task: `:app:buildPlayReleaseAab`
   - CLI: `./gradlew :app:buildPlayReleaseAab`

- `buildPlayReleaseAab` runs release unit tests before packaging.
- AAB output path: `app/build/outputs/bundle/release/app-release.aab`.

## Release automation (GitHub Actions)

Workflow: [`.github/workflows/play-internal-cd.yml`](../.github/workflows/play-internal-cd.yml)

- **Push to `main`:** After [**CI**](../.github/workflows/ci.yml) passes for a push to `main`, Play internal CD bumps version, builds `:app:bundleRelease`, uploads to Play internal, and commits updated `gradle/version.properties` with `[skip ci]`.
- **Manual run:** GitHub -> Actions -> **Play internal CD** -> **Run workflow**.
- If the version-bump push is blocked, allow GitHub Actions (or a scoped token) to push to `main`.

Workflow secrets:

- `PLAY_SERVICE_ACCOUNT_JSON` - Google Play Developer API service account JSON (invited in Play Console for this app)
- `RELEASE_KEYSTORE_BASE64` - Base64 of the upload keystore `.jks` used locally
- `RELEASE_KEYSTORE_PASSWORD` - Keystore `storePassword` (matches `keystore.properties`)
- `RELEASE_KEY_PASSWORD` - Signing `keyPassword` (matches `keystore.properties`)
- `RELEASE_KEY_ALIAS` - Signing `keyAlias` (matches `keystore.properties`)