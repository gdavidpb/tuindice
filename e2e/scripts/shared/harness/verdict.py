"""The evidence verdict of a platform, as `status` reports it: current, reusable, incomplete, unpublished, partial, rerun, exhausted.

Evidence counts when a trusted `success` status named like the platform's context, whose description names
"fp <first 12 of the fingerprint>", exists on HEAD (current) or on a commit preflight considers for reuse
(reusable). The remote is the authority; the ledger's publication records only stand in when GitHub cannot be read.
A lookup that fails is asked once more; one that fails twice makes the verdict `incomplete` (the commits are listed in
`remote.incomplete`): absence of evidence is not claimed for commits GitHub did not answer about.
"""

import json
import subprocess
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

from . import publish
from .config import UsageError, shared_function
from .gitstate import run_git

MAX_REMOTE_LOOKUPS = 100  # commits asked about; preflight's own fallback window is 50
FALLBACK_WINDOW = 50


def reuse_candidates(root, head):
    """Commits preflight looks at for reusable evidence, newest first, HEAD excluded: `rev-list HEAD ^base` plus
    the base itself (preflight-production.sh includes it). Without a production ref: the last 50 commits."""
    code, refs = shared_function(root, "e2e_base_ref", root)
    base = run_git(root, "merge-base", refs[0], head) if code == 0 and refs and refs[0] else (1, "")
    if base[0] == 0 and base[1]:
        _, between = run_git(root, "rev-list", head, "^" + base[1])
        found = between.split() + [base[1]]
    else:
        _, listed = run_git(root, "rev-list", "--max-count=%d" % FALLBACK_WINDOW, head)
        found = listed.split()
    ordered = []
    for sha in found:
        if sha != head and sha not in ordered:
            ordered.append(sha)
    return ordered


def _gh(cfg, *args):
    """(stdout, unknown): stdout is None when the call failed; `unknown` when GitHub answered that the commit does not
    exist (404/422, as for a commit that was not pushed), which is an answer and not a failed lookup."""
    try:
        result = subprocess.run(publish.gh_command(cfg) + list(args), cwd=str(cfg.root), stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, universal_newlines=True, timeout=60)
    except (OSError, subprocess.SubprocessError):
        return None, False
    if result.returncode == 0:
        return result.stdout, False
    return None, "HTTP 404" in result.stderr or "HTTP 422" in result.stderr


class Remote:
    """The statuses of commits on GitHub: each commit fetched once (eight at a time) and shared by the platforms."""

    def __init__(self, cfg):
        self.cfg = cfg
        self.owner = (_gh(cfg, "api", "repos/{owner}/{repo}", "--jq", ".owner.login")[0] or "").strip()
        self.statuses = {}  # sha -> list of status objects, None when GitHub did not answer for that commit
        self.failed = set()  # the shas whose lookup failed twice (an unknown commit is not one of them)

    def trusted(self):
        """Logins whose success status counts: e2e_trusted_status_creators of the shared library, which reads
        E2E_TRUSTED_STATUS_CREATORS exactly as the preflight does (default: the owner)."""
        code, lines = shared_function(self.cfg.root, "e2e_trusted_status_creators", self.owner)
        logins = {line.strip() for line in lines if line.strip()}
        if code != 0 or not logins:
            raise UsageError("e2e_trusted_status_creators is not defined in the shared shell library")
        return logins

    def _fetch(self, shas):
        """{sha: (raw, unknown)} for each sha, eight at a time."""
        with ThreadPoolExecutor(max_workers=8) as pool:
            return dict(zip(shas, pool.map(
                lambda sha: _gh(self.cfg, "api", "repos/{owner}/{repo}/commits/%s/statuses" % sha), shas)))

    def _load(self, shas):
        missing = [sha for sha in shas if sha not in self.statuses]
        if not self.owner or not missing:
            return
        replies = self._fetch(missing)
        for sha in [sha for sha, (raw, unknown) in replies.items() if raw is None and not unknown]:
            replies[sha] = self._fetch([sha])[sha]  # a failed lookup is asked once more
        for sha, (raw, unknown) in replies.items():
            try:
                parsed = json.loads(raw) if raw is not None else None
            except ValueError:
                parsed = None
            self.statuses[sha] = parsed if isinstance(parsed, list) else None
            if self.statuses[sha] is None and not unknown:
                self.failed.add(sha)

    def find(self, platform, fingerprint, shas):
        """Looks for the trusted success status on `shas` (HEAD first, then the rest together). `reachable` is
        False when GitHub could not be asked at all (no owner, or every lookup failed); one unpublished commit
        answering 404 does not make it so. `incomplete` lists the commits looked at whose lookup failed twice."""
        found = {"reachable": False, "sha": None, "checked": 0, "truncated": len(shas) > MAX_REMOTE_LOOKUPS,
            "incomplete": []}
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
        found["incomplete"] = [sha for sha in shas[:found["checked"]] if sha in self.failed]
        return found


def locate_evidence(cfg, platform, fingerprint, head):
    """(found, trusted): the remote lookup of trusted evidence for `fingerprint` on HEAD and then on the commits preflight
    considers, and the logins that count as trusted (for the explanation when there is none). Reads only."""
    remote = Remote(cfg)
    found = remote.find(platform, fingerprint, [head] + reuse_candidates(cfg.root, head))
    return found, (remote.trusted() if remote.owner else set())


def decide(info, head, candidates, remote):
    """(verdict, evidence) from the ledger facts of one platform (`info`) and the remote lookup.

    current      the remote has the status on HEAD
    reusable     the remote has it on a candidate commit
    incomplete   no such status among the commits GitHub answered about, and it did not answer for others (even
                 after a second ask): neither present nor absent there, so nothing is concluded
    unpublished  no such status, and the ledger is green for every in-scope scenario
    exhausted    no such status, and an in-scope scenario used all its attempts
    partial      no such status, and at least one scenario is green
    rerun        none of the above: nothing green for this fingerprint
    With GitHub unreachable, the ledger's own publication records for this fingerprint stand in for the remote."""
    if remote["sha"]:
        return ("current" if remote["sha"] == head else "reusable"), {"sha": remote["sha"], "source": "remote"}
    if remote["incomplete"]:
        return "incomplete", None
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
