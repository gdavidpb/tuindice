# TuIndice PR Certification Runbook

Procedure for certifying a `feat/*` branch with local native E2E evidence (XCUITest on iOS, UI Automator on Android)
before a PR against `production`. Organised by decision. `SKILL.md` has the verdict table, the failure classes and
the stop conditions; this file has the detail behind them.

## 1. What is certified, and what has been measured

- **The remote SHA.** Evidence counts when the tree is clean, `HEAD == @{u}`, and a trusted `success` status exists
  on that SHA with the context `local-e2e/<platform>/local-certification-suite` (one definition,
  `e2e_status_context` in `.github/scripts/common.sh`; `e2e.py contexts` prints it). Trusted means created by the
  repository owner or `github-actions[bot]`. Its description is `Local E2E <p> <N>/<N> passed for <sha7> fp
  <fp12>.`, plus counts of retried, quarantined and overridden scenarios when there are any.
- **The ledger.** `build/e2e/ledger/<platform>/<fingerprint>/ledger.json` records every attempt of every scenario
  (SHA, duration, class, summary, load, artifacts). It is read only when platform, fingerprint and catalog hash
  match. A scenario green for the fingerprint is never rerun; a scenario gets 2 attempts per fingerprint across
  invocations (`E2E_MAX_RETRIES`, default 1, allowed 0 to 2). Root `./gradlew clean` deletes `build/e2e`, ledgers
  included: do not run it mid-certification. `status` falls back to remote statuses, local accumulation is lost.
- **The fingerprint** (`e2e/scripts/shared/e2e-fingerprint.sh`, version in `layout.env`) is a function of the
  git tree alone, so CI and this machine agree. Platform-scoped: an iOS-only fix keeps Android evidence valid.
  Covered: app and KMP runtime sources, `mocks/`, `e2e/catalog`, scenario and runner sources, `e2e/scripts/shared`
  plus the platform's scripts, `e2e/toolchain/<platform>.lock`, the Gradle version catalog. Not covered: unit tests,
  version bumps, `.github/**`, `.codex/**`, `docs/**`, `e2e/tools/**`, `e2e/platform/**`, `*.md`, root
  `build.gradle.kts`. Every fix under `e2e/scripts/**` invalidates the evidence of the platform it touches.
- **Verdicts** (`current`, `reusable`, `unpublished`, `partial`, `rerun`, `exhausted`) are computed by
  `e2e.py status`: GitHub first (HEAD, then the branch's commits since the merge-base with `production`, then the
  base itself; commits are asked about once and shared by both platforms, at most 100), the local ledger's
  publication records only when GitHub cannot be read. Never rerun because the SHA moved.

### Measured data

These are the only measurements this runbook relies on. Each says where it came from.

- Audit of the 13-hour certification of `feat/university-states-backlog` (October 2026), section F, on a 10-core M1
  Max with 64 GB: load averages 8.19 / 25.99 / 34.33 at 13:16 (F1); corrupted long entries rose from 9 % to 36 %
  from 10:00 and the load source was not recorded (F2); key delivery latency from HID to insertion stayed at a
  12-15 ms median through the night, p99 30 to 44 ms (F3); time since reboot showed no effect before 10:00: 9 %,
  7 %, 13 %, 8 % per segment (F4); the host had been up 34 days (F6).
- The same audit and plan reproduced the text loss without any device: the auth fields turned `abc` into `ac` and
  `12-3` into `13-` in two host tests that failed before the fix. That is why a typed-text mismatch is a product
  or driver defect and not load.
- Measured on the same 10-core host during the native E2E work (October 2026; the reports of that work hold the
  runs): Android failed through emulator slowness when the host load exceeded about 1.3 per core: 8 of 160 runs, all with
  a final `load1` of 12.9 or more on 10 cores, while iOS passed 80 of 80 with host load up to 54. With a 4-core
  emulator the emulator's own load reached 12-13; with 8 cores and 16 GB it stayed at 2-5. One real source of load
  peaks was another session running `pytest -n auto`.
- Same host, emulator and simulator running scenarios together (October 2026): `load1` was 7.7 (0.77 per core)
  while `top` showed 53 % of the CPU idle, and a parallel probe waited on load 37 times with the machine half free.
  The load average overstates contention there; only the `pytest -n auto` session brought the idle CPU near zero.

