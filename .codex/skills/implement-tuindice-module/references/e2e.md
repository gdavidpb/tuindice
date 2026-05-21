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
- `e2e/scripts/`: local build/reset/run scripts.
- `e2e/platform/android/`: Android-specific edge tests or instructions.
- `e2e/platform/ios/`: iOS-specific edge tests or instructions.
- `testkit/e2e/flow-catalog.yaml`: source of truth for module coverage and platform-edge assignment.
- `testkit/e2e/critical-selectors.txt`: selectors that must stay available for stable E2E.
- `testkit/e2e/validate-e2e-contract.sh`: validates catalog entries, flow files, and critical selectors.
- `testkit/e2e/selector-policy.md`, `fixture-contract.md`, and `local-runbook.md`: reusable QA policy.

## Frontend Change Rules

- When adding or changing a user-visible flow, update `testkit/e2e/flow-catalog.yaml` in the same change.
- Add or adjust a Maestro flow under `e2e/maestro/flows/<module>/` for happy path, critical interactions, navigation entry/exit, and empty/failed/retry states when applicable.
- Prefer stable selectors based on Compose `Modifier.testTag`; avoid text-only selectors for dynamic, translated, formatted, or duplicated labels.
- Keep Android test tags visible to Maestro through `testTagsAsResourceId` in the Android host.
- Audit iOS accessibility identifiers before escalating a case to XCUITest.
- If a flow depends on backend behavior, update WireMock mappings and response bodies under `mocks/` with the app change.

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
- Platform edge placeholders are verified by `./gradlew e2ePlatformAndroid` and `./gradlew e2ePlatformIos` until concrete tests are added.

## Local Backend And Debug URLs

- E2E runs use the local WireMock backend on `http://127.0.0.1:8080/`.
- Android debug accepts `-Ptuindice.apiBaseUrl` or `TUINDICE_API_BASE_URL`; local runners build with `http://127.0.0.1:8080/` and apply `adb reverse tcp:8080 tcp:8080`.
- iOS debug uses `TUINDICE_API_BASE_URL=http://localhost:8080/`, bundle id `com.gdavidpb.tuindice.debug`, and `iosApp/scripts/ci-build-ios-host.sh` for simulator builds.
