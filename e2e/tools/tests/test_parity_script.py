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


if __name__ == "__main__":
    unittest.main()
