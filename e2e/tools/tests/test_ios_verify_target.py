"""verify-ui-test-target.sh scans the Debug and the Release app, and a binary that `nm` cannot read is a failure (B-17).
It needs macOS (`file`, `nm`, `strings` on a Mach-O), so the tests that scan a binary are skipped elsewhere on purpose."""

import os
import shutil
import stat
import subprocess
import sys
import tempfile
import unittest

import support  # puts e2e/scripts/shared on sys.path

SCRIPT = os.path.join(support.TESTS, "..", "..", "..", "iosApp", "scripts", "verify-ui-test-target.sh")


@unittest.skipUnless(sys.platform == "darwin", "scans Mach-O binaries")
class UiTestTargetScanTests(unittest.TestCase):
    def setUp(self):
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-verify-target-"))
        self.addCleanup(shutil.rmtree, self.dir, True)
        self.bin = os.path.join(self.dir, "bin")
        os.makedirs(self.bin)

    def app(self, name):
        path = os.path.join(self.dir, name, "TuIndiceHost.app")
        os.makedirs(path)
        binary = os.path.join(path, "TuIndiceHost")
        shutil.copy("/usr/bin/true", binary)  # a Mach-O with no ScenarioKit in it
        os.chmod(binary, os.stat(binary).st_mode | stat.S_IXUSR)
        return path

    def fake(self, tool, body):
        path = os.path.join(self.bin, tool)
        with open(path, "w") as handle:
            handle.write("#!/usr/bin/env bash\n" + body)
        os.chmod(path, 0o755)

    def check(self, *args):
        env = dict(os.environ, PATH=self.bin + os.pathsep + os.path.join(support.TESTS, "fake_bin") + os.pathsep + os.environ["PATH"],
            FAKE_XCRUN_DIR=os.path.join(self.dir, "xcrun"))
        done = subprocess.run(["bash", SCRIPT] + list(args), env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            universal_newlines=True, timeout=120)
        # The fake xcodebuild lists no target, so the exit status is always 1 here: the tests read the report.
        return done.returncode, done.stdout

    def test_both_apps_are_scanned_and_a_clean_pair_passes(self):
        debug, release = self.app("Debug-iphonesimulator"), self.app("Release-iphonesimulator")
        code, out = self.check("--app", debug, "--app", release, "--require-app")
        self.assertIn(debug, out)
        self.assertIn(release, out)
        self.assertEqual(out.count("contain no ScenarioKit"), 2, out)

    def test_scenariokit_in_the_release_app_alone_fails(self):
        debug, release = self.app("Debug-iphonesimulator"), self.app("Release-iphonesimulator")
        self.fake("nm", 'if [[ "$*" == *Release-iphonesimulator* ]]; then echo "0000000100000000 T _kfun:scenariokit.Interpreter"; fi\n')
        code, out = self.check("--app", debug, "--app", release)
        self.assertIn("FAIL: the app binaries in %s mention ScenarioKit (1 matches)" % release, out)
        self.assertIn("OK: the app binaries in %s contain no ScenarioKit" % debug, out)

    def test_an_nm_that_fails_is_a_failure_and_not_a_count_of_zero(self):
        self.fake("nm", "echo 'nm: file format not recognized' >&2\nexit 1\n")
        code, out = self.check("--app", self.app("Release-iphonesimulator"))
        self.assertIn("FAIL: nm could not read", out)
        self.assertNotIn("contain no ScenarioKit", out)

    def test_a_strings_that_fails_is_a_failure_too(self):
        self.fake("strings", "exit 1\n")
        code, out = self.check("--app", self.app("Release-iphonesimulator"))
        self.assertIn("FAIL: strings could not read", out)


if __name__ == "__main__":
    unittest.main()
