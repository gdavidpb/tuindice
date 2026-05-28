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

The evidence tasks are scope-aware by default: without `E2E_MAESTRO_SUITE`
they diff the current branch against `production` or `origin/production`, run
only the required suite/platform pairs, and exit successfully without publishing
anything when no E2E suite is required. Use `E2E_BASE_SHA` or `E2E_HEAD_SHA` to
override the diff inputs, or `E2E_SCOPE_FILE` to replay a resolved
`platform,suite,reason` scope file.

`e2eMaestroEvidenceLocal` runs Android and iOS in parallel when both local
toolchains are available, prefixes live output with `[android]` and `[ios]`,
and isolates WireMock plus temporary files per platform. It skips a platform
when the resolved scope has no suites for it. Override the default ports or
temporary roots with `E2E_ANDROID_WIREMOCK_PORT`,
`E2E_IOS_WIREMOCK_PORT`, `E2E_ANDROID_TMP_DIR`, and `E2E_IOS_TMP_DIR`.

Evidence is written to `build/e2e/certifications/<sha>/<platform>/<suite>/`
and mirrored to
`build/e2e/certifications/by-fingerprint/<fingerprint>/<platform>/<suite>/`.
The fingerprint is computed from functional app inputs plus Maestro flows and
WireMock fixtures, so pipeline-only changes can reuse a previous passing
certification for the same app behavior. Unit-test-only sources such as
`commonTest`, `androidTest`, and `app/src/test` are intentionally excluded so
test fixes do not invalidate E2E evidence for unchanged runtime behavior.
Passing evidence publishes GitHub commit statuses automatically when `gh` is
installed, authenticated, the working tree is clean, the certified commit is
`HEAD`, and the commit exists on GitHub. The aggregate
`local-certification-suite` publishes its own status plus the covered suite
statuses, for example `local-e2e/android/auth-suite`. Set
`E2E_PUBLISH_GITHUB_STATUS=0` for evidence-only local runs. Failed runs keep
their local evidence but never publish GitHub commit statuses.

The default Maestro runners prepare an optimized copy of the requested suite
under `E2E_TMP_DIR`: auth and wizard flows still use real UI setup, while
non-auth module flows use a debug-only seeded authenticated launch helper to
avoid repeated login and wizard traversal. Set `E2E_MAESTRO_OPTIMIZE_SETUP=0`
to run the source YAML exactly as written.

Profile local Maestro timing without publishing evidence:

```bash
./gradlew e2eMaestroProfileAndroid
./gradlew e2eMaestroProfileIos
```

Profiles are written to `build/e2e/profiles/<sha>/<platform>/` by default. Use
`E2E_PROFILE_OUTPUT_DIR` to choose another root.

On PR preflight, if the current SHA is missing a required `local-e2e/...`
status, CI searches previous commits in the PR. When it finds the same
fingerprint with a successful status, it publishes a reused success status on
the current SHA instead of requiring a full local E2E rerun.

The suite uses the WireMock runtime under `mocks/` and does not install external tools.
E2E runners start WireMock with `E2E_WIREMOCK_DELAY_PROFILE=fast` by default,
which rewrites generated runtime mappings to keep normal responses short and
slow/loading fixtures bounded. Set `E2E_WIREMOCK_DELAY_PROFILE=legacy` to keep
the checked-in fixture delays unchanged for debugging.
Set `E2E_MAESTRO_SUITE` only for ad-hoc debugging when you want to bypass smart
scope resolution and run one explicit suite.
