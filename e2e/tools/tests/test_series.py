"""The count of a `--repeat` series: environment failures are not product failures, and the summary says how many runs were valid."""

import os
import signal
import subprocess
import sys
import time
import unittest
from unittest import mock

import support
from support import Workspace, scenario
from harness.config import Config
from harness.gitstate import GitState
from harness.ledger import Ledger
from harness.manifest import Manifest
from harness.runner import Options, PlatformRun
from test_run_integrity import real_wiremock


class SeriesCountTests(unittest.TestCase):
    def test_a_series_with_an_environment_failure_counts_valid_and_environment_runs_apart(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-b": ["env", "pass"]}})
        result = ws.diagnose("ios", "--repeat", "3")
        self.assertEqual(result.code, 0, result.out)
        self.assertIn("SERIES 3 repetitions, 6 scenario runs: 6 valid (6 passed, 0 failed), 0 lost to the environment; "
            "1 environment attempt was rerun", result.out)
        series = ws.manifest()["series"]
        self.assertEqual((series["scenarioRuns"], series["valid"], series["passed"], series["failed"], series["environment"],
            series["environmentAttempts"]), (6, 6, 6, 0, 0, 1))

    def test_a_survey_run_that_could_not_be_measured_is_environment_not_a_failure_of_the_product(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b"), scenario("fix-c")],
            {"behaviours": {"fix-a": ["env"], "fix-b": ["fail:assertion"]}})
        result = ws.diagnose("ios", "--survey", "--repeat", "2")
        self.assertIn("SERIES 2 repetitions, 6 scenario runs: 4 valid (2 passed, 2 failed), 2 lost to the environment; "
            "2 environment attempts were not rerun", result.out)
        self.assertEqual(ws.manifest()["series"]["environment"], 2)

    def test_a_series_without_environment_failures_keeps_the_line_it_had(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:assertion", "pass"]}})
        result = ws.diagnose("ios", "--repeat", "2")
        self.assertIn("REPEAT 2 runs: 1 scenarios failed in at least one", result.out)
        self.assertNotIn("SERIES", result.out)
        self.assertEqual(ws.manifest()["series"]["environmentAttempts"], 0)


class JunitWriterTests(unittest.TestCase):
    def test_a_result_without_class_or_summary_is_written_instead_of_breaking_the_report(self):
        from harness import junit
        import tempfile
        path = os.path.join(tempfile.mkdtemp(), "junit.xml")
        junit.write(path, "ios", [
            {"id": "a", "status": "failed", "class": None, "summary": None, "seconds": 1.0},
            {"id": "b", "status": "skipped", "summary": None, "seconds": 0.0}])
        with open(path) as handle:
            report = handle.read()
        self.assertIn('type="unknown"', report)
        self.assertIn('message="not run"', report)


class CutSeriesTests(unittest.TestCase):
    """A `--repeat` series is cut by hand often; the run must still say what it measured and clean up (ΔC-1, ΔC-4, ΔC-10)."""

    def test_a_series_cut_by_sigterm_after_a_failed_repetition_is_finalised_and_leaves_no_wiremock(self):
        ws = Workspace(self, [scenario("fix-a", 60)], {"behaviours": {"fix-a": ["fail:assertion", "hang"]}})
        env = dict(ws.env, **real_wiremock())
        process = subprocess.Popen([sys.executable, support.E2E_PY, "run", "--platform", "ios", "--mode", "diagnose", "--repeat", "3"],
            cwd=ws.repo, env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        self.addCleanup(process.kill)
        pid_file = os.path.join(ws.fake, "ios", "hang.pid")
        deadline = time.monotonic() + support.RUN_READY_SECONDS
        while not os.path.exists(pid_file) and time.monotonic() < deadline:
            time.sleep(0.1)
        self.assertTrue(os.path.exists(pid_file), "the second repetition never hung")
        time.sleep(0.3)
        process.send_signal(signal.SIGTERM)
        out, err = process.communicate(timeout=60)
        manifest = ws.manifest()
        self.assertEqual((manifest["outcome"], manifest["exitCode"]), ("interrupted", 143), out + err)
        self.assertNotIn("HARNESS ERROR", out)
        self.assertTrue(os.path.exists(os.path.join(ws.run_dirs()[-1], "junit.xml")))
        self.assertFalse(os.path.exists(os.path.join(ws.dir, "tmp", "ios", "wiremock", "lock", "owner.json")),
            "the cleanup must have stopped WireMock")
        self.assertEqual((manifest["series"]["scenarioRuns"], manifest["series"]["failed"]), (1, 1))
        self.assertIn("REPEAT 1 of 3 runs: 1 scenarios failed in at least one", out)
        self.assertEqual((manifest["series"]["repetitions"], manifest["series"]["repetitionsCompleted"]), (3, 1))

    def test_a_survey_series_cut_while_a_scenario_that_never_failed_runs_is_finalised_with_it_as_not_run(self):
        ws = Workspace(self, [scenario("fix-a", 60)], {"behaviours": {"fix-a": ["pass", "hang"]}})
        env = dict(ws.env, **real_wiremock())
        process = subprocess.Popen([sys.executable, support.E2E_PY, "run", "--platform", "ios", "--mode", "diagnose", "--repeat", "3",
            "--survey"], cwd=ws.repo, env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        self.addCleanup(process.kill)
        pid_file = os.path.join(ws.fake, "ios", "hang.pid")
        deadline = time.monotonic() + support.RUN_READY_SECONDS
        while not os.path.exists(pid_file) and time.monotonic() < deadline:
            time.sleep(0.1)
        self.assertTrue(os.path.exists(pid_file), "the second repetition never hung")
        time.sleep(0.3)
        process.send_signal(signal.SIGTERM)
        out, err = process.communicate(timeout=60)
        manifest = ws.manifest()
        self.assertEqual((manifest["outcome"], manifest["exitCode"]), ("interrupted", 143), out + err)
        self.assertNotIn("internal error", out + err)
        with open(os.path.join(ws.run_dirs()[-1], "junit.xml")) as handle:
            report = handle.read()
        self.assertIn('failures="0"', report)
        self.assertIn('errors="0"', report)

    def test_a_series_cut_in_its_first_repetition_after_a_failure_counts_it_and_says_how_many_repetitions_finished(self):
        ws = Workspace(self, [scenario("fix-a", 60), scenario("fix-b", 60)], {"behaviours": {"fix-a": ["fail:assertion"], "fix-b": ["hang"]}})
        env = dict(ws.env, **real_wiremock())
        process = subprocess.Popen([sys.executable, support.E2E_PY, "run", "--platform", "ios", "--mode", "diagnose", "--repeat", "3"],
            cwd=ws.repo, env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        self.addCleanup(process.kill)
        pid_file = os.path.join(ws.fake, "ios", "hang.pid")
        deadline = time.monotonic() + support.RUN_READY_SECONDS
        while not os.path.exists(pid_file) and time.monotonic() < deadline:
            time.sleep(0.1)
        self.assertTrue(os.path.exists(pid_file), "the first repetition never hung")
        time.sleep(0.3)
        process.send_signal(signal.SIGTERM)
        out, err = process.communicate(timeout=60)
        self.assertIn("REPEAT 0 of 3 runs: 1 scenarios failed in at least one", out)
        self.assertIn("RESULT interrupted", out)
        series = ws.manifest()["series"]
        self.assertEqual((series["repetitions"], series["repetitionsCompleted"]), (3, 0))

    def test_a_survey_series_cut_in_its_second_repetition_reports_what_passed_in_the_first_not_as_not_run(self):
        ws = Workspace(self, [scenario("fix-a", 60), scenario("fix-b", 60)], {"behaviours": {"fix-a": ["pass", "hang"], "fix-b": ["pass"]}})
        env = dict(ws.env, **real_wiremock())
        process = subprocess.Popen([sys.executable, support.E2E_PY, "run", "--platform", "ios", "--mode", "diagnose", "--repeat", "3",
            "--survey"], cwd=ws.repo, env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        self.addCleanup(process.kill)
        pid_file = os.path.join(ws.fake, "ios", "hang.pid")
        deadline = time.monotonic() + support.RUN_READY_SECONDS
        while not os.path.exists(pid_file) and time.monotonic() < deadline:
            time.sleep(0.1)
        self.assertTrue(os.path.exists(pid_file), "the second repetition never hung")
        time.sleep(0.3)
        process.send_signal(signal.SIGTERM)
        out, err = process.communicate(timeout=60)
        manifest = ws.manifest()
        self.assertEqual((manifest["outcome"], manifest["exitCode"]), ("interrupted", 143), out + err)
        self.assertEqual((manifest["series"]["repetitionsCompleted"], manifest["scenarios"]["notRun"]), (1, 0))
        self.assertIn("REPEAT 1 of 3 runs: 0 scenarios failed in at least one", out)
        self.assertEqual([r["status"] for r in manifest["results"]], ["passed", "passed"])
        self.assertNotIn("<skipped", support.text(os.path.join(ws.run_dirs()[-1], "junit.xml")))

    def test_a_scenario_with_no_attempt_in_the_cut_repetition_is_reported_with_the_attempt_that_failed(self):
        ws = Workspace(self, [scenario("fix-a")])
        cfg = Config(ws.repo, ws.env)
        run = PlatformRun(cfg, "ios", Options("diagnose", repeat=3))
        run.git = GitState(ws.repo)
        run.manifest = Manifest("", "x", "diagnose", "ios", cfg, enabled=False)
        run.ledger, run.quarantined = Ledger.memory("ios"), []  # a new repetition: nothing recorded yet
        run.runnable = [mock.Mock(id="fix-a")]
        run.failed_overall = {"fix-a": {"failureClass": "product_assertion", "failureSummary": "step 3 WaitVisible(x): gone",
            "durationMs": 4000, "tolerances": {}}}
        (item,) = run._results()
        self.assertEqual((item["status"], item["class"], item["summary"], item["seconds"]),
            ("failed", "product_assertion", "step 3 WaitVisible(x): gone", 4.0))

    def test_a_scenario_that_failed_and_then_passed_is_reported_with_the_attempt_that_failed(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:assertion", "pass"]}})
        self.assertEqual(ws.diagnose("ios", "--repeat", "2").code, 1)
        summary = support.text(os.path.join(ws.run_dirs()[-1], "summary.txt"))
        self.assertIn("FAILED fix-a step 3 WaitVisible(tag:record_container): not visible after 5000ms", summary)

    def test_a_series_cut_by_the_second_environment_failure_still_leaves_its_count(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-a": ["env", "pass"], "fix-b": ["env"]}})
        result = ws.diagnose("ios", "--repeat", "3")
        self.assertEqual(result.code, 3, result.out)
        self.assertIn("SERIES 0 of 3 repetitions, 2 scenario runs: 1 valid (1 passed, 0 failed), 1 lost to the environment; "
            "1 environment attempt was rerun; 1 environment attempt was not rerun", result.out)
        self.assertEqual(ws.manifest()["series"]["environment"], 1)


class FinishFailureTests(unittest.TestCase):
    """A defect while the results are written must not leave a manifest that contradicts the exit code (ZC-5)."""

    def test_a_failure_writing_the_results_is_a_harness_error_in_the_manifest_the_output_and_the_exit_code(self):
        ws = Workspace(self, [scenario("fix-a")])
        code = ("import sys; sys.path.insert(0, %r); import e2e; from harness import junit\n"
            "def boom(*args): raise TypeError('junit broke')\n"
            "junit.write = boom\nsys.exit(e2e.main(['run', '--platform', 'ios', '--mode', 'diagnose']))" % support.SHARED)
        done = subprocess.run([sys.executable, "-c", code], cwd=ws.repo, env=ws.env, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            universal_newlines=True, timeout=120)
        self.assertEqual(done.returncode, 70, done.stdout + done.stderr)
        manifest = ws.manifest()
        self.assertEqual((manifest["outcome"], manifest["exitCode"]), ("harness_error", 70))
        self.assertIn("junit broke", manifest["stop"]["reason"])
        self.assertIn("junit broke", support.text(os.path.join(ws.run_dirs()[-1], "harness-error.txt")))
        self.assertIn("RESULT harness_error", done.stdout)
        self.assertFalse(os.path.exists(os.path.join(ws.dir, "tmp", "ios", "wiremock", "lock", "owner.json")))

    def test_a_failure_of_the_handler_itself_still_ends_in_harness_error_not_in_passed_with_exit_0(self):
        # The disk is full: the results fail to be written and so does the line that says it (YC-4a).
        ws = Workspace(self, [scenario("fix-a")])
        cfg = Config(ws.repo, ws.env)
        run = PlatformRun(cfg, "ios", Options("diagnose"))
        run.git = GitState(ws.repo)
        run.manifest = Manifest("", "x", "diagnose", "ios", cfg, enabled=False)
        run.ledger, run.quarantined, run.runnable, run.run_dir = Ledger.memory("ios"), [], [], None
        run.log = mock.Mock()
        run.log.say.side_effect = OSError(28, "No space left on device")
        run._write_results = mock.Mock(side_effect=OSError(28, "No space left on device"))
        self.assertEqual(run._finish("passed", 0), ("harness_error", 70))
        self.assertEqual((run.manifest.data["outcome"], run.manifest.data["exitCode"]), ("harness_error", 70))

    def test_a_failure_writing_the_results_of_a_cut_keeps_the_diagnosis_of_the_cut_and_its_exit_code(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-a": ["fail:typed"]}})
        code = ("import sys; sys.path.insert(0, %r); import e2e; from harness import junit\n"
            "def boom(*args): raise TypeError('junit broke')\n"
            "junit.write = boom\nsys.exit(e2e.main(['run', '--platform', 'ios', '--mode', 'diagnose']))" % support.SHARED)
        done = subprocess.run([sys.executable, "-c", code], cwd=ws.repo, env=ws.env, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            universal_newlines=True, timeout=120)
        self.assertEqual(done.returncode, 5, done.stdout + done.stderr)
        stop = ws.manifest()["stop"]
        self.assertEqual((stop["scenario"], stop["failureClass"]), ("fix-a", "typed_text_mismatch"))
        self.assertIn("junit broke", stop["harnessError"])
        self.assertIn("fix-a", done.stdout.split("RESULT", 1)[1])

    def test_a_failure_writing_the_results_of_a_cut_run_does_not_replace_the_reason_of_the_cut(self):
        ws = Workspace(self, [scenario("fix-a")])
        cfg = Config(ws.repo, ws.env)
        run = PlatformRun(cfg, "ios", Options("diagnose"))
        run.git = GitState(ws.repo)
        run.manifest = Manifest("", "x", "diagnose", "ios", cfg, enabled=False)
        run.ledger, run.quarantined, run.runnable, run.run_dir = Ledger.memory("ios"), [], [], None
        run.log = mock.Mock()
        run.manifest.data["stop"]["reason"] = "signal 15"
        run._write_results = mock.Mock(side_effect=TypeError("junit broke"))
        self.assertEqual(run._finish("interrupted", 143), ("interrupted", 143))
        stop = run.manifest.data["stop"]
        self.assertEqual(stop["reason"], "signal 15")
        self.assertEqual(stop["harnessError"], "TypeError: junit broke")


if __name__ == "__main__":
    unittest.main()
