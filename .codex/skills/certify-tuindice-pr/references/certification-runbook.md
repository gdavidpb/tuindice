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

`./gradlew syncAppVersion verifyAppVersionSync` in one invocation has no
declared task dependency between the two, so Gradle is free to run
`verifyAppVersionSync` first — it then fails against the not-yet-regenerated
xcconfig even though `syncAppVersion` fixes it moments later in the same
build. A `BUILD FAILED` here after editing `gradle/app-version.properties`
is not necessarily real: rerun `verifyAppVersionSync` alone once `syncAppVersion`
has completed before treating it as a genuine mismatch.

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

## Pre-Certification Diff Audit

Mandatory, and it runs *before* preflight parity and evidence — not after a
failure. Certification is a verification mechanism, not a discovery mechanism:
every regression found during an evidence run costs a full rotation (30-100+
minutes of device time) plus a preflight rerun, and invalidates the fingerprint
so the next attempt pays again. An audit that takes minutes routinely finds what
several rotations would have surfaced one at a time.

Audit the entire diff against `production`. Fix everything found **in one
batch**, then certify once.

### What to look for

- **Regressions in the diff itself.** Read every production-code change and ask
  what behaviour it altered that nothing now protects. Pay special attention to
  changes that alter *timing* rather than logic: replacing one source of truth
  with two combined sources, moving a write across an await point, or reordering
  a persist against a delete. Those are invisible to reviews and to deterministic
  tests.
- **Reactive read → durable write loops.** Any code that observes state and, as a
  side effect, writes something durable (settings, Room, the outbox, the network)
  can turn one transiently-inconsistent read into a permanent wrong value. Find
  them and check each against a lagging source.
- **Imperative reads that sample two sources sequentially.** A `combine` holds the
  latest of both at once; two consecutive `suspend` reads do not. An ordering
  invariant that holds for the reactive path can be silently broken on the
  imperative one.
- **Consumers of anything the diff changed**, including in *other* modules. Sibling
  modules that read shared Room tables directly will not appear in the diff at
  all, yet can regress because the diff stopped writing those tables.
- **Test doubles that cannot fail.** If every fake is a zero-latency
  `MutableStateFlow`, no dispatch-order race is reachable under `runTest`, and a
  green suite proves nothing about the class of bug that timing changes cause.
  Check whether the harness can *express* the failure before trusting it.
- **Harness state that leaks across cases and retries.** WireMock scenario reset
  does not clear custom transformer datasets. A suite that fails after mutating a
  dataset can poison its own retry so it is unpassable, burning the whole
  rotation for nothing.
- **Assertions whose detection power the diff removed.** If a projection now shows
  optimistic local state, an E2E assertion that "the value appears" may pass even
  when the backend rejected the write. Know which greens still mean something.
- **Behaviour reachable only through E2E.** Anything the diff changed that has no
  unit coverage is something certification will discover expensively. Prefer
  adding the cheap test now.

### Verify before believing

Confirm each finding against the code yourself before acting on it, and confirm
each proposed fix is actually correct in this codebase. Audits produce false
positives, and a plausible-sounding fix can be wrong for reasons only visible
locally. Record what you deliberately ruled out and why — an audit that only
lists hits is indistinguishable from a fishing expedition.

Also separate **regressions this branch introduced** from **pre-existing issues**
(`git diff production...HEAD` on the specific lines settles it). Only the former
block certification; file the rest instead of expanding scope.

### Then verify cheaply, in risk order

Before paying for a full evidence rotation, run the highest-risk flows in
isolation. `e2e/scripts/diagnose-flows.sh` takes them in order, runs both
platforms, and stops at the first failure:

```bash
e2e/scripts/diagnose-flows.sh \
  e2e/maestro/flows/<module>/<highest-risk-flow>.yaml \
  e2e/maestro/flows/<module>/<next>.yaml \
  e2e/maestro/flows/suites/<module>-suite.yaml
```

Each target is roughly four minutes per platform, needs no clean or pushed
tree, and publishes nothing. Order by which assertions depend on behaviour the
diff changed; put anything with no unit coverage first. Finish with the
affected suite, adding `--survey` on that last pass so every remaining failure
is reported at once — and because only a suite run can expose state leaking
between cases, which single-flow runs cannot reproduce.

This is the step that decides whether certification takes one rotation or
several. Skipping it does not save time; it moves the same discoveries to the
most expensive place to make them.

## Inner-Loop Discipline

Three ways this loop lies about being green.

**A green Android run says nothing about iOS.** `testAndroidHostTest` does not
compile the `iosTest` source set. Renaming anything in `commonMain` can leave
iOS call sites broken through an entire local verification cycle, surfacing
only minutes into preflight. After any signature change in shared code, compile
both sides before trusting the result:

```bash
./gradlew --continue :<module>:compileTestKotlinIosSimulatorArm64
```

It takes seconds against the nine minutes preflight costs to tell you the same
thing.

