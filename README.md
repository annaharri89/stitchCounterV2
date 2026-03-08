# Stitch Counter

A modern Android app for counting stitches with support for multiple themes and color schemes. This is rewritten from an older [Stitch Counter](https://github.com/annaharri89/stitchCounter) project I previously created.

## Features

- Single and Double counter project modes for tracking stitches and/or rows
- Library system to save counters and return to them later using Room, an abstraction layer over SQLite, for the database.
- Three different customizable color themes using Material3 and DataStore to save the theme selection. The theme selection changes the app icon. Light and Dark mode also supported.
- Responsive design for all device sizes using Jetpack Compose. Optimized for portrait and landscape orientations.
- The user can add up to 6 photos to each project. Photos are compressed and saved in app-managed internal storage (`project_images`), and relative file paths are stored in Room. Images are loaded with Coil from these resolved local paths.
- Import/Export Library creates and restores an offline backup that includes both project data and project photos, so local-only data can be transferred to a new device.
- No account, login, or cloud service is required to use backup and transfer features.
- Stitch Counter does not collect analytics, tracking data, or personal information. All data is stored locally on the user’s device.

## Engineering Guardrails

- CI runs `:app:testDebugUnitTest` and fails if Kotlin source in `app/src/main` changes without corresponding changes in `app/src/test`.
- Install local Git hooks with:
  - `bash scripts/install-git-hooks.sh`
- The pre-commit hook runs `:app:testDebugUnitTest` when staged changes include Kotlin production or test files.

## Release: Signed Play Store AAB

This project is configured to build signed release AABs with an upload key from `keystore.properties`.

1. Create `keystore.properties` in the project root:
   - `storeFile=/absolute/path/to/upload-keystore.jks`
   - `storePassword=YOUR_STORE_PASSWORD`
   - `keyAlias=upload`
   - `keyPassword=YOUR_KEY_PASSWORD`
1. Build the signed AAB:
   - Android Studio Gradle task: `:app:buildPlayReleaseAab`
   - CLI equivalent: `./gradlew :app:buildPlayReleaseAab`

Notes:
- `buildPlayReleaseAab` runs release unit tests before packaging by way of the `bundleRelease` task dependency chain.
- On success, it opens the output folder in Finder.
- AAB output path: `app/build/outputs/bundle/release/app-release.aab`.