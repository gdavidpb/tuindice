---
name: certify-tuindice-pr
description: Certify TuIndice feature branches for production PRs. Use when a TuIndice feat/* branch must be prepared or verified before opening or updating a pull request against production: local commit-bound native E2E evidence (XCUITest on iOS, UI Automator on Android), evidence verdicts and stop conditions, GitHub status publication, fixing failures, pushing, and opening a ready-for-review PR.
---

# Certify TuIndice PR

Read `references/certification-runbook.md` before paying for evidence or opening a PR. This file is organised by
decision: what to do when something is the case.

## What is certified

- What is certified is the remote SHA: the commit the PR head points to. Evidence counts only when the tree is
  clean, `HEAD == @{u}`, and a trusted `success` status `local-e2e/<platform>/local-certification-suite` naming
  that platform's fingerprint exists on that SHA. Any change to the branch needs a push, and the evidence has to
  match the pushed SHA.
- Evidence is a full run of every scenario on each required platform, accumulated in a ledger per platform and
  fingerprint. A scenario that is green for the fingerprint is never rerun.
- The person who certifies launches the evidence (`git push`, then `./gradlew e2eEvidence`). An agent prepares,
  diagnoses and reads; it launches evidence only when asked to.

## What the audit helper says

```bash
python3 .codex/skills/certify-tuindice-pr/scripts/inspect_certification_state.py
```

Read-only. It checks branch, clean tree, upstream and version bump, then prints the verdict of each required
platform from `python3 e2e/scripts/shared/e2e.py status --json` (also `./gradlew e2eStatus`). Exit 0: done. Exit 1:
evidence still to produce. Exit 2: a stop condition holds.

| Verdict | Condition | Do |
|---|---|---|
| `current` | A trusted success status naming the platform's fingerprint is on HEAD | Nothing |
| `reusable` | The same status is on a commit preflight considers: the branch's commits since the merge-base with `production`, or that base itself | Nothing; preflight republishes it |
| `unpublished` | No such status; the ledger is green for every in-scope scenario | Push if HEAD is not on GitHub, then `python3 e2e/scripts/shared/e2e.py publish --platform <p>` |
| `partial` | No such status; at least one scenario is green | `./gradlew e2eEvidence` (or the platform task); only the pending ones run |
| `rerun` | No such status; nothing is green for this fingerprint | `./gradlew e2eEvidence` (or the platform task) |
| `exhausted` | No such status; a scenario used all its attempts | Stop (below); a new run is refused with exit 7 |

The fingerprint of a platform covers: its app and KMP runtime sources, `mocks/`, the scenario catalog and runner
sources, `e2e/scripts/shared` plus the platform's own scripts, its toolchain lock and the Gradle version catalog.
It does not cover unit-test sources, version bumps, `.github/**`, `.codex/**`, `docs/**`, `e2e/tools/**` or `*.md`.
Never rerun because the SHA moved. Never rerun a platform that is `current` or `reusable`.

## Workflow

1. Branch `feat/*` from `production`, tree clean, pushed.
2. Run the pre-certification diff audit (runbook section 2) before paying for anything: every regression found
   during evidence costs a full run.
3. Run the audit helper. A missing version bump stops here.
4. Run `.codex/skills/certify-tuindice-pr/scripts/run_preflight_parity_checks.sh`: the focused Android and iOS
   Gradle tasks of CI, plus the iOS UI test target build when CI will run it (`--dry-run` prints the commands).
5. Diagnose what the branch changed: `python3 e2e/scripts/shared/e2e.py list --changed-since origin/production`, then
   `python3 e2e/scripts/shared/e2e.py run --platform all --mode diagnose --scenario <ids>`. It needs no clean
   tree and publishes nothing.
6. Only when the helper says `partial` or `rerun`: `./gradlew e2eEvidence`, or `e2eEvidenceAndroid` /
   `e2eEvidenceIos` for one platform.
7. On a non-zero exit read the run's `summary.txt` and follow "Failure handling" and "Stop conditions".
8. After the final push, rerun the helper: every required platform is `current` or `reusable`.
9. Open or update a non-draft PR against `production` and verify its head SHA equals the certified SHA.
10. Stop the devices: `python3 e2e/scripts/shared/e2e.py stop-devices` (`--platform <p>` for one).
11. Deliver the Spanish store-copy proposal in the session (runbook section 8), never in the PR body.
12. Close with a short retrospective grounded in what happened; apply the maintenance rules below.

## Failure handling by class

The class is in the `FAIL` line, `summary.txt` and the attempt's `classification.json`.

| Class | Meaning | Do | Never |
|---|---|---|---|
| `typed_text_mismatch` | The field or the backend got text different from what the scenario typed; the message shows both | Reproduce with `--mode diagnose --scenario <id> --repeat 20`; fix the product or the driver, or report it | Call it load; retry; split the input; add guard characters |
| `app_crash` | The crash probe found a crash or an ANR of the app | Read `crash.txt`; fix | Retry |
| `product_assertion` | The expected UI did not appear | Diagnose with `--trace` | Weaken the assertion |
| `backend_mismatch` | The mock rejected the declared credential, had no stub, or saw another Bearer | Fix the mapping or the scenario's mock state | Change the product |
| `timeout`, `tooling_error` | Runner or harness | Read `runner.log` and `result.json`; fix the harness | Raise timeouts first |
| `environment` | Device or host not usable | Fix the machine (`./gradlew e2eEnvCheck`), rerun | Change code |

## Stop conditions

Stop and report to the person who owns the branch on any of these:

- the harness exits 5 or 7;
- it exits 3 twice in the session;
- the same scenario fails under two consecutive fingerprints (a fix did not fix it);
- three evidence invocations for one platform in the session;
- four hours of evidence wall time in the session.

The helper prints these counters; a session is a platform's runs under the current fingerprint since its last
complete run (a green run or a fix that moves the fingerprint starts a new one). Raising `E2E_MAX_RETRIES`, invoking evidence again on the same fingerprint,
forcing `E2E_PARALLEL`, rebooting devices or the host, and `E2E_ENV_OVERRIDE` are not remedies. Hand over: the
`STOP` line and the `stop` block of the run's `manifest.json` (reason, scenario, class, diagnosis), the attempt
artifacts in `build/e2e/runs/<runId>/scenarios/<id>/attempt-<n>/`, the class, and the helper output. Say that you
are not trying again.

## Host load

The harness measures free CPU (not just the load average), the emulator's own load, disk, uptime, memory, competing
processes and foreign devices, waits or refuses from those numbers, decides parallel or sequential, and records
them in the manifest. Do not record `uptime` by hand, do not infer load
from symptoms, and do not choose the mode. A typed-text mismatch is never load.

## Long branches

Certify in increments: at each milestone run `E2E_PUBLISH_GITHUB_STATUS=0 ./gradlew e2eEvidence` on a clean tree
(nothing is published), so failures surface while the diff is small. The helper warns when HEAD is more than 20
commits or 150 files past the last complete run. `run --platform all --mode diagnose --survey` lists every failing
scenario in one pass without accumulating anything.

## Product integrity gate

E2E and preflight fixes must not alter the product experience to please automation.

- Prefer fixing tests, scenarios, fixtures, mock state, drivers and harness when the failure is instability.
- Change production UI, copy, layout, navigation, timing, gestures or behavior only for a real regression or an
  explicit request; then state the product rationale and add focused coverage.
- Never move, hide, resize, reorder, relabel or weaken surfaces to make a scenario pass. A change in how text is
  typed, or a guard character, is a defect workaround and is not allowed.
- If the only passing path changes the product and the rationale is unclear, stop and ask before committing.

## Pull request rules

- Base `production`, head the current `feat/*`, not draft, title without `Codex`.
- Body: summary, verification commands, evidence outcome (certified SHA, platforms, verdicts), notable fix loops.
- Prefer the GitHub connector when `gh auth status` fails; otherwise `gh pr create`.

## Maintaining this skill

- A learning becomes a harness check or classification rule with a test, a host test, or one row of a table here.
  Incident narrative goes in the commit message. No sentence states a cause without a measured datum and source.
- Edits to this skill and to `e2e/tools/**` leave the fingerprint alone; edits to `e2e/scripts/**` change it, so
  batch them before the final evidence run or ship them in a follow-up PR.
- Line budgets of this file, the runbook and `e2e/scripts/{shared,android,ios}` live in
  `e2e/tools/verify/line-budgets.env` and are checked by `./gradlew verifyE2eHarness`; raising one needs its own commit.

## Resources

- `references/certification-runbook.md`: the procedure, the exit codes, the environment and the maintenance rules.
- `scripts/inspect_certification_state.py`: branch, version, verdicts and session counters.
- `scripts/run_preflight_parity_checks.sh`: local reproduction of the PR preflight jobs.