**Reading a long task through `tail` reports the wrong exit code.** In a
`cmd | tail -n` pipeline the status belongs to `tail`, so a failed build looks
like a success, and the surviving lines can show an unrelated part of the run —
a retry-absorbed failure reads exactly like a fatal one. Redirect to a file and
capture the status:

```bash
<command> > "${log}" 2>&1; echo "EXIT=$?"; tail -5 "${log}"
```

Then read the failure from the full log, not from the tail.

**Verify a fix by reverting it.** A test that passes with the fix removed is
not coverage. This matters most for anything timing-related, where the default
test doubles cannot express the failure at all: revert the fix, confirm the
test fails, restore it with an explicit edit — never `git checkout`/`restore`,
which silently discards other uncommitted work.

### Delegating parts of the loop

Subagents handle well-scoped refactors and audits well, but their output needs
the same verification as anything else — re-run the checks yourself rather than
trusting the report. State these in the prompt, because each has been violated:

- Never suppress findings in a `detekt-baseline.xml`. Debt the change itself
  introduces gets fixed, not recorded. Never regenerate a baseline: it silently
  drops unrelated suppressions.
- Never use `git checkout --`/`restore`, never commit, push, or switch branches;
  uncommitted work from the main session is usually present.
- Prove any new test is falsifiable and report the experiment.
- Update every call site of a renamed symbol across all source sets, not just
  the one the local test task compiles.

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

Do not pipe this script (or the evidence command below) through `tee` or any
other wrapper for logging. Both scripts use `set -euo pipefail` internally,
but wrapping the whole invocation in `command | tee file` makes the *outer*
shell's exit code `tee`'s, not the script's — a real failure deep inside can
report exit 0 to whatever is watching it. Redirect straight to a file
(`command > file 2>&1`) or, when running in the background, rely on the
background task's own captured output instead of adding a manual `tee`.

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

**This parallel invocation is the default and the standing policy — both
platforms run together in the same command.** Do not pre-emptively split
Android and iOS into separate sequential runs (`e2eMaestroEvidenceAndroid`
then `e2eMaestroEvidenceIos` on distinct `E2E_WIREMOCK_PORT` values) to save
time or "because it seemed safer." Sequential is a diagnosed fallback for one
specific symptom only: the parallel run fails the *same* early flow on
*repeated* attempts (not a random flow, not a one-off), the failure screenshot
shows no product error (the screen/inputs look correct, a tap/action simply
never registers), and the identical flow passes cleanly when run standalone.
That signature points at local device/resource contention (both
emulator+simulator plus any other heavyweight process — e.g. leftover Gradle
or Kotlin daemons from unrelated work — competing for CPU/memory), not a
product regression. Only then fall back to sequential, and say so explicitly
to the user when you do — it is a deviation from policy that needs to be
visible, not a silent substitution.

The task resolves the diff against `production` or `origin/production`, selects required suites, runs locally available platforms, writes evidence, and publishes passing GitHub commit statuses when possible.

Before starting the platform workers it cold-reboots the in-scope
emulator/simulators — endurance flakiness after hours of device uptime is
real and was measured during certification. Set
`E2E_DEVICE_REBOOT_BEFORE_EVIDENCE=0` to skip the reboot. The same reboot also
runs before the suite's single retry rotation (not just the first attempt),
since a retry re-runs the full suite and adds just as much continuous device
uptime as the initial pass.

When the audit shows only one platform needs `rerun` (the other is already
`current` or `reusable`), do not pay for a full re-verification of the
platform that already passed:

```bash
E2E_SKIP_ANDROID=1 ./gradlew --continue --console=plain e2eMaestroEvidenceLocal
```

This flag only exists for Android — there is no symmetric `E2E_SKIP_IOS`. When
only Android needs `rerun` and iOS is already `current`/`reusable`, the iOS
worker still starts and runs the full suite again; there is no local way to
skip it today.

If evidence runs after `stop-devices.sh` already shut the local emulator and
simulator down (for example, a harness-only fix landed post-PR and changed
the fingerprint), boot them again first:

```bash
e2e/scripts/boot-devices.sh android ios
```

This resolves the Android emulator through `$ANDROID_HOME`/`$ANDROID_SDK_ROOT`
or `local.properties`, never bare `emulator` on `PATH` — on Apple Silicon that
usually resolves to the legacy `tools/emulator` launcher, which fails looking
for a `darwin-x86_64` qemu binary that does not exist on arm64.

If a PR already exists and its `shared-preflight` gate already failed on the
current SHA because evidence was missing, publishing that evidence afterward
does not retrigger CI on its own — no new commit was pushed, so there is
nothing for GitHub to react to. Rerun the workflow explicitly once the audit
confirms the SHA is `current`/`reusable`:

```bash
gh run rerun <run-id>
```

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

### CI-Only Failures

