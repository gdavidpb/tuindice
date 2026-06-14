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


def detect_e2e_requirement(head: str) -> tuple[bool | None, str]:
    before_sha = merge_base_for_e2e_scope(head)
    if not before_sha:
        return None, "Unable to resolve merge-base with production."

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
            return None, f"detect-changed-app failed: {detail}"

        values = parse_github_output(output_path)
        requires_e2e = values.get("requires_e2e_certification") == "true"
        scope = values.get("e2e_scope_csv") or "<none>"
        return requires_e2e, f"scope={scope}"


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

    evidence_root = REPO_ROOT / "build" / "e2e" / "certifications" / head
    manifests = sorted(evidence_root.glob("*/*/manifest.json"))

    if not manifests:
        requires_e2e, detail = detect_e2e_requirement(head)
        if requires_e2e is False:
            print_check(True, "no E2E evidence required for HEAD", detail)
            return 1 if failures else 0

        print_check(False, "evidence manifests exist for HEAD", str(evidence_root))
        if detail:
            print(f"  {detail}")
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
