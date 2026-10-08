# Local E2E

End-to-end scenarios that run the real debug app on an Android emulator and an iOS simulator against the WireMock
runtime of `mocks/`. A scenario is data written once in Kotlin; each platform has a small native driver that
performs it, and a harness runs the scenarios one by one, keeps a ledger of what passed and publishes the result.

```text
scenarios/            the catalog: accounts, fixtures, texts and the scenarios, written with the DSL of scenariokit
scenariokit/          the step model, the DSL, the interpreter, the driver contract and the JSON codec
e2e/catalog/          scenarios.json, generated from the Kotlin catalog and versioned
scenariorunner/       Android driver: UI Automator out of process, an instrumentation test per scenario
iosApp/UITests/       iOS driver: XCUITest, one generated test method per scenario (Generated/ is versioned too)
e2e/scripts/          the harness: shared/ (orchestrator in Python 3.9, stdlib only), android/ and ios/ (adapters)
e2e/toolchain/        android.lock and ios.lock: the tool and device versions evidence is allowed to run on
e2e/tools/            profile, retention, verifiers and the harness tests (outside the fingerprint)
e2e/platform/         notes on the platform edges that no scenario drives
mocks/                the WireMock runtime and its mappings (the QA backend); see testkit/e2e/fixture-contract.md
```

The per-scenario flow: the interpreter (`ScenarioRunner` in scenariokit) resets WireMock, launches the app with the
launch arguments of the scenario, runs the steps and writes `result.json`; the driver only touches the screen. The
harness cleans the app state between scenarios (Android `pm clear`, iOS uninstall plus keychain reset); a driver
never does.

## Gradle tasks

| Task | What it does |
|---|---|
| `e2eAndroid`, `e2eIos` | Diagnostic run of one platform. `E2E_SCENARIOS=<id>,<id>` narrows it. Never produces evidence. |
| `e2eEvidenceAndroid`, `e2eEvidenceIos`, `e2eEvidence` | Evidence for one platform or both: the whole catalog, accumulated per fingerprint and published as a commit status. They depend on `verifyE2eArtifactsFresh`. |
| `e2eStatus` | Prints the ledger of the current fingerprint per platform. |
| `e2eEnvCheck` | Measures the host against the environment thresholds. |
| `syncE2eArtifacts` | Regenerates `e2e/catalog/scenarios.json` and `iosApp/UITests/Generated/ScenarioTests.generated.swift` from the scenarios. |
| `verifyE2eArtifactsFresh` | Fails when those two files differ from what the scenarios generate. |
| `verifyE2eContract` | The aggregate: host tests of `:scenariokit` and `:scenarios`, `verifyE2eArtifactsFresh`, `verifyE2eHarness` and `verifyLaunchArgumentContract`. |
| `verifyE2eHarness` | `e2e/tools/tests/run-harness-tests.sh`: shell and Python syntax, the harness tests, fingerprint coverage, line budgets and the vocabulary gate. |
| `verifyLaunchArgumentContract` | Debug launch arguments are declared once and never read in release builds. |
| `verifyIosUiTestsBuild` | Builds the host app and the `TuIndiceUITests` bundle (macOS only). |

The tasks are registered in `gradle/e2e-tasks.gradle.kts`. Everything the harness does is also available directly:

```bash
python3 e2e/scripts/shared/e2e.py --help
python3 e2e/scripts/shared/e2e.py list [--platform android|ios] [--changed-since REF]
python3 e2e/scripts/shared/e2e.py env-check [--json]
python3 e2e/scripts/shared/e2e.py status [--json]
python3 e2e/scripts/shared/e2e.py contexts
```

`status` and `contexts` serve certification; see `.codex/skills/certify-tuindice-pr/`.

## Run a scenario in diagnostic mode

A diagnostic run does not need a clean tree, publishes nothing and writes no ledger.

```bash
python3 e2e/scripts/shared/e2e.py run --platform ios --mode diagnose --scenario auth-login-success
python3 e2e/scripts/shared/e2e.py run --platform all --mode diagnose --tag record --survey
E2E_SCENARIOS=auth-login-success ./gradlew e2eAndroid
```

- `--scenario` takes ids separated by commas; `--tag` selects a module's scenarios or a tag such as `smoke`.
- `--survey` keeps going after failures and lists them all; `--repeat N` repeats the selection and prints the pass
  rate per scenario; `--trace` asks the runner for step traces; `--dry-run` prints the plan, the toolchain check and
  the environment check without building or booting anything.
- `--driver-contract` also runs the driver contract (the `driver-contract` adapter verb) before the first scenario;
  evidence always runs it. When environment failures mix with greens, a `--repeat` run prints a `SERIES` line with how
  many scenario runs were valid and how many the environment took (the manifest keeps it in `series`).
- `--platform all` prepares one platform at a time and, when the host has the capacity, runs both in parallel; the
  decision and its reason are recorded.
- The harness boots the dedicated devices itself (the AVD and the `TuIndice-E2E` simulator named by the locks) and
  leaves them running; `e2e.py stop-devices [--platform <p>]` stops them.

## Where the results are

Everything is under `build/e2e/` (`E2E_STATE_ROOT` moves it; `./gradlew clean` deletes it):

| Path | Content |
|---|---|
| `runs/<runId>/summary.txt`, `run.log`, `manifest.json`, `junit.xml` | The verdict and the log of one run; the manifest also records host, load, toolchain and device. `runId` is `<UTC time>-<platform>-<mode>-<sha7>`. |
| `runs/<runId>/scenarios/<id>/attempt-<n>/` | `result.json` (steps with durations, and the failure with step, expected and actual text), `classification.json`, `runner.log`, `wiremock-requests.json`, screenshots and the device log of a failed attempt. |
| `ledger/<platform>/<fingerprint>/ledger.json` | Every attempt of every scenario for one fingerprint. Only evidence runs write it. |

