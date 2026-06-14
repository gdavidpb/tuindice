#!/usr/bin/env python3
"""Inspect TuIndice branch and local E2E certification evidence."""

from __future__ import annotations

import json
import subprocess
import sys
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