## 2. Before paying for evidence

Certification verifies; it does not discover. Every regression found during evidence costs a full run. Audit the
whole diff against `production` and fix everything in one batch, then certify once.

- Read every production-code change and ask what behavior it altered that nothing now protects; watch changes that
  alter timing (two sources combined, a write moved across an await, persist reordered against delete).
- Reactive read followed by a durable write (settings, Room, the outbox, the network): one transiently
  inconsistent read can become a permanent wrong value.
- A latch set from a catch-all failure path: check every write against which failures can reach it, terminal or
  transient.
- Imperative reads that sample two sources one after the other do not see what `combine` sees.
- Consumers of what the diff changed, in other modules too (modules that read shared Room tables directly).
- Test doubles that cannot fail: zero-latency fakes make dispatch-order races unreachable under `runTest`.
- Harness state that leaks between scenarios: WireMock scenario reset does not clear custom transformer datasets;
  the interpreter resets them before each scenario, so a new dataset needs its reset endpoint.
- Assertions whose detection power the diff removed (optimistic projections can pass while the backend rejects).
- Harness scripts the diff adds or changes: run each once in isolation (`bash -n`, its happy path, the fallback its
  own error message recommends).
- Behavior reachable only through E2E and without unit coverage: add the cheap test now.

Verify each finding in the code before acting; record what you ruled out and why. Separate regressions this branch
introduced from pre-existing issues (`git diff production...HEAD` settles it); only the former block certification.

Then diagnose the scenarios the branch changed, in risk order, with no clean tree and nothing published:

```bash
python3 e2e/scripts/shared/e2e.py list --changed-since origin/production
python3 e2e/scripts/shared/e2e.py run --platform all --mode diagnose --scenario <id>,<id>
```

`--tag <module>` selects a module's scenarios. `--survey` keeps going after failures and lists them all (diagnose
only; evidence refuses it with exit 2). `--repeat N` (1 to 50) repeats the selection and prints the pass rate per
scenario. `--trace` asks the runner for step traces. `E2E_SCENARIOS=<id> ./gradlew e2eAndroid` (or `e2eIos`) is
the same through Gradle. `--dry-run` prints what would run and the toolchain and environment checks (a refusal
appears as `WOULD REFUSE`), and builds or boots nothing.

## 3. Running evidence

```bash
./gradlew e2eEvidence            # both platforms; the harness decides parallel or sequential
./gradlew e2eEvidenceIos         # one platform (e2eEvidenceAndroid is the same)
E2E_SKIP_ANDROID=1 ./gradlew e2eEvidence   # E2E_SKIP_IOS=1 is symmetric
python3 e2e/scripts/shared/e2e.py run --platform all --mode evidence --dry-run
```

- Run it only when the helper says `partial` or `rerun`. A second invocation runs only what is not green; a failed
  scenario is retried alone. Order: scenarios with failed attempts, then those whose steps changed since the base,
  then catalog order.
- Evidence runs the whole catalog: it rejects `--scenario`, `--tag`, `--repeat`, `--trace`, `--survey` and
  `E2E_SCENARIOS` with exit 2. Each platform is an independent process: one failing does not cancel the other.
- Budget: `E2E_BUDGET_MINUTES` (default 120, 10 to 240) per platform. When it runs out the greens are kept (exit
  4) and the next invocation continues. It counts as an invocation for the stop conditions.
- Each platform publishes its own status when it is fully green, `HEAD == @{u}` and the commit is visible on
  GitHub. `E2E_PUBLISH_GITHUB_STATUS=0` runs without publishing.
- One runner invocation per scenario; the host cleans app state between scenarios, the driver never does. WireMock
  has an ownership lock per platform and its log is kept inside the run directory.
- Devices stay running afterwards. Stop them in the wrap-up (section 8).
- After evidence run the helper again; a published platform shows `current`.

## 4. Reading a failed run

Everything is under `build/e2e/runs/<runId>/` (`--platform all` adds a parent directory with `summary.json`):

