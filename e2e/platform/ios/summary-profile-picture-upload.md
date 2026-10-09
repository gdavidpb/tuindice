# Summary Profile Picture Upload Edge (iOS)

Status: platform-edge note; the scenario `summary-profile-picture-sources` drives the hand-off up to its cancellation, no scenario drives the upload.

Scenario coverage: `summary-profile-picture` verifies the in-app profile picture settings sheet (it shows the pick, take and remove actions) and the remove confirmation flow. `summary-profile-picture-sources` taps the gallery option and the camera option (`summary.Summary.PickProfilePicture`, `TakeProfilePicture`): the simulator shows the photo picker and the camera over the app, the scenario waits for each to finish loading (`Sort and Filter`, `PhotoCapture`), closes it with its own control (`Cancel`, `DismissButton`) and sees it go (10 of 10).

Platform-only scope:

- verify a selected image returns to the app and reaches `summary.Summary.UploadProfilePicture`
- cover invalid image and oversized image picker payloads once a stable debug file-source adapter exists

Reason: the upload (`summary.Summary.UploadProfilePicture`) runs once the system picker or camera hands a file back, through security-scoped file access, and no scenario can put a file in them; it needs a debug-only source of that file.
