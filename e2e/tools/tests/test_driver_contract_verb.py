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

    def test_a_probe_that_was_skipped_is_red_and_only_the_series_may_be(self):
        self.put()
        self.box.write("probes.mode", "skip-probe\n")
        done = self.box.run("driver-contract", self.dir, "18626")
        self.assertFalse(done.json["ok"])
        self.assertTrue(any("AndroidDriverProbesTest#waitGoneIsFalseWhenTheAppIsNotThereToBeRead: did not pass (skipped)" in f
            for f in done.json["failed"]), done.json)
        self.assertIn("AndroidTypingProbesTest#typingSeries", done.json["skipped"])

    def test_a_run_that_ends_without_the_instrumentation_code_is_red(self):
        self.put()
        self.box.write("probes.mode", "no-end\n")
        done = self.box.run("driver-contract", self.dir, "18626")
        self.assertFalse(done.json["ok"])
        self.assertTrue(any("INSTRUMENTATION_CODE: -1" in f for f in done.json["failed"]), done.json)

    def test_a_test_that_closes_with_a_status_the_reader_does_not_know_is_red_not_dropped(self):
        self.put()
        self.box.write("probes.mode", "odd-code\n")
        done = self.box.run("driver-contract", self.dir, "18626")
        self.assertFalse(done.json["ok"])
        self.assertTrue(any("AndroidDriverProbesTest#waitGoneIsFalseWhenTheAppIsNotThereToBeRead" in f and "status code 2" in f
            for f in done.json["failed"]), done.json)

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

    def xcode(self, *cases, executed=None):
        lines = ["Test Case '-[TuIndiceUITests.%s %s]' %s (0.1 seconds)." % case for case in cases]
        lines.append("\t Executed %d tests, with 0 failures (0 unexpected) in 1.0 (1.0) seconds" % (len(cases) if executed is None else executed))
        return self.write("log", "\n".join(lines) + "\n")

    CLASSES = "DriverContractTests,SettleWatchTests,ObjCCatchTests,GuardedIncidentTests"
    GREEN_CASES = (("DriverContractTests", "test_driver_contract", "passed"), ("SettleWatchTests", "test_a", "passed"),
        ("ObjCCatchTests", "test_b", "passed"), ("GuardedIncidentTests", "test_c", "passed"))

    def test_the_ios_contract_run_with_its_probe_classes_is_green_when_every_class_passes(self):
        result = self.write("result.json", json.dumps(CONTRACT_RESULT))
        answer = adapter_tools.xctest_contract(self.xcode(*self.GREEN_CASES), "0", result, None, self.CLASSES)
        self.assertTrue(answer["ok"], answer)

    def test_a_probe_class_that_did_not_run_or_failed_is_red(self):
        result = self.write("result.json", json.dumps(CONTRACT_RESULT))
        missing = adapter_tools.xctest_contract(self.xcode(*self.GREEN_CASES[:3]), "0", result, None, self.CLASSES)
        self.assertIn("probes:GuardedIncidentTests: no test of the class passed", missing["failed"])
        broken = self.GREEN_CASES[:3] + (("GuardedIncidentTests", "test_c", "failed"),)
        red = adapter_tools.xctest_contract(self.xcode(*broken), "1", result, None, self.CLASSES)
        self.assertFalse(red["ok"])
        self.assertIn("probes:GuardedIncidentTests#test_c failed", red["failed"])

    def stream(self, *tests, end="INSTRUMENTATION_CODE: -1\n"):
        """An `am instrument -r` stream: (class, test, closing code or None for a test that starts and never closes)."""
        text = ""
        for klass, test, code in tests:
            text += "INSTRUMENTATION_STATUS: class=%s.%s\nINSTRUMENTATION_STATUS: test=%s\nINSTRUMENTATION_STATUS_CODE: 1\n" % (PACKAGE, klass, test)
            if code is not None:
                text += "INSTRUMENTATION_STATUS: class=%s.%s\nINSTRUMENTATION_STATUS: test=%s\nINSTRUMENTATION_STATUS_CODE: %d\n" % (
                    PACKAGE, klass, test, code)
        return self.write("log", text + end)

    def probes(self, log, required="DriverContractTest#driverHonoursTheContract"):
        return adapter_tools.instrument_probes(log, required, self.write("result.json", json.dumps(CONTRACT_RESULT)))

    def test_every_test_of_the_run_is_required_except_the_listed_measurements(self):
        green = self.probes(self.stream(("DriverContractTest", "driverHonoursTheContract", 0), ("AndroidDriverProbesTest", "a", 0),
            ("AndroidTypingProbesTest", "typingSeries", -4)))
        self.assertTrue(green["ok"], green)
        self.assertEqual(green["skipped"], ["AndroidTypingProbesTest#typingSeries"])
        for code in (-3, -4):
            red = self.probes(self.stream(("DriverContractTest", "driverHonoursTheContract", 0), ("AndroidDriverProbesTest", "a", code)))
            self.assertFalse(red["ok"])
            self.assertIn("AndroidDriverProbesTest#a: did not pass (skipped)", red["failed"])

    def test_a_test_that_starts_and_never_closes_is_red(self):
        red = self.probes(self.stream(("DriverContractTest", "driverHonoursTheContract", 0), ("AndroidDriverProbesTest", "a", None)))
        self.assertFalse(red["ok"])
        self.assertIn("AndroidDriverProbesTest#a: did not finish", red["failed"])

    def test_a_status_code_outside_the_known_ones_is_red(self):
        red = self.probes(self.stream(("DriverContractTest", "driverHonoursTheContract", 0), ("AndroidDriverProbesTest", "a", 7)))
        self.assertFalse(red["ok"])
        self.assertIn("AndroidDriverProbesTest#a: closed with the status code 7, which this reader does not know", red["failed"])

    def test_a_stream_without_the_closing_instrumentation_code_is_red_however_green_its_tests(self):
        red = self.probes(self.stream(("DriverContractTest", "driverHonoursTheContract", 0), end=""))
        self.assertFalse(red["ok"])
        self.assertIn("instrumentation: the run did not end with INSTRUMENTATION_CODE: -1", red["failed"])

    def test_a_class_that_never_ran_is_red_when_the_run_requires_it(self):
        red = self.probes(self.stream(("DriverContractTest", "driverHonoursTheContract", 0)),
            "DriverContractTest#driverHonoursTheContract,AndroidDriverProbesTest")
        self.assertFalse(red["ok"])
        self.assertIn("AndroidDriverProbesTest: no test of the class passed", red["failed"])

    def test_a_green_stream_with_a_failed_required_name_missing_from_it_is_red(self):
        log = self.write("log", "INSTRUMENTATION_CODE: -1\n")
        result = self.write("result.json", json.dumps(CONTRACT_RESULT))
        answer = adapter_tools.instrument_probes(log, "DriverContractTest#driverHonoursTheContract", result)
        self.assertFalse(answer["ok"])
        self.assertIn("(not run)", " ".join(answer["failed"]))


if __name__ == "__main__":
    unittest.main()