| File | What it holds |
|---|---|
| `summary.txt` | The `RESULT` line, then one line per scenario: passed, failed with class and summary, or not run |
| `run.log` | The exact log lines (`START`, `PASS`, `FAIL`, `RETRY`, `STOP`, `ENV`, `LOAD`, `BUDGET`, `RESULT`) |
| `manifest.json` | Outcome, exit code, fingerprint, phases, budget, parallel decision and reason, host, load samples, competing processes, env check, toolchain against the lock, device, attempts, results, `stop` block |
| `junit.xml` | Real counts: failed, skipped (not run, quarantined) |
| `scenarios/<id>/attempt-<n>/` | `result.json` (steps with durations and the failure with step, expected and actual), `classification.json`, `runner.log`, `wiremock-requests.json`, `crash.txt` when the probe found one, screenshots, `logcat.txt` (Android) or `app.log` (iOS) |

Classification takes the first match: a failed pre-step is `environment`; a crash or ANR of the app is `app_crash`;
a scenario killed by its timeout is `timeout`; a missing, foreign or contradicting `result.json` is `tooling_error`;
a `TYPED_TEXT_MISMATCH`, or a 401 on `/auth/v2/bootstrap` whose decoded `Authorization` differs from the account,
is `typed_text_mismatch` ("typed X but the backend received Y"); the same 401 with the right credential, a request
with no stub, or another Bearer is `backend_mismatch`; the rest is `product_assertion`. `APP_NOT_RUNNING` before
the first step is `environment`; from the first step on it is `app_crash` only when the crash probe found a crash
or an ANR, otherwise `product_assertion`.

Where the time went: `python3 e2e/tools/e2e-profile.py` (latest run per platform), `--compare 1` (against the
previous run), `--last 5`. It prints per-scenario duration, per-primitive p50/p95 and the cost of one runner
invocation. Do not delete under `build/e2e` by hand. The harness runs `e2e/tools/e2e-retention.py` when a run
ends: last 10 runs per platform, a failed attempt keeps everything up to 200 MB, managed content capped at
`E2E_ARTIFACTS_MAX_GB` (5). `e2e-retention.py` without flags only simulates. `--purge-legacy --yes` removes the
leftovers of the previous harness and is the owner's command.

## 5. Decisions by exit code

| Exit | Meaning | Do |
|---|---|---|
| 0 | Green, and published when publishing was required | Open or update the PR |
| 1 | Some scenarios failed; greens are kept | Read `summary.txt`, fix by class (`SKILL.md`), push, rerun the helper |
| 2 | A precondition failed: dirty tree, an invalid catalog or expired quarantine, a variable out of range, a refused flag, a status context the harness cannot publish, a scenario selection that matches nothing | Fix what the message names; nothing was run |
| 3 | The environment was refused or could not be recovered | Fix the machine (section 6), rerun once. A second exit 3 in the session is a stop condition |
| 4 | Budget exhausted; greens are kept | Rerun; it continues with the pending scenarios |
| 5 | Stopped with a diagnosis: `typed_text_mismatch` or `app_crash` at the first attempt, or the same class twice for a scenario | Stop |
| 6 | Green, but publishing failed | Fix `gh` or the push, then `python3 e2e/scripts/shared/e2e.py publish --platform <p>`; the ledger is intact |
| 7 | A scenario used all its attempts for this fingerprint; refused before touching a device | Stop |
| 130, 143 | Interrupted; the manifest was still finalised | Rerun; the ledger kept what finished |

Precedence when `--platform all` returns several: 2, 3, 5, 7, 1, 4, 6.

### Stop conditions, in full

Stop and report on: exit 5; exit 7; exit 3 twice; the same scenario failing under two consecutive fingerprints
after a fix; three evidence invocations for one platform; four hours of evidence wall time (platforms that ran
together count once). `inspect_certification_state.py` computes the last four from the run manifests and ledgers
of the branch and exits 2 when one holds.

Raising retries, reinvoking, forcing sequential mode and rebooting are not remedies: raising `E2E_MAX_RETRIES`,
invoking evidence again on the same fingerprint, `E2E_PARALLEL=never|always`, rebooting a device or the host, and
`E2E_ENV_OVERRIDE` do not diagnose anything. The harness caps attempts across invocations (exit 7); the other
overrides are recorded in the manifest and in the status description. `e2e.py reset-scenario --platform <p> --id
<id> --reason <text>` grants one extra attempt once per scenario and fingerprint; it is the owner's decision.

