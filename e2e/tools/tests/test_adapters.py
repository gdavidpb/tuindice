"""adapter.sh of both platforms against fake adb, xcrun and xcodebuild, and the readers in adapter_tools.py (plan F16)."""

import json
import os
import signal
import subprocess
import tempfile
import time
import unittest

import support  # puts e2e/scripts/shared on sys.path
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

    def test_run_scenario_passes_the_filter_class_the_wiremock_url_and_the_trace(self):
        self.put_result()
        done = self.box.run("run-scenario", "auth-login-cancel", os.path.join(self.box.dir, "a"), "18626", E2E_TRACE="1")
        self.assertEqual(done.json, {"nativeOk": True, "testsExecuted": 1})
        call = [c for c in self.box.adb_calls() if "am instrument" in c][0]
        self.assertTrue(call.startswith("-s emulator-5554 shell am instrument -w -r"), call)
        self.assertIn("-e class %s.ScenarioSuiteTest" % TEST_ID, call)
        self.assertIn("-e scenario auth-login-cancel", call)
        self.assertIn("-e wiremockUrl http://10.0.2.2:18626", call)
        self.assertIn("-e e2eTrace true", call)
        self.assertTrue(call.endswith("%s/androidx.test.runner.AndroidJUnitRunner" % TEST_ID), call)

    def test_a_hung_run_is_stopped_on_the_device_when_the_harness_kills_it(self):
        self.box.write("instrument.hang", "")
        process = subprocess.Popen(["bash", self.box.script, "run-scenario", "auth-login-cancel", os.path.join(self.box.dir, "h"), "18626"],
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
        try:
            os.killpg(process.pid, signal.SIGKILL)  # the fake's sleep
        except ProcessLookupError:
            pass

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

    def test_collect_failure_keeps_bounded_logs_and_a_fallback_screenshot(self):
        attempt = os.path.join(self.box.dir, "c")
        os.makedirs(attempt)
        done = self.box.run("collect-failure", attempt, "1")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertTrue(os.path.exists(os.path.join(attempt, "fallback-screen.png")))
        self.assertIn("logcat -b all -d -v threadtime -t 4000", "\n".join(self.box.adb_calls()))

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
            handle.write('{"exception":{"type":"EXC_CRASH","signal":"SIGSEGV"}}')
        attempt = os.path.join(self.box.dir, "p")
        os.makedirs(attempt)
        done = self.box.run("crash-probe", str(int(time.time()) - 60), attempt)
        self.assertEqual(done.json["kind"], "app_crash")
        self.assertIn("SIGSEGV", support.text(os.path.join(attempt, "crash.txt")))
        self.assertEqual(self.box.run("crash-probe", str(int(time.time()) + 60), attempt).json["kind"], "none")

    def test_every_simulator_call_names_the_udid(self):
        attempt = os.path.join(self.box.dir, "s")
        os.makedirs(attempt)
        self.box.write("tests.list", "TuIndiceUITests/auth/test_auth_login_cancel()\n", self.box.xcrun)
        for verb in (("install",), ("enumerate",), ("health",), ("reset-app",), ("run-scenario", "auth-login-cancel", attempt, "18627"),
                ("collect-failure", attempt, "1")):
            self.assertEqual(self.box.run(*verb).returncode, 0, verb)
        simctl = [c for c in self.box.xcrun_calls() if c.startswith("simctl ")]
        unaddressed = [c for c in simctl if c.split(" ")[1] not in ("list", "create") and UDID not in c]
        self.assertEqual(unaddressed, [])
        for call in (c for c in self.box.xcrun_calls() if c.startswith("xcodebuild ")):
            self.assertIn("id=%s" % UDID, call)


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

    def test_a_native_crash_of_the_app_is_a_crash(self):
        log = self.write("1791346198.100  100  100 F libc    : Fatal signal 11 (SIGSEGV)\n"
                         "1791346198.200  100  100 F DEBUG   : pid: 100, tid: 100, name: x  >>> %s <<<\n" % APP_ID)
        evidence = os.path.join(os.path.dirname(log), "crash.txt")
        self.assertEqual(adapter_tools.logcat_crash(log, "1791346000", APP_ID, evidence)["kind"], "app_crash")

    def test_an_anr_of_the_app_is_an_app_anr(self):
        log = self.write("1791346198.100   689   771 E ActivityManager: ANR in %s (%s/.Main)\n" % (APP_ID, APP_ID))
        evidence = os.path.join(os.path.dirname(log), "crash.txt")
        self.assertEqual(adapter_tools.logcat_crash(log, "1791346000", APP_ID, evidence)["kind"], "app_anr")


if __name__ == "__main__":
    unittest.main()
