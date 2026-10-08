# Summary Profile Picture Upload Edge (iOS)

Status: platform-edge note; no scenario drives it.

Scenario coverage: `summary-profile-picture` verifies the in-app profile picture settings sheet (it shows the pick, take and remove actions) and the remove confirmation flow. No scenario taps the camera or gallery option (`summary.Summary.TakeProfilePicture`, `PickProfilePicture` are `Pending` in `ActionDispositions.kt`).

Platform-only scope:

- verify the camera and photo picker hand-offs
- verify a selected image returns to the app and reaches `summary.Summary.UploadProfilePicture`
- cover invalid image and oversized image picker payloads once a stable debug file-source adapter exists

Reason: the upload (`summary.Summary.UploadProfilePicture`) runs once the system picker or camera hands a file back, through security-scoped file access, and no scenario can put a file in them; it needs a debug-only source of that file. The picker and camera screens themselves are system UI that the driver could reach with `system(...)` queries and `foreground()`, but nobody has written that scenario.
