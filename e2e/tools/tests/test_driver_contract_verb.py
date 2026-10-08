"""The `driver-contract` verb of both adapters against the fake adb and xcodebuild, and the readers behind it (B-5)."""

import json
import os
import tempfile
import unittest

import support
from test_adapters import APP_ID, TEST_ID, UDID, Adapter

import adapter_tools

PACKAGE = "com.gdavidpb.tuindice.scenariorunner"
CONTRACT_RESULT = {"scenarioId": "driver-contract", "outcome": "passed", "failure": None, "steps": [
    {"index": 0, "primitive": "launch", "outcome": "passed"}, {"index": 1, "primitive": "foreground", "outcome": "passed"}]}


def red(result):
    steps = [dict(s) for s in result["steps"]]
    steps[1]["outcome"] = "failed"
    return dict(result, outcome="failed", steps=steps)


class AndroidVerbTests(unittest.TestCase):
    def setUp(self):
        self.box = Adapter(self, "android")
        self.dir = os.path.join(self.box.dir, "contract")

    def put(self, result=CONTRACT_RESULT):
        self.box.write("contract-files/driver-contract/result.json", json.dumps(result))
        self.box.write("contract-files/driver-contract/driver.log", "10:00:00.000 launch\n")

    def test_it_runs_the_three_classes_in_one_instrumentation_on_a_cleared_app_without_a_scenario_filter(self):
        self.put()
        self.box.write("probes.mode", "pass\n")
        done = self.box.run("driver-contract", self.dir, "18626")
        self.assertEqual(done.returncode, 0, done.stderr)
        calls = self.box.adb_calls()
        clear = [i for i, c in enumerate(calls) if "pm clear %s" % APP_ID in c]
        instrument = [i for i, c in enumerate(calls) if "am instrument" in c]
        self.assertEqual((len(clear), len(instrument)), (1, 1))
        self.assertLess(clear[0], instrument[0])
        call = calls[instrument[0]]
        for expected in ("%s.DriverContractTest" % PACKAGE, "%s.AndroidDriverProbesTest" % PACKAGE,
                "%s.AndroidTypingProbesTest" % PACKAGE, "-e wiremockUrl http://10.0.2.2:18626"):
            self.assertIn(expected, call)
        self.assertNotIn("-e scenario", call)
        self.assertNotIn("typingSeries", call, "the long series is a measurement, not a gate")

    def test_green_lists_the_probes_and_skips_the_series(self):
        self.put()
        self.box.write("probes.mode", "pass\n")
        done = self.box.run("driver-contract", self.dir, "18626")
        self.assertTrue(done.json["ok"], done.json)
        self.assertEqual(done.json["failed"], [])
        self.assertIn("DriverContractTest#driverHonoursTheContract", done.json["passed"])
        self.assertIn("contract:launch", done.json["passed"])
        self.assertEqual(done.json["skipped"], ["AndroidTypingProbesTest#typingSeries"])
        self.assertEqual(done.json["artifacts"], self.dir)
        self.assertEqual(support.text(os.path.join(self.dir, "driver-contract", "driver.log")), "10:00:00.000 launch\n")

    def test_a_red_probe_and_a_red_contract_step_are_named(self):
        self.put(red(CONTRACT_RESULT))
        self.box.write("probes.mode", "fail\n")
        done = self.box.run("driver-contract", self.dir, "18626")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertFalse(done.json["ok"])
        self.assertIn("AndroidDriverProbesTest#waitGoneIsFalseWhenTheAppIsNotThereToBeRead", done.json["failed"])
        self.assertIn("contract:foreground", done.json["failed"])

    def test_a_contract_that_was_skipped_is_not_a_green_one(self):
        self.put()
        self.box.write("probes.mode", "skip-contract\n")
        done = self.box.run("driver-contract", self.dir, "18626")
        self.assertFalse(done.json["ok"])
        self.assertTrue(any("DriverContractTest#driverHonoursTheContract: did not pass (skipped)" in f for f in done.json["failed"]), done.json)

    def test_a_process_that_died_is_red(self):
        self.put()
        self.box.write("probes.mode", "crash\n")
        done = self.box.run("driver-contract", self.dir, "18626")
        self.assertFalse(done.json["ok"])
        self.assertTrue(any("crashed" in f for f in done.json["failed"]), done.json)

    def test_a_run_that_leaves_no_result_json_is_red(self):
        self.box.write("probes.mode", "pass\n")
        done = self.box.run("driver-contract", self.dir, "18626")
        self.assertFalse(done.json["ok"])
        self.assertTrue(any("result.json" in f for f in done.json["failed"]), done.json)


