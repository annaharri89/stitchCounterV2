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

## Support This Project

This app is developed independently and provided without ads, tracking, or data collection.

If you'd like to support its development:

☕ https://ko-fi.com/annaharri

Thank you for supporting privacy-friendly software 💖
