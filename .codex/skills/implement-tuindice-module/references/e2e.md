# Local E2E Reference

## Architecture

- Keep executable E2E assets in root `e2e/`, not inside KMP modules.
- Keep reusable contract and QA policy in `testkit/e2e/`.
- Use Maestro as the default runner for cross-platform black-box flows.
- Use platform-specific tests only for technology edges Maestro cannot validate stably:
  - `Compose/Espresso` for Android Compose synchronization, technical state inspection, or intents.
  - `UI Automator` for Android system surfaces such as PDF/file opener, permissions, camera, share sheet, or external browser.
  - `XCUITest` for iOS simulator lifecycle, file opener, permissions, camera/photo picker, share sheet, or inaccessible host/system assertions.
- Do not duplicate a flow in platform-specific tests just because it already exists in Maestro.

## Root Layout

- `e2e/maestro/flows/`: Maestro YAML flows grouped by module.
- `e2e/maestro/flows/_shared/`: shared setup, clean launch, login, reset, and navigation helpers.
- `e2e/maestro/flows/suites/`: suite aggregators that must include active module flows.
- `e2e/scripts/`: local build/reset/run scripts.
- `e2e/platform/android/`: Android-specific edge tests or instructions.
- `e2e/platform/ios/`: iOS-specific edge tests or instructions.
- `testkit/e2e/flow-catalog.yaml`: source of truth for module coverage and platform-edge assignment.
- `testkit/e2e/mvi-action-catalog.yaml`: source of truth mapping MVI Action declarations to required E2E coverage.
- `testkit/e2e/critical-selectors.txt`: selectors that must stay available for stable E2E.
- `testkit/e2e/fixture-contract.env`: canonical fixture values (single source of truth, mirrored in testkit's `E2eFixtureContract.kt`; the validator fails on drift).
- `testkit/e2e/quarantine.txt`: temporarily skipped flows; removed from aggregator suites with a visible QUARANTINED line and validated by the contract.
- `testkit/e2e/validate-e2e-contract.sh`: validates catalog entries, flow files, critical selectors, fixture contract, and quarantine.
- `testkit/e2e/selector-policy.md`, `fixture-contract.md`, and `local-runbook.md`: reusable QA policy.

## E2E Scope Gate

At the start of every module change, classify whether local E2E is in scope. It is in scope when the change touches any of:

- user-visible behavior, state, copy, empty/loading/failed/retry branches, or screen acceptance criteria
- navigation destinations, back results, top-level tabs, dialogs, deep links, or app startup/reset behavior
- presentation `Action`s, especially added/removed/renamed user actions or actions reclassified as internal/platform-edge
- `Modifier.testTag`, accessibility identifiers, selectors, or UI structure used by existing flows
- local backend contracts, WireMock mappings/bodies, fixture values, scenario state, debug URLs, or reset/run scripts
- platform hand-offs such as browser, file/PDF opener, camera/photo picker, permissions, share sheet, mail, store, or external intents

When E2E is in scope, include the test and every required supporting artifact in the same change. Do not update only app code and leave E2E as a follow-up. If E2E is out of scope, be ready to state the internal-only reason.

## Frontend Change Rules

- When adding or changing a user-visible flow, update `testkit/e2e/flow-catalog.yaml`, the module Maestro flow, and affected suite aggregators in the same change.
- When changing a presentation `Action` in `commonMain`, update `testkit/e2e/mvi-action-catalog.yaml` and either map user-visible coverage or document why it is internal/platform-edge.
- Add or adjust a Maestro flow under `e2e/maestro/flows/<module>/` for happy path, critical interactions, navigation entry/exit, and empty/failed/retry states when applicable.
- Prefer stable selectors based on Compose `Modifier.testTag`; avoid text-only selectors for dynamic, translated, formatted, or duplicated labels.
- Add, preserve, or rename test tags and accessibility identifiers together with the flow that needs them; update `testkit/e2e/critical-selectors.txt` when a selector is suite-critical.
- Keep Android test tags visible to Maestro through `testTagsAsResourceId` in the Android host.
- Audit iOS accessibility identifiers before escalating a case to XCUITest.
- If a flow depends on backend behavior, update WireMock mappings and response bodies under `mocks/` with the app change.
- If fixture values or state setup change, update `testkit/e2e/fixture-contract.env`, `testkit/src/commonMain/kotlin/com/gdavidpb/tuindice/testkit/e2e/E2eFixtureContract.kt`, affected catalog `fixture_state` entries, and reset/run scripts.
- If Maestro cannot verify the edge stably, add or update the platform-edge entry in `testkit/e2e/mvi-action-catalog.yaml` and the matching placeholder/test under `e2e/platform/android/` or `e2e/platform/ios/`.

## Module Coverage Order

1. `testkit`: contract, catalog, selectors, fixtures, validator, runbook.
2. `app` and `iosApp`: debug build/install/launch/reset, mock URLs, state cleanup, Maestro compatibility.
3. `maincore`: startup, tabs, navigation, snackbar, top bar, back, sign out, browser dialog, session state.
4. `auth`: login success, invalid credentials, update password, sign out, invalidated session, retry.
5. `summary`: initial load, refresh/sync status, profile picture actions, remove confirmation, outdated credentials.
6. `record`: academic record load, period changes, grade/status edit, visual mode, synthetic term create/edit/delete.
7. `pensum`: version/modality selection, zoom, node navigation, subject-detail link.
8. `subjects`: search, detail, career/global tabs, unavailable/failed/retry.
9. `evaluations`: list, filters, create, edit, date/grade/max-grade selection, swipe edit/delete.
10. `enrollmentproof`: top-bar entry, fetching sheet, cancel, PDF/open-file success, error/outdated credentials.
11. `wizard`: welcome, next/back, skip, focus per screen, finish into summary.
12. `about`: internal/external links, browser dialog, support/contact/share/rate.

## Local Validation

- Always run `./gradlew verifyE2eContract` after editing E2E docs, catalog, selectors, or flows.
- For Android local E2E, run `./gradlew e2eMaestroAndroid` when Maestro CLI, an Android device/emulator, and `adb` are available.
- For iOS local E2E, run `./gradlew e2eMaestroIos` when Maestro CLI, macOS/Xcode tooling, and a booted simulator are available.
- For both platforms, run `./gradlew e2eMaestroLocal`; outside macOS, iOS should only be required in strict mode.
- Failed suites retry once from the failed flow by default (`E2E_MAESTRO_SUITE_RETRIES`, composes with resume-first checkpoints); flaky flows can be parked in `testkit/e2e/quarantine.txt` with a reason instead of deleting coverage.
- For MVI action coverage changes, run `./gradlew verifyE2eContract` and the relevant unified platform task: `./gradlew e2eMaestroAndroid` or `./gradlew e2eMaestroIos`.
- Platform edge placeholders are verified by `./gradlew e2ePlatformAndroid` and `./gradlew e2ePlatformIos` until concrete tests are added.

## Local Backend And Debug URLs

- E2E runs use the local WireMock backend on `http://127.0.0.1:8080/`.
- Android debug accepts `-Ptuindice.apiBaseUrl` or `TUINDICE_API_BASE_URL`; local runners build with `http://127.0.0.1:8080/` and apply `adb reverse tcp:8080 tcp:8080`.
- iOS debug uses `TUINDICE_API_BASE_URL=http://localhost:8080/`, bundle id `com.gdavidpb.tuindice.debug`, and `iosApp/scripts/ci-build-ios-host.sh` for simulator builds.
