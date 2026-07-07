# TuIndice PR Certification Runbook

Use this runbook when a TuIndice feature branch needs local E2E certification and a PR against `production`.

## Pre-Certification

- Work on a `feat/*` branch. If the current branch belongs to another PR, create a new branch from `production`.
- Keep the working tree clean before certification:

```bash
git status --short --branch
```

- Confirm the final commit is pushed before running evidence:

```bash
git rev-parse HEAD
git rev-parse @{u}
```

The two SHAs must match. Evidence statuses are commit-bound and only publish when the certified commit is `HEAD`, the tree is clean, and the commit exists remotely.

- Use the helper for a compact audit:

```bash
python3 .codex/skills/certify-tuindice-pr/scripts/inspect_certification_state.py
```

If the helper reports `missing_version_bump`, do not run E2E evidence yet.
First bump the missing build number(s) in `gradle/app-version.properties`, sync
`iosApp/Config/Version.xcconfig`, commit, push, and rerun the helper. E2E
evidence is commit-bound and should only be spent on a SHA that production
preflight can accept.

The helper also prints the focused Android and iOS Gradle tasks selected by
`.github/scripts/detect-changed-app.sh`. Treat these as the local preflight
contract for the branch.

The helper also reports, per required platform/suite, whether passing E2E
evidence already exists for the current content fingerprint:

- `current`: the evidence for this fingerprint was produced by HEAD.
- `reusable`: an earlier commit produced passing evidence for the same
  fingerprint. Production preflight republishes that status onto the PR head
  (`E2E_REUSE_STATUS_BY_FINGERPRINT`), so no local rerun is needed.
- `rerun`: no passing evidence exists for the fingerprint; evidence must run.

Fingerprints are platform-scoped (`e2e/scripts/e2e-fingerprint.sh`): Android
hashes the Android host plus shared runtime paths, iOS hashes the iOS host plus
shared runtime paths. A host-only fix on one platform keeps the other
platform's evidence valid. Version bumps in `gradle/app-version.properties` do
not change fingerprints.

## Running PR Preflight Parity

Before spending time on Maestro evidence, run the local parity helper:

```bash
.codex/skills/certify-tuindice-pr/scripts/run_preflight_parity_checks.sh
```

This helper resolves the same diff against `production`, runs
`.github/scripts/validate-ci-config.sh` when CI/CD files are touched, and
executes the selected Android/iOS Gradle tasks through
`.github/scripts/run-gradle-with-retry.sh`. The iOS command uses the same host
build environment variables, iOS resource flags, and `ios-host-cache` init script
as the PR `Run focused iOS checks` job.

Use `--dry-run` to inspect the exact commands without executing them:

```bash
.codex/skills/certify-tuindice-pr/scripts/run_preflight_parity_checks.sh --dry-run
```

If the local machine cannot run an impacted platform's focused preflight, do not
claim full certification for a ready production PR. Either fix the local
environment, run the platform check on suitable hardware, or explicitly report
that the branch still depends on GitHub preflight for that platform.

During the correction loop, you may skip rerunning parity when the incremental
diff since the last parity-passed commit only touches `e2e/maestro/**`,
`mocks/**`, or documentation/skill files — none of these are Gradle inputs.
Parity must still pass for the final SHA before opening the PR.

## Running Evidence

Run the audit helper first and skip this step entirely when every required
platform/suite is `current` or `reusable`; production preflight republishes
fingerprint-matched statuses on its own. Otherwise run the aggregate local
evidence task:

```bash
./gradlew --continue --console=plain e2eMaestroEvidenceLocal
```

The task resolves the diff against `production` or `origin/production`, selects required suites, runs locally available platforms, writes evidence, and publishes passing GitHub commit statuses when possible.

Before starting the platform workers it cold-reboots the in-scope
emulator/simulators — endurance flakiness after hours of device uptime is
real and was measured during certification. Set
`E2E_DEVICE_REBOOT_BEFORE_EVIDENCE=0` to skip the reboot.

Evidence is written under:

```text
build/e2e/certifications/<sha>/<platform>/<suite>/
```

Each suite directory should contain `manifest.json`, `maestro.log`, `junit.xml`, and Maestro outputs. The manifest must describe the same SHA that will be used as the PR head.

## Iterative Failure Handling

If evidence fails, stop the PR path and diagnose:

- Read the terminal output first for platform, suite, and flow names.
- Inspect `build/e2e/certifications/<sha>/<platform>/<suite>/maestro.log`.
- Inspect `junit.xml` and any screenshots/videos/flow logs emitted in the same evidence directory.
- If the failure is a product regression, fix the product behavior.
- If the failure is stale or insufficient E2E coverage, fix the flow, fixture, assertion, or selector.
- If the failure is local environment only, clean the specific simulator/device, WireMock process, port, or temporary state and rerun without unrelated code changes.

### Diagnosis Runs

Reproduce and iterate on failures with targeted runs before spending on
commit-bound evidence. Diagnosis runs need no clean or pushed tree, publish
nothing, and write no evidence:

```bash
E2E_MAESTRO_SUITE=e2e/maestro/flows/suites/<suite>.yaml ./gradlew --console=plain e2eMaestroAndroid
E2E_MAESTRO_SUITE=e2e/maestro/flows/<failing-flow>.yaml ./gradlew --console=plain e2eMaestroIos
```

Diagnosis doctrine, in order:

