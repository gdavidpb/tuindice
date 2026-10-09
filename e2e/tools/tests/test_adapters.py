"""adapter.sh of both platforms against fake adb, xcrun and xcodebuild, and the readers in adapter_tools.py (plan F16)."""

import json
import os
import signal
import subprocess
import tempfile
import time
import unittest

import support  # puts e2e/scripts/shared on sys.path
from support import Workspace, scenario
from test_device import Sandbox

import adapter_tools

SCRIPTS = os.path.join(support.SHARED, "..")
UDID = "FAKE-0000-0000-0000-000000000001"
APP_ID = "com.gdavidpb.tuindice.debug"
TEST_ID = "com.gdavidpb.tuindice.scenariorunner"
RESULT = {"scenarioId": "auth-login-cancel", "outcome": "passed", "steps": [], "failure": None}


class Adapter(Sandbox):
    """A Sandbox with the build state a `build` verb leaves, and an adapter.sh runner."""

    def __init__(self, test, platform):
        Sandbox.__init__(self, test, platform)
        self.script = os.path.join(SCRIPTS, platform, "adapter.sh")
        self.work = os.path.join(self.dir, "tmp", platform)
        self.reports = os.path.join(self.dir, "reports")
        self.env.update(E2E_FAKE_DIAGNOSTIC_REPORTS=self.reports, E2E_CATALOG_FILE=os.path.join(self.dir, "catalog.json"))
        os.makedirs(self.reports)
        with open(os.path.join(self.dir, "catalog.json"), "w") as handle:
            json.dump(support.catalog(support.scenario("auth-login-cancel")), handle)
        os.makedirs(self.work)
        if platform == "android":
            self.online()
            state = {"app": "/x/app.apk", "appId": APP_ID, "test": "/x/test.apk", "testId": TEST_ID}
        else:
            self.write("sim", "%s Booted\n" % UDID, self.xcrun)
            state = {"app": "/x/TuIndiceHost.app", "appId": APP_ID, "executable": "TuIndiceHost", "derivedData": "/x/dd"}
        with open(os.path.join(self.work, "build.json"), "w") as handle:
            json.dump(state, handle)

    def run(self, verb, *args, **env):
        done = subprocess.run(["bash", self.script, verb] + list(args), env=dict(self.env, **env), stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, universal_newlines=True, timeout=60)
        done.json = json.loads(done.stdout.strip().splitlines()[-1]) if done.returncode == 0 and done.stdout.strip() else {}
        return done

    def adb_calls(self):
        return self.calls(self.adb)

    def xcrun_calls(self):
        return self.calls(self.xcrun)


