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


class DriverContractClassesTests(unittest.TestCase):
    """The classes of probes each adapter enumerates by hand are the ones the sources define: a new one that is not listed would not
    run and the gate would stay green (ZC-7)."""

    def test_the_android_contract_classes_are_the_test_classes_of_the_runner_less_the_scenario_suite(self):
        sources = os.path.join(ROOT, "scenariorunner", "src", "main", "kotlin", "com", "gdavidpb", "tuindice", "scenariorunner")
        defined = set()
        for name in os.listdir(sources):
            if name.endswith(".kt") and "@Test" in read(sources, name):
                defined.update(re.findall(r"^class (\w+)", read(sources, name), re.M))
        defined.discard("ScenarioSuiteTest")
        adapter = read(ROOT, "e2e", "scripts", "android", "adapter.sh")
        listed = set(re.findall(r"\.(\w+)(?:,|\")", re.search(r'^CONTRACT_CLASSES="(.*)"$', adapter, re.M).group(1) + '"'))
        self.assertEqual(listed, defined)
        required = re.search(r'^CONTRACT_REQUIRED="(.*)"$', adapter, re.M).group(1)
        self.assertEqual({item.split("#")[0] for item in required.split(",")}, defined)

    def test_the_ios_probe_classes_are_the_xctest_classes_of_the_contract_file_and_each_is_selected(self):
        defined = set(re.findall(r"^final class (\w+): (?:XCTestCase|ScenarioTestCase)", read(ROOT, "iosApp", "UITests", "DriverContractTests.swift"), re.M))
        adapter = read(ROOT, "e2e", "scripts", "ios", "adapter.sh")
        listed = set(re.search(r'^PROBE_CLASSES="(.*)"$', adapter, re.M).group(1).split(","))
        self.assertEqual(listed, defined)
        selected = set(re.findall(r"-only-testing:\$\{(?:E2E_IOS_UITEST_SCHEME)\}/(\w+)", adapter))
        self.assertEqual(selected | {"DriverContractTests"}, defined)


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
        for name in ("e2eEvidenceAndroid", "e2eEvidenceIos", "e2eEvidence", "verifyE2eHarness",
                "verifyLaunchArgumentContract", "syncE2eArtifacts", "verifyIosUiTestsBuild", "verifyE2eContract"):
            self.assertIn('"%s"' % name, script, name)
            self.assertNotIn('"%s"' % name, root_build, name)

    def fingerprint_files(self):
        """Every versioned file some platform's fingerprint reads (the `excluded:` entries leave files out)."""
        files, excluded = set(), []
        listed = subprocess.run(["git", "ls-files", "--cached", "--others", "--exclude-standard"], cwd=ROOT,
            stdout=subprocess.PIPE, universal_newlines=True, check=True).stdout.splitlines()
        for platform in ("android", "ios"):
            done = subprocess.run(["bash", os.path.join(SHARED, "e2e-fingerprint.sh"), "--print-pathspecs", platform, "HEAD"],
                cwd=ROOT, stdout=subprocess.PIPE, universal_newlines=True, check=True)
            specs = [line.split(":", 1) for line in done.stdout.splitlines() if ":" in line]
            excluded += [spec for kind, spec in specs if kind == "excluded"]
            for path in listed:
                if any(kind != "excluded" and (path == spec or path.startswith(spec.rstrip("/") + "/")) for kind, spec in specs):
                    files.add(path)
        return {path for path in files if not any(path.startswith(spec) for spec in excluded)}

    @staticmethod
    def pathspecs():
        """The paths each platform's fingerprint reads (directories or files), `excluded:` entries left out."""
        found = set()
        for platform in ("android", "ios"):
            done = subprocess.run(["bash", os.path.join(SHARED, "e2e-fingerprint.sh"), "--print-pathspecs", platform, "HEAD"],
                cwd=ROOT, stdout=subprocess.PIPE, universal_newlines=True, check=True)
            found.update(line.split(":", 1)[1] for line in done.stdout.splitlines() if line.split(":", 1)[0] in ("required", "optional"))
        return found

    def test_what_the_skill_and_the_runbook_call_not_covered_is_not_read_by_the_fingerprint(self):
        # ΔD-6: the runbook said the root build script was not covered while the fingerprint reads it. Every path named
        # after "Not covered:" must be absent from the pathspecs, or be named as an exception in the same document.
        specs = self.pathspecs()
        for name in ("SKILL.md", os.path.join("references", "certification-runbook.md")):
            text = " ".join(read(SKILL, name).split())
            match = re.search(r"Not covered: (.*?)\. [A-Z]", text)
            self.assertTrue(match, "%s has no 'Not covered:' sentence" % name)
            tokens = re.findall(r"`([^`]+)`", match.group(1))
            elsewhere = text.replace(match.group(0), "")  # an exception is named outside the sentence it excepts from
            self.assertGreaterEqual(len(tokens), 4, name)
            for token in tokens:
                base = token[:-3] if token.endswith("/**") else token
                for spec in sorted(specs):
                    if spec == base or spec.startswith(base.rstrip("/") + "/"):
                        self.assertIn(os.path.basename(spec), elsewhere, "%s calls %s not covered but the fingerprint reads %s" % (name, token, spec))
                    elif not token.startswith("*") and base.startswith(spec + "/"):
                        self.fail("%s calls %s not covered but the fingerprint reads its parent %s" % (name, token, spec))

    def test_the_runbook_names_the_root_files_the_fingerprint_reads(self):
        text = " ".join(read(SKILL, "references", "certification-runbook.md").split())
        covered = re.search(r"Covered: (.*?) Not covered:", text)
        self.assertTrue(covered)
        for spec in sorted(self.pathspecs()):
            if ("/" not in spec and os.path.isfile(os.path.join(ROOT, spec))) or spec.startswith("gradle/"):
                token = "version catalog" if spec.endswith("libs.versions.toml") else os.path.basename(spec)
                self.assertIn(token, covered.group(1), "the runbook does not say the fingerprint reads %s" % spec)

    def test_what_the_build_and_the_harness_execute_of_the_shared_library_lives_inside_the_fingerprint(self):
        # D-4/ΔD-10: common.sh serves the CI scripts and is outside the fingerprint, yet the two scripts the iOS build runs
        # and the harness source it. The functions they use are defined in e2e/scripts/shared/ci-common.sh, which
        # common.sh loads; what stays in common.sh is only what no fingerprinted file executes.
        library = os.path.join("e2e", "scripts", "shared", "ci-common.sh")
        inside = self.fingerprint_files()
        self.assertIn(library, inside)
        self.assertIn('ci-common.sh', read(COMMON))
        outside = set(re.findall(r"(?m)^([A-Za-z_][A-Za-z_0-9]*)\(\)", read(COMMON)))
        self.assertTrue(outside)
        self.assertEqual(sorted(name for name in outside if name.startswith("e2e_")), [])
        users = [path for path in sorted(inside) if path.endswith(".sh") and re.search(r"(?m)^\s*source .*common\.sh", read(ROOT, path))]
        self.assertIn(".github/scripts/sync-app-version.sh", users)
        self.assertIn(".github/scripts/materialize-firebase-configs.sh", users)
        for path in users:
            leaked = sorted(name for name in outside if re.search(r"\b%s\b" % name, read(ROOT, path)))
            self.assertEqual(leaked, [], "%s runs in the build but uses functions defined outside the fingerprint" % path)
        for name in sorted(os.listdir(os.path.join(SHARED, "harness"))):
            if name.endswith(".py"):
                text = read(SHARED, "harness", name)
                for function in re.findall(r"\be2e_[a-z_]+\b", text):
                    self.assertIn("%s()" % function, read(ROOT, library), "%s names %s" % (name, function))

    def test_verify_e2e_contract_aggregates_the_checks_and_no_retired_task_remains(self):
        # F26: the contract is the five checks below, and the Maestro/platform tasks and verifyScenarioContract are gone.
        script = read(ROOT, "gradle", "e2e-tasks.gradle.kts")
        start = script.index('tasks.register("verifyE2eContract")')
        block = script[start:script.index("\n}\n", start)]
        for dependency in (":scenariokit:testAndroidHostTest", ":scenarios:testAndroidHostTest", "verifyE2eArtifactsFresh",
                "verifyE2eHarness", "verifyLaunchArgumentContract"):
            self.assertIn('"%s"' % dependency, block, dependency)
        for retired in ("e2eMaestro", "e2ePlatform", "verifyScenarioContract", "validate-e2e-contract"):
            self.assertNotIn(retired, script, retired)


if __name__ == "__main__":
    unittest.main()
