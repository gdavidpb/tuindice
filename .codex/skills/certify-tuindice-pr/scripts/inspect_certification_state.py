#!/usr/bin/env python3
"""Inspect a TuIndice branch before and after local E2E evidence. Read-only.

Git, scope and version checks live here. The evidence verdict (current, reusable, incomplete, unpublished,
partial, rerun, exhausted) comes from `e2e/scripts/shared/e2e.py status --json`; this script adds the session counters that the
runbook's stop conditions need, read from the harness's run manifests and ledgers.

Exit codes: 0 every check passed and every required platform is current (reusable evidence is one `e2e.py publish` away: the workflow of a PR does not republish it); 1 something is not done yet;
2 a stop condition holds: report to the person who owns the branch instead of trying again.
"""

from __future__ import annotations

import datetime
import glob
import json
import os
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[4]
HARNESS = Path("e2e") / "scripts" / "shared" / "e2e.py"

SAFE_VERDICTS = ("current",)
NO_STOP_VERDICTS = ("current", "reusable")  # reusable needs one command, not a rerun, so no stop condition applies
COMPLETE_EXITS = (0, 6)  # green: published (0) or green with the publication failing (6)
# A session, for a platform, is its evidence invocations under the fingerprint HEAD has now, made after the last
# complete one: a fix that moves the fingerprint, or a green run, starts a new one.
MAX_EVIDENCE_INVOCATIONS = 3  # per platform, in one session
MAX_EVIDENCE_HOURS = 4.0  # wall time of evidence, in one session
MAX_ENVIRONMENT_EXITS = 2  # exit 3 runs, in one session
MAX_DRIFT_COMMITS = 20  # HEAD past the last complete run: warn beyond this many commits...
MAX_DRIFT_FILES = 150  # ...or this many files
NEXT_ACTION = {
    "incomplete": "ask GitHub again (python3 e2e/scripts/shared/e2e.py status); this is not missing evidence, so do not run {task}",
    "reusable": "python3 e2e/scripts/shared/e2e.py publish --platform {platform}   (cites the evidence on {evidence}; the workflow of a pull request does not republish it)",
    "unpublished": "git push if HEAD is not on GitHub, then: python3 e2e/scripts/shared/e2e.py publish --platform {platform}",
    "partial": "./gradlew {task}   (only the pending scenarios run)",
    "rerun": "./gradlew {task}",
    "exhausted": "fix the cause; a new run is refused with exit 7 (python3 e2e/scripts/shared/e2e.py reset-scenario is "
    "the owner's decision, once per scenario)",
}
TASKS = {"android": "e2eEvidenceAndroid", "ios": "e2eEvidenceIos"}


@dataclass(frozen=True)
class GitResult:
    code: int
    stdout: str
    stderr: str


@dataclass(frozen=True)
class CertificationScope:
    requires_e2e: bool | None
    e2e_scope: str
    missing_version_bump: str
    android_tasks: str = ""
    ios_tasks: str = ""
    ios_ci_scripts_touched: bool = False
    ios_uitest_build_required: bool = False
    app_version_changed: bool = False
    has_release_impact: bool = False
    error: str = ""