class IosVerbTests(unittest.TestCase):
    def setUp(self):
        self.box = Adapter(self, "ios")
        self.dir = os.path.join(self.box.dir, "contract")

    def put(self, result=CONTRACT_RESULT, rc=0):
        self.box.write("xc.id", "driver-contract", self.box.xcrun)
        self.box.write("xc.result", json.dumps(result), self.box.xcrun)
        self.box.write("xc.rc", "%d\n" % rc, self.box.xcrun)

    def test_it_resets_the_app_then_runs_only_the_contract_test_with_the_wiremock_url(self):
        self.put()
        done = self.box.run("driver-contract", self.dir, "18627")
        self.assertEqual(done.returncode, 0, done.stderr)
        calls = self.box.xcrun_calls()
        words = [c.split(" ")[1] for c in calls if c.startswith("simctl")]
        self.assertEqual([w for w in words if w in ("terminate", "uninstall", "keychain", "install")], ["terminate", "uninstall", "keychain", "install"])
        xcodebuild = [c for c in calls if c.startswith("xcodebuild ") and "test-without-building" in c]
        self.assertEqual(len(xcodebuild), 1)
        self.assertIn("-only-testing:TuIndiceUITests/DriverContractTests/test_driver_contract", xcodebuild[0])
        self.assertIn("env TEST_RUNNER_E2E_WIREMOCK_URL=http://localhost:18627", "\n".join(calls))
        self.assertEqual(done.json["ok"], True, done.json)
        self.assertEqual(done.json["passed"], ["launch", "foreground"])
        self.assertEqual(done.json["artifacts"], self.dir)
        self.assertTrue(os.path.exists(os.path.join(self.dir, "result.json")))

    def test_a_red_step_is_named(self):
        self.put(red(CONTRACT_RESULT), rc=65)
        done = self.box.run("driver-contract", self.dir, "18627")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertFalse(done.json["ok"])
        self.assertEqual(done.json["failed"], ["foreground"])

    def test_a_test_that_wrote_nothing_is_red_not_green(self):
        done = self.box.run("driver-contract", self.dir, "18627")
        self.assertFalse(done.json["ok"])
        self.assertTrue(any("result.json" in f for f in done.json["failed"]), done.json)


class ReaderTests(unittest.TestCase):
    def write(self, name, text):
        directory = tempfile.mkdtemp(prefix="e2e-probe-reader-")
        self.addCleanup(support.shutil.rmtree, directory, True)
        path = os.path.join(directory, name)
        with open(path, "w") as handle:
            handle.write(text)
        return path

    def test_an_xcodebuild_that_ran_two_tests_is_not_the_one_contract_test(self):
        log = self.write("log", "\t Executed 2 tests, with 0 failures (0 unexpected) in 1.0 (1.0) seconds\n")
        result = self.write("result.json", json.dumps(CONTRACT_RESULT))
        answer = adapter_tools.xctest_contract(log, "0", result)
        self.assertFalse(answer["ok"])
        self.assertIn("2 tests executed", " ".join(answer["failed"]))

    def test_a_green_stream_with_a_failed_required_name_missing_from_it_is_red(self):
        log = self.write("log", "INSTRUMENTATION_CODE: -1\n")
        result = self.write("result.json", json.dumps(CONTRACT_RESULT))
        answer = adapter_tools.instrument_probes(log, "DriverContractTest#driverHonoursTheContract", result)
        self.assertFalse(answer["ok"])
        self.assertIn("(not run)", " ".join(answer["failed"]))


if __name__ == "__main__":
    unittest.main()
