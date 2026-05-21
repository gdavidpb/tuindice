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

Run Maestro locally:

```bash
./gradlew e2eMaestroAndroid
./gradlew e2eMaestroIos
./gradlew e2eMaestroLocal
```

The suite uses the WireMock runtime under `mocks/` and does not install external tools.
