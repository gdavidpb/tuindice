# Missing Credentials Password Prompt Edge (Android)

Status: platform-edge placeholder for `e2ePlatformAndroid`.

Maestro coverage: `e2e/maestro/flows/auth/update-password.yaml` verifies the same update-password dialog when it is reached because the backend rejected the stored password (`SyncStatus.OutdatedCredentials`). The `SyncStatus.MissingCredentials` entry into it has no flow.

Platform-only scope:

- sign in, invalidate the stored university password without ending the session (delete or re-key the Android Keystore entry behind `AndroidTinkSecureKeyValueDataSource`, or wipe only its preferences file), relaunch, and verify the app asks for the password again instead of skipping every sync
- verify typing the password restores sync and that pending changes kept flushing in between

Covered elsewhere:

- `maincore/src/commonTest/.../domain/usecase/ScheduleSyncUseCaseTest.kt` (`executeOnBackground_latchesMissingCredentials_whenPasswordIsMissing`, `executeOnBackground_keepsOutdatedCredentials_whenPasswordIsMissing`) verifies the latch: a signed-in session without a readable password stops skipping the sync in silence
- `maincore/src/commonTest/.../presentation/route/TuIndiceAppHostRouteUiTest.kt` (`when_syncStatusIsMissingCredentials_then_hostRouteNavigatesToUpdatePasswordDialog`) verifies the host asks for the password
- `auth/src/commonTest/.../domain/usecase/AuthUseCaseContractTest.kt` (`updatePasswordUseCase_whenTheStoredPasswordCouldNotBeRead_storesItAgainAndClearsTheLatch`) verifies the way out: the typed password is stored again, the latch is cleared and the sync is scheduled
- `maincore/src/commonTest/.../data/source/credentials/CredentialsDataSourceTest.kt` verifies how the password is read from the active and legacy secure stores
- `app/src/test/kotlin/com/gdavidpb/tuindice/data/source/secure/AndroidTinkSecureKeyValueDataSourceTest.kt` verifies that a tampered ciphertext, or one moved under another entry, fails to read

Reason: the state is "valid session, unreadable password". Maestro can only clear the whole app state, which also drops the session and lands on sign-in, and it has no access to the Keystore or to app-private storage; no debug seed state signs in without a stored password (`E2eSeedBridge` only knows the authenticated coachmark states).
