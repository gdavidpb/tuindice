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

The suite uses the WireMock runtime under `mocks/` and does not install external tools.
By default, the Gradle tasks run `e2e/maestro/flows/suites/local-certification-suite.yaml`.
