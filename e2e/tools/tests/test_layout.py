"""layout.env points the harness at scripts that exist and at the fingerprint version those scripts implement."""

import os
import re
import subprocess
import unittest

from support import SHARED

from harness.config import parse_layout

ROOT = os.path.realpath(os.path.join(SHARED, "..", "..", ".."))


class LayoutTests(unittest.TestCase):
    def setUp(self):
        self.layout = parse_layout(os.path.join(SHARED, "layout.env"))

    def test_the_scripts_the_harness_calls_exist_under_shared(self):
        for key in ("E2E_FINGERPRINT_SCRIPT", "E2E_SCOPE_SCRIPT", "E2E_STATUS_CONTEXT_SOURCE", "E2E_MOCK_START_SCRIPT"):
            self.assertTrue(os.path.isfile(os.path.join(ROOT, self.layout[key])), "%s -> %s" % (key, self.layout[key]))
        self.assertTrue(self.layout["E2E_FINGERPRINT_SCRIPT"].startswith("e2e/scripts/shared/"))
        self.assertTrue(self.layout["E2E_SCOPE_SCRIPT"].startswith("e2e/scripts/shared/"))

    def test_the_declared_fingerprint_version_is_the_one_the_script_hashes(self):
        script = open(os.path.join(ROOT, self.layout["E2E_FINGERPRINT_SCRIPT"])).read()
        header = re.search(r"tuindice-e2e-fingerprint-(v\d+)", script)
        self.assertIsNotNone(header)
        self.assertEqual(header.group(1), self.layout["E2E_FINGERPRINT_VERSION"])

    def test_the_fingerprint_script_prints_the_hash_the_harness_expects(self):
        script = os.path.join(ROOT, self.layout["E2E_FINGERPRINT_SCRIPT"])
        result = subprocess.run(["bash", script, "ios", "local-certification-suite", "HEAD"], cwd=ROOT,
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertRegex(result.stdout.strip(), r"^[0-9a-f]{64}$")


if __name__ == "__main__":
    unittest.main()
