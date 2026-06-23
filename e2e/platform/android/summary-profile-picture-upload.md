# Summary Profile Picture Upload Edge (Android)

Status: platform-edge placeholder for `e2ePlatformAndroid`.

Maestro coverage: `e2e/maestro/flows/summary/summary-profile-picture.yaml` verifies the in-app profile picture settings sheet, pick/take triggers, and remove confirmation flow.

Platform-only scope:

- verify camera and Android photo picker hand-offs with UI Automator
- verify a selected image returns to the app and reaches `summary.Summary.UploadProfilePicture`
- cover invalid image and oversized image picker payloads once a stable debug file-source adapter exists

Reason: native camera/photo picker surfaces and returned file sources are Android system UI edges that Maestro cannot assert stably.