When stopping, give the owner: the harness's diagnosis (the `STOP` line, and `stop.reason`, `stop.scenario`,
`stop.failureClass`, `stop.diagnosis` in the run's `manifest.json`), the attempt directory of the failing scenario,
the class, the helper output, and what you tried. State that you are not trying again.

## 6. Environment, toolchain and long branches

`./gradlew e2eEnvCheck` (or `e2e.py env-check [--json]`) measures the host with the thresholds below, which are
provisional until compared against `e2e-profile.py --compare` data. Evidence is refused (exit 3) only on `cpu` and
`disk`; everything else warns. `E2E_ENV_OVERRIDE` accepts only those two ids. All of it is recorded in the manifest.

| Check | Measure | Warn | Refuse (evidence only) |
|---|---|---|---|
| `load` | `load1 / ncpu` (informational) | 0.50 or more | never |
| `cpu` | idle CPU % (`top`, 1 s window); a reading under 15 is confirmed by a second sample 5 s later | under 35 | under 15 in both samples |
| `disk` | free GB | under 40 | under 15 |
| `uptime` | days up | 14 or more | never |
| `memory` | available GB | under 8 | never |
| `procs` | foreign processes at 100 % CPU or more | any | never |
| `daemons` | Gradle and Kotlin daemons | more than 3 | never |
| `foreign_device` | other emulator or simulator running | any | never |

- **Parallel or sequential** is decided by the harness: parallel only with `ncpu >= 8`, idle CPU of 50 % or more and
  memory of 32 GB or more; otherwise sequential. The decision and its reason are in the manifest. The skill sets no
  policy; do not choose it.
- **Before each scenario** the harness measures the idle CPU when `load1/ncpu` is 1.0 or more, and waits only if it
  is under 20 %, until it reaches 35 % (at most 300 s per scenario, 900 s per run); the Android adapter checks the
  device's own load (`health`, limit 6.0). The waits and the idle CPU are recorded per attempt in the manifest.
- **`--platform all`** prepares one platform at a time (device, build, install, under a host lock); scenarios still
  run in parallel. The wait for the lock is in the manifest (`prepareLock`).
- **Devices.** Android: the AVD of `e2e/toolchain/android.lock` as an 8-core, 16 GB emulator without a window,
  started with no snapshot. iOS: the dedicated simulator `TuIndice-E2E`, created from `e2e/toolchain/ios.lock`.
  A booted device is not rebooted. Reboot exists only as the recovery after an environment failure, once per run.
  The tunnel is `10.0.2.2` unless `E2E_ANDROID_TUNNEL=reverse`.
- **Toolchain lock.** Every key of `e2e/toolchain/{android,ios}.lock` is strict; a difference is exit 3 in
  evidence mode. To change a component (Xcode, runtime, system image, locale, emulator shape) edit the lock, one
  component per commit; that platform's evidence reruns. CI builds the UI test target with the Xcode that
  `.github/actions/setup-ios-build` selects, which can differ from the lock's.
- **Long branches: certify in increments.** At each milestone run `E2E_PUBLISH_GITHUB_STATUS=0 ./gradlew
  e2eEvidence` on a clean tree, so failures surface while the diff is small. The helper warns when HEAD is more than
  20 commits or 150 files past the last complete run (read from `build/e2e/ledger/<platform>/index.json`).

## 7. Preflight parity and CI ordering

