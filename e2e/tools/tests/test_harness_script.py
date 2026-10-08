"""run-harness-tests.sh and verifyE2eHarness: the suite runs on the interpreter the harness targets, with room to finish (dC-16)."""

import os
import re
import unittest

import support

ROOT = os.path.join(support.TESTS, "..", "..", "..")


class HarnessScriptTests(unittest.TestCase):
    def test_the_unit_tests_run_on_the_system_python_when_there_is_one(self):
        script = support.text(os.path.join(support.TESTS, "run-harness-tests.sh"))
        self.assertIn("-x /usr/bin/python3", script)
        self.assertIn('suite_python=/usr/bin/python3', script)
        self.assertIn('"${suite_python}" -m unittest discover', script)
        self.assertNotIn("\npython3 -m unittest", script, "the suite must not run on whatever python3 the PATH finds")

    def test_verify_e2e_harness_has_twenty_five_minutes(self):
        build = support.text(os.path.join(ROOT, "gradle", "e2e-tasks.gradle.kts"))
        block = re.search(r'tasks\.register<Exec>\("verifyE2eHarness"\) \{.*?\n\}', build, re.S).group(0)
        self.assertIn("Duration.ofMinutes(30)", block)


if __name__ == "__main__":
    unittest.main()
