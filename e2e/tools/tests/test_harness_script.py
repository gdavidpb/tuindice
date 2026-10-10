"""run-harness-tests.sh and verifyE2eHarness: the suite runs on every interpreter that runs the harness, with room to finish (dC-16, ZC-2)."""

import os
import re
import unittest

import support

ROOT = os.path.join(support.TESTS, "..", "..", "..")


class HarnessScriptTests(unittest.TestCase):
    def test_the_unit_tests_run_on_every_interpreter_of_the_list_the_path_one_first(self):
        script = support.text(os.path.join(support.TESTS, "run-harness-tests.sh"))
        self.assertIn("interpreters=(python3)", script)
        self.assertIn("interpreters+=(/usr/bin/python3)", script)
        loop = script.index('for suite_python in "${interpreters[@]}"; do')
        self.assertIn('"${suite_python}" -m unittest discover', script[loop:])
        self.assertNotIn("\npython3 -m unittest", script, "the suite must run on the list, not on a fixed interpreter")
        self.assertNotIn("suite_python=/usr/bin/python3", script)

    def test_verify_e2e_harness_has_its_deadline(self):
        build = support.text(os.path.join(ROOT, "gradle", "e2e-tasks.gradle.kts"))
        block = re.search(r'tasks\.register<Exec>\("verifyE2eHarness"\) \{.*?\n\}', build, re.S).group(0)
        self.assertIn("Duration.ofMinutes(45)", block)

    def test_the_macos_harness_job_has_room_for_the_suite_in_two_interpreters(self):
        # The suite takes about 19 minutes with two interpreters on a 10-core machine; a 3-core runner needs more (YC-2).
        workflow = support.text(os.path.join(ROOT, ".github", "workflows", "preflight-production-pr.yml"))
        job = re.search(r"\n  e2e-harness-preflight:\n(.*?)\n  [a-z0-9-]+:\n", workflow + "\n  end:\n", re.S).group(1)
        self.assertIn("    timeout-minutes: 60\n", job)


if __name__ == "__main__":
    unittest.main()