- `.codex/skills/certify-tuindice-pr/scripts/run_preflight_parity_checks.sh [--dry-run]` resolves the same diff
  against `production` as `preflight-production-pr.yml`: workflow references, CI config validation, semgrep, the
  Android and iOS Gradle tasks with CI's flags, and the iOS UI test target build (`e2e/scripts/ios/build.sh
  --for-testing-only`) when the detector requires it. It needs a clean tree. Do not wrap it in `| tee`: the exit code
  becomes `tee`'s; redirect to a file instead.
- If the machine cannot run an impacted platform's preflight, do not claim full certification for it: fix the
  environment, use suitable hardware, or say the branch depends on GitHub preflight for that platform.
- Parity may be skipped only when the incremental diff since the last parity-passed commit is limited to `.codex/**`,
  `docs/**` and `*.md`; it must pass for the final SHA.
- **The first CI run of a SHA fails** with `Missing successful E2E status(es)` until evidence is published; that is
  ordering. After publishing, `gh run rerun <run-id>`: no new commit means GitHub does not retrigger by itself.
- **A green local run can be stale.** The preflight names the test tasks it reused (`UP-TO-DATE`, `FROM-CACHE`);
  rerun those with `--rerun-tasks`, always after a bisection in the same worktree.
- **A green Android run says nothing about iOS.** After a signature change in shared code run
  `./gradlew --continue :<module>:compileTestKotlinIosSimulatorArm64`.
- **`cmd | tail` hides the build's exit code.** Redirect to a file, capture `EXIT=$?`, read the full log.
- `./gradlew syncAppVersion verifyAppVersionSync` in one invocation can fail on the second task before the first
  regenerates the xcconfig; rerun `verifyAppVersionSync` alone before calling it a mismatch.
- A missing version bump stops evidence: bump `gradle/app-version.properties`, sync `iosApp/Config/Version.xcconfig`,
  commit, push, rerun the helper.
- **A failure only the hosted runner shows:** `verifyE2eContract` once failed twice on one SHA with two different
  false "missing" verdicts while 18 local and containerized reproductions passed. Harden the fragile pattern
  (a subprocess read through `< <(...)` into `while read`) to fail loudly on a killed producer, push, and verify on
  real CI.
- Delegating to subagents: forbid suppressing findings in `detekt-baseline.xml`, `git checkout --`/`restore`,
  commits, pushes and branch switches; require proof that new tests fail without the fix; and re-run the checks
  yourself.

## 8. PR, wrap-up and store copy

Before opening or updating a PR: branch `feat/*`, clean tree, `HEAD == @{u}`, parity passed for `HEAD`, and the
helper shows every required platform `current` or `reusable`.

```bash
gh auth status
gh pr create --base production --head "$(git branch --show-current)" --title "<title>" --body "<body>"
```

If `gh auth status` fails use the GitHub connector. Create a ready-for-review PR, not a draft. Title: concise
product or change title, no `Codex`. Body: summary of user-visible or workflow changes, verification commands,
evidence outcome with the certified SHA and platform verdicts, notable fix loops. Afterwards verify base
`production`, head branch, not draft, head SHA equal to the certified SHA.

Wrap-up:

1. Stop the devices: `bash e2e/scripts/android/device.sh stop` and `bash e2e/scripts/ios/device.sh stop`. Evidence
   needs them again after a later fix: the harness boots them on its own.
2. Deliver in the session, never in the PR body, a Spanish store-copy proposal derived from `git diff
   production..HEAD`: **Promotional Text** (170 characters max) and **What's New in This Version** (4000 max). End-user
   language in the app's voice; external functionality only (never tests, CI, harness, E2E, refactors, state
   machines, dependencies); new screens, flows, copy and fixes a user would notice. With no user-visible change, say
   so and propose keeping the current texts.
3. Retrospective: a short prioritized list, each item naming a concrete moment of this run (a failure that took
   several rounds, a false lead and the signal that would have prevented it, a rule here that was missing or
   contradicted, a manual step a script could do). No filler. Apply what is mechanical under section 9; what
   needs a product or design decision stays as text for the owner.

## 9. Maintaining this runbook

- A learning becomes, in this order: (1) a harness check or classification rule with a fake-adapter test in
  `e2e/tools/tests/`; (2) a host test in `:scenarios`; (3) one row in a table here. The incident narrative goes in
  the commit message, not here.
- No sentence may state a cause without a measured datum and its source. Section 1 lists the data this runbook
  relies on; a new cause enters there with its measurement or does not enter.
- Line budgets are in `e2e/tools/verify/line-budgets.env` and are checked by `./gradlew verifyE2eHarness`
  (`e2e/tools/verify/verify-line-budgets.sh`): `SKILL.md` 140, this runbook 300, `e2e/scripts/{shared,android,ios}`
  4,500. Raising one needs its own commit stating why.
- Edits here, in `SKILL.md`, the scripts of the skill and `e2e/tools/**` do not change the fingerprint, so they can
  ride the certified branch; the evidence stays `reusable`. Edits under `e2e/scripts/**` change it: batch them
  before the final evidence run or ship them in a follow-up PR.
- Keep `agents/openai.yaml` in step with `SKILL.md` (description and triggers).
