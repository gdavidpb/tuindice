# Missing Credentials Password Prompt Edge (iOS)

Status: platform-edge placeholder for `e2ePlatformIos`.

Maestro coverage: `e2e/maestro/flows/auth/update-password.yaml` verifies the same update-password dialog when it is reached because the backend rejected the stored password (`SyncStatus.OutdatedCredentials`). The `SyncStatus.MissingCredentials` entry into it has no flow.

Platform-only scope:

- sign in, remove the stored university password from the secure store behind `IosSecureKeyValueDataSource` without ending the session, relaunch, and verify the app asks for the password again instead of skipping every sync
- verify typing the password restores sync and that pending changes kept flushing in between

Covered elsewhere:

- `maincore/src/commonTest/.../domain/usecase/ScheduleSyncUseCaseTest.kt` (`executeOnBackground_latchesMissingCredentials_whenPasswordIsMissing`, `executeOnBackground_keepsOutdatedCredentials_whenPasswordIsMissing`) verifies the latch: a signed-in session without a readable password stops skipping the sync in silence
- `maincore/src/commonTest/.../presentation/route/TuIndiceAppHostRouteUiTest.kt` (`when_syncStatusIsMissingCredentials_then_hostRouteNavigatesToUpdatePasswordDialog`) verifies the host asks for the password
- `auth/src/commonTest/.../domain/usecase/AuthUseCaseContractTest.kt` (`updatePasswordUseCase_whenTheStoredPasswordCouldNotBeRead_storesItAgainAndClearsTheLatch`) verifies the way out: the typed password is stored again, the latch is cleared and the sync is scheduled
- `maincore/src/commonTest/.../data/source/credentials/CredentialsDataSourceTest.kt` verifies how the password is read from the active and legacy secure stores

Reason: the state is "valid session, unreadable password". Maestro can only clear the whole app state, which also drops the session and lands on sign-in, and it cannot reach the simulator's secure storage for a single item; no debug seed state signs in without a stored password (the host only knows the authenticated coachmark states).
