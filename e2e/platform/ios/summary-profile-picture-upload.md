# Summary Profile Picture Upload Edge (iOS)

Status: platform-edge placeholder for `e2ePlatformIos`.

Maestro coverage: `e2e/maestro/flows/summary/summary-profile-picture.yaml` verifies the in-app profile picture settings sheet, pick/take triggers, and remove confirmation flow.

Platform-only scope:

- verify camera and photo picker hand-offs with XCUITest
- verify a selected image returns to the app and reaches `summary.Summary.UploadProfilePicture`
- cover invalid image and oversized image picker payloads once a stable debug file-source adapter exists

Reason: native camera/photo picker surfaces, security-scoped file access, and returned file sources are iOS host/system edges that Maestro cannot assert stably.
