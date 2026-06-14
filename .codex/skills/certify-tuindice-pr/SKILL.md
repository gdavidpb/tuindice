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

4. Run local commit-bound evidence:

```bash
./gradlew --continue --console=plain e2eMaestroEvidenceLocal
```

5. If evidence fails, enter the iterative correction loop. Do not open or mark a PR ready.
6. After evidence passes, rerun the audit helper and verify manifests for the final remote SHA.
7. Open or update a non-draft PR against `production` with a title that does not mention Codex.
8. Verify the PR head SHA matches the certified SHA.

## Iterative Correction Loop

Treat failures as normal certification work:

1. Identify the failing platform, suite, and flow from Gradle output, `maestro.log`, and `junit.xml`.
2. Decide whether the failure is product behavior, E2E coverage, or local environment.
3. Fix product code or E2E fixtures/tests when the failure is real.
4. For local environment failures, clean the affected simulator/device/WireMock/port state and rerun without unrelated code changes.
5. Commit every code or test fix, push it, and verify `HEAD == @{u}`.
6. Rerun `./gradlew --continue --console=plain e2eMaestroEvidenceLocal`.
7. Repeat until the final pushed SHA has passing evidence.

Never rely on evidence from an older SHA after new commits are pushed.

## Pull Request Rules

- Base branch: `production`.
- Head branch: current `feat/*` branch.
- Draft: false.
- Title: concise product/change title, without `Codex`.
- Body: include summary, verification commands, evidence outcome, and any notable failure/fix loop.
- Prefer the GitHub connector when `gh auth status` is unavailable or stale; otherwise `gh pr create` is acceptable.

## Resources

- `references/certification-runbook.md`: detailed TuIndice certification and PR procedure.
- `scripts/inspect_certification_state.py`: local audit helper for branch/upstream/evidence manifests.
