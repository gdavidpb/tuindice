#!/usr/bin/env python3
"""Inspect TuIndice branch and local E2E certification evidence."""

from __future__ import annotations

import json
import os
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[4]


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
    app_version_changed: bool = False
    has_release_impact: bool = False
    error: str = ""


@dataclass(frozen=True)
class SuiteReuse:
    platform: str
    suite: str
    fingerprint: str
    verdict: str  # "current" | "reusable" | "rerun"
    detail: str


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


def load_manifest(path: Path) -> tuple[dict[str, object] | None, str | None]:
    try:
        return json.loads(path.read_text(encoding="utf-8")), None
    except Exception as exc:  # noqa: BLE001 - surface any malformed manifest.
        return None, str(exc)


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
    for ref in ("origin/production", "production"):
        result = run_git("merge-base", ref, head)
        if result.code == 0 and result.stdout:
            return result.stdout
    return None


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
            app_version_changed=values.get("app_version_changed") == "true",
            has_release_impact=values.get("has_release_impact") == "true",
        )


def resolve_e2e_scope_pairs() -> tuple[list[tuple[str, str]], str | None]:
    result = run_repo_script("e2e/scripts/resolve-e2e-scope.sh", "all")
    if result.code != 0:
        return [], result.stderr or "resolve-e2e-scope failed"

    pairs: list[tuple[str, str]] = []
    for line in result.stdout.splitlines():
        parts = line.strip().split(",")
        if len(parts) >= 2 and parts[0] and parts[1]:
            pair = (parts[0], parts[1])
            if pair not in pairs:
                pairs.append(pair)
    return pairs, None


def e2e_fingerprint(platform: str, suite: str, ref: str = "HEAD") -> str:
    result = run_repo_script("e2e/scripts/e2e-fingerprint.sh", platform, suite, ref)
    if result.code != 0:
        return ""
    return result.stdout


def passing_manifest_for_fingerprint(
    fingerprint: str, platform: str, suite: str
) -> dict[str, object] | None:
    manifest_path = (
        REPO_ROOT
        / "build"
        / "e2e"
        / "certifications"
        / "by-fingerprint"
        / fingerprint
        / platform
        / suite
        / "manifest.json"
    )
    if not manifest_path.is_file():
        return None

    manifest, _ = load_manifest(manifest_path)
    if manifest is None or manifest.get("statusCode") != 0:
        return None
    return manifest


def suite_reuse_verdict(head: str, platform: str, suite: str) -> SuiteReuse:
    fingerprint = e2e_fingerprint(platform, suite)
    if not fingerprint:
        return SuiteReuse(platform, suite, "", "rerun", "unable to compute fingerprint")

    manifest = passing_manifest_for_fingerprint(fingerprint, platform, suite)
    covered_by = suite
    if manifest is None and suite != "local-certification-suite":
        aggregate_fingerprint = e2e_fingerprint(platform, "local-certification-suite")
        if aggregate_fingerprint:
            manifest = passing_manifest_for_fingerprint(
                aggregate_fingerprint, platform, "local-certification-suite"
            )
            covered_by = "local-certification-suite"

    if manifest is None:
        return SuiteReuse(
            platform, suite, fingerprint, "rerun", "no passing evidence for this fingerprint"
        )

    commit_sha = str(manifest.get("commitSha", ""))
    finished_at = str(manifest.get("finishedAt", ""))
    if commit_sha == head:
        return SuiteReuse(
            platform, suite, fingerprint, "current", f"evidence produced by HEAD ({finished_at})"
        )

    source = f"{commit_sha[:7]} ({finished_at})"
    if covered_by != suite:
        source += f", covered by {covered_by}"
    return SuiteReuse(
        platform,
        suite,
        fingerprint,
        "reusable",
        f"passing evidence from {source}; preflight republishes its status by fingerprint",
    )


def report_fingerprint_reusability(head: str) -> tuple[list[SuiteReuse], bool]:
    pairs, error = resolve_e2e_scope_pairs()
    if error:
        print_check(False, "E2E fingerprint reusability resolved", error)
        return [], True

    if not pairs:
        print_check(True, "E2E fingerprint reusability resolved", "no suites in scope")
        return [], False

    print("Evidence reusability by fingerprint (HEAD):")
    rows: list[SuiteReuse] = []
    for platform, suite in pairs:
        row = suite_reuse_verdict(head, platform, suite)
        rows.append(row)
        ok = row.verdict in ("current", "reusable")
        fingerprint_label = row.fingerprint[:12] if row.fingerprint else "<unknown>"
        print_check(ok, f"{platform}/{suite} fp={fingerprint_label}", f"{row.verdict}: {row.detail}")

    rerun_rows = [row for row in rows if row.verdict == "rerun"]
    if rerun_rows:
        targets = ", ".join(f"{row.platform}/{row.suite}" for row in rerun_rows)
        print(f"  E2E rerun required for: {targets}")
    else:
        print("  No local E2E rerun required: every required suite has passing evidence for its current fingerprint.")
    return rows, False


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

    if certification_scope.missing_version_bump:
        print_check(
            False,
            "required app build number bump is present",
            f"missing: {certification_scope.missing_version_bump}",
        )
        print("  Bump gradle/app-version.properties before running commit-bound E2E evidence.")
        return failures + 1

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
            return failures + 1

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
            return failures + 1
        print_check(
            True,
            "app versionName is unreleased",
            f"tag {tag_name} points at HEAD ({tag_source})"
            if tag_target
            else f"tag {tag_name} not found on {tag_source}",
        )

    reuse_rows: list[SuiteReuse] = []
    if certification_scope.requires_e2e:
        print()
        reuse_rows, reuse_error = report_fingerprint_reusability(head)
        if reuse_error:
            failures += 1

    print()
    evidence_root = REPO_ROOT / "build" / "e2e" / "certifications" / head
    manifests = sorted(evidence_root.glob("*/*/manifest.json"))

    if not manifests:
        if certification_scope.requires_e2e is False:
            print_check(True, "no E2E evidence required for HEAD", f"scope={certification_scope.e2e_scope}")
            return 1 if failures else 0

        if reuse_rows and all(row.verdict in ("current", "reusable") for row in reuse_rows):
            print_check(
                True,
                "evidence reusable via fingerprint; local E2E rerun not required",
                "preflight republishes fingerprint-matched statuses",
            )
            return 1 if failures else 0

        print_check(False, "evidence manifests exist for HEAD", str(evidence_root))
        return failures + 1

    print(f"Evidence root: {evidence_root}")
    for manifest_path in manifests:
        manifest, error = load_manifest(manifest_path)
        rel_path = manifest_path.relative_to(REPO_ROOT)
        if manifest is None:
            print_check(False, f"manifest parses: {rel_path}", error or "")
            failures += 1
            continue

        commit_sha = str(manifest.get("commitSha", ""))
        status_code = manifest.get("statusCode")
        platform = manifest.get("platform") or manifest_path.parents[1].name
        suite = manifest.get("suiteId") or manifest.get("suite") or manifest_path.parent.name

        ok_commit = commit_sha == head
        ok_status = status_code == 0
        print_check(ok_commit and ok_status, f"{platform}/{suite}", str(rel_path))
        if not ok_commit:
            print(f"  commitSha mismatch: {commit_sha or '<missing>'}")
            failures += 1
        if not ok_status:
            print(f"  statusCode is {status_code!r}")
            failures += 1

    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