Some certification-harness failures only manifest on the GitHub-hosted
runner and never reproduce locally, even in a matching Linux container: a PR
preflight once failed `verifyE2eContract` twice on the identical SHA with two
different false "missing" verdicts for catalog entries that were genuinely
present, while 18 local and containerized reproduction attempts (matching
bash version, matching `awk`/`grep` implementation, sequential and
concurrent, constrained file descriptors) all passed cleanly. The common
thread across these is resource pressure unique to the real runner — the
Android preflight job compiles and tests a dozen Kotlin/Android modules
concurrently under `--max-workers=2`, which a lightweight local repro of the
one failing script does not recreate.

When local reproduction is exhausted and the failure is still CI-only:

1. Identify the most resource-fragile pattern in the failing code path — a
   subprocess whose output is consumed by `< <(...)` into a `while read` loop
   is a common one: a killed producer looks identical to an empty result to
   the reader, no error surfaces.
2. Harden that pattern to fail loudly instead of silently on a killed or
   partial producer (write to a real file, check the producer's exit status)
   rather than chasing the exact trigger indefinitely.
3. Push and verify against real CI directly — local reproduction has already
   proven insufficient as a verification signal for this class of failure.

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

## Post-PR Wrap-up

Once the PR exists and its head SHA is verified:

1. Stop the local test devices — evidence runs leave an Android emulator and
   an iOS simulator running:

```bash
e2e/scripts/stop-devices.sh android ios
```

2. Deliver a store-copy proposal in the session (never inside the PR body),
   written in Spanish and derived from the certified diff against
   `production`:

- **Promotional Text** — 170 characters max.
- **What's New in This Version** — 4000 characters max.

Rules for both texts:

- End-user language in the app's voice: describe what the user can now do or
  what annoyance went away.
- External functionality only. Never mention tests, CI, harness, E2E,
  refactors, state machines, dependencies, or any internal detail invisible
  to the user.
- Source the content from the user-visible changes in
  `git diff production..HEAD` — new screens, flows, copy, and fixes a user
  would actually notice.
- When the diff contains no user-visible changes, say so explicitly and
  propose keeping the store's current texts instead of inventing content.

## Post-Certification Meta-Analysis

After the store-copy proposal, close the session with a short retrospective
on this specific certification run — not a generic checklist. Then act on it:
a recommendation that only ever exists as session text is lost the moment the
session ends, which is how the same cost gets paid twice.

Where it goes is covered below in Carrying Feedback Forward. The certified
branch is off limits: its diff is already certified, and evidence is bound to
its fingerprint.

Ground every recommendation in something that actually happened during this
run:

- A failure that took more than one diagnosis round to root-cause, and what
  would have caught it sooner (a lint, a doctrine rule, a script).
- A false lead chased before the real cause surfaced (e.g., isolating too
  narrowly, misreading a log) — and what signal, surfaced earlier, would have
  prevented it.
- A step in this runbook, or a script's guardrail, that was missing,
  ambiguous, or contradicted what actually happened.
- A manual step a script could have automated, if the pattern is likely to
  recur — not a one-off.

Skip filler. If nothing meaningful surfaced this run, say so plainly instead
of padding the list with generic advice ("add more tests", "improve
documentation"). A recommendation with no concrete moment behind it does not
belong here.

Format: a short prioritized list, each item naming the concrete trigger from
this run and the specific change proposed (file, script, or doctrine point).
Do not restate points already closed by a prior certification's
meta-analysis unless this run surfaced a gap in that fix.

## Carrying Feedback Forward

Improvements found while certifying wait on `chore/certification-feedback` and
are absorbed by the next `feat/*` branch. **The branch existing is what marks
feedback as pending**; nothing else tracks it.

The certified branch never carries them: its diff is certified and its evidence
is bound to a fingerprint, so touching it invalidates both.

### Writing feedback out (end of certification, step 15)

Apply only what is mechanical — scripts, lints, runbook and `SKILL.md` text,
harness fixtures. Anything needing a product or design decision stays as text
for the user; do not guess it into the branch.

1. Branch from current `origin/production`, not from the certified branch, so
   the feedback carries no product changes.
2. Apply the changes and verify them the same way any other change is verified.
   A broken lint shipped here breaks the *next* branch, where nobody expects it.
3. Commit with what the finding actually cost — "hid a BUILD FAILED and cost a
   nine-minute preflight" is what makes a later reader keep the rule. A commit
   that only says what changed loses the reason within a month.
4. Push. If the branch already exists, add commits to it rather than replacing
   it: an earlier certification's feedback may still be waiting.

### Absorbing it (start of the next certification, step 2)

Merge, do not cherry-pick — and note that a squash-merged PR does not make the
feedback commits ancestors of `production`, so absorption cannot be detected
from ancestry. That is why the branch is deleted explicitly once absorbed.

1. Merge `origin/chore/certification-feedback` into the new `feat/*` branch.
2. Verify what it brought: run the lints or scripts it touches before trusting
   them, since they now gate this branch's own certification.
3. Push the merge, then delete the remote branch — the feedback is now carried
   by a branch heading for `production`.
4. Report what was absorbed, so it is visible in the PR that will ship it.

If the feedback does not belong in this branch — an unrelated hotfix, or a
release branch that must stay minimal — say so and leave the branch untouched
for the next one. Never carry it silently.