class AndroidAdapterTests(unittest.TestCase):
    def setUp(self):
        self.box = Adapter(self, "android")

    def put_result(self, scenario="auth-login-cancel", result=RESULT):
        self.box.write("testfiles/files/e2e/%s/result.json" % scenario, json.dumps(result))

    def test_reset_app_clears_the_app_and_never_the_test_package(self):
        done = self.box.run("reset-app")
        self.assertEqual(done.returncode, 0, done.stderr)
        calls = "\n".join(self.box.adb_calls())
        self.assertIn("shell pm clear %s" % APP_ID, calls)
        self.assertNotIn("pm clear %s" % TEST_ID, calls)
        self.assertIn("shell run-as %s rm -rf files/e2e" % TEST_ID, calls)

    def test_reset_app_that_does_not_report_success_is_an_environment_failure(self):
        self.box.write("pmclear.answer", "Failed\n")
        done = self.box.run("reset-app")
        self.assertEqual(done.returncode, 3)
        self.assertIn("pm clear %s answered 'Failed'" % APP_ID, done.stderr)

    def test_a_verb_without_a_recorded_build_exits_3(self):
        os.remove(os.path.join(self.box.work, "build.json"))
        done = self.box.run("reset-app")
        self.assertEqual(done.returncode, 3)
        self.assertIn("run the build verb first", done.stderr)

    def test_run_scenario_reports_the_facts_and_never_the_exit_status(self):
        # am instrument exits 0 even when the scenario failed: the failure is in the status code of its output.
        failing = {"scenarioId": "auth-login-cancel", "outcome": "failed", "steps": [], "failure": {"kind": "ASSERTION"}}
        self.put_result(result=failing)
        self.box.write("instrument.mode", "fail\n")
        attempt = os.path.join(self.box.dir, "attempt")
        done = self.box.run("run-scenario", "auth-login-cancel", attempt, "18626")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(done.json, {"nativeOk": False, "testsExecuted": 1})
        self.assertEqual(support.text(os.path.join(attempt, "result.json")), json.dumps(failing))
        self.assertIn("exited 0", done.stderr)

    def test_run_scenario_passes_the_filter_class_and_the_wiremock_url_and_no_trace_argument(self):
        self.put_result()
        done = self.box.run("run-scenario", "auth-login-cancel", os.path.join(self.box.dir, "a"), "18626", E2E_TRACE="1")
        self.assertEqual(done.json, {"nativeOk": True, "testsExecuted": 1})
        call = [c for c in self.box.adb_calls() if "am instrument" in c][0]
        self.assertTrue(call.startswith("-s emulator-5554 shell am instrument -w -r"), call)
        self.assertIn("-e class %s.ScenarioSuiteTest" % TEST_ID, call)
        self.assertIn("-e scenario auth-login-cancel", call)
        self.assertIn("-e wiremockUrl http://10.0.2.2:18626", call)
        self.assertNotIn("e2e" + "Trace", call)  # split: the vocabulary gate scans this file
        self.assertTrue(call.endswith("%s/androidx.test.runner.AndroidJUnitRunner" % TEST_ID), call)

    def test_run_scenario_brings_back_the_driver_log_along_with_result_json(self):
        # The runner appends driver.log line by line; a run that ends brings it home with result.json (a killed one gets it through
        # collect-failure, which the next test covers).
        self.put_result()
        self.box.write("testfiles/files/e2e/auth-login-cancel/driver.log", "10:00:00.000 [3] WaitVisible x -> passed (12 ms)\n")
        attempt = os.path.join(self.box.dir, "a")
        done = self.box.run("run-scenario", "auth-login-cancel", attempt, "18626")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(support.text(os.path.join(attempt, "driver.log")), "10:00:00.000 [3] WaitVisible x -> passed (12 ms)\n")
        self.assertEqual(support.text(os.path.join(attempt, "result.json")), json.dumps(RESULT))

    def test_a_hung_run_is_stopped_on_the_device_when_the_harness_kills_it(self):
        self.box.write("instrument.hang", "")
        self.box.write("testfiles/files/e2e/auth-login-cancel/driver.log", "10:00:00.000 [3] WaitVisible x -> passed (12 ms)\n")
        attempt = os.path.join(self.box.dir, "h")
        process = subprocess.Popen(["bash", self.box.script, "run-scenario", "auth-login-cancel", attempt, "18626"],
            env=self.box.env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True, start_new_session=True)
        deadline = time.time() + 20
        while time.time() < deadline and not any("am instrument" in c for c in self.box.adb_calls()):
            time.sleep(0.1)
        process.send_signal(signal.SIGTERM)
        code = process.wait(timeout=20)
        self.assertEqual(code, 143)
        calls = "\n".join(self.box.adb_calls())
        self.assertIn("-s emulator-5554 shell am force-stop %s" % TEST_ID, calls)
        self.assertIn("-s emulator-5554 shell am force-stop %s" % APP_ID, calls)
        # The run that was cut already wrote its log line by line: it comes home with the trap, after the instrumentation stopped.
        self.assertEqual(support.text(os.path.join(attempt, "driver.log")), "10:00:00.000 [3] WaitVisible x -> passed (12 ms)\n")
        try:
            os.killpg(process.pid, signal.SIGKILL)  # the fake's sleep
        except ProcessLookupError:
            pass

    def test_the_driver_log_of_a_run_killed_without_a_trap_reaches_the_attempt_through_collect_failure(self):
        # A SIGKILL leaves no trap to pull anything and the next reset-app empties the runner's directory: collect-failure, which runs
        # in between, can still bring the log home.
        self.box.write("instrument.hang", "")
        self.box.write("testfiles/files/e2e/auth-login-cancel/driver.log", "10:00:00.000 [3] WaitVisible x -> passed (12 ms)\n")
        attempt = os.path.join(self.box.dir, "h")
        process = subprocess.Popen(["bash", self.box.script, "run-scenario", "auth-login-cancel", attempt, "18626"],
            env=self.box.env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True, start_new_session=True)
        deadline = time.time() + 20
        while time.time() < deadline and not any("am instrument" in c for c in self.box.adb_calls()):
            time.sleep(0.1)
        os.killpg(process.pid, signal.SIGKILL)
        process.communicate(timeout=20)
        self.assertFalse(os.path.exists(os.path.join(attempt, "driver.log")))
        done = self.box.run("collect-failure", attempt, "1", E2E_CURRENT_SCENARIO="auth-login-cancel")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(support.text(os.path.join(attempt, "driver.log")), "10:00:00.000 [3] WaitVisible x -> passed (12 ms)\n")

    def test_collect_failure_does_not_replace_a_driver_log_the_run_already_brought(self):
        self.box.write("testfiles/files/e2e/auth-login-cancel/driver.log", "from the device\n")
        attempt = os.path.join(self.box.dir, "k2")
        os.makedirs(attempt)
        with open(os.path.join(attempt, "driver.log"), "w") as handle:
            handle.write("brought by run-scenario\n")
        self.assertEqual(self.box.run("collect-failure", attempt, "1", E2E_CURRENT_SCENARIO="auth-login-cancel").returncode, 0)
        self.assertEqual(support.text(os.path.join(attempt, "driver.log")), "brought by run-scenario\n")

    def test_collect_failure_of_an_attempt_without_a_directory_on_the_device_still_brings_the_log_and_the_screenshot(self):
        # health or reset-app failed before the driver opened its directory: the evidence is the device log and the screen.
        attempt = os.path.join(self.box.dir, "nodir")
        os.makedirs(attempt)
        self.box.write("logcat.txt", "1791346195.500  2  2 E App: it failed here\n")
        with open(os.path.join(attempt, "runner.log"), "w") as handle:
            handle.write("started\n")
        done = self.box.run("collect-failure", attempt, "1", E2E_CURRENT_SCENARIO="auth-login-cancel")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(sorted(os.listdir(attempt)), ["fallback-hierarchy.xml", "fallback-screen.png", "logcat.txt", "runner.log"])
        # Listing through exec-out exits 0 even when the directory is absent and would iterate the error text (YC-1).
        self.assertFalse([c for c in self.box.adb_calls() if "exec-out run-as" in c and " ls " in c])

    def test_the_fake_adb_answers_exec_out_of_a_missing_path_like_the_real_one_with_the_error_as_text_and_exit_0(self):
        # The real exec-out exits 0 and prints the error as output; a fake that exits 1 hides every listing done through it (ZC-1, YC-1).
        for tail in (["ls", "files/e2e/nope"], ["cat", "files/e2e/nope/result.json"]):
            done = subprocess.run(["adb", "exec-out", "run-as", TEST_ID] + tail, env=self.box.env, stdout=subprocess.PIPE,
                stderr=subprocess.PIPE, universal_newlines=True, timeout=60)
            self.assertEqual(done.returncode, 0, done.stderr)
            self.assertIn("No such file or directory", done.stdout)

    def test_collect_failure_brings_nothing_home_from_an_attempt_that_never_ran(self):
        # Without runner.log the attempt did not reach the instrumentation: the directory on the device is another attempt's.
        self.put_result()
        self.box.write("testfiles/files/e2e/auth-login-cancel/driver.log", "from the previous attempt\n")
        attempt = os.path.join(self.box.dir, "never")
        os.makedirs(attempt)
        done = self.box.run("collect-failure", attempt, "1", E2E_CURRENT_SCENARIO="auth-login-cancel")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertFalse(os.path.exists(os.path.join(attempt, "driver.log")))
        self.assertFalse(os.path.exists(os.path.join(attempt, "result.json")))

    def test_collect_failure_takes_the_log_and_the_screenshot_before_it_pulls_the_directory(self):
        self.put_result()
        attempt = os.path.join(self.box.dir, "order")
        os.makedirs(attempt)
        with open(os.path.join(attempt, "runner.log"), "w") as handle:
            handle.write("started\n")
        self.assertEqual(self.box.run("collect-failure", attempt, "1", E2E_CURRENT_SCENARIO="auth-login-cancel").returncode, 0)
        calls = self.box.adb_calls()
        pulled = min(i for i, c in enumerate(calls) if "run-as" in c and " ls " in c)
        self.assertLess(max(i for i, c in enumerate(calls) if " logcat " in c), pulled)
        self.assertLess(max(i for i, c in enumerate(calls) if "screencap" in c), pulled)

    def test_enumerate_names_each_scenario_exactly_once(self):
        self.box.write("tests.list", "auth-login-cancel\nsummary-profile-picture\n")
        done = self.box.run("enumerate")
        self.assertEqual(done.json, {"tests": ["auth-login-cancel", "summary-profile-picture"]})
        call = [c for c in self.box.adb_calls() if "-e log true" in c][0]
        self.assertIn("-e class %s.ScenarioSuiteTest" % TEST_ID, call)

    def test_crash_probe_reads_a_crash_an_anr_and_ignores_old_lines(self):
        crash = ("1791346198.148  6054  6054 E AndroidRuntime: FATAL EXCEPTION: main\n"
                 "1791346198.148  6054  6054 E AndroidRuntime: Process: %s, PID: 6054\n" % APP_ID)
        self.box.write("logcat.txt", crash)
        attempt = os.path.join(self.box.dir, "p")
        os.makedirs(attempt)
        done = self.box.run("crash-probe", "1791346190", attempt)
        self.assertEqual(done.json["kind"], "app_crash")
        self.assertIn("FATAL EXCEPTION", support.text(os.path.join(attempt, "crash.txt")))
        self.assertEqual(self.box.run("crash-probe", "1791346200", attempt).json, {"kind": "none", "excerpt": ""})
        self.box.write("logcat.txt", "1791346198.148   689   771 E ActivityManager: ANR in com.android.systemui\n")
        self.assertEqual(self.box.run("crash-probe", "1791346190", attempt).json["kind"], "system_anr")

    def test_collect_failure_keeps_a_fallback_screenshot_and_reads_the_log_store_once(self):
        attempt = os.path.join(self.box.dir, "c")
        os.makedirs(attempt)
        done = self.box.run("collect-failure", attempt, "1")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertTrue(os.path.exists(os.path.join(attempt, "fallback-screen.png")))
        reads = [c for c in self.box.adb_calls() if " logcat " in c and not c.endswith(" -c")]
        self.assertEqual(reads, ["-s emulator-5554 logcat -b all -d -v epoch"])
        self.assertNotIn("-t 4000", "\n".join(self.box.adb_calls()))

    def test_collect_failure_keeps_only_the_lines_of_the_attempt(self):
        self.box.write("logcat.txt", "1791346100.000  1  1 I Old: before the attempt\n"
            "1791346189.999  1  1 I Old: one second before\n"
            "1791346190.000  1  1 I App: the attempt starts\n"
            "1791346195.500  2  2 E App: it failed here\n")
        attempt = os.path.join(self.box.dir, "w")
        os.makedirs(attempt)
        done = self.box.run("collect-failure", attempt, "1791346190")
        self.assertEqual(done.returncode, 0, done.stderr)
        kept = support.text(os.path.join(attempt, "logcat.txt"))
        self.assertIn("the attempt starts", kept)
        self.assertIn("it failed here", kept)
        self.assertNotIn("before the attempt", kept)
        self.assertNotIn("one second before", kept)

    def test_collect_failure_caps_the_log_and_keeps_the_end_of_it(self):
        self.box.write("logcat.txt", "".join("17913461%02d.000  1  1 I App: line %04d %s\n" % (i % 100, i, "x" * 60) for i in range(400)))
        attempt = os.path.join(self.box.dir, "k")
        os.makedirs(attempt)
        done = self.box.run("collect-failure", attempt, "1", E2E_FAKE_LOG_CAP_BYTES="4096")
        self.assertEqual(done.returncode, 0, done.stderr)
        path = os.path.join(attempt, "logcat.txt")
        self.assertLessEqual(os.path.getsize(path), 4096)
        kept = support.text(path)
        self.assertTrue(kept.startswith("# e2e: truncated"), kept[:80])
        self.assertIn("line 0399", kept)
        self.assertNotIn("line 0000", kept)

    def test_every_device_call_names_the_serial(self):
        self.put_result()
        attempt = os.path.join(self.box.dir, "s")
        os.makedirs(attempt)
        for verb in (("install",), ("enumerate",), ("health",), ("reset-app",), ("run-scenario", "auth-login-cancel", attempt, "18626"),
                ("crash-probe", "1", attempt), ("collect-failure", attempt, "1")):
            self.assertEqual(self.box.run(*verb).returncode, 0, verb)
        # `adb devices` lists every emulator and `adb version` takes no device: the only calls without -s.
        bare = [c for c in self.box.adb_calls() if not c.startswith("-s emulator-5554 ") and c not in ("devices", "version")]
        self.assertEqual(bare, [])


