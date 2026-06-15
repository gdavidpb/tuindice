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

## Running Evidence

Run the aggregate local evidence task:

```bash
./gradlew --continue --console=plain e2eMaestroEvidenceLocal
```

The task resolves the diff against `production` or `origin/production`, selects required suites, runs locally available platforms, writes evidence, and publishes passing GitHub commit statuses when possible.

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

After a code or test fix:

```bash
git status --short --branch
git add <files>
git commit -m "<focused message>"
git push
git rev-parse HEAD
git rev-parse @{u}
.codex/skills/certify-tuindice-pr/scripts/run_preflight_parity_checks.sh
./gradlew --continue --console=plain e2eMaestroEvidenceLocal
```

Repeat until the final pushed SHA has passing preflight parity and evidence. Do
not reuse evidence from a previous commit after pushing new changes.

If `production` advances or the branch is rebased, rerun evidence for the new final SHA.

## Evidence Audit

Run the helper after evidence:

```bash
python3 .codex/skills/certify-tuindice-pr/scripts/inspect_certification_state.py
```

For each required platform/suite manifest, verify:

- `commitSha` equals `git rev-parse HEAD`.
- `statusCode` is `0`.
- Platform and suite names match the resolved scope.
- The local branch has no uncommitted changes.
- `HEAD` equals upstream.

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
