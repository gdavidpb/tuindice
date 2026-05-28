# Local E2E runbook

The local E2E suite runs against the existing WireMock runtime in `mocks/`.

Prerequisites:

- Android: `adb`, Android SDK, a connected emulator/device, and Maestro.
- iOS: macOS, `xcrun`, a booted iOS simulator, CocoaPods dependencies already available when needed, and Maestro.
- WireMock dependencies already present in `mocks/`.

Platform execution commands:

```bash
./gradlew verifyE2eContract
./gradlew e2eMaestroAndroid
./gradlew e2eMaestroIos
```

Maestro profiling commands:

```bash
./gradlew e2eMaestroProfileAndroid
./gradlew e2eMaestroProfileIos
```

Optional aggregate command:

```bash
./gradlew e2eMaestroLocal
```

Parallel Android/iOS execution is supported when each platform gets its own WireMock port, temp directory, and report directory:

```bash
E2E_WIREMOCK_PORT=18081 \
E2E_TMP_DIR=/tmp/tuindice-e2e-android \
E2E_REPORT_DIR=build/e2e/android \
./gradlew e2eMaestroAndroid

E2E_WIREMOCK_PORT=18082 \
E2E_TMP_DIR=/tmp/tuindice-e2e-ios \
E2E_REPORT_DIR=build/e2e/ios \
./gradlew e2eMaestroIos
```

Useful environment variables:

- `E2E_WIREMOCK_PORT`: defaults to `8080`.
- `E2E_TMP_DIR`: defaults to `/tmp/tuindice-e2e`; use a unique value per platform when running in parallel.
- `E2E_ANDROID_API_BASE_URL`: defaults to `http://127.0.0.1:8080/`.
- `E2E_ANDROID_WEB_BASE_URL`: defaults to `http://127.0.0.1:8080`; used for local legal/browser pages.
- `E2E_IOS_API_BASE_URL`: defaults to `http://localhost:8080/`.
- `E2E_IOS_WEB_BASE_URL`: defaults to `http://localhost:8080`; used for local legal/browser pages.
- `TUINDICE_API_BASE_URL`: alternative Android debug API base URL input.
- `E2E_IOS_DEVICE_ID`: defaults to `booted`.
- `E2E_BASE_SHA`: optional base ref for smart evidence scope; otherwise evidence compares the current branch to `production` or `origin/production`.
- `E2E_HEAD_SHA`: optional head ref for smart evidence scope; otherwise evidence uses `E2E_COMMIT_SHA` or `HEAD`.
- `E2E_SCOPE_FILE`: optional `platform,suite,reason` file to replay a previously resolved smart scope.
- `E2E_MAESTRO_SUITE`: bypasses smart scope resolution and runs one explicit suite; use it only for ad-hoc debugging.
- `E2E_MAESTRO_OPTIMIZE_SETUP`: defaults to `1`; set to `0` to run source YAML without the seeded authenticated setup optimization.
- `E2E_MAESTRO_HOME`: optional isolated home for Maestro CLI runtime logs; platform runners default it under `E2E_TMP_DIR`.
- `E2E_WIREMOCK_DELAY_PROFILE`: defaults to `fast` in E2E runners; use `legacy` to keep checked-in WireMock delays unchanged.
- `E2E_PROFILE_OUTPUT_DIR`: defaults to `build/e2e/profiles` for profile runs.
- `TUINDICE_E2E_API_BASE_URL`: iOS debug runtime API URL override used by Maestro launch arguments.
- `TUINDICE_E2E_WEB_BASE_URL`: iOS debug runtime web URL override used by Maestro launch arguments.
- `E2E_STRICT_IOS=1`: makes `e2eMaestroLocal` fail when iOS cannot run.
- `E2E_REPORT_DIR`: defaults to `build/e2e`.
- `E2E_DISABLE_KEYBOARD_HELPERS`: defaults to `1`; reset scripts best-effort disable spellcheck, autofill, autocorrection, and prediction helpers that can surface keyboard recommendation popups during Maestro input.

Execution order:

1. Start or reuse local WireMock.
2. Reset WireMock scenarios and request journal.
3. Build the debug app.
4. Reset app state.
5. Install and prepare an optimized suite copy unless disabled.
6. Run the Maestro suite.

MVI action coverage:

- `testkit/e2e/mvi-action-catalog.yaml` maps every `presentation/contract/*.kt` Action to `user`, `internal`, or `platform-edge`.
- `user` actions must declare a Maestro flow or an explicit platform edge assignment.
- `internal` actions are lifecycle, observation, loading, bootstrap, or renderer callback actions and do not require direct black-box coverage.
- `platform-edge` actions still require a Maestro trigger when stable, plus a platform-edge rationale for future UI Automator/XCUITest coverage.

Do not add Firebase Test Lab behavior here yet. Future cloud execution should reuse the same flows and add a separate runner layer.
