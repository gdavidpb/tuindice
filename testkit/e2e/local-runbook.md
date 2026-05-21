# Local E2E runbook

The local E2E suite runs against the existing WireMock runtime in `mocks/`.

Prerequisites:

- Android: `adb`, Android SDK, a connected emulator/device, and Maestro.
- iOS: macOS, `xcrun`, a booted iOS simulator, CocoaPods dependencies already available when needed, and Maestro.
- WireMock dependencies already present in `mocks/`.

Commands:

```bash
./gradlew verifyE2eContract
./gradlew e2eMaestroAndroid
./gradlew e2eMaestroIos
./gradlew e2eMaestroLocal
```

Useful environment variables:

- `E2E_WIREMOCK_PORT`: defaults to `8080`.
- `E2E_ANDROID_API_BASE_URL`: defaults to `http://127.0.0.1:8080/`.
- `TUINDICE_API_BASE_URL`: alternative Android debug API base URL input.
- `E2E_IOS_DEVICE_ID`: defaults to `booted`.
- `E2E_STRICT_IOS=1`: makes `e2eMaestroLocal` fail when iOS cannot run.
- `E2E_REPORT_DIR`: defaults to `build/e2e`.

Execution order:

1. Start or reuse local WireMock.
2. Reset WireMock scenarios and request journal.
3. Build the debug app.
4. Reset app state.
5. Install and run the Maestro suite.

Do not add Firebase Test Lab behavior here yet. Future cloud execution should reuse the same flows and add a separate runner layer.
