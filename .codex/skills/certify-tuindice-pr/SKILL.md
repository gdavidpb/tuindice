---
name: certify-tuindice-pr
description: Certify TuIndice feature branches for production PRs. Use when Codex needs to prepare or verify a TuIndice feat/* branch before opening or updating a pull request against production, including local commit-bound Maestro E2E evidence, evidence manifest audits, GitHub status publication checks, iterative failure fixes, pushes, and ready-for-review PR creation.
---

# Certify TuIndice PR

## Core Workflow

Use this skill for TuIndice app branches that need production PR certification.
Read `references/certification-runbook.md` for the detailed runbook before running long E2E evidence or opening a PR.

1. Ensure the branch is `feat/*`, based on `production`, and not dirty.
2. Commit and push all intended changes before certifying. The certified SHA must exist on GitHub.
3. Run the non-mutating audit helper:

```bash
python3 .codex/skills/certify-tuindice-pr/scripts/inspect_certification_state.py
```

If this reports a missing app version/build-number bump, stop and fix the bump
before running E2E evidence. Evidence from a SHA that preflight will reject is
not useful.

The audit also prints, per required platform/suite, whether passing E2E
evidence already exists for the current content fingerprint: `current`
(produced by HEAD), `reusable` (produced by an earlier commit with the same
fingerprint; production preflight republishes its status onto the PR head via
`E2E_REUSE_STATUS_BY_FINGERPRINT`), or `rerun` (no passing evidence for the
fingerprint). Suites marked `current` or `reusable` need no local rerun.

4. Run local PR preflight parity checks:

```bash
.codex/skills/certify-tuindice-pr/scripts/run_preflight_parity_checks.sh
```

This resolves the same focused Android/iOS Gradle tasks as
`preflight-production-pr.yml` for the current diff, including the iOS host flags
used by `Run focused iOS checks`.

5. Run local commit-bound evidence only when the audit reports `rerun` suites:

```bash
./gradlew --continue --console=plain e2eMaestroEvidenceLocal
```

Skip this step entirely when every required suite is `current` or `reusable`.

6. If preflight parity or evidence fails, enter the iterative correction loop. Do not open or mark a PR ready.
7. Before accepting any E2E/preflight stabilization fix, enforce the product integrity gate below.
8. After the final push, rerun the audit helper and confirm every required platform/suite is `current` or `reusable` for the final remote SHA.
9. Open or update a non-draft PR against `production` with a title that does not mention Codex.
10. Verify the PR head SHA matches the certified SHA.
11. Stop the local test devices left running by evidence: `e2e/scripts/stop-devices.sh android ios`. If a later fix forces another evidence run after devices are stopped, boot them again first with `e2e/scripts/boot-devices.sh android ios`.
12. Deliver in the session (never in the PR body) a Spanish store-copy proposal derived from the certified diff against `production`: **Promotional Text** (170 characters max) and **What's New in This Version** (4000 characters max). Both in end-user language describing external functionality only — no tests, CI, harness, refactors, or other internals. If the diff has no user-visible changes, say so and propose keeping the current store texts. See the runbook's Post-PR Wrap-up for the full rules.
13. Close with a self meta-analysis of this certification run: recommend — do not implement — concrete improvements to this skill's own scripts, runbook, or `SKILL.md`, grounded in what actually happened during the fix loop rather than generic advice. Present it as text in the session; this step must not edit skill files, commit, branch, or spawn a task to implement its own suggestions. See the runbook's Post-Certification Meta-Analysis for the full rules.

## Product Integrity Gate

E2E and preflight fixes must not arbitrarily alter the product experience.

- Prefer fixing tests, selectors, waits, fixtures, mock state, reset scripts, simulator/device state, or platform harnesses when the failure is test instability.
- Change production UI, copy, layout, navigation, timing, gestures, or business behavior only when the failure exposes a real product regression or the user explicitly requests that product change.
- If a certification fix touches user-visible product code, state the product rationale, compare it against the intended experience, and add/update focused product or UI coverage that protects the intended behavior.
- Do not move, hide, resize, reorder, relabel, or weaken product surfaces merely to make Maestro or preflight pass.
- If the only passing path requires changing the product experience and the rationale is not clear, stop and ask before committing or pushing that fix.

## Iterative Correction Loop

Treat failures as normal certification work. Iterate with cheap targeted
diagnosis runs; pay for full commit-bound evidence once, on the final SHA:

1. Identify the failing platform, suite, and flow from Gradle output, `maestro.log`, and `junit.xml`.
2. Decide whether the failure is product behavior, E2E coverage, or local environment.
3. Reproduce and fix with diagnosis runs, which need no clean or pushed tree and publish nothing:

```bash
E2E_MAESTRO_SUITE=e2e/maestro/flows/<failing-flow-or-suite>.yaml ./gradlew --console=plain e2eMaestroAndroid   # or e2eMaestroIos
```

Prefix with `E2E_MAESTRO_SURVEY_MODE=1` to report every failing case in one
pass instead of stopping at the first. When a tap/assert fails against an
element the hierarchy claims visible, run `e2e/scripts/make-probe.sh <flow>`
and read the per-step screenshots before hypothesizing. Verify flow fixes on
both platforms with `e2e/scripts/diagnose-suite.sh <suite> [--survey]` before
committing them. For iOS Compose UI-test failures, reproduce at full-module
granularity (`:module:iosSimulatorArm64Test` without `--tests`) before
investigating: narrow filters shift the process cold-start onto a different
test and manufacture phantom ComposeTimeoutExceptions. See the runbook's
Diagnosis Runs doctrine for details.

4. Fix product code or E2E fixtures/tests when the failure is real. For local environment failures, clean the affected simulator/device/WireMock/port state and rerun without unrelated code changes.
5. When every known failure is fixed, commit the batch, push it, and verify `HEAD == @{u}`.
6. Rerun `.codex/skills/certify-tuindice-pr/scripts/run_preflight_parity_checks.sh`. You may skip this rerun when the incremental diff since the last parity-passed commit only touches `e2e/maestro/**`, `mocks/**`, or documentation/skill files (none are Gradle inputs); parity must still pass for the final SHA before opening the PR.
7. Rerun the audit helper and run `./gradlew --continue --console=plain e2eMaestroEvidenceLocal` only when it reports `rerun` suites.
8. Repeat until the final pushed SHA has passing preflight parity and every required suite `current` or `reusable`.

Evidence validity follows the content fingerprint — the same rule production
preflight enforces. Do not rerun evidence just because the SHA moved; rerun the
suites whose fingerprint changed. Fingerprints are platform-scoped: an
iOS-host-only fix keeps Android evidence reusable, and vice versa.

## Pull Request Rules

- Base branch: `production`.
- Head branch: current `feat/*` branch.
- Draft: false.
- Title: concise product/change title, without `Codex`.
- Body: include summary, verification commands, evidence outcome, and any notable failure/fix loop.
- Prefer the GitHub connector when `gh auth status` is unavailable or stale; otherwise `gh pr create` is acceptable.

## Resources

- `references/certification-runbook.md`: detailed TuIndice certification and PR procedure.
- `scripts/inspect_certification_state.py`: local audit helper for branch/upstream/evidence manifests and fingerprint reusability.
- `scripts/run_preflight_parity_checks.sh`: local reproduction of the PR focused preflight jobs.
