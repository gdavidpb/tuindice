"""What the drivers tolerate (B-7): counted from driver.log per attempt, per scenario and per run, and shown in the manifest and the summary."""

import json
import os
import unittest

from support import TESTS, Workspace, scenario, text
from harness import tolerances

LOGS = os.path.join(TESTS, "fixtures", "driver-logs")


def fixture(name):
    return text(os.path.join(LOGS, name))


class CountTests(unittest.TestCase):
    def test_ios_reads_the_final_summary_line(self):
        self.assertEqual(tolerances.count(fixture("ios.log")), {"dismissed-alert": 2, "allowed-paste": 1})

    def test_ios_zeros_are_not_tolerances(self):
        self.assertEqual(tolerances.count(fixture("ios-quiet.log")), {})

    def test_ios_without_the_summary_counts_the_lines_so_a_killed_run_still_shows_them(self):
        self.assertEqual(tolerances.count(fixture("ios-unfinished.log")), {"dismissed-alert": 1, "allowed-paste": 2})

    def test_android_counts_the_requests_to_bring_the_app_back_the_failure_and_the_refused_gestures(self):
        self.assertEqual(tolerances.count(fixture("android.log")),
            {"foreground-request": 2, "foreground-not-in-front": 1, "gesture-refused": 3})

    def test_android_without_a_tolerance_is_empty_and_so_is_nothing(self):
        self.assertEqual(tolerances.count(fixture("android-quiet.log")), {})
        self.assertEqual(tolerances.count(""), {})

    def test_the_sum_of_counts(self):
        self.assertEqual(tolerances.merge({"a": 1}, {"a": 2, "b": 1}, {}), {"a": 3, "b": 1})

    def test_a_file_that_does_not_exist_is_empty(self):
        self.assertEqual(tolerances.count_file("/nonexistent/driver.log"), {})


class RunTests(unittest.TestCase):
    def test_the_attempt_the_scenario_and_the_run_carry_the_counts(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-a": [{"do": "pass", "driverlog": "ios.log"}]}})
        result = ws.evidence()
        self.assertEqual(result.code, 0, result.out)
        expected = {"dismissed-alert": 2, "allowed-paste": 1}
        manifest = ws.manifest()
        self.assertEqual(manifest["attempts"][0]["tolerances"], expected)
        self.assertEqual(manifest["attempts"][1]["tolerances"], {}, "no driver.log: an empty object, not an absent key")
        self.assertEqual({r["id"]: r["tolerances"] for r in manifest["results"]}, {"fix-a": expected, "fix-b": {}})
        self.assertEqual(manifest["tolerances"], expected)
        self.assertEqual(ws.ledger()["scenarios"]["fix-a"]["attempts"][0]["tolerances"], expected)
        summary = text(os.path.join(ws.run_dirs()[-1], "summary.txt"))
        self.assertIn("tolerances: %s" % json.dumps(expected, sort_keys=True), summary)

    def test_a_run_without_tolerances_says_so_with_an_empty_object(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence().code, 0)
        self.assertEqual(ws.manifest()["tolerances"], {})
        self.assertIn("tolerances: {}", text(os.path.join(ws.run_dirs()[-1], "summary.txt")))

    def test_the_run_sums_every_attempt_a_failed_one_included(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": [
            {"do": "fail:assertion", "driverlog": "ios.log"}, {"do": "pass", "driverlog": "ios.log"}]}})
        self.assertEqual(ws.evidence().code, 0)
        manifest = ws.manifest()
        self.assertEqual(manifest["tolerances"], {"dismissed-alert": 4, "allowed-paste": 2})
        self.assertEqual([a["tolerances"] for a in manifest["attempts"]], [{"dismissed-alert": 2, "allowed-paste": 1}] * 2)

    def test_android_logs_are_read_the_same_way(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": [{"do": "pass", "driverlog": "android.log"}]}})
        self.assertEqual(ws.evidence("android").code, 0)
        self.assertEqual(ws.manifest()["tolerances"], {"foreground-request": 2, "foreground-not-in-front": 1, "gesture-refused": 3})


if __name__ == "__main__":
    unittest.main()
