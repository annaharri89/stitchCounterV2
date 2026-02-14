# Stitch Counter

A modern Android app for counting stitches with support for multiple themes and color schemes. This is rewritten from an older [Stitch Counter](https://github.com/annaharri89/stitchCounter) project I previously created.

## Features

- Single and Double counter project modes for tracking stitches and/or rows
- Library system to save counters and return to them later using Room, an abstraction layer over SQLite, for the database.
- Three different customizable color themes using Material3 and DataStore to save the theme selection. The theme selection changes the app icon. Light and Dark mode also supported.
- Responsive design for all device sizes using Jetpack Compose. Optimized for portrait and landscape orientations.
- The user can upload up to 10 photos to each project. Photos are saved to the device's file system, and file paths are stored in Room. Images are loaded with Coil using these stored paths.
- An import/export library feature to backup your library of projects since the app storage is local to your device and not uploaded to any cloud based service.

## Privacy
Stitch Counter does not collect analytics, tracking data, or personal information.
All data is stored locally on the user’s device.
