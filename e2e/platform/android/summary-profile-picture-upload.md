# Summary Profile Picture Upload Edge (Android)

Status: platform-edge note; the scenario `summary-profile-picture-sources` drives the hand-off up to its cancellation, no scenario drives the upload.

Scenario coverage: `summary-profile-picture` verifies the in-app profile picture settings sheet (it shows the pick, take and remove actions) and the remove confirmation flow. `summary-profile-picture-sources` taps the gallery option and the camera option (`summary.Summary.PickProfilePicture`, `TakeProfilePicture`): the app leaves the foreground for the Android photo picker (`com.google.android.photopicker`) and for the camera (`com.android.camera2`), the back key closes them, and the app is back on the summary (3 of 3).

Platform-only scope:

- verify a selected image returns to the app and reaches `summary.Summary.UploadProfilePicture`
- cover invalid image and oversized image picker payloads once a stable debug file-source adapter exists

Reason: the upload (`summary.Summary.UploadProfilePicture`) runs once the system picker or camera hands a file back, and no scenario can put a file in them; it needs a debug-only source of that file.