class IosAdapterTests(unittest.TestCase):
    def setUp(self):
        self.box = Adapter(self, "ios")

    def test_reset_app_terminates_uninstalls_resets_the_keychain_and_reinstalls_in_that_order(self):
        done = self.box.run("reset-app")
        self.assertEqual(done.returncode, 0, done.stderr)
        calls = [c for c in self.box.xcrun_calls() if c.split(" ")[1] in ("terminate", "uninstall", "keychain", "install")]
        self.assertEqual(calls, ["simctl terminate %s %s" % (UDID, APP_ID), "simctl uninstall %s %s" % (UDID, APP_ID),
            "simctl keychain %s reset" % UDID, "simctl install %s /x/TuIndiceHost.app" % UDID])

    def test_an_app_that_is_not_running_is_the_normal_case_and_any_other_terminate_failure_is_not(self):
        self.box.write("terminate.answer", "An error was encountered processing the command (domain=NSPOSIXErrorDomain, code=3):\nfound nothing to terminate\n", self.box.xcrun)
        self.assertEqual(self.box.run("reset-app").returncode, 0)
        self.box.write("terminate.answer", "Unable to boot device\n", self.box.xcrun)
        done = self.box.run("reset-app")
        self.assertEqual(done.returncode, 3)
        self.assertIn("simctl terminate %s answered: Unable to boot device" % APP_ID, done.stderr)

    def test_run_scenario_uses_the_canonical_invocation_and_reports_facts_not_the_exit_status(self):
        self.box.write("xc.id", "auth-login-cancel", self.box.xcrun)
        self.box.write("xc.result", json.dumps(RESULT), self.box.xcrun)
        self.box.write("xc.rc", "65\n", self.box.xcrun)
        attempt = os.path.join(self.box.dir, "attempt")
        done = self.box.run("run-scenario", "auth-login-cancel", attempt, "18627", E2E_TRACE="1")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(done.json, {"nativeOk": False, "testsExecuted": 1})
        self.assertEqual(json.loads(support.text(os.path.join(attempt, "result.json")))["scenarioId"], "auth-login-cancel")
        call = [c for c in self.box.xcrun_calls() if c.startswith("xcodebuild ")][0]
        for expected in ("test-without-building", "-scheme TuIndiceUITests", "-destination platform=iOS Simulator,id=%s" % UDID,
                "-only-testing:TuIndiceUITests/%s/test_auth_login_cancel" % "auth", "-parallel-testing-enabled NO",
                "-collect-test-diagnostics never", "CODE_SIGNING_ALLOWED=NO", "-resultBundlePath %s/attempt.xcresult" % attempt):
            self.assertIn(expected, call)
        calls = "\n".join(self.box.xcrun_calls())
        self.assertIn("env TEST_RUNNER_E2E_WIREMOCK_URL=http://localhost:18627", calls)
        self.assertIn("env TEST_RUNNER_E2E_OUTPUT_DIR=%s/results" % attempt, calls)
        self.assertIn("env TEST_RUNNER_E2E_TRACE=1", calls)

    def test_a_hung_run_dies_with_its_process_group(self):
        self.box.write("xc.hang", "", self.box.xcrun)
        process = subprocess.Popen(["bash", self.box.script, "run-scenario", "auth-login-cancel", os.path.join(self.box.dir, "h"), "18627"],
            env=self.box.env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True, start_new_session=True)
        deadline = time.time() + 20
        while time.time() < deadline and not any(c.startswith("xcodebuild") for c in self.box.xcrun_calls()):
            time.sleep(0.1)
        os.killpg(process.pid, signal.SIGTERM)
        process.communicate(timeout=20)
        self.assertNotEqual(process.returncode, 0)

    def test_enumerate_drops_the_parentheses_so_the_ids_match_the_catalog(self):
        self.box.write("tests.list", "TuIndiceUITests/auth/test_auth_login_cancel()\nTuIndiceUITests/ScenarioTestCase\n", self.box.xcrun)
        done = self.box.run("enumerate")
        self.assertEqual(done.json, {"tests": ["TuIndiceUITests/auth/test_auth_login_cancel", "TuIndiceUITests/ScenarioTestCase"]})
        self.assertIn("-enumerate-tests", [c for c in self.box.xcrun_calls() if c.startswith("xcodebuild ")][0])

    def test_crash_probe_reads_the_crash_report_of_the_app_process(self):
        report = os.path.join(self.box.reports, "TuIndiceHost-2026-10-07-011506.ips")
        with open(report, "w") as handle:
            handle.write('{"exception":{"type":"EXC_CRASH","signal":"SIGSEGV"},"procPath":"/CoreSimulator/Devices/%s/data/x"}' % UDID)
        attempt = os.path.join(self.box.dir, "p")
        os.makedirs(attempt)
        now = int(time.time())
        done = self.box.run("crash-probe", str(now - 60), attempt, str(now + 5), "0")
        self.assertEqual(done.json["kind"], "app_crash")
        self.assertIn("SIGSEGV", support.text(os.path.join(attempt, "crash.txt")))
        self.assertEqual(self.box.run("crash-probe", str(now + 60), attempt, str(now + 90), "0").json["kind"], "none")

    def test_a_run_cut_by_a_signal_brings_the_driver_log_of_its_results_directory_home(self):
        self.box.write("xc.hang", "", self.box.xcrun)
        self.box.write("xc.id", "auth-login-cancel", self.box.xcrun)
        self.box.write("xc.driverlog", "[3] WaitVisible x -> passed (12 ms)\n", self.box.xcrun)
        attempt = os.path.join(self.box.dir, "cut")
        process = subprocess.Popen(["bash", self.box.script, "run-scenario", "auth-login-cancel", attempt, "18627"],
            env=self.box.env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True, start_new_session=True)
        deadline = time.time() + 20
        while time.time() < deadline and not any(c.startswith("xcodebuild") for c in self.box.xcrun_calls()):
            time.sleep(0.1)
        time.sleep(0.3)
        os.killpg(process.pid, signal.SIGTERM)
        process.communicate(timeout=20)
        self.assertEqual(process.returncode, 143)
        self.assertEqual(support.text(os.path.join(attempt, "driver.log")), "[3] WaitVisible x -> passed (12 ms)\n")

    def test_the_trace_of_the_environment_is_not_inherited_by_a_run_that_did_not_ask_for_it(self):
        self.box.write("xc.id", "auth-login-cancel", self.box.xcrun)
        self.box.write("xc.result", json.dumps(RESULT), self.box.xcrun)
        done = self.box.run("run-scenario", "auth-login-cancel", os.path.join(self.box.dir, "q"), "18627", TEST_RUNNER_E2E_TRACE="1")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertNotIn("TEST_RUNNER_E2E_TRACE", "\n".join(self.box.xcrun_calls()))

    def test_collect_failure_with_an_empty_results_directory_still_brings_the_log_and_the_screenshot(self):
        attempt = os.path.join(self.box.dir, "empty")
        os.makedirs(os.path.join(attempt, "results", "auth-login-cancel"))
        done = self.box.run("collect-failure", attempt, "1", E2E_CURRENT_SCENARIO="auth-login-cancel")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertTrue(os.path.exists(os.path.join(attempt, "app.log")))
        self.assertTrue(os.path.exists(os.path.join(attempt, "fallback-screen.png")))

    def test_collect_failure_reads_the_log_store_before_it_takes_the_screenshot_and_a_failed_screenshot_does_not_cost_the_log(self):
        self.box.write("screenshot.fails", "", self.box.xcrun)
        attempt = os.path.join(self.box.dir, "shot")
        os.makedirs(attempt)
        done = self.box.run("collect-failure", attempt, "1")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertTrue(os.path.exists(os.path.join(attempt, "app.log")))
        self.assertIn("screenshot", done.stderr)

    def test_collect_failure_reads_the_log_store_from_the_start_of_the_attempt(self):
        attempt = os.path.join(self.box.dir, "c")
        os.makedirs(attempt)
        since = 1791346190
        done = self.box.run("collect-failure", attempt, str(since))
        self.assertEqual(done.returncode, 0, done.stderr)
        calls = [c for c in self.box.xcrun_calls() if " log show " in c]
        self.assertEqual(len(calls), 1, calls)
        start = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(since))
        self.assertIn("simctl spawn %s log show --start %s --style compact --predicate " % (UDID, start), calls[0])
        self.assertIn('process == "TuIndiceHost" OR process CONTAINS "UITests"', calls[0])
        self.assertNotIn("--last", calls[0])
        self.assertIn("app log line", support.text(os.path.join(attempt, "app.log")))

    def test_collect_failure_brings_the_results_of_a_killed_run_and_keeps_what_the_run_already_copied(self):
        attempt = os.path.join(self.box.dir, "r")
        results = os.path.join(attempt, "results", "auth-login-cancel")
        os.makedirs(results)
        for name, text in (("driver.log", "[3] WaitVisible x -> passed (12 ms)\n"), ("result.json", "from results\n")):
            with open(os.path.join(results, name), "w") as handle:
                handle.write(text)
        with open(os.path.join(attempt, "result.json"), "w") as handle:
            handle.write("already copied\n")
        done = self.box.run("collect-failure", attempt, "1", E2E_CURRENT_SCENARIO="auth-login-cancel")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(support.text(os.path.join(attempt, "driver.log")), "[3] WaitVisible x -> passed (12 ms)\n")
        self.assertEqual(support.text(os.path.join(attempt, "result.json")), "already copied\n")

    def test_collect_failure_caps_the_app_log_and_keeps_the_end_of_it(self):
        self.box.write("log.txt", "".join("2026-10-07 01:22:%02d.000 I TuIndiceHost[1:1] line %04d %s\n" % (i % 60, i, "y" * 60)
            for i in range(600)), self.box.xcrun)
        attempt = os.path.join(self.box.dir, "k")
        os.makedirs(attempt)
        done = self.box.run("collect-failure", attempt, "1", E2E_FAKE_LOG_CAP_BYTES="5000")
        self.assertEqual(done.returncode, 0, done.stderr)
        path = os.path.join(attempt, "app.log")
        self.assertLessEqual(os.path.getsize(path), 5000)
        kept = support.text(path)
        self.assertTrue(kept.startswith("# e2e: truncated"), kept[:80])
        self.assertIn("line 0599", kept)
        self.assertNotIn("line 0000", kept)

    def test_every_simulator_call_names_the_udid(self):
        attempt = os.path.join(self.box.dir, "s")
        os.makedirs(attempt)
        self.box.write("tests.list", "TuIndiceUITests/auth/test_auth_login_cancel()\n", self.box.xcrun)
        self.box.write("prefs/%s/-g/AppleLocale" % UDID, "%s\n" % self.box.lock["IOS_LOCALE"], self.box.xcrun)  # what health reads
        for verb in (("install",), ("enumerate",), ("health",), ("reset-app",), ("run-scenario", "auth-login-cancel", attempt, "18627"),
                ("collect-failure", attempt, "1")):
            self.assertEqual(self.box.run(*verb).returncode, 0, verb)
        simctl = [c for c in self.box.xcrun_calls() if c.startswith("simctl ")]
        unaddressed = [c for c in simctl if c.split(" ")[1] not in ("list", "create") and UDID not in c]
        self.assertEqual(unaddressed, [])
        for call in (c for c in self.box.xcrun_calls() if c.startswith("xcodebuild ")):
            self.assertIn("id=%s" % UDID, call)


