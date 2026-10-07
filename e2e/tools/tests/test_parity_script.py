"""run_preflight_parity_checks.sh builds the iOS UI test target exactly when the detector says CI will."""

import os
import shutil
import stat
import subprocess
import tempfile
import unittest

TESTS = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.realpath(os.path.join(TESTS, "..", "..", ".."))
PARITY = os.path.join(ROOT, ".codex", "skills", "certify-tuindice-pr", "scripts", "run_preflight_parity_checks.sh")
COMMON = os.path.join(ROOT, ".github", "scripts", "common.sh")

FAKE_DETECT = """#!/usr/bin/env bash
printf 'has_relevant_changes=true\\nios_uitest_build_required=%s\\nandroid_tasks=\\nios_tasks=\\n' "$FAKE_UITEST" >> "$GITHUB_OUTPUT"
"""
FAKE_BUILD = """#!/usr/bin/env bash
printf '%s\\n' "$*" >> "$FAKE_BUILD_LOG"
"""


class ParityScriptTests(unittest.TestCase):
    def setUp(self):
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-parity-"))
        self.addCleanup(shutil.rmtree, self.dir, True)
        self.repo = os.path.join(self.dir, "repo")
        self.log = os.path.join(self.dir, "build.log")
        for path in (os.path.join(self.repo, ".github", "scripts"), os.path.join(self.repo, "e2e", "scripts", "ios")):
            os.makedirs(path)
        shutil.copy(COMMON, os.path.join(self.repo, ".github", "scripts", "common.sh"))
        self.script(".github/scripts/detect-changed-app.sh", FAKE_DETECT)
        self.script(".github/scripts/verify-workflow-refs.sh", "#!/usr/bin/env bash\nexit 0\n")
        self.script("e2e/scripts/ios/build.sh", FAKE_BUILD)
        self.git("init", "-q", "-b", "main")
        self.git("config", "user.name", "Parity Test")
        self.git("config", "user.email", "test@example.invalid")
        self.git("add", "-A")
        self.git("commit", "-q", "-m", "base")
        self.git("branch", "production")
        self.git("commit", "-q", "--allow-empty", "-m", "change")

    def git(self, *args):
        subprocess.run(["git"] + list(args), cwd=self.repo, check=True, stdout=subprocess.DEVNULL)

    def script(self, relative, body):
        path = os.path.join(self.repo, relative)
        with open(path, "w") as handle:
            handle.write(body)
        os.chmod(path, os.stat(path).st_mode | stat.S_IXUSR)

    def parity(self, uitest, *argv):
        env = dict(os.environ, FAKE_UITEST=uitest, FAKE_BUILD_LOG=self.log)
        env.pop("TARGET_GIT_SHA", None)
        env.pop("BASE_SHA", None)
        return subprocess.run(["bash", PARITY] + list(argv), cwd=self.repo, env=env, stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, universal_newlines=True)

    def builds(self):
        if not os.path.exists(self.log):
            return []
        with open(self.log) as handle:
            return handle.read().splitlines()

    def test_the_dry_run_shows_the_ui_test_build_when_the_detector_requires_it(self):
        done = self.parity("true", "--dry-run")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertIn("bash ./e2e/scripts/ios/build.sh --for-testing-only", done.stdout)
        self.assertEqual(self.builds(), [], "a dry run must not build")

    def test_the_dry_run_omits_it_when_the_detector_does_not(self):
        done = self.parity("false", "--dry-run")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertNotIn("build.sh", done.stdout)

    def test_the_real_run_builds_the_target_once_with_the_ci_argument(self):
        done = self.parity("true")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(self.builds(), ["--for-testing-only"])

    def test_the_real_run_does_not_build_it_when_it_is_not_required(self):
        done = self.parity("false")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(self.builds(), [])


SHARED = os.path.join(ROOT, "e2e", "scripts", "shared")
FAKE_SCOPE_DETECT = """#!/usr/bin/env bash
printf 'android,local-certification-suite,%s\\n' "$1" > "$E2E_SCOPE_FILE"
"""


class BaseRefTests(unittest.TestCase):
    """D-7: the base every local tool diffs against has one definition, e2e_base_ref in common.sh (origin first)."""

    def setUp(self):
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-base-ref-"))
        self.addCleanup(shutil.rmtree, self.dir, True)
        self.repo = self.dir
        for path in (".github/scripts", "e2e/scripts/shared"):
            os.makedirs(os.path.join(self.repo, path))
        shutil.copy(COMMON, os.path.join(self.repo, ".github", "scripts", "common.sh"))
        shutil.copy(os.path.join(SHARED, "resolve-e2e-scope.sh"), os.path.join(self.repo, "e2e", "scripts", "shared"))
        with open(os.path.join(self.repo, ".github", "scripts", "detect-changed-app.sh"), "w") as handle:
            handle.write(FAKE_SCOPE_DETECT)
        self.git("init", "-q", "-b", "main")
        self.git("config", "user.name", "Base Ref Test")
        self.git("config", "user.email", "test@example.invalid")
        self.git("add", "-A")
        self.git("commit", "-q", "-m", "first")
        self.first = self.rev("HEAD")
        self.git("commit", "-q", "--allow-empty", "-m", "second")
        self.second = self.rev("HEAD")
        self.git("commit", "-q", "--allow-empty", "-m", "third")

    def git(self, *args):
        subprocess.run(["git"] + list(args), cwd=self.repo, check=True, stdout=subprocess.DEVNULL)

    def rev(self, ref):
        return subprocess.run(["git", "rev-parse", ref], cwd=self.repo, check=True, stdout=subprocess.PIPE,
            universal_newlines=True).stdout.strip()

    def base_ref(self):
        done = subprocess.run(["bash", "-c", 'source "$1"; e2e_base_ref "$2"', "_", COMMON, self.repo],
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        return done.returncode, done.stdout.strip()

    def scope_base(self):
        done = subprocess.run(["bash", os.path.join(self.repo, "e2e", "scripts", "shared", "resolve-e2e-scope.sh"), "all"],
            cwd=self.repo, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        return done.returncode, done.stdout.strip().split(",")[-1]

    def test_neither_ref_exists(self):
        self.assertEqual(self.base_ref()[0], 1)
        self.assertEqual(self.scope_base()[0], 1)

    def test_the_local_branch_alone_is_used(self):
        self.git("branch", "production", self.first)
        self.assertEqual(self.base_ref(), (0, "production"))
        self.assertEqual(self.scope_base(), (0, self.first))

    def test_origin_wins_when_both_exist(self):
        self.git("branch", "production", self.first)
        self.git("update-ref", "refs/remotes/origin/production", self.second)
        self.assertEqual(self.base_ref(), (0, "origin/production"))
        self.assertEqual(self.scope_base(), (0, self.second))

    def test_the_trusted_creators_default_and_override(self):
        def creators(**env):
            done = subprocess.run(["bash", "-c", 'source "$1"; e2e_trusted_status_creators "$2"', "_", COMMON, "owner"],
                env=dict(os.environ, **env), stdout=subprocess.PIPE, universal_newlines=True)
            return done.stdout.split()

        self.assertEqual(creators(E2E_TRUSTED_STATUS_CREATORS=""), ["owner", "github-actions[bot]"])
        self.assertEqual(creators(E2E_TRUSTED_STATUS_CREATORS="a,b"), ["a", "b"])


if __name__ == "__main__":
    unittest.main()
