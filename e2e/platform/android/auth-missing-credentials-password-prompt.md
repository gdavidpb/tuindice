# Missing Credentials Password Prompt Edge (Android)

Status: platform-edge note; no scenario drives it.

Scenario coverage: `auth-update-password` verifies the same update-password dialog when it is reached because the backend rejected the stored password (`SyncStatus.OutdatedCredentials`). The `SyncStatus.MissingCredentials` entry into it has no scenario.

Platform-only scope:

- sign in, invalidate the stored university password without ending the session (delete or re-key the Android Keystore entry behind `AndroidTinkSecureKeyValueDataSource`, or wipe only its preferences file), relaunch, and verify the app asks for the password again instead of skipping every sync
- verify typing the password restores sync and that pending changes kept flushing in between

Covered elsewhere:

- `maincore/src/commonTest/.../domain/usecase/ScheduleSyncUseCaseTest.kt` (`executeOnBackground_latchesMissingCredentials_whenPasswordIsMissing`, `executeOnBackground_keepsOutdatedCredentials_whenPasswordIsMissing`) verifies the latch: a signed-in session without a readable password stops skipping the sync in silence
- `maincore/src/commonTest/.../presentation/route/TuIndiceAppHostRouteUiTest.kt` (`when_syncStatusIsMissingCredentials_then_hostRouteNavigatesToUpdatePasswordDialog`) verifies the host asks for the password
- `auth/src/commonTest/.../domain/usecase/AuthUseCaseContractTest.kt` (`updatePasswordUseCase_whenTheStoredPasswordCouldNotBeRead_storesItAgainAndClearsTheLatch`) verifies the way out: the typed password is stored again, the latch is cleared and the sync is scheduled
- `app/src/test/kotlin/com/gdavidpb/tuindice/data/source/secure/AndroidTinkSecureKeyValueDataSourceTest.kt` verifies that a tampered ciphertext, or one moved under another entry, fails to read

Reason: the state is "valid session, unreadable password". The harness resets the whole app (`pm clear`), which also drops the session and lands on sign-in, and the driver has no access to the Keystore or to app-private storage for one item. The launch arguments cannot seed it either: a session seed carries its password (`SEED_PASSWORD`) and `DebugLaunchArguments.parse` rejects a partial seed (`partialSessionSeed_fails`).
