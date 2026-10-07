"""Git state of the checkout: what evidence may be produced and published from."""

import subprocess

from .config import UsageError


def run_git(root, *args):
    """(exit code, stdout stripped) of one git command."""
    result = subprocess.run(
        ["git"] + list(args), cwd=str(root),
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True,
    )
    return result.returncode, result.stdout.strip()


def run_gh(gh_command, root, *args, capture=False, timeout=60):
    """Runs gh; a missing binary or a hang is a usage error (exit 2), never a traceback."""
    try:
        return subprocess.run(gh_command + list(args), cwd=str(root), stdout=subprocess.PIPE if capture else subprocess.DEVNULL,
            stderr=subprocess.DEVNULL, universal_newlines=True, timeout=timeout)
    except OSError as error:
        raise UsageError("cannot run %s: %s (install the GitHub CLI or set E2E_PUBLISH_GITHUB_STATUS=0)" % (" ".join(gh_command), error))
    except subprocess.TimeoutExpired:
        raise UsageError("%s did not answer within %ds" % (" ".join(gh_command + list(args[:2])), timeout))


class GitState:
    def __init__(self, root):
        self.root = root
        _, self.sha = run_git(root, "rev-parse", "HEAD")
        _, self.branch = run_git(root, "rev-parse", "--abbrev-ref", "HEAD")
        code, upstream = run_git(root, "rev-parse", "--verify", "--quiet", "@{u}")
        self.upstream_sha = upstream if code == 0 else None
        _, porcelain = run_git(root, "status", "--porcelain", "--untracked-files=all")
        self.dirty_files = [line for line in porcelain.splitlines() if line]
        self.tree_clean = not self.dirty_files
        self.head_equals_upstream = self.upstream_sha == self.sha
        _, self.user = run_git(root, "config", "user.name")

    @property
    def sha7(self):
        return self.sha[:7] if self.sha else "nogit"

    def require_clean(self):
        if not self.tree_clean:
            shown = "; ".join(self.dirty_files[:5])
            raise UsageError("evidence needs a clean working tree including untracked files (%d dirty: %s)"
                % (len(self.dirty_files), shown))

    def require_unchanged(self, moment):
        """Evidence is produced from the checkout as it was when the run began: the commit and the clean tree are read
        again at `moment`, and a difference is a usage error (exit 2) so that nothing from the new tree is recorded."""
        _, sha = run_git(self.root, "rev-parse", "HEAD")
        _, porcelain = run_git(self.root, "status", "--porcelain", "--untracked-files=all")
        dirty = [line for line in porcelain.splitlines() if line]
        if sha == self.sha and not dirty:
            return
        what = "HEAD moved from %s to %s" % (self.sha7, sha[:7]) if sha != self.sha \
            else "the working tree has %d changed file(s): %s" % (len(dirty), "; ".join(dirty[:5]))
        raise UsageError("the checkout changed during the run (%s, found %s); evidence is produced from one commit and a clean "
            "tree, so nothing is recorded or published from this point. Rerun on a quiet checkout" % (what, moment))

    def require_publishable(self, gh_command):
        if not self.head_equals_upstream:
            raise UsageError("publishing needs HEAD == @{u}; push the branch first (HEAD %s, upstream %s)"
                % (self.sha7, (self.upstream_sha or "none")[:7]))
        auth = run_gh(gh_command, self.root, "auth", "status")
        if auth.returncode != 0:
            raise UsageError("gh is not authenticated; run gh auth login or set E2E_PUBLISH_GITHUB_STATUS=0")
        visible = run_gh(gh_command, self.root, "api", "repos/{owner}/{repo}/commits/%s" % self.sha, "--jq", ".sha", capture=True)
        if visible.returncode != 0 or visible.stdout.strip() != self.sha:
            raise UsageError("commit %s is not visible on GitHub; push it first" % self.sha7)
