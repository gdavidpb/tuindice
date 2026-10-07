"""Values the repository states once: the suite id, the line budgets and the E2E tasks registration."""

import os
import re
import subprocess
import unittest

from support import SHARED, Workspace, scenario

from harness import catalog
from harness.config import SUITE_ID, Config, parse_layout

ROOT = os.path.realpath(os.path.join(SHARED, "..", "..", ".."))
COMMON = os.path.join(ROOT, ".github", "scripts", "common.sh")
SKILL = os.path.join(ROOT, ".codex", "skills", "certify-tuindice-pr")


def read(*parts):
    with open(os.path.join(*parts)) as handle:
        return handle.read()


def head(ws):
    return subprocess.run(["git", "rev-parse", "HEAD"], cwd=ws.repo, stdout=subprocess.PIPE, universal_newlines=True,
        check=True).stdout.strip()


class SingleDefinitionTests(unittest.TestCase):
    def test_the_harness_suite_id_is_the_one_of_the_status_context(self):
        # D-13: common.sh defines the suite and the context; the harness' constant must not diverge from it, or the
        # local fingerprint and the CI one would differ for good.
        done = subprocess.run(["bash", "-c", 'source "$1"; e2e_status_context ios', "_", COMMON],
            stdout=subprocess.PIPE, universal_newlines=True, check=True)
        self.assertEqual(done.stdout.strip(), "local-e2e/ios/%s" % SUITE_ID)

    def test_the_harness_does_not_write_the_suite_id_itself(self):
        # The suite id is read from common.sh when the harness loads: no literal of it anywhere under harness/.
        harness = os.path.join(SHARED, "harness")
        for name in sorted(os.listdir(harness)):
            if name.endswith(".py"):
                self.assertNotIn("local-certification-suite", read(harness, name), name)

    def test_the_default_base_ref_is_the_one_of_the_shared_library(self):
        # The catalog's --changed-since default asks e2e_base_ref, as the verdict does: origin/production first.
        ws = Workspace(self, [scenario("fix-a")])
        cfg = Config(ws.repo, {})
        self.assertIsNone(catalog.default_base_ref(cfg))
        first = head(ws)
        ws.git("branch", "production")
        subprocess.run(["git", "commit", "-q", "--allow-empty", "-m", "second"], cwd=ws.repo, check=True)
        second = head(ws)
        self.assertEqual(catalog.default_base_ref(cfg), first)
        ws.git("update-ref", "refs/remotes/origin/production", second)
        self.assertEqual(catalog.default_base_ref(cfg), second)

    def test_the_harness_runs_git_through_one_helper(self):
        # C-23: gitstate.run_git is the only place that spawns git (config.py needs it to find the root, before it exists).
        harness = os.path.join(SHARED, "harness")
        for name in sorted(os.listdir(harness)):
            if name.endswith(".py") and name not in ("gitstate.py", "config.py"):
                self.assertNotIn('["git"', read(harness, name), name)
        self.assertNotIn("def _git", read(harness, "verdict.py"))

    def test_the_shell_helpers_of_the_adapters_live_only_in_lib_sh(self):
        # C-23: fail, load_lock, read_build, log and emit_json are defined once, in e2e/scripts/shared/lib.sh.
        lib = read(SHARED, "lib.sh")
        for platform in ("android", "ios"):
            directory = os.path.join(SHARED, "..", platform)
            for name in sorted(os.listdir(directory)):
                text = read(directory, name)
                self.assertIn("shared/lib.sh", text, name)
                for helper in ("fail", "load_lock", "read_build", "log", "emit_json"):
                    self.assertNotRegex(text, r"(?m)^(function )?%s\(\)" % helper, "%s/%s defines %s again" % (platform, name, helper))
        for helper in ("fail", "load_lock", "read_build", "log", "emit_json"):
            self.assertRegex(lib, r"(?m)^%s\(\)" % helper)

    def test_the_detector_does_not_define_the_suite_again(self):
        self.assertNotIn("local-certification-suite", read(ROOT, ".github", "scripts", "detect-changed-app.sh"))

    def test_the_budget_numbers_live_only_in_line_budgets_env(self):
        # D-13: the skill and its runbook point at the file instead of repeating the numbers.
        limits = parse_layout(os.path.join(ROOT, "e2e", "tools", "verify", "line-budgets.env"))
        self.assertTrue(limits)
        for name in ("SKILL.md", os.path.join("references", "certification-runbook.md")):
            # Only the sentences about budgets: other numbers (a 300 s timeout) may coincide.
            text = "\n".join(line for line in read(SKILL, name).splitlines() if "budget" in line.lower())
            self.assertTrue(text, name)
            for key, value in limits.items():
                formatted = {value, "{:,}".format(int(value))}
                for number in formatted:
                    self.assertFalse(re.search(r"(?<![\d.])%s(?![\d])" % re.escape(number), text),
                        "%s repeats %s=%s" % (name, key, value))

    def test_the_e2e_tasks_are_registered_by_the_script_outside_the_fingerprint(self):
        # D-4: tasks.register of the e2e*/verifyE2e* tasks lives in gradle/e2e-tasks.gradle.kts.
        root_build = read(ROOT, "build.gradle.kts")
        script = read(ROOT, "gradle", "e2e-tasks.gradle.kts")
        self.assertIn('apply(from = "gradle/e2e-tasks.gradle.kts")', root_build)
        for name in ("e2eEvidenceAndroid", "e2eEvidenceIos", "e2eEvidence", "verifyE2eHarness", "verifyScenarioContract",
                "verifyLaunchArgumentContract", "syncE2eArtifacts", "verifyIosUiTestsBuild", "verifyE2eContract"):
            self.assertIn('"%s"' % name, script, name)
            self.assertNotIn('"%s"' % name, root_build, name)


if __name__ == "__main__":
    unittest.main()