`python3 e2e/tools/e2e-profile.py` shows where the time went; `python3 e2e/tools/e2e-retention.py` prunes old runs
(it only simulates without `--apply`). Reading a failed run is described in the certification runbook.

## Add a scenario

1. Pick the file of the module: `scenarios/src/commonMain/kotlin/com/gdavidpb/tuindice/scenarios/catalog/<Module>Scenarios.kt`
   (for example `AboutScenarios.kt`). Declare the scenario with `scenario("<module>-<name>", "<module>", start) { ... }`
   and add it to the `<module>Scenarios` list at the bottom of the file. A scenario that is not in the list never runs.
2. Say how it starts: `Start.Seeded(E2eAccounts.<Account>)` (already signed in, on a section) or `Start.Clean()` (the
   sign-in screen). Only the ten scenarios of the allowlist in `CatalogStartTest` type the credential, and only
   through `signInThroughUi`.
3. Use the selectors the app already declares in the `*UiTags` objects of the owning module, texts from `Copy`
   and ids or values from `E2eFixtures`. A literal tag, text or bare timeout fails a host test; use the named
   timeouts (`Within.Assert`, `Action`, `Wait`, `Long`, `Sync`).
4. Declare `covers("<module>.<Contract>.<Action>")` for the MVI actions the scenario really fires. An action no
   scenario fires gets an entry in `ActionDispositions.kt` instead, never both.
5. If it needs backend behavior, add mappings under `mocks/mappings/<domain>/` and bodies under `mocks/__files/`; a
   new account goes in `E2eAccounts.kt` with the mapping that accepts its credential.
6. Regenerate and check:

```bash
./gradlew syncE2eArtifacts      # runs the :scenarios host tests and copies the generated JSON and Swift
./gradlew verifyE2eContract
python3 e2e/scripts/shared/e2e.py run --platform all --mode diagnose --scenario <module>-<name>
```

A scenario that must be parked is declared with `quarantine(reason, until = "YYYY-MM-DD")` in its block (no scenario is
quarantined today). The harness reports it as skipped, counts it in the status description, and an expired date makes the
run exit 2 until the scenario is fixed or the date is renewed on purpose.

Commit the two generated files with the scenario. The host tests in `scenarios/src/androidHostTest/` are the rules
(ids, one smoke scenario per product module, branch budget per platform, tags that exist, texts bound to resources,
action coverage, mock contract); their messages say what to fix.

## Evidence

Before its first scenario an evidence run executes the `driver-contract` verb of the platform's adapter once: the
driver's contract (Android also runs the on-device driver and typing probes, not the `typingSeries` measurement). A red
contract, a missing verb or an answer the harness cannot read is exit 3 and no scenario runs; the manifest records the
phase and `driverContract`. A simulator that stops serving preferences is `environment` (see the runbook).
The manifest also counts what the drivers tolerated on their own as `tolerances: {key: n}` (empty when none).

Evidence is produced by `./gradlew e2eEvidence` (or one platform) on a clean tree whose `HEAD` is pushed. The
fingerprint depends on the git tree alone: app and runtime sources, `mocks/`, `e2e/catalog`, the driver sources, the
shared scripts plus the platform's, and the toolchain lock. Tests, version bumps, `.github/`, `.codex/`, `docs/`,
`e2e/tools/`, `e2e/platform/` and markdown files do not change it. How to certify a branch, the verdicts, the stop
conditions and the environment thresholds are in `.codex/skills/certify-tuindice-pr/` and its runbook.

## Environment variables

| Variable | Meaning |
|---|---|
| `E2E_SKIP_ANDROID`, `E2E_SKIP_IOS` | `1` leaves that platform out of an `all` run. |
| `E2E_SCENARIOS` | Comma-separated ids; diagnostic runs only. |
| `E2E_MAX_RETRIES` | Retries per scenario (default 1, allowed 0 to 2); fixed for a fingerprint when its ledger is created. |
| `E2E_BUDGET_MINUTES` | Wall-clock budget per platform (default 120, 10 to 240). |
| `E2E_PARALLEL` | `auto` (default), `never` or `always`; recorded when changed. |
| `E2E_PUBLISH_GITHUB_STATUS` | `auto` (default), `0` or `1`. |
| `E2E_BASE_SHA` | Overrides the base of the diff that decides which platforms need evidence (default: the merge-base with `origin/production` or `production`). |
| `E2E_ENV_OVERRIDE` | `cpu` and/or `disk`: lets evidence run past a refusal; recorded. |
| `E2E_ANDROID_WIREMOCK_PORT`, `E2E_IOS_WIREMOCK_PORT` | 18626 and 18627 by default. |
| `E2E_ANDROID_TUNNEL` | `host-alias` (default, `10.0.2.2`) or `reverse` (`adb reverse`). |
| `E2E_WIREMOCK_DELAY_PROFILE` | `fast` (default) shortens fixture delays unless a mapping pins its own; `legacy` keeps the checked-in ones. |
| `E2E_TMP_ROOT`, `E2E_STATE_ROOT` | Scratch directory and the `build/e2e` root. |
| `E2E_ARTIFACTS_MAX_GB` | Cap of the retention (default 5). |

`E2E_FAKE_*`, `E2E_*_CMD`, `E2E_CATALOG_FILE` and `E2E_SCOPE_FILE` are test seams of the harness tests; evidence that
is published refuses to run with any of them set.
