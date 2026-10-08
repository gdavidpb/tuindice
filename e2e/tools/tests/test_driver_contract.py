"""The driver contract as a gate of the evidence run (B-5): once per platform, after enumerate, before the first scenario."""

import unittest

from support import Workspace, scenario


def verbs(ws, platform="ios"):
    return [call[1] for call in ws.calls(platform=platform)]


def two():
    return [scenario("fix-a"), scenario("fix-b")]


class EvidenceGateTests(unittest.TestCase):
    def test_a_green_contract_runs_once_between_enumerate_and_the_first_scenario(self):
        ws = Workspace(self, two())
        result = ws.evidence()
        self.assertEqual(result.code, 0, result.out)
        sequence = verbs(ws)
        self.assertEqual(sequence.count("driver-contract"), 1)
        self.assertEqual(sequence[sequence.index("enumerate") + 1], "driver-contract")
        self.assertLess(sequence.index("driver-contract"), sequence.index("run-scenario"))
        record = ws.manifest()["driverContract"]
        self.assertEqual((record["ran"], record["ok"], record["failed"]), (True, True, []))
        self.assertEqual(record["passed"], ["launch", "present-element", "foreground"])
        phase = [p for p in ws.manifest()["phases"] if p["name"] == "driver-contract"]
        self.assertEqual([p["ok"] for p in phase], [True])

    def test_a_red_contract_is_exit_3_with_the_failed_probes_and_no_scenario_runs(self):
        ws = Workspace(self, two(), {"driverContract": "fail"})
        result = ws.evidence()
        self.assertEqual(result.code, 3, result.out)
        self.assertEqual(ws.executed(), [])
        self.assertIn("present-element", result.out)
        self.assertIn("foreground", result.out)
        manifest = ws.manifest()
        self.assertEqual(manifest["outcome"], "environment_refused")
        self.assertEqual(manifest["driverContract"]["failed"], ["present-element", "foreground"])
        self.assertEqual([p["ok"] for p in manifest["phases"] if p["name"] == "driver-contract"], [False])

    def test_a_verb_that_does_not_exist_is_never_taken_as_good(self):
        ws = Workspace(self, two(), {"driverContract": "missing"})
        result = ws.evidence()
        self.assertEqual(result.code, 3, result.out)
        self.assertEqual(ws.executed(), [])

    def test_an_answer_that_is_not_json_is_never_taken_as_good(self):
        ws = Workspace(self, two(), {"driverContract": "invalid-json"})
        result = ws.evidence()
        self.assertEqual(result.code, 3, result.out)
        self.assertEqual(ws.executed(), [])

    def test_an_answer_without_ok_is_never_taken_as_good(self):
        ws = Workspace(self, two(), {"driverContract": "no-ok"})
        self.assertEqual(ws.evidence().code, 3)
        self.assertEqual(ws.executed(), [])

    def test_a_contract_in_which_no_probe_ran_is_never_taken_as_good(self):
        ws = Workspace(self, two(), {"driverContract": "empty"})
        self.assertEqual(ws.evidence().code, 3)
        self.assertEqual(ws.executed(), [])

    def test_an_ok_answer_with_a_failing_exit_status_is_never_taken_as_good(self):
        ws = Workspace(self, two(), {"driverContract": "exit1"})
        self.assertEqual(ws.evidence().code, 3)
        self.assertEqual(ws.executed(), [])

    def test_each_platform_of_an_all_run_runs_its_own(self):
        ws = Workspace(self, two())
        result = ws.run("run", "--platform", "all", "--mode", "evidence", E2E_PARALLEL="never")
        self.assertEqual(result.code, 0, result.out)
        for platform in ("android", "ios"):
            self.assertEqual(verbs(ws, platform).count("driver-contract"), 1, platform)

    def test_nothing_pending_means_no_contract(self):
        ws = Workspace(self, two())
        self.assertEqual(ws.evidence().code, 0)
        self.assertEqual(ws.evidence().code, 0)
        self.assertEqual(len(ws.calls("driver-contract")), 1, "the second run had nothing to run and touched no device")


class DiagnoseTests(unittest.TestCase):
    def test_diagnose_does_not_run_it_by_default(self):
        ws = Workspace(self, two(), {"driverContract": "fail"})
        result = ws.diagnose()
        self.assertEqual(result.code, 0, result.out)
        self.assertEqual(ws.calls("driver-contract"), [])
        self.assertFalse(ws.manifest()["driverContract"]["ran"])

    def test_the_flag_runs_it_in_diagnose_and_a_red_one_stops_the_run(self):
        ws = Workspace(self, two(), {"driverContract": "fail"})
        result = ws.diagnose("ios", "--driver-contract")
        self.assertEqual(result.code, 3, result.out)
        self.assertEqual(ws.executed(), [])

    def test_the_flag_with_a_green_contract_goes_on(self):
        ws = Workspace(self, two())
        result = ws.diagnose("ios", "--driver-contract")
        self.assertEqual(result.code, 0, result.out)
        self.assertEqual(len(ws.calls("driver-contract")), 1)

    def test_the_flag_reaches_every_platform_of_an_all_run(self):
        ws = Workspace(self, two())
        result = ws.run("run", "--platform", "all", "--mode", "diagnose", "--driver-contract", E2E_PARALLEL="never")
        self.assertEqual(result.code, 0, result.out)
        self.assertEqual(len(ws.calls("driver-contract")), 2)


class DryRunTests(unittest.TestCase):
    def test_the_dry_run_says_it_would_run_the_contract_and_touches_no_device(self):
        ws = Workspace(self, two())
        result = ws.run("run", "--platform", "ios", "--mode", "evidence", "--dry-run")
        self.assertEqual(result.code, 0, result.out)
        self.assertIn("DRY-RUN would run the driver contract", result.out)
        self.assertEqual(ws.calls("driver-contract"), [])


if __name__ == "__main__":
    unittest.main()
