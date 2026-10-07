"""e2e/scripts/ios/build.sh keeps its derived data per checkout (D-19). It needs macOS (plutil, the Darwin check), so
the test is skipped elsewhere on purpose and the macOS job runs it."""

import json
import os
import shutil
import stat
import subprocess
import sys
import tempfile
import unittest

TESTS = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.realpath(os.path.join(TESTS, "..", "..", ".."))
IOS = os.path.join(ROOT, "e2e", "scripts", "ios")
SHARED = os.path.join(ROOT, "e2e", "scripts", "shared")

FAKE_XCODEBUILD = """#!/usr/bin/env bash
while [[ $# -gt 0 ]]; do
	if [[ "$1" == "-derivedDataPath" ]]; then derived="$2"; fi
	shift
done
app="${derived}/Build/Products/Debug-iphonesimulator/TuIndiceHost.app"
mkdir -p "${app}"
cat > "${app}/Info.plist" <<'PLIST'
<?xml version="1.0" encoding="UTF-8"?>
<plist version="1.0"><dict>
<key>CFBundleIdentifier</key><string>com.example.host</string>
<key>CFBundleExecutable</key><string>TuIndiceHost</string>
</dict></plist>
PLIST
"""


def executable(path, body):
    with open(path, "w") as handle:
        handle.write(body)
    os.chmod(path, os.stat(path).st_mode | stat.S_IXUSR)


@unittest.skipUnless(sys.platform == "darwin", "build.sh needs macOS (plutil); the macOS job runs this test")
class IosBuildScriptTests(unittest.TestCase):
    def setUp(self):
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-ios-build-"))
        self.addCleanup(shutil.rmtree, self.dir, True)
        self.tmp_root = os.path.join(self.dir, "tmp-root")
        self.bin = os.path.join(self.dir, "bin")
        os.makedirs(self.bin)
        executable(os.path.join(self.bin, "xcodebuild"), FAKE_XCODEBUILD)

    def checkout(self, name):
        repo = os.path.join(self.dir, name)
        os.makedirs(os.path.join(repo, "e2e", "scripts", "ios"))
        os.makedirs(os.path.join(repo, "e2e", "scripts", "shared"))
        os.makedirs(os.path.join(repo, ".github", "scripts"))
        os.makedirs(os.path.join(repo, "iosApp", "Resources"))
        os.makedirs(os.path.join(repo, "iosApp", "scripts"))
        shutil.copy(os.path.join(IOS, "build.sh"), os.path.join(repo, "e2e", "scripts", "ios"))
        for shared in ("lib.sh", "layout.env"):
            shutil.copy(os.path.join(SHARED, shared), os.path.join(repo, "e2e", "scripts", "shared"))
        for script in (".github/scripts/materialize-firebase-configs.sh", ".github/scripts/sync-app-version.sh"):
            executable(os.path.join(repo, script), "#!/usr/bin/env bash\nexit 0\n")
        with open(os.path.join(repo, "iosApp", "Resources", "GoogleService-Info.plist"), "w") as handle:
            handle.write("placeholder\n")
        return repo

    def build(self, repo):
        env = dict(os.environ, PATH=self.bin + os.pathsep + os.environ["PATH"], E2E_TMP_ROOT=self.tmp_root)
        done = subprocess.run(["bash", os.path.join(repo, "e2e", "scripts", "ios", "build.sh")], cwd=repo, env=env,
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        self.assertEqual(done.returncode, 0, done.stderr)
        return json.loads(done.stdout)

    def test_two_checkouts_sharing_a_temp_root_get_different_derived_data(self):
        first = self.build(self.checkout("worktree-a"))
        second = self.build(self.checkout("worktree-b"))
        self.assertNotEqual(first["derivedData"], second["derivedData"])
        self.assertTrue(first["derivedData"].startswith(self.tmp_root))
        self.assertTrue(second["app"].startswith(second["derivedData"]))

    def test_the_same_checkout_keeps_one_derived_data_across_builds(self):
        repo = self.checkout("worktree-a")
        self.assertEqual(self.build(repo)["derivedData"], self.build(repo)["derivedData"])


if __name__ == "__main__":
    unittest.main()
