"""Git state of the checkout: what evidence may be produced and published from."""

import subprocess

from .config import UsageError


def _git(root, *args):
    result = subprocess.run(
        ["git"] + list(args), cwd=str(root),
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True,
    )
    return result.returncode, result.stdout.strip()


class GitState:
    def __init__(self, root):
        self.root = root
        _, self.sha = _git(root, "rev-parse", "HEAD")
        _, self.branch = _git(root, "rev-parse", "--abbrev-ref", "HEAD")
        code, upstream = _git(root, "rev-parse", "--verify", "--quiet", "@{u}")
        self.upstream_sha = upstream if code == 0 else None
        _, porcelain = _git(root, "status", "--porcelain", "--untracked-files=all")
        self.dirty_files = [line for line in porcelain.splitlines() if line]
        self.tree_clean = not self.dirty_files
        self.head_equals_upstream = self.upstream_sha == self.sha
        _, self.user = _git(root, "config", "user.name")

    @property
    def sha7(self):
        return self.sha[:7] if self.sha else "nogit"

    def require_clean(self):
        if not self.tree_clean:
            shown = "; ".join(self.dirty_files[:5])
            raise UsageError("evidence needs a clean working tree including untracked files (%d dirty: %s)"
                % (len(self.dirty_files), shown))

    def require_publishable(self, gh_command):
        if not self.head_equals_upstream:
            raise UsageError("publishing needs HEAD == @{u}; push the branch first (HEAD %s, upstream %s)"
                % (self.sha7, (self.upstream_sha or "none")[:7]))
        auth = subprocess.run(
            gh_command + ["auth", "status"], cwd=str(self.root),
            stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
        )
        if auth.returncode != 0:
            raise UsageError("gh is not authenticated; run gh auth login or set E2E_PUBLISH_GITHUB_STATUS=0")
        visible = subprocess.run(
            gh_command + ["api", "repos/{owner}/{repo}/commits/%s" % self.sha, "--jq", ".sha"], cwd=str(self.root),
            stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, universal_newlines=True,
        )
        if visible.returncode != 0 or visible.stdout.strip() != self.sha:
            raise UsageError("commit %s is not visible on GitHub; push it first" % self.sha7)
