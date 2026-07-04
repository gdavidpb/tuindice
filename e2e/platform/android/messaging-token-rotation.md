# Messaging Token Rotation Edge (Android)

Status: platform-edge placeholder for `e2ePlatformAndroid`.

Maestro coverage: none. FCM token rotation is a Google Play services system callback with no user-visible surface or stable black-box selector.

Platform-only scope:

- verify `TuIndiceMessagingService.onNewToken` reaches `PushTokenRotationHandler` and re-subscribes through `MessagingRepository.subscribe()` once an instrumented Firebase harness exists
- verify rotation while signed out performs no backend call

Covered elsewhere:

- `app/src/test/kotlin/com/gdavidpb/tuindice/platform/android/PushTokenRotationHandlerTest.kt` verifies the rotation reaction against the session gate, including non-fatal failure reporting
- `app/src/test/kotlin/com/gdavidpb/tuindice/data/repository/messaging/MessagingDataRepositoryTest.kt` verifies rotated-token re-subscription and persisted token updates
- the shared resume repair (`EnsureMessagingSubscribedUseCase` on `Main.Action.RequestSync`) keeps both platforms eventually consistent

Reason: triggering a real FCM token rotation requires Play services internals that neither Maestro nor local instrumentation can drive deterministically.
