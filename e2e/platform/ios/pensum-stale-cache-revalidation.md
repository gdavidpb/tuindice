# Pensum Stale Cache Revalidation Edge (iOS)

Status: platform-edge note; no scenario exercises the revalidation of a saved pensum, because the age that triggers it cannot be reached from a run. The code is shared, so the Android note says the same.

Scope that remains:

- a saved pensum older than one day (`CooldownTimes.COOLDOWN_GET_PENSUM`) is revalidated with `GET /pensums/v4` when the pensum tab is entered (`EnsurePensumLoadedUseCase`, `InitialContentRefreshPolicy.Always`) and on every sync (`SyncDataSource.launchPensumRevalidation`)
- when that automatic revalidation fails, the saved pensum stays on screen and no warning is shown; only an explicit refresh (pull or retry) that fails shows `pensum_local_data_warning_*`

Covered elsewhere:

- `pensum/src/commonTest/.../data/source/PensumDataSourceTest.kt` verifies the age rule: a pensum younger than a day is kept unless the refresh is forced (`refreshPensum_keepsAPensumYoungerThanADay_unlessForced`), an older one is revalidated (`refreshPensum_revalidatesAPensumOlderThanADay`), a failed revalidation holds the next automatic one off for an hour but not a forced one (`refreshPensum_afterAFailedRevalidation_holdsTheNextAutomaticOneOff_butNotAForcedOne`), and the sync-side revalidation respects the age (`revalidateSelectedPensum_respectsTheAgeOfTheCachedPensum`)
- `pensum/src/commonTest/.../presentation/viewmodel/PensumViewModelContractTest.kt` verifies that a failing revalidation on entry keeps the content without a warning (`ensureLoaded_whenRevalidationFails_keepsTheContentWithoutAWarning`) and that a failing explicit refresh keeps it with the warning (`refreshFailureOverContent_keepsContentWithLocalDataWarning`)
- `pensum/src/commonTest/.../ui/view/PensumLocalDataWarningViewUiTest.kt` and `PensumErrorMessagesTest` verify how the warning is drawn and which message each error gets

Reason: the age of the saved pensum is compared with `currentTimeMillis()` (`Clock.System`, `base/.../utils/Time.kt`), not with the injectable clock that `TUINDICE_E2E_NOW` sets (`OverridableClock`), and the stamp is written with the same call. In a run the saved pensum is always seconds old, so reopening the app and returning to the tab sends no `GET /pensums/v4` (seen in the WireMock journal when the scenario `pensum-cache-refresh-failed` was deleted; see `docs/e2e-mediciones.md`, section 3). If the age read the injectable clock, a scenario could relaunch with `TUINDICE_E2E_NOW` two days later and make the service answer 503; that is a product change and is not made here.
