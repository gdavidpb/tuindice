"""verify-line-budgets.sh: the harness scripts, SKILL.md and the runbook stay within e2e/tools/verify/line-budgets.env."""

import os
import shutil
import subprocess
import tempfile
import unittest

TESTS = os.path.dirname(os.path.abspath(__file__))
VERIFY = os.path.join(TESTS, "..", "verify")
SCRIPT = os.path.join(VERIFY, "verify-line-budgets.sh")
ROOT = os.path.realpath(os.path.join(TESTS, "..", "..", ".."))


def budgets():
    values = {}
    with open(os.path.join(VERIFY, "line-budgets.env")) as handle:
        lines = handle.read().splitlines()
    for line in lines:
        if line.strip() and not line.startswith("#"):
            key, _, value = line.partition("=")
            values[key] = int(value)
    return values


def write_lines(path, count):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as handle:
        handle.write("x\n" * count)


class LineBudgetTests(unittest.TestCase):
    def setUp(self):
        self.tree = tempfile.mkdtemp(prefix="e2e-budgets-")
        self.addCleanup(shutil.rmtree, self.tree, True)
        self.limits = budgets()
        self.skill = os.path.join(self.tree, ".codex", "skills", "certify-tuindice-pr")
        self.set(scripts=self.limits["E2E_SCRIPTS_MAX_LINES"], skill=self.limits["SKILL_MD_MAX_LINES"],
            runbook=self.limits["CERTIFICATION_RUNBOOK_MAX_LINES"])

    def set(self, scripts=None, skill=None, runbook=None):
        if scripts is not None:
            # Spread over the three directories, and with a bytecode file that must not be counted.
            third = scripts // 3
            write_lines(os.path.join(self.tree, "e2e", "scripts", "shared", "a.py"), third)
            write_lines(os.path.join(self.tree, "e2e", "scripts", "android", "adapter.sh"), third)
            write_lines(os.path.join(self.tree, "e2e", "scripts", "ios", "adapter.sh"), scripts - 2 * third)
            write_lines(os.path.join(self.tree, "e2e", "scripts", "shared", "__pycache__", "a.cpython-39.pyc"), 999)
        if skill is not None:
            write_lines(os.path.join(self.skill, "SKILL.md"), skill)
        if runbook is not None:
            write_lines(os.path.join(self.skill, "references", "certification-runbook.md"), runbook)

    def verify(self):
        done = subprocess.run(["bash", SCRIPT, "--root", self.tree], stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            universal_newlines=True)
        return done.returncode, done.stdout + done.stderr

    def test_the_budgets_are_the_firm_ones_and_the_scripts_cap_is_declared_once(self):
        self.assertEqual(self.limits["SKILL_MD_MAX_LINES"], 140)
        self.assertEqual(self.limits["CERTIFICATION_RUNBOOK_MAX_LINES"], 300)
        self.assertIn("E2E_SCRIPTS_MAX_LINES", self.limits)
        with open(SCRIPT) as handle:
            text = handle.read()
        for number in ("4500", "140", "300"):
            self.assertNotIn(number, text, "the verifier must read the budgets, not repeat them")

    def test_every_budget_exactly_at_its_limit_passes(self):
        code, out = self.verify()
        self.assertEqual(code, 0, out)
        self.assertIn("line budget: SKILL.md 140 of 140", out)

    def test_one_line_over_the_scripts_budget_fails_and_names_it(self):
        self.set(scripts=self.limits["E2E_SCRIPTS_MAX_LINES"] + 1)
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("OVER BUDGET: e2e/scripts/{shared,android,ios} has %d lines" % (self.limits["E2E_SCRIPTS_MAX_LINES"] + 1), out)

    def test_one_line_over_the_skill_budget_fails(self):
        self.set(skill=self.limits["SKILL_MD_MAX_LINES"] + 1)
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("OVER BUDGET: SKILL.md", out)

    def test_one_line_over_the_runbook_budget_fails(self):
        self.set(runbook=self.limits["CERTIFICATION_RUNBOOK_MAX_LINES"] + 1)
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("OVER BUDGET: references/certification-runbook.md", out)

    def test_a_missing_file_is_a_usage_error_not_a_pass(self):
        os.remove(os.path.join(self.skill, "SKILL.md"))
        code, out = self.verify()
        self.assertEqual(code, 2, out)

    def test_this_repository_is_within_its_budgets(self):
        done = subprocess.run(["bash", SCRIPT], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            universal_newlines=True)
        self.assertEqual(done.returncode, 0, done.stdout + done.stderr)


if __name__ == "__main__":
    unittest.main()
