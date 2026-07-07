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
and isolates WireMock plus temporary files per platform. If either worker
fails, the aggregate runner cancels the remaining platform worker and exits
failed. It skips a platform when the resolved scope has no suites for it.
Override the default ports or temporary roots with `E2E_ANDROID_WIREMOCK_PORT`,
`E2E_IOS_WIREMOCK_PORT`, `E2E_ANDROID_TMP_DIR`, and `E2E_IOS_TMP_DIR`.

Evidence is written to `build/e2e/certifications/<sha>/<platform>/<suite>/`
and mirrored to
`build/e2e/certifications/by-fingerprint/<fingerprint>/<platform>/<suite>/`.
The fingerprint is computed from functional app inputs plus Maestro flows and
WireMock fixtures, so pipeline-only changes and version-only release metadata
can reuse a previous passing certification for the same app behavior.
Unit-test-only sources such as `commonTest`, `androidTest`, `app/src/test`, and
the generated `iosApp/Config/Version.xcconfig` are intentionally excluded so
test fixes or version bumps do not invalidate E2E evidence for unchanged
runtime behavior.
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

Maestro runners execute suite `runFlow` entries one case at a time by default.
When a case fails, the runner stores the failed target under
`build/e2e/checkpoints/<platform>/<suite>/`. The next run starts from that
target to fail fast after a local fix, then wraps around and executes the
earlier cases before reporting success. Checkpoints never skip cases: a passing
run always executes the full suite, only with a rotated start point. Set
`E2E_MAESTRO_RESUME_FIRST=0` to use the legacy monolithic Maestro execution.
The segmented runner prints the plan, one `START` line per case, and the final
`PASS` or `FAIL` for that case; it does not emit heartbeat lines while Maestro
is still running. Use `E2E_MAESTRO_RAW_OUTPUT=1` when debugging Maestro itself
and you want raw CLI output in the terminal; otherwise raw output stays in the
per-case logs.

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
which rewrites every generated runtime mapping's `fixedDelayMilliseconds` to
250ms unless the mapping pins its own value in `metadata.fastDelayMilliseconds`
(see `mocks/scripts/apply-fast-delay-profile.sh`). A mapping whose flow depends
on the delay — cancel windows, reveal timers — must declare that marker;
`verifyE2eContract` fails any mapping with a legacy delay of 5s or more that
lacks it. Set `E2E_WIREMOCK_DELAY_PROFILE=legacy` to keep the checked-in
fixture delays unchanged for debugging.
Platform runners stop their owned WireMock process on success, failure, or
interruption so the default `8080` port is not left occupied after local E2E.
Platform runners isolate Maestro CLI runtime logs under `E2E_TMP_DIR` by
default; set `E2E_MAESTRO_HOME` only when debugging Maestro itself.
Set `E2E_MAESTRO_SUITE` only for ad-hoc debugging when you want to bypass smart
scope resolution and run one explicit suite.

Suite plans run changed flows first: a case whose yaml (or a direct runFlow
ref) differs from the merge-base with `origin/production` — committed,
unstaged, or untracked — executes before untouched cases, so a broken new flow
fails within the first cases instead of minutes into the rotation. Checkpoint
resume and retry rotation compose with the reordered plan unchanged. Set
`E2E_MAESTRO_CHANGED_FIRST=0` to disable, `E2E_MAESTRO_CHANGED_FIRST_BASE` to
diff against another ref, or `E2E_MAESTRO_CHANGED_FLOWS_FILE` to inject the
changed list explicitly (paths relative to `e2e/maestro/flows`).
