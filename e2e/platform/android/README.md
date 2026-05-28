# Android platform E2E edge suites

Use this directory for Android-only E2E cases that Maestro cannot cover with stable black-box selectors.

Expected cases:

- Compose/Espresso synchronization and assertions that need Compose internals.
- UI Automator flows that leave the app, such as PDF openers, share sheet, permissions, camera, or external browser.
- Intent assertions that would duplicate a Maestro flow without adding platform-specific coverage.

The root `e2ePlatformAndroid` task is wired and intentionally succeeds until concrete suites are registered.
