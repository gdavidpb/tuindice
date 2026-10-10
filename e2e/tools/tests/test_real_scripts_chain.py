"""runner -> e2e-fingerprint.sh and resolve-e2e-scope.sh, the real ones (C-24, D-18).

Every other test of the harness replaces the fingerprint and the scope with E2E_FINGERPRINT_CMD and E2E_SCOPE_CMD. Here
neither seam is set: a throwaway repository gets the minimal `required:` paths of the platform (generated from
`e2e-fingerprint.sh --print-pathspecs`), the real scripts and the real detector, and the runner calls them as it does
on the developer's machine."""

import os
import shutil
import subprocess
import unittest

from support import SHARED, Workspace, scenario

ROOT = os.path.realpath(os.path.join(SHARED, "..", "..", ".."))
FINGERPRINT = os.path.join(ROOT, "e2e", "scripts", "shared", "e2e-fingerprint.sh")
SUITE = "local-certification-suite"
COPIED = ("e2e/scripts/shared/e2e-fingerprint.sh", "e2e/scripts/shared/resolve-e2e-scope.sh", "e2e/scripts/shared/layout.env",
    ".github/scripts/detect-changed-app.sh", "scripts/module-graph.txt")


class RealScriptsChainTests(unittest.TestCase):
    def setUp(self):
        self.ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")])
        for key in ("E2E_FINGERPRINT_CMD", "E2E_SCOPE_CMD", "E2E_FAKE_FP"):
            self.ws.env.pop(key)
        for relative in COPIED:
            self.copy(relative)
        pathspecs = subprocess.run(["bash", FINGERPRINT, "--print-pathspecs", "ios", "HEAD"], cwd=ROOT, stdout=subprocess.PIPE,
            universal_newlines=True, check=True).stdout.splitlines()
        self.required = [line.split(":", 1)[1] for line in pathspecs if line.startswith("required:")]
        self.assertGreater(len(self.required), 10)
        for path in self.required:
            target = os.path.join(self.ws.repo, path)
            if os.path.isdir(os.path.join(ROOT, path)):
                target = os.path.join(target, "probe.txt")
            if not os.path.exists(target):
                self.write(os.path.relpath(target, self.ws.repo), "base %s\n" % path)
        self.commit("minimal required paths of the platform")
        self.ws.git("branch", "production")

    def copy(self, relative):
        target = os.path.join(self.ws.repo, relative)
        os.makedirs(os.path.dirname(target), exist_ok=True)
        shutil.copy(os.path.join(ROOT, relative), target)

    def write(self, relative, text):
        target = os.path.join(self.ws.repo, relative)
        os.makedirs(os.path.dirname(target), exist_ok=True)
        with open(target, "w") as handle:
            handle.write(text)

    def commit(self, message):
        self.ws.git("add", "-A")
        self.ws.git("commit", "-q", "-m", message)

    def script_fingerprint(self, platform="ios"):
        script = os.path.join(self.ws.repo, "e2e", "scripts", "shared", "e2e-fingerprint.sh")  # the copy, which hashes this repo
        done = subprocess.run(["bash", script, platform, SUITE, "HEAD"], cwd=self.ws.repo, stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, universal_newlines=True)
        self.assertEqual(done.returncode, 0, done.stderr)
        return done.stdout.strip()

    def test_the_ledger_is_keyed_by_the_fingerprint_the_script_prints_and_follows_a_change_of_a_fingerprint_file(self):
        self.write("iosApp/Sources/Changed.swift", "let a = 1\n")
        self.commit("a change the platform's evidence covers")

        diagnose = self.ws.diagnose("ios")
        self.assertEqual(diagnose.code, 0, diagnose.err)
        self.assertEqual(self.ws.manifest()["fingerprint"], "diagnose", "a diagnostic run has no fingerprint of its own")
        self.assertEqual(sorted(self.ws.executed("ios")), ["fix-a", "fix-b"])

        first = self.script_fingerprint()
        self.assertRegex(first, r"^[0-9a-f]{64}$")
        evidence = self.ws.evidence("ios")
        self.assertEqual(evidence.code, 0, evidence.err + evidence.out)
        manifest = self.ws.manifest()
        self.assertEqual(manifest["fingerprint"], first, "the manifest carries the fingerprint of the real script")
        self.assertEqual(self.ws.ledger("ios", first)["fingerprint"], first)
        self.assertEqual(sorted(self.ws.executed("ios")), ["fix-a", "fix-a", "fix-b", "fix-b"], "diagnose, then evidence")

        self.write("iosApp/Sources/Changed.swift", "let a = 2\n")
        self.commit("a second change under a path the fingerprint reads")
        second = self.script_fingerprint()
        self.assertNotEqual(second, first)
        again = self.ws.evidence("ios")
        self.assertEqual(again.code, 0, again.err + again.out)
        self.assertEqual(self.ws.manifest()["fingerprint"], second)
        self.assertTrue(os.path.exists(os.path.join(self.ws.state, "ledger", "ios", second, "ledger.json")))
        self.assertTrue(os.path.exists(os.path.join(self.ws.state, "ledger", "ios", first, "ledger.json")),
            "the ledger of the previous fingerprint stays where it was")
        self.assertEqual(sorted(self.ws.executed("ios")), ["fix-a"] * 3 + ["fix-b"] * 3,
            "a new fingerprint is a new ledger: nothing carries over as green")

    def test_a_file_the_fingerprint_does_not_read_moves_nothing(self):
        self.write("iosApp/Sources/Changed.swift", "let a = 1\n")
        self.commit("covered")
        before = self.script_fingerprint()
        self.assertEqual(self.ws.evidence("ios").code, 0)
        self.write("docs/notes.md", "not part of the fingerprint\n")
        self.commit("docs")
        self.assertEqual(self.script_fingerprint(), before)
        self.assertEqual(self.ws.evidence("ios").code, 0)
        self.assertEqual(self.ws.manifest()["fingerprint"], before)
        self.assertEqual(self.ws.executed("ios"), ["fix-a", "fix-b"], "the same fingerprint reuses the green ledger")

    def test_the_real_scope_asks_for_nothing_when_the_platform_has_no_change(self):
        # An iOS-only change: Android's evidence is not required, and the run says so instead of running scenarios.
        self.write("iosApp/Sources/Changed.swift", "let a = 1\n")
        self.commit("ios only")
        done = self.ws.evidence("android")
        self.assertEqual(done.code, 0, done.err + done.out)
        self.assertEqual(self.ws.executed("android"), [])


if __name__ == "__main__":
    unittest.main()