1. **Survey first when more than one case might be broken.** Run with
   `E2E_MAESTRO_SURVEY_MODE=1` so the runner keeps executing after failures
   and reports every failing case in one pass; the checkpoint lands on the
   first failure and suite retries are disabled. One survey run replaces one
   full run per discovered failure.
2. **Probe before hypothesizing on tap/assert failures.** iOS reports
   off-viewport lazy-list items as visible, so hierarchy asserts can pass
   while taps silently no-op. Generate a step-screenshot probe and observe
   the actual screens instead:

   ```bash
   e2e/scripts/make-probe.sh e2e/maestro/flows/<failing-flow>.yaml
   e2e/scripts/make-probe.sh --clean
   ```

3. **Verify flow fixes on BOTH platforms before committing.** Screen-geometry
   differences resurface one-platform fixes as fresh failures during the next
   60-90 minute evidence run:

   ```bash
   e2e/scripts/diagnose-suite.sh e2e/maestro/flows/suites/<suite>.yaml [--survey]
   ```

4. **Reproduce iOS Compose UI-test failures at full-module granularity before
   investigating.** The first test in a fresh `iosSimulatorArm64Test` process
   pays a cold-start tax, so a narrow `--tests` filter changes which test goes
   first and can manufacture `ComposeTimeoutException` failures that do not
   exist in the unfiltered module run — the granularity CI and preflight
   parity use. Confirm with `./gradlew :module:iosSimulatorArm64Test
   --max-workers=1` (no `--tests` filter): a test that only fails under a
   filter is a phantom, not a regression, and diagnosing it wastes the
   isolation rounds it appears to justify.

Module flows that open with shared runFlow refs run as a single flow when
targeted directly; only pure runFlow-list suites expand into per-case
execution, so a diagnosis run always exercises the flow's inline commands.

Batch every fix found this way instead of certifying fix-by-fix. Resume-first
checkpoints make the eventual evidence rerun start at the previously failing
case, so an unfixed failure still surfaces within minutes.

### Product Integrity Gate

Treat E2E stabilization as a test and certification activity unless the evidence
proves a real product bug. Do not change the product experience simply because a
flow becomes easier to drive.

- Do not move, hide, resize, reorder, relabel, or weaken UI surfaces just to make
  Maestro, preflight, or platform automation pass.
- Prefer harness-level fixes: stable selectors, waits, reset state, fixtures,
  mocked responses, simulator/device cleanup, or platform-specific test helpers.
- If production UI, copy, layout, navigation, gestures, timing, or business
  behavior must change, document why it is the intended product behavior and not
  an automation workaround.
- Add or update focused product/UI coverage for any user-visible product change
  made during certification, so the intended experience is protected from future
  stabilization regressions.
- Stop and ask before committing or pushing when a passing certification path
  depends on a user-visible product change whose product rationale is unclear.

After the batch of code or test fixes:

```bash
git status --short --branch
git add <files>
git commit -m "<focused message>"
git push
git rev-parse HEAD
git rev-parse @{u}
.codex/skills/certify-tuindice-pr/scripts/run_preflight_parity_checks.sh
python3 .codex/skills/certify-tuindice-pr/scripts/inspect_certification_state.py
./gradlew --continue --console=plain e2eMaestroEvidenceLocal   # only when the audit reports rerun suites
```

Repeat until the final pushed SHA has passing preflight parity and every
required suite `current` or `reusable`. Evidence validity follows the content
fingerprint — the same rule production preflight enforces — so pushing commits
that do not change a platform's fingerprint does not require rerunning that
platform's evidence.

If `production` advances or the branch is rebased, rerun the audit for the new
final SHA; evidence stays valid for any platform/suite whose fingerprint is
unchanged.

## Evidence Audit

Run the helper after evidence:

```bash
python3 .codex/skills/certify-tuindice-pr/scripts/inspect_certification_state.py
```

For each required platform/suite, verify one of:

- A HEAD manifest: `commitSha` equals `git rev-parse HEAD`, `statusCode` is `0`,
  and platform/suite names match the resolved scope.
- A `reusable` fingerprint verdict: a passing manifest from an earlier commit
  whose fingerprint equals HEAD's; preflight republishes that status onto the
  PR head.

In both cases the local branch must have no uncommitted changes and `HEAD` must
equal upstream.

The aggregate evidence command may publish covered suite statuses from `local-certification-suite`; PR preflight requires trusted success statuses with the expected fingerprint, not just local files.

## Opening Or Updating The PR

Before opening a PR:

- Branch is `feat/*`.
- Working tree is clean.
- `HEAD == @{u}`.
- Local PR preflight parity has passed for `HEAD`.
- Evidence manifests for `HEAD` pass audit.

Use `gh` only when authenticated:

```bash
gh auth status
gh pr create --base production --head "$(git branch --show-current)" --title "<title>" --body "<body>"
```

If `gh auth status` fails, use the GitHub connector instead. Create a ready-for-review PR, not a draft.

PR title rules:

- Do not include `Codex`.
- Prefer a concise product/change title.

PR body should include:

- Summary of user-visible or workflow changes.
- Tests and evidence commands.
- Evidence result, including the certified SHA and relevant platform/suite outcome.
- Any notable failure/fix iterations that explain why the final SHA differs from an earlier attempted SHA.

After creation or update, verify:

- PR base is `production`.
- PR head branch is the current branch.
- PR is not draft.
- PR head SHA equals the certified SHA.