def crash_report(directory, name, captured, udid=UDID, mtime=None):
    """A crash report the way macOS writes it: its own captureTime, and the path of the app inside the simulator's data."""
    path = os.path.join(directory, name)
    with open(path, "w") as handle:
        handle.write('{"app_name":"TuIndiceHost"}\n{"captureTime" : "%s", "procPath" : "\\/CoreSimulator\\/Devices\\/%s\\/data"}'
            % (captured, udid))
    if mtime is not None:
        os.utime(path, (mtime, mtime))
    return path


class IosCrashAttributionTests(unittest.TestCase):
    """The report is the attempt's when the crash happened inside it, whatever the file's date says."""

    def setUp(self):
        self.dir = tempfile.mkdtemp(prefix="e2e-crash-test-")
        self.addCleanup(__import__("shutil").rmtree, self.dir, True)
        self.evidence = os.path.join(self.dir, "crash.txt")
        # 2026-10-07 01:15:00 -0300 is 04:15:00 UTC.
        self.at = 1791346500

    def probe(self, since, until, udid=UDID, wait="0"):
        return adapter_tools.ios_crash(self.dir, since, until, "TuIndiceHost", self.evidence, udid, wait)

    def test_a_crash_is_the_attempts_by_its_capture_time_not_by_the_file_date(self):
        crash_report(self.dir, "TuIndiceHost-a.ips", "2026-10-07 01:15:00.6272 -0300", mtime=self.at + 3600)  # written an hour later
        self.assertEqual(self.probe(self.at - 10, self.at + 10)["kind"], "app_crash")
        self.assertEqual(self.probe(self.at + 11, self.at + 60)["kind"], "none", "a crash before the attempt began is not this one")
        self.assertEqual(self.probe(self.at - 60, self.at - 1)["kind"], "none", "a report that lands during a later attempt is not its")

    def test_a_report_of_another_simulator_is_not_the_dedicated_ones(self):
        crash_report(self.dir, "TuIndiceHost-a.ips", "2026-10-07 01:15:00.6272 -0300", udid="OTHER-SIMULATOR")
        self.assertEqual(self.probe(self.at - 10, self.at + 10)["kind"], "none")

    def test_a_report_that_lands_late_is_waited_for(self):
        timer = __import__("threading").Timer(1.5, crash_report, [self.dir, "TuIndiceHost-late.ips", "2026-10-07 01:15:00.1 -0300"])
        timer.start()
        self.addCleanup(timer.cancel)
        began = time.monotonic()
        found = self.probe(self.at - 10, self.at + 10, wait="10")
        self.assertEqual(found["kind"], "app_crash")
        self.assertLess(time.monotonic() - began, 8, "it stops waiting as soon as the report is there")
        self.assertEqual(adapter_tools.ios_crash(os.path.join(self.dir, "none"), self.at - 10, self.at + 10, "TuIndiceHost",
            self.evidence, UDID, "0")["kind"], "none")