def run_git(*args: str) -> GitResult:
    completed = subprocess.run(
        ["git", *args],
        cwd=REPO_ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    return GitResult(
        code=completed.returncode,
        stdout=completed.stdout.strip(),
        stderr=completed.stderr.strip(),
    )


def run_repo_script(*args: str) -> GitResult:
    completed = subprocess.run(
        ["bash", *args],
        cwd=REPO_ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    return GitResult(
        code=completed.returncode,
        stdout=completed.stdout.strip(),
        stderr=completed.stderr.strip(),
    )


def print_check(ok: bool, label: str, detail: str = "") -> None:
    prefix = "PASS" if ok else "FAIL"
    suffix = f" - {detail}" if detail else ""
    print(f"[{prefix}] {label}{suffix}")


def load_json(path) -> dict | list | None:
    try:
        return json.loads(Path(path).read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return None


def parse_github_output(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    if not path.is_file():
        return values

    for line in path.read_text(encoding="utf-8").splitlines():
        key, separator, value = line.partition("=")
        if separator:
            values[key] = value
    return values


def merge_base_for_e2e_scope(head: str) -> str | None:
    """Merge-base with the base branch; the base ref has one definition, e2e_base_ref in common.sh (origin first)."""
    completed = subprocess.run(
        ["bash", "-c", 'source .github/scripts/common.sh; e2e_base_ref "$1"', "_", str(REPO_ROOT)],
        cwd=REPO_ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.DEVNULL,
        check=False,
    )
    ref = completed.stdout.strip()
    if completed.returncode != 0 or not ref:
        return None
    result = run_git("merge-base", ref, head)
    return result.stdout if result.code == 0 and result.stdout else None


def app_version_name() -> str:
    properties_path = REPO_ROOT / "gradle" / "app-version.properties"
    try:
        for line in properties_path.read_text(encoding="utf-8").splitlines():
            key, separator, value = line.partition("=")
            if separator and key.strip() == "versionName":
                return value.strip()
    except OSError:
        return ""
    return ""


def release_tag_target(tag_name: str) -> tuple[str, str]:
    """Commit a release tag points at, and which source answered.

    origin is authoritative (local tags can be stale); the local lookup only
    covers offline runs. An empty sha means the tag does not exist on that
    source.
    """
    # The peeled ref must be requested explicitly: the plain pattern only
    # returns the annotated tag object, whose sha never equals a commit.
    remote = run_git(
        "ls-remote",
        "--tags",
        "origin",
        f"refs/tags/{tag_name}",
        f"refs/tags/{tag_name}^{{}}",
    )
    if remote.code == 0:
        target = ""
        for line in remote.stdout.splitlines():
            sha, _, ref = line.partition("\t")
            if ref.strip() == f"refs/tags/{tag_name}^{{}}":
                return sha.strip(), "origin"
            if ref.strip() == f"refs/tags/{tag_name}":
                target = sha.strip()
        return target, "origin"

    local = run_git("rev-parse", "-q", "--verify", f"refs/tags/{tag_name}^{{commit}}")
    if local.code == 0:
        return local.stdout, "local tags (origin unreachable)"
    return "", "local tags (origin unreachable)"


def detect_certification_scope(head: str) -> CertificationScope:
    before_sha = merge_base_for_e2e_scope(head)
    if not before_sha:
        return CertificationScope(
            requires_e2e=None,
            e2e_scope="<unknown>",
            missing_version_bump="",
            error="Unable to resolve merge-base with production.",
        )

    with tempfile.TemporaryDirectory(prefix="tuindice-cert-audit.") as temp_dir_name:
        temp_dir = Path(temp_dir_name)
        output_path = temp_dir / "detect-output.env"
        state_dir = temp_dir / "state"
        env = {
            **os.environ,
            "GITHUB_OUTPUT": str(output_path),
            "STATE_DIR": str(state_dir),
        }
        completed = subprocess.run(
            ["bash", ".github/scripts/detect-changed-app.sh", before_sha, head],
            cwd=REPO_ROOT,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            env=env,
            check=False,
        )
        if completed.returncode != 0:
            detail = completed.stderr.strip() or completed.stdout.strip()
            return CertificationScope(
                requires_e2e=None,
                e2e_scope="<unknown>",
                missing_version_bump="",
                error=f"detect-changed-app failed: {detail}",
            )

        values = parse_github_output(output_path)
        return CertificationScope(
            requires_e2e=values.get("requires_e2e_certification") == "true",
            e2e_scope=values.get("e2e_scope_csv") or "<none>",
            missing_version_bump=values.get("missing_version_bump_csv") or "",
            android_tasks=values.get("android_tasks") or "",
            ios_tasks=values.get("ios_tasks") or "",
            ios_ci_scripts_touched=values.get("ios_ci_scripts_touched") == "true",
            ios_uitest_build_required=values.get("ios_uitest_build_required") == "true",
            app_version_changed=values.get("app_version_changed") == "true",
            has_release_impact=values.get("has_release_impact") == "true",
        )


def required_platforms() -> tuple[list[str], str | None]:
    """Platforms the diff requires evidence for: one `<platform>,<suite>,<reason>` line each."""
    result = run_repo_script("e2e/scripts/shared/resolve-e2e-scope.sh", "all")
    if result.code != 0:
        return [], result.stderr or "resolve-e2e-scope failed"
    platforms: list[str] = []
    for line in result.stdout.splitlines():
        platform = line.split(",")[0].strip()
        if platform and platform not in platforms:
            platforms.append(platform)
    return platforms, None


def load_status() -> tuple[dict | None, str]:
    """`e2e.py status --json`: the ledger and remote facts of each platform, with the verdict."""
    try:
        completed = subprocess.run(
            [sys.executable, str(HARNESS), "status", "--json"],
            cwd=REPO_ROOT,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=600,
            check=False,
        )
    except (OSError, subprocess.SubprocessError) as exc:
        return None, str(exc)
    if completed.returncode != 0:
        return None, completed.stderr.strip() or f"status exited {completed.returncode}"
    try:
        return json.loads(completed.stdout), ""
    except ValueError as exc:
        return None, f"status did not print JSON: {exc}"


def state_root() -> Path:
    configured = os.environ.get("E2E_STATE_ROOT") or "build/e2e"
    path = Path(configured)
    return path if path.is_absolute() else REPO_ROOT / path


def branch_commits(head: str) -> set[str] | None:
    """Commits of this branch since the merge-base with production; None when there is no base."""
    base = merge_base_for_e2e_scope(head)
    if not base:
        return None
    result = run_git("rev-list", head, f"^{base}")
    return {line.strip() for line in result.stdout.splitlines() if line.strip()} if result.code == 0 else None


def epoch(text: object) -> float:
    try:
        parsed = datetime.datetime.strptime(str(text), "%Y-%m-%dT%H:%M:%SZ")
    except ValueError:
        return 0.0
    return parsed.replace(tzinfo=datetime.timezone.utc).timestamp()


def evidence_runs(root: Path, shas: set[str] | None, platform: str | None = None) -> list[dict]:
    """Evidence invocations of this session, oldest first, from the run manifests.

    A run counts when it did more than refuse a bad precondition: exit 2 and a run that found no evidence
    required are not invocations. `shas` limits the session to this branch's commits (None: no limit).
    """
    runs = []
    for path in glob.glob(str(root / "runs" / "*" / "manifest.json")):
        data = load_json(path)
        if not isinstance(data, dict) or data.get("mode") != "evidence":
            continue
        if platform and data.get("platform") != platform:
            continue
        if shas is not None and data.get("commitSha") not in shas:
            continue
        if data.get("exitCode") == 2 or data.get("outcome") == "not_required":
            continue
        toolchain = data.get("toolchain") or {}
        runs.append(
            {
                "runId": data.get("runId", ""),
                # One `e2eEvidence` invocation starts one run per platform, each pointing at the run that started it.
                "parentRunId": data.get("parentRunId"),
                "reason": (data.get("stop") or {}).get("reason") if isinstance(data.get("stop"), dict) else None,
                "platform": data.get("platform", ""),
                "exitCode": data.get("exitCode"),
                "start": epoch(data.get("startedAt")),
                "seconds": float(data.get("durationSeconds") or 0),
                "lockMatches": toolchain.get("lockMatches"),
                "fingerprint": data.get("fingerprint"),
            }
        )
    return sorted(runs, key=lambda run: run["start"])


def session_runs(runs: list[dict], fingerprint: str | None) -> list[dict]:
    """The session of one platform: its runs under `fingerprint` made after the last complete one (oldest first)."""
    if not fingerprint:
        return []
    mine = [run for run in runs if run.get("fingerprint") == fingerprint]
    last_complete = max((run["start"] for run in mine if run["exitCode"] in COMPLETE_EXITS), default=None)
    return [run for run in mine if last_complete is None or run["start"] > last_complete]


def evidence_hours(runs: list[dict]) -> float:
    """Wall time covered by the runs; platforms that ran together are counted once."""
    total, reach = 0.0, 0.0
    for start, end in sorted((run["start"], run["start"] + run["seconds"]) for run in runs):
        if end > reach:
            total += end - max(start, reach)
            reach = end
    return total / 3600


def repeated_failures(root: Path, platform: str, shas: set[str] | None) -> list[str]:
    """Scenarios that failed under the two most recent fingerprints of the platform (and are green in neither)."""
    ledgers = []
    for path in glob.glob(str(root / "ledger" / platform / "*" / "ledger.json")):
        data = load_json(path)
        if not isinstance(data, dict):
            continue
        scenarios = data.get("scenarios") or {}
        attempts = [a for entry in scenarios.values() for a in entry.get("attempts", [])]
        if shas is not None and not any(a.get("sha") in shas for a in attempts):
            continue
        failing = {
            scenario_id
            for scenario_id, entry in scenarios.items()
            if entry.get("status") != "passed"
            and any(a.get("outcome") == "failed" and a.get("countsAgainstCap") for a in entry.get("attempts", []))
        }
        ledgers.append((str(data.get("createdAt", "")), failing))
    ledgers.sort(key=lambda item: item[0])
    return sorted(ledgers[-1][1] & ledgers[-2][1]) if len(ledgers) >= 2 else []


def drift_since_last_complete_run(root: Path, platform: str, head: str) -> tuple[int, int] | None:
    """(commits, files) between the last complete run of the platform and HEAD; None when there is none."""
    index = load_json(root / "ledger" / platform / "index.json")
    if not isinstance(index, list) or not index:
        return None
    last = max(index, key=lambda entry: str(entry.get("completedAt", "")))
    sha = str(last.get("completeAtSha", ""))
    if not sha or run_git("cat-file", "-e", f"{sha}^{{commit}}").code != 0:
        return None
    commits = run_git("rev-list", "--count", f"{sha}..{head}").stdout
    files = run_git("diff", "--name-only", sha, head).stdout.splitlines()
    return (int(commits) if commits.isdigit() else 0, len(files))


def stop_reasons(verdict: str, runs: list[dict], repeated: list[str]) -> list[str]:
    """Why the person certifying must stop and report for this platform (runbook section 5)."""
    if verdict in NO_STOP_VERDICTS:
        return []
    reasons = []
    if runs and runs[-1]["exitCode"] in (5, 7):
        reasons.append(f"the last evidence run ({runs[-1]['runId']}) ended with exit {runs[-1]['exitCode']}")
    if verdict == "exhausted" and not (runs and runs[-1]["exitCode"] == 7):
        reasons.append("a scenario used all its attempts: any new run is refused with exit 7")
    if repeated:
        reasons.append(f"failed under two consecutive fingerprints: {', '.join(repeated)}")
    if len(runs) >= MAX_EVIDENCE_INVOCATIONS:
        reasons.append(f"{len(runs)} evidence invocations in this session without a complete result")
    return reasons


def invocations(runs: list[dict]) -> int:
    """How many `e2eEvidence*` invocations the runs came from: the runs of one `--platform all` share a parent."""
    return len({run.get("parentRunId") or run["runId"] for run in runs})


def refusal_detail(runs: list[dict], limit: int = 3) -> str:
    """The reasons the manifests of the exit-3 runs give (the distinct ones, shortened), or "" when none says."""
    seen: list[str] = []
    for run in runs:
        reason = " ".join(str(run.get("reason") or "").split())
        if run["exitCode"] == 3 and reason and reason[:240] not in seen:
            seen.append(reason[:240])
    return "; ".join(seen[:limit])


def session_stop_reasons(runs: list[dict], hours: float) -> list[str]:
    reasons = []
    # Invocations, not manifests: a rejected `--platform all` leaves one exit-3 run per platform under one parent.
    refused = [run for run in runs if run["exitCode"] == 3]
    environment = invocations(refused)
    if environment >= MAX_ENVIRONMENT_EXITS:
        detail = refusal_detail(refused)
        reasons.append(f"{environment} evidence invocations ended with exit 3 (environment)" + (f": {detail}" if detail else ""))
    if hours >= MAX_EVIDENCE_HOURS:
        reasons.append(f"{hours:.1f} h of evidence in this session (limit {MAX_EVIDENCE_HOURS:.0f} h)")
    return reasons


def describe_ids(items: list, limit: int = 6) -> str:
    shown = ", ".join(items[:limit])
    return shown + (f", ... (+{len(items) - limit})" if len(items) > limit else "")


def report_platform(platform: str, info: dict, runs: list[dict], repeated: list[str], drift: tuple[int, int] | None) -> tuple[list[str], list[str]]:
    """(lines, stop reasons) for one platform."""
    if "error" in info:
        return [f"[FAIL] {platform} - status could not be computed: {info['error']}"], []
    verdict = info.get("verdict", "?")
    evidence = info.get("evidence") or {}
    green = f"green {len(info.get('green', []))}/{info.get('inScope', 0)}"
    where = f"; evidence from {evidence['sha'][:7]} ({evidence['source']})" if evidence else ""
    lines = [f"[{'PASS' if verdict in SAFE_VERDICTS else 'FAIL'}] {platform} fp={info.get('fingerprint', '')[:12]} "
             f"verdict={verdict} - {green}{where}"]
    remote = info.get("remote") or {}
    if remote.get("incomplete"):
        lines.append(f"  GitHub did not answer for {len(remote['incomplete'])} commit(s), even asked twice "
                     f"({describe_ids([sha[:12] for sha in remote['incomplete']], 4)}): the evidence on them is neither present nor absent")
    elif remote and not remote.get("reachable"):
        lines.append("  GitHub could not be read: the verdict falls back to this machine's ledger records")
    if remote.get("truncated"):
        lines.append(f"  only {remote.get('checked')} candidate commits were asked about; older evidence is not seen")
    if info.get("pending") and verdict != "exhausted":
        lines.append(f"  pending: {describe_ids(info['pending'])}")
    for failed in info.get("failed", []):
        lines.append(f"  failed: {failed['id']} class={failed.get('class')} attempts={failed.get('attempts')}"
                     f"{' EXHAUSTED' if failed.get('exhausted') else ''}: {failed.get('summary')}")
    if verdict in NEXT_ACTION:
        lines.append("  next: " + NEXT_ACTION[verdict].format(
            platform=platform, task=TASKS.get(platform, "e2eEvidence"), evidence=evidence["sha"][:7] if evidence else "an ancestor"))
    if runs:
        lock = runs[-1]["lockMatches"]
        why = refusal_detail(runs[-1:])
        lines.append(f"  session: {len(runs)} evidence invocation(s) on {platform}; last exit {runs[-1]['exitCode']}"
                     f"{f' ({why})' if why else ''}; toolchain lock {'matches' if lock else 'DIFFERS' if lock is False else 'not recorded'}")
    if drift and (drift[0] > MAX_DRIFT_COMMITS or drift[1] > MAX_DRIFT_FILES):
        lines.append(f"  WARN: HEAD is {drift[0]} commits and {drift[1]} files past the last complete run; on a long "
                     "branch certify in increments (runbook section 6)")
    reasons = stop_reasons(verdict, runs, repeated)
    return lines, reasons


def report_evidence(platforms: list[str], status: dict, head: str, shas: set[str] | None, root: Path) -> tuple[int, int]:
    """Prints the verdict of each required platform; returns (failures, stop conditions)."""
    failures, stops = 0, 0
    all_runs = evidence_runs(root, shas)
    session: list[dict] = []
    unsafe = []
    print("Evidence by platform (what counts is the remote SHA; the verdict reads GitHub, then the local ledger):")
    for platform in platforms:
        info = (status.get("platforms") or {}).get(platform) or {"error": "platform missing from status"}
        runs = session_runs([run for run in all_runs if run["platform"] == platform], info.get("fingerprint"))
        session.extend(runs)
        lines, reasons = report_platform(
            platform, info, runs, repeated_failures(root, platform, shas), drift_since_last_complete_run(root, platform, head)
        )
        print("\n".join(lines))
        if lines[0].startswith("[FAIL]"):
            failures += 1
            unsafe.append(platform)
        for reason in reasons:
            print(f"  STOP: {reason}")
            stops += 1
    if unsafe:
        reasons = session_stop_reasons(session, evidence_hours(session))
        for reason in reasons:
            print(f"STOP: {reason}")
            stops += 1
        print(f"Session so far: {invocations(session)} evidence invocation(s), {evidence_hours(session):.1f} h of evidence.")
        if stops:
            print("A stop condition holds: hand the diagnosis to the person who owns the branch; do not try again "
                  "(raising retries, re-invoking, forcing sequential or rebooting are not remedies).")
        elif len(unsafe) == 2:
            print("  next, both platforms: ./gradlew e2eEvidence")
    return failures, stops


def main() -> int:
    failures = 0

    branch = run_git("branch", "--show-current").stdout
    head = run_git("rev-parse", "HEAD").stdout
    status_short = run_git("status", "--short", "--branch").stdout
    dirty = bool(run_git("status", "--porcelain").stdout)
    upstream_name = run_git("rev-parse", "--abbrev-ref", "--symbolic-full-name", "@{u}")
    upstream_sha = run_git("rev-parse", "@{u}") if upstream_name.code == 0 else GitResult(1, "", "")

    print(f"Repo: {REPO_ROOT}")
    print(f"Branch: {branch or '<detached>'}")
    print(f"HEAD: {head or '<unknown>'}")
    print()
    print(status_short)
    print()

    checks: list[tuple[bool, str, str]] = [
        (branch.startswith("feat/"), "branch is feat/*", branch),
        (not dirty, "working tree is clean", "dirty" if dirty else "clean"),
        (upstream_name.code == 0, "upstream is configured", upstream_name.stdout or upstream_name.stderr),
    ]

    if upstream_name.code == 0:
        checks.append((head == upstream_sha.stdout, "HEAD equals upstream", upstream_sha.stdout))

    for ok, label, detail in checks:
        print_check(ok, label, detail)
        failures += 0 if ok else 1

    print()
    if not head:
        print_check(False, "cannot inspect evidence without HEAD")
        return 1

    certification_scope = detect_certification_scope(head)
    if certification_scope.error:
        print_check(False, "certification scope resolved", certification_scope.error)
        failures += 1
    else:
        print_check(True, "certification scope resolved", f"scope={certification_scope.e2e_scope}")
        print(f"  Android preflight tasks: {certification_scope.android_tasks or '<none>'}")
        print(f"  iOS preflight tasks: {certification_scope.ios_tasks or '<none>'}")
        print(f"  iOS CI scripts touched: {certification_scope.ios_ci_scripts_touched}")
        print(f"  iOS UI tests target build required: {certification_scope.ios_uitest_build_required}")

    if certification_scope.missing_version_bump:
        print_check(
            False,
            "required app build number bump is present",
            f"missing: {certification_scope.missing_version_bump}",
        )
        print("  Bump gradle/app-version.properties before running commit-bound E2E evidence.")
        return 1

    # A bumped build number is not enough: production preflight also rejects a
    # versionName whose app-<version> tag already exists at another SHA, and a
    # plain local validate-app-version.sh run skips that branch because
    # TARGET_GIT_SHA is unset outside CI. CI parity: the conflict check only
    # applies to release-impacting changes (SKIP_APP_VERSION_TAG_CONFLICT_CHECK
    # stays 1 otherwise), so a non-release branch may keep the released name.
    if certification_scope.app_version_changed or certification_scope.has_release_impact:
        version_name = app_version_name()
        if not version_name:
            print_check(
                False,
                "app versionName is unreleased",
                "unable to read versionName from gradle/app-version.properties",
            )
            return 1

        tag_name = f"app-{version_name}"
        tag_target, tag_source = release_tag_target(tag_name)
        if tag_target and tag_target != head:
            print_check(
                False,
                "app versionName is unreleased",
                f"tag {tag_name} already exists at {tag_target[:12]} ({tag_source})",
            )
            print(
                "  Bump versionName in gradle/app-version.properties and run"
                " ./gradlew syncAppVersion: production preflight rejects an"
                " already-released versionName."
            )
            return 1
        print_check(
            True,
            "app versionName is unreleased",
            f"tag {tag_name} points at HEAD ({tag_source})"
            if tag_target
            else f"tag {tag_name} not found on {tag_source}",
        )

    print()
    if certification_scope.requires_e2e is False:
        print_check(True, "no E2E evidence required for HEAD", f"scope={certification_scope.e2e_scope}")
        return 1 if failures else 0

    platforms, error = required_platforms()
    if error:
        print_check(False, "required E2E platforms resolved", error)
        return 1
    if not platforms:
        # The detector says evidence is required; a resolver that lists no platform must not turn that into a pass.
        print_check(False, "required E2E platforms resolved", f"the detector requires evidence (scope={certification_scope.e2e_scope}) but the scope resolver listed no platform")
        return 1
    status, error = load_status()
    if status is None:
        print_check(False, "e2e.py status --json", error)
        return 1

    evidence_failures, stops = report_evidence(platforms, status, head, branch_commits(head), state_root())
    failures += evidence_failures
    if stops:
        return 2
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
