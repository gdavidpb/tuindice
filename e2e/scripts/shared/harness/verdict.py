"""The evidence verdict of a platform, as `status` reports it: current, reusable, unpublished, partial, rerun, exhausted.

Evidence counts when a trusted `success` status named like the platform's context, whose description names
"fp <first 12 of the fingerprint>", exists on HEAD (current) or on a commit preflight considers for reuse
(reusable). The remote is the authority; the ledger's publication records only stand in when GitHub cannot be read.
"""

import json
import subprocess
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

from . import publish
from .config import UsageError, parse_layout

VERDICTS = ("current", "reusable", "unpublished", "partial", "rerun", "exhausted")
MAX_REMOTE_LOOKUPS = 100  # commits asked about; preflight's own fallback window is 50
FALLBACK_WINDOW = 50
LAYOUT = Path(__file__).resolve().parent.parent / "layout.env"


def _git(root, *args):
    result = subprocess.run(["git"] + list(args), cwd=str(root), stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
        universal_newlines=True)
    return result.returncode, result.stdout.split()


def _shared(root, function, *args):
    """(exit code, lines) of a function of the shared shell library, the one e2e_status_context lives in: the base
    ref and the trusted creators have one definition there, for the preflight and for this verdict."""
    source = Path(root) / parse_layout(LAYOUT)["E2E_STATUS_CONTEXT_SOURCE"]
    if not source.exists():
        raise UsageError("%s does not exist" % source)
    result = subprocess.run(["bash", "-c", 'source "$1"; shift; "$@"', "_", str(source), function] + [str(a) for a in args],
        cwd=str(root), stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, universal_newlines=True)
    return result.returncode, result.stdout.split("\n") if result.stdout else []


def reuse_candidates(root, head):
    """Commits preflight looks at for reusable evidence, newest first, HEAD excluded: `rev-list HEAD ^base` plus
    the base itself (preflight-production.sh includes it). Without a production ref: the last 50 commits."""
    code, refs = _shared(root, "e2e_base_ref", root)
    base = _git(root, "merge-base", refs[0], head) if code == 0 and refs and refs[0] else (1, [])
    if base[0] == 0 and base[1]:
        _, between = _git(root, "rev-list", head, "^" + base[1][0])
        found = between + [base[1][0]]
    else:
        _, found = _git(root, "rev-list", "--max-count=%d" % FALLBACK_WINDOW, head)
    ordered = []
    for sha in found:
        if sha != head and sha not in ordered:
            ordered.append(sha)
    return ordered


def _gh(cfg, *args):
    try:
        result = subprocess.run(publish.gh_command(cfg) + list(args), cwd=str(cfg.root), stdout=subprocess.PIPE,
            stderr=subprocess.DEVNULL, universal_newlines=True, timeout=60)
    except (OSError, subprocess.SubprocessError):
        return None
    return result.stdout if result.returncode == 0 else None


class Remote:
    """The statuses of commits on GitHub: each commit fetched once (eight at a time) and shared by the platforms."""

    def __init__(self, cfg):
        self.cfg = cfg
        self.owner = (_gh(cfg, "api", "repos/{owner}/{repo}", "--jq", ".owner.login") or "").strip()
        self.statuses = {}  # sha -> list of status objects, None when GitHub did not answer for that commit

    def trusted(self):
        """Logins whose success status counts: e2e_trusted_status_creators of the shared library, which reads
        E2E_TRUSTED_STATUS_CREATORS exactly as the preflight does (default: the owner and the Actions bot)."""
        code, lines = _shared(self.cfg.root, "e2e_trusted_status_creators", self.owner)
        logins = {line.strip() for line in lines if line.strip()}
        if code != 0 or not logins:
            raise UsageError("e2e_trusted_status_creators is not defined in the shared shell library")
        return logins

    def _load(self, shas):
        missing = [sha for sha in shas if sha not in self.statuses]
        if not self.owner or not missing:
            return
        with ThreadPoolExecutor(max_workers=8) as pool:
            replies = pool.map(lambda sha: _gh(self.cfg, "api", "repos/{owner}/{repo}/commits/%s/statuses" % sha), missing)
            for sha, raw in zip(missing, replies):
                try:
                    parsed = json.loads(raw) if raw is not None else None
                except ValueError:
                    parsed = None
                self.statuses[sha] = parsed if isinstance(parsed, list) else None

    def find(self, platform, fingerprint, shas):
        """Looks for the trusted success status on `shas` (HEAD first, then the rest together). `reachable` is
        False when GitHub could not be asked at all (no owner, or every lookup failed); one unpublished commit
        answering 404 does not make it so."""
        found = {"reachable": False, "sha": None, "checked": 0, "truncated": len(shas) > MAX_REMOTE_LOOKUPS}
        if not self.owner:
            return found
        trusted, context, marker = self.trusted(), publish.status_context(self.cfg, platform), "fp %s" % fingerprint[:12]
        for chunk in (shas[:1], shas[1:MAX_REMOTE_LOOKUPS]):
            self._load(chunk)
            for sha in chunk:
                found["checked"] += 1
                latest = next((s for s in self.statuses.get(sha) or [] if isinstance(s, dict) and s.get("context") == context), None)
                if latest and latest.get("state") == "success" and (latest.get("creator") or {}).get("login") in trusted \
                        and marker in (latest.get("description") or ""):
                    found["sha"] = sha
                    break
            if found["sha"]:
                break
        found["reachable"] = any(self.statuses.get(sha) is not None for sha in shas[:found["checked"]])
        return found


def decide(info, head, candidates, remote):
    """(verdict, evidence) from the ledger facts of one platform (`info`) and the remote lookup.

    current      the remote has the status on HEAD
    reusable     the remote has it on a candidate commit
    unpublished  no such status, and the ledger is green for every in-scope scenario
    exhausted    no such status, and an in-scope scenario used all its attempts
    partial      no such status, and at least one scenario is green
    rerun        none of the above: nothing green for this fingerprint
    With GitHub unreachable, the ledger's own publication records for this fingerprint stand in for the remote."""
    if remote["sha"]:
        return ("current" if remote["sha"] == head else "reusable"), {"sha": remote["sha"], "source": "remote"}
    if not remote["reachable"]:
        marker = "fp %s" % info["fingerprint"][:12]
        recorded = [p["sha"] for p in info["publications"] if marker in p.get("description", "")]
        if head in recorded:
            return "current", {"sha": head, "source": "ledger"}
        for sha in candidates:
            if sha in recorded:
                return "reusable", {"sha": sha, "source": "ledger"}
    if info["exhausted"]:
        return "exhausted", None
    if info["inScope"] > 0 and not info["pending"]:
        return "unpublished", None
    return ("partial" if info["green"] else "rerun"), None