class FailureCaptureTests(unittest.TestCase):
    """The orchestrator asks for logs only when an attempt failed."""

    def test_logs_are_collected_for_failed_attempts_only_and_from_the_start_of_the_attempt(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-b": ["fail:assertion", "pass"]}})
        before = int(time.time())
        self.assertEqual(ws.evidence().code, 0)
        calls = ws.calls("collect-failure")
        self.assertEqual(len(calls), 1, calls)
        self.assertTrue(calls[0][2].endswith(os.path.join("fix-b", "attempt-1")), calls[0])
        self.assertTrue(before <= int(calls[0][3]) <= int(time.time()), "the window starts at the attempt, not at the run")


class UiTestTargetCheckTests(unittest.TestCase):
    """verify-ui-test-target.sh: a missing app is a skip for a caller that did not build one and a failure for one that did."""

    def check(self, *args):
        env = dict(os.environ, PATH=os.path.join(support.TESTS, "fake_bin") + os.pathsep + os.environ["PATH"], FAKE_XCRUN_DIR="/tmp/e2e-unused")
        script = os.path.join(support.TESTS, "..", "..", "..", "iosApp", "scripts", "verify-ui-test-target.sh")
        return subprocess.run(["bash", script] + list(args), env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            universal_newlines=True, timeout=60).stdout

    def test_without_an_app_the_symbol_check_is_skipped(self):
        self.assertIn("SKIP: no built app given", self.check())

    def test_require_app_turns_the_skip_into_a_failure(self):
        out = self.check("--require-app")
        self.assertIn("FAIL: --require-app was given but no built app", out)
        self.assertNotIn("SKIP: no built app given", out)


class ReaderTests(unittest.TestCase):
    def write(self, text):
        path = os.path.join(tempfile.mkdtemp(prefix="e2e-reader-"), "log")
        self.addCleanup(support.shutil.rmtree, os.path.dirname(path), True)
        with open(path, "w") as handle:
            handle.write(text)
        return path

    def test_an_instrumentation_that_crashed_is_not_ok_even_with_a_zero_status(self):
        log = self.write("INSTRUMENTATION_STATUS: test=run[a]\nINSTRUMENTATION_STATUS_CODE: 1\nProcess crashed.\nINSTRUMENTATION_CODE: 0\n")
        self.assertEqual(adapter_tools.instrument_summary(log), {"nativeOk": False, "testsExecuted": 0})

    def test_two_finished_tests_are_not_one(self):
        log = self.write("INSTRUMENTATION_STATUS_CODE: 1\nINSTRUMENTATION_STATUS_CODE: 0\nINSTRUMENTATION_STATUS_CODE: 1\n"
                         "INSTRUMENTATION_STATUS_CODE: 0\nINSTRUMENTATION_CODE: -1\n")
        self.assertEqual(adapter_tools.instrument_summary(log), {"nativeOk": False, "testsExecuted": 2})

    def test_the_largest_executed_count_is_the_total(self):
        log = self.write("\t Executed 1 test, with 0 failures in 1 seconds\n\t Executed 3 tests, with 0 failures in 3 seconds\n")
        self.assertEqual(adapter_tools.xctest_summary(log, 0), {"nativeOk": True, "testsExecuted": 3})
        self.assertFalse(adapter_tools.xctest_summary(log, 65)["nativeOk"])

    def test_a_crash_report_is_waited_for_only_when_the_app_can_have_gone_away(self):
        for text, expected in (("", "yes"), ("{not json", "yes"), ("[]", "yes"),
                (json.dumps({"outcome": "failed", "failure": {"kind": "APP_NOT_RUNNING"}}), "yes"),
                (json.dumps({"outcome": "failed", "failure": {"kind": "ASSERTION"}}), "no"),
                (json.dumps({"outcome": "passed", "failure": None}), "no")):
            self.assertEqual(adapter_tools.app_may_have_crashed(self.write(text)), expected, text)
        self.assertEqual(adapter_tools.app_may_have_crashed("/nonexistent/result.json"), "yes")

    def test_a_native_crash_of_the_app_is_a_crash(self):
        log = self.write("1791346198.100  100  100 F libc    : Fatal signal 11 (SIGSEGV)\n"
                         "1791346198.200  100  100 F DEBUG   : pid: 100, tid: 100, name: x  >>> %s <<<\n" % APP_ID)
        evidence = os.path.join(os.path.dirname(log), "crash.txt")
        self.assertEqual(adapter_tools.logcat_crash(log, "1791346000", APP_ID, evidence)["kind"], "app_crash")

    def test_a_crash_of_the_app_wins_over_an_earlier_anr_of_another_process(self):
        log = self.write("1791346198.100   689   771 E ActivityManager: ANR in com.google.android.gms (x)\n"
                         "1791346199.100  6054  6054 E AndroidRuntime: FATAL EXCEPTION: main\n"
                         "1791346199.101  6054  6054 E AndroidRuntime: Process: %s, PID: 6054\n" % APP_ID)
        evidence = os.path.join(os.path.dirname(log), "crash.txt")
        found = adapter_tools.logcat_crash(log, "1791346000", APP_ID, evidence)
        self.assertEqual(found["kind"], "app_crash")
        self.assertIn("FATAL EXCEPTION", support.text(evidence))

    def test_an_anr_of_the_app_is_an_app_anr(self):
        log = self.write("1791346198.100   689   771 E ActivityManager: ANR in %s (%s/.Main)\n" % (APP_ID, APP_ID))
        evidence = os.path.join(os.path.dirname(log), "crash.txt")
        self.assertEqual(adapter_tools.logcat_crash(log, "1791346000", APP_ID, evidence)["kind"], "app_anr")

    def test_a_log_window_starts_at_the_first_second_of_the_attempt_and_keeps_unstamped_continuations(self):
        log = self.write("1791346189.999 1 1 I A: before\n1791346190.000 1 1 E A: first\n    at continuation without a stamp\n1791346191.000 1 1 I A: second\n")
        out = os.path.join(os.path.dirname(log), "out")
        expected = ["1791346190.000 1 1 E A: first", "    at continuation without a stamp", "1791346191.000 1 1 I A: second"]
        result = adapter_tools.logcat_window(log, "1791346190", 1000, out)
        self.assertEqual(result, {"bytes": sum(len(line) + 1 for line in expected), "truncated": False})
        self.assertEqual(support.text(out).splitlines(), expected)

    def test_an_empty_log_window_says_so_instead_of_leaving_an_empty_file(self):
        log = self.write("1791346100.000 1 1 I A: old\n")
        out = os.path.join(os.path.dirname(log), "out")
        adapter_tools.logcat_window(log, "1791346190", 1000, out)
        self.assertIn("has no lines", support.text(out))

    def test_a_capped_log_never_exceeds_the_cap_and_ends_with_the_last_line(self):
        log = self.write("".join("1791346200.%03d 1 1 I A: line %03d\n" % (i, i) for i in range(500)))
        out = os.path.join(os.path.dirname(log), "out")
        for cap in (300, 1000, 5000):
            result = adapter_tools.logcat_window(log, "1", cap, out)
            self.assertLessEqual(os.path.getsize(out), cap, cap)
            self.assertTrue(result["truncated"])
            self.assertTrue(support.text(out).endswith("line 499\n"))
            self.assertTrue(support.text(out).splitlines()[1].startswith("1791346200."), "the cut must fall on a line boundary")

    def test_cap_log_reads_stdin_and_keeps_the_end(self):
        source = "".join("2026-10-07 01:22:00.%03d I TuIndiceHost: line %04d\n" % (i, i) for i in range(2000))
        out = os.path.join(tempfile.mkdtemp(prefix="e2e-reader-"), "app.log")
        self.addCleanup(support.shutil.rmtree, os.path.dirname(out), True)
        done = subprocess.run(["python3", os.path.join(support.SHARED, "adapter_tools.py"), "cap-log", "2000", out],
            input=source, stdout=subprocess.PIPE, universal_newlines=True, timeout=60)
        self.assertEqual(done.returncode, 0)
        self.assertEqual(json.loads(done.stdout)["truncated"], True)
        self.assertLessEqual(os.path.getsize(out), 2000)
        self.assertTrue(support.text(out).endswith("line 1999\n"))
        self.assertNotIn("line 0000", support.text(out))


if __name__ == "__main__":
    unittest.main()
