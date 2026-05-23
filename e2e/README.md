# Local E2E

This directory contains the executable local E2E suite for TuIndice.

- `maestro/flows`: cross-platform flows, prioritized for both Android and iOS.
- `scripts`: local runners used by Gradle tasks and by direct shell execution.
- `platform/android`: Android-only edge suites for Compose/Espresso/UI Automator.
- `platform/ios`: iOS-only edge suites for XCUITest.

Run the local contract validator before adding new flows:

```bash
./gradlew verifyE2eContract
```

Run Maestro locally by platform:

```bash
./gradlew e2eMaestroAndroid
./gradlew e2eMaestroIos
```

Run every locally available platform with the optional aggregate task:

```bash
./gradlew e2eMaestroLocal
```

Run commit-bound E2E evidence for release certification:

```bash
./gradlew e2eMaestroEvidenceAndroid
./gradlew e2eMaestroEvidenceIos
./gradlew e2eMaestroEvidenceLocal
```

`e2eMaestroEvidenceLocal` runs Android and iOS in parallel when both local
toolchains are available, prefixes live output with `[android]` and `[ios]`,
and isolates WireMock plus temporary files per platform. Override the default
ports or temporary roots with `E2E_ANDROID_WIREMOCK_PORT`,
`E2E_IOS_WIREMOCK_PORT`, `E2E_ANDROID_TMP_DIR`, and `E2E_IOS_TMP_DIR`.

Evidence is written to `build/e2e/certifications/<sha>/<platform>/<suite>/`
and mirrored to
`build/e2e/certifications/by-fingerprint/<fingerprint>/<platform>/<suite>/`.
The fingerprint is computed from functional app inputs plus Maestro flows and
WireMock fixtures, so pipeline-only changes can reuse a previous passing
certification for the same app behavior.
Set `E2E_PUBLISH_GITHUB_STATUS=1` to publish the required commit status, for example
`local-e2e/android/auth-suite`.

On PR preflight, if the current SHA is missing a required `local-e2e/...`
status, CI searches previous commits in the PR. When it finds the same
fingerprint with a successful status, it publishes a reused success status on
the current SHA instead of requiring a full local E2E rerun.

The suite uses the WireMock runtime under `mocks/` and does not install external tools.
By default, the Gradle tasks run `e2e/maestro/flows/suites/local-certification-suite.yaml`.
