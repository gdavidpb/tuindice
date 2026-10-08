"""What the drivers tolerate (B-7): counted from driver.log per attempt, per scenario and per run, and shown in the manifest and the summary."""

import json
import os
import unittest

import support
from support import TESTS, Workspace, scenario, text
from harness import proc, tolerances

LOGS = os.path.join(TESTS, "fixtures", "driver-logs")


def fixture(name):
    return text(os.path.join(LOGS, name))


class CountTests(unittest.TestCase):
    def test_ios_reads_the_final_summary_line_and_accepts_keys_it_does_not_know(self):
        self.assertEqual(tolerances.count(fixture("ios.log")), {"dismissed-alert": 2, "future-kind": 1})

    def test_ios_zeros_are_not_tolerances(self):
        self.assertEqual(tolerances.count(fixture("ios-quiet.log")), {})

    def test_ios_without_the_summary_counts_the_lines_so_a_killed_run_still_shows_them(self):
        self.assertEqual(tolerances.count(fixture("ios-unfinished.log")), {"dismissed-alert": 1, "future-kind": 2})

    def test_android_counts_the_requests_to_bring_the_app_back_and_the_failure_to_do_it(self):
        self.assertEqual(tolerances.count(fixture("android.log")), {"foreground-request": 2, "foreground-not-in-front": 1})

    def test_a_refusal_is_not_a_tolerance_and_both_platforms_count_the_same_three_phrases(self):
        self.assertEqual(tolerances.refusals(fixture("android.log")), {"gesture-refused": 2, "touch-refused": 1})
        # iOS, whose log ends with a summary that does not mention them, and its keyboard guard ("tap refused").
        self.assertEqual(tolerances.refusals(fixture("ios-refusal.log")), {"gesture-refused": 1, "tap-refused": 1})
        self.assertEqual(tolerances.count(fixture("ios-refusal.log")), {"dismissed-alert": 1})
        self.assertEqual(tolerances.refusals(fixture("ios-unfinished.log")), {})
        self.assertEqual(tolerances.refusals(""), {})

    def test_android_without_a_tolerance_is_empty_and_so_is_nothing(self):
        self.assertEqual(tolerances.count(fixture("android-quiet.log")), {})
        self.assertEqual(tolerances.count(""), {})

    def test_only_the_end_of_a_big_file_is_read(self):
        path = os.path.join(support.tempfile.mkdtemp(prefix="e2e-tail-"), "log")
        self.addCleanup(support.shutil.rmtree, os.path.dirname(path), True)
        with open(path, "w") as handle:
            handle.write("0123456789" * 10)
        self.assertEqual(proc.tail_text(path, 15), "56789" + "0123456789")
        self.assertEqual(proc.tail_text(path, 1000), "0123456789" * 10)
        self.assertEqual(proc.tail_text("/nonexistent/log", 15), "")

    def test_the_sum_of_counts(self):
        self.assertEqual(tolerances.merge({"a": 1}, {"a": 2, "b": 1}, {}), {"a": 3, "b": 1})

    def test_a_file_that_does_not_exist_is_empty(self):
        self.assertEqual(tolerances.read("/nonexistent/driver.log"), ({}, {}))
        self.assertEqual(tolerances.read(os.path.join(LOGS, "ios-refusal.log")),
            ({"dismissed-alert": 1}, {"gesture-refused": 1, "tap-refused": 1}))


class RunTests(unittest.TestCase):
    def test_the_attempt_the_scenario_and_the_run_carry_the_counts(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-a": [{"do": "pass", "driverlog": "ios.log"}]}})
        result = ws.evidence()
        self.assertEqual(result.code, 0, result.out)
        expected = {"dismissed-alert": 2, "future-kind": 1}
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

    def test_only_an_attempt_that_passed_tolerated_anything_while_every_attempt_counts_its_refusals(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": [
            {"do": "fail:assertion", "driverlog": "ios-refusal.log"}, {"do": "pass", "driverlog": "ios-refusal.log"}]}})
        result = ws.evidence()
        self.assertEqual(result.code, 0, result.out)
        manifest = ws.manifest()
        self.assertEqual(manifest["tolerances"], {"dismissed-alert": 1}, "what a failed attempt put up with is not a tolerance")
        self.assertEqual([a["tolerances"] for a in manifest["attempts"]], [{}, {"dismissed-alert": 1}])
        self.assertEqual(manifest["refusals"], {"gesture-refused": 2, "tap-refused": 2})
        self.assertEqual([a["refusals"] for a in manifest["attempts"]], [{"gesture-refused": 1, "tap-refused": 1}] * 2)
        self.assertEqual(ws.ledger()["scenarios"]["fix-a"]["attempts"][0]["refusals"], {"gesture-refused": 1, "tap-refused": 1})
        self.assertIn("refusals: %s" % json.dumps(manifest["refusals"], sort_keys=True), text(os.path.join(ws.run_dirs()[-1], "summary.txt")))

    def test_the_log_says_the_tolerances_of_the_run_when_there_are_any(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": [{"do": "pass", "driverlog": "ios.log"}]}})
        result = ws.evidence()
        self.assertIn("TOLERANCES dismissed-alert=2 future-kind=1", result.out)
        quiet = Workspace(self, [scenario("fix-a")])
        self.assertNotIn("TOLERANCES", quiet.evidence().out)

    def test_android_logs_are_read_the_same_way(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": [{"do": "pass", "driverlog": "android.log"}]}})
        self.assertEqual(ws.evidence("android").code, 0)
        manifest = ws.manifest()
        self.assertEqual(manifest["tolerances"], {"foreground-request": 2, "foreground-not-in-front": 1})
        self.assertEqual(manifest["refusals"], {"gesture-refused": 2, "touch-refused": 1})


if __name__ == "__main__":
    unittest.main()
