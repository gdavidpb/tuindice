"""A simulator that stops serving accessibility and preferences after hours of runs is the environment's fault, not the product's:
how an attempt is classified, how health says it, and how many recoveries a run allows for it."""

import unittest

from support import Workspace, scenario
from harness import classify as cl
from test_classify import evidence, result

AX_LINE = "2026-10-08 02:31:00.1 XCTest[1:2] Accessibility error kAXErrorAPIDisabled\n"
PREFS_READ_LINE = "cfprefsd: Couldn't read values in CFPrefsPlistSource<0x1> (Domain: com.apple.x, User: kCFPreferencesAnyUser)\n"
PREFS_WRITE_LINE = "cfprefsd: Couldn't write values for keys ( a ) in CFPrefsPlistSource<0x1>\n"
# What a degraded simulator writes (measured: about 1050 and 111 lines); a healthy one writes none of them.
AX = AX_LINE * 3
PREFS_READ = PREFS_READ_LINE * 20


class ClassificationTests(unittest.TestCase):
    def test_a_product_assertion_with_the_accessibility_marker_in_the_app_log_is_environment(self):
        verdict = cl.classify(evidence(result(), logs={"app.log": "line\n" + AX}))
        self.assertEqual(verdict.klass, cl.ENVIRONMENT)
        self.assertTrue(verdict.degraded)
        self.assertIn("the simulator stopped serving accessibility/preferences", verdict.summary)
        self.assertIn("kAXErrorAPIDisabled x3", verdict.summary)
        self.assertEqual(verdict.markers, {"kAXErrorAPIDisabled": 3, "Couldn't read values in CFPrefsPlistSource": 0})

    def test_a_single_line_of_either_marker_keeps_the_class_of_the_product(self):
        for text in (AX_LINE, PREFS_READ_LINE, AX_LINE * 2 + PREFS_READ_LINE * 19):
            verdict = cl.classify(evidence(result(), logs={"app.log": text}))
            self.assertEqual((verdict.klass, verdict.degraded), (cl.PRODUCT, False), text)

    def test_each_marker_at_its_threshold_and_each_log_counts(self):
        for name, text in (("app.log", PREFS_READ), ("runner.log", AX), ("logcat.txt", AX_LINE * 7)):
            verdict = cl.classify(evidence(result(), logs={name: text}))
            self.assertEqual((verdict.klass, verdict.degraded), (cl.ENVIRONMENT, True), (name, text))

    def test_the_lines_of_different_logs_are_never_added_up(self):
        # The same event can be written in two logs: two here and two there is still four lines of one kind but no log at 3.
        verdict = cl.classify(evidence(result(), logs={"app.log": AX_LINE * 2}, runner_log=AX_LINE * 2))
        self.assertEqual((verdict.klass, verdict.degraded), (cl.PRODUCT, False))
        verdict = cl.classify(evidence(result(), logs={"app.log": AX_LINE * 3}, runner_log=AX_LINE))
        self.assertEqual((verdict.klass, verdict.degraded), (cl.ENVIRONMENT, True))
        self.assertEqual(verdict.marker_files["app.log"]["kAXErrorAPIDisabled"], 3)
        self.assertEqual(verdict.marker_files["runner_output"]["kAXErrorAPIDisabled"], 1)

    def test_the_prefs_marker_edge_is_nineteen_and_twenty_in_one_log_whatever_the_other_has(self):
        split = cl.classify(evidence(result(), logs={"app.log": PREFS_READ_LINE * 19}, runner_log=PREFS_READ_LINE * 19))
        self.assertEqual((split.klass, split.degraded), (cl.PRODUCT, False))
        edge = cl.classify(evidence(result(), logs={"app.log": PREFS_READ_LINE * 20}, runner_log=PREFS_READ_LINE * 19))
        self.assertEqual((edge.klass, edge.degraded), (cl.ENVIRONMENT, True))

    def test_the_write_marker_is_not_a_sign_of_degradation_however_often_it_appears(self):
        verdict = cl.classify(evidence(result(), logs={"app.log": PREFS_WRITE_LINE * 500}))
        self.assertEqual((verdict.klass, verdict.degraded), (cl.PRODUCT, False))

    def test_android_logs_never_decide_a_degraded_simulator(self):
        verdict = cl.classify(evidence(result(), logs={"logcat.txt": AX_LINE * 500 + PREFS_READ_LINE * 500}, platform="android"))
        self.assertEqual((verdict.klass, verdict.degraded), (cl.PRODUCT, False))

    def test_a_request_without_a_stub_is_blamed_on_a_degraded_simulator_too(self):
        journal = [{"request": {"url": "/record/v5/terms", "headers": {}}, "wasMatched": False}]
        plain = cl.classify(evidence(result(), journal=journal))
        self.assertEqual((plain.klass, plain.degraded), (cl.BACKEND, False))
        verdict = cl.classify(evidence(result(), journal=journal, logs={"app.log": AX}))
        self.assertEqual((verdict.klass, verdict.degraded), (cl.ENVIRONMENT, True))

    def test_the_runner_log_of_the_attempt_counts_without_a_collected_log(self):
        verdict = cl.classify(evidence(result(), runner_log="a\n" + AX))
        self.assertEqual((verdict.klass, verdict.degraded), (cl.ENVIRONMENT, True))

    def test_timeout_and_tooling_failures_are_environment_too_but_a_crash_and_a_typed_mismatch_are_not(self):
        logs = {"app.log": AX}
        self.assertEqual(cl.classify(evidence(result(), killed_after=60, logs=logs)).klass, cl.ENVIRONMENT)
        self.assertEqual(cl.classify(evidence(None, native_ok=False, logs=logs)).klass, cl.ENVIRONMENT)
        self.assertEqual(cl.classify(evidence(result(), crash={"kind": "app_crash", "excerpt": "x"}, logs=logs)).klass, cl.CRASH)
        typed = cl.classify(evidence(result(kind="TYPED_TEXT_MISMATCH"), logs=logs))
        self.assertEqual(typed.klass, cl.TYPED)
        self.assertFalse(typed.degraded)

    def test_a_pass_is_never_reclassified_by_a_marker(self):
        verdict = cl.classify(evidence(result("passed"), logs={"app.log": AX}))
        self.assertTrue(verdict.passed)

    def test_without_a_marker_a_product_assertion_stays_one(self):
        verdict = cl.classify(evidence(result(), logs={"app.log": "nothing wrong\n"}))
        self.assertEqual((verdict.klass, verdict.degraded), (cl.PRODUCT, False))

    def test_a_failed_health_that_names_the_degradation_is_flagged(self):
        verdict = cl.classify(evidence(result(), pre_failure="health failed: simulator degraded: x", degraded_health=True))
        self.assertEqual((verdict.klass, verdict.degraded), (cl.ENVIRONMENT, True))


def numbered(count):
    return [scenario("fix-%02d" % i) for i in range(count)]


def degrade(then="pass"):
    return ["fail:assertion", then]


def with_log(kind="fail:assertion", text=AX):
    return {"do": kind, "applog": text}


class RunTests(unittest.TestCase):
    def test_a_degraded_attempt_is_environment_recovers_the_device_and_does_not_count(self):
        ws = Workspace(self, numbered(2), {"behaviours": {"fix-00": [with_log(), "pass"]}})
        run = ws.evidence()
        self.assertEqual(run.code, 0, run.out)
        self.assertEqual(len(ws.calls("recover")), 1)
        attempts = ws.ledger()["scenarios"]["fix-00"]["attempts"]
        self.assertEqual([a["failureClass"] for a in attempts], ["environment", None])
        self.assertIn("stopped serving accessibility/preferences", attempts[0]["failureSummary"])
        classification = ws.run_dirs()[-1] + "/scenarios/fix-00/attempt-1/classification.json"
        self.assertIn("environment", open(classification).read())
        events = ws.manifest()["deviceDegradation"]
        self.assertEqual(events["count"], 1)
        self.assertEqual(events["events"][0]["scenario"], "fix-00")
        self.assertEqual(events["events"][0]["scenariosSincePrevious"], 0)
        self.assertEqual(events["events"][0]["markers"]["kAXErrorAPIDisabled"], 3)
        self.assertEqual(events["events"][0]["markersByFile"]["app.log"]["kAXErrorAPIDisabled"], 3)

    def test_a_single_marker_line_is_a_failure_of_the_product_that_counts_and_recovers_nothing(self):
        ws = Workspace(self, numbered(2), {"behaviours": {"fix-00": [with_log(text=AX_LINE), "pass"]}})
        run = ws.evidence()
        self.assertEqual(run.code, 0, run.out)
        self.assertEqual(len(ws.calls("recover")), 0)
        self.assertEqual(ws.ledger()["scenarios"]["fix-00"]["attempts"][0]["failureClass"], "product_assertion")

    def test_a_degraded_survey_recovers_the_simulator_and_does_not_rerun_the_scenario(self):
        ws = Workspace(self, numbered(3), {"behaviours": {"fix-00": [with_log(), "pass"]}})
        run = ws.diagnose("ios", "--survey")
        self.assertEqual(len(ws.calls("recover")), 1, run.out)
        self.assertEqual(ws.executed(), ["fix-00", "fix-01", "fix-02"], "the degraded scenario is not repeated")
        self.assertIn("the simulator degraded; recovering it; this attempt does not count", run.out)
        self.assertEqual(ws.manifest()["deviceDegradation"]["count"], 1)

    def test_the_second_degradation_of_the_same_scenario_says_so_and_stops_the_run(self):
        ws = Workspace(self, numbered(2), {"behaviours": {"fix-00": [with_log(), with_log(), "pass"]}})
        run = ws.evidence()
        self.assertEqual(run.code, 5, run.out)
        self.assertIn("the simulator degraded twice on this scenario", run.out)
        self.assertEqual(len(ws.calls("recover")), 1)

    def test_a_degraded_attempt_after_another_kind_of_environment_failure_keeps_the_generic_message(self):
        ws = Workspace(self, numbered(2), {"behaviours": {"fix-00": ["env", with_log(), "pass"]}})
        run = ws.evidence()
        self.assertEqual(run.code, 5, run.out)
        self.assertIn("failed with class environment twice", run.out)

    def test_a_degradation_that_ensure_recovered_enters_the_record(self):
        ws = Workspace(self, numbered(2), {"ensureRecovered": True})
        run = ws.evidence()
        self.assertEqual(run.code, 0, run.out)
        events = ws.manifest()["deviceDegradation"]
        self.assertEqual((events["count"], events["events"][0]["scenario"]), (1, None))
        self.assertIn("did not accept its settings", events["events"][0]["summary"])

    def test_a_failed_health_that_says_degraded_takes_the_same_path(self):
        ws = Workspace(self, numbered(2), {"behaviours": {"fix-00": ["degraded-health", "pass"]}})
        run = ws.evidence()
        self.assertEqual(run.code, 0, run.out)
        self.assertEqual(len(ws.calls("recover")), 1)
        self.assertEqual(ws.manifest()["deviceDegradation"]["count"], 1)

    def test_two_degradations_in_a_row_are_exit_3(self):
        behaviours = {"fix-00": [with_log(), "pass"], "fix-05": [with_log(), "pass"]}
        ws = Workspace(self, numbered(8), {"behaviours": behaviours})
        run = ws.evidence()
        self.assertEqual(run.code, 3, run.out)
        self.assertEqual(len(ws.calls("recover")), 1, "the second degradation does not get a recovery")
        self.assertIn("degraded again after 5 green scenarios", run.out)
        manifest = ws.manifest()
        self.assertEqual(manifest["outcome"], "environment_refused")
        self.assertEqual(manifest["deviceDegradation"]["count"], 1)

    def test_a_second_degradation_after_thirty_green_scenarios_gets_another_recovery(self):
        behaviours = {"fix-00": [with_log(), "pass"], "fix-32": [with_log(), "pass"]}
        ws = Workspace(self, numbered(34), {"behaviours": behaviours})
        run = ws.evidence()
        self.assertEqual(run.code, 0, run.out)
        self.assertEqual(len(ws.calls("recover")), 2)
        events = ws.manifest()["deviceDegradation"]
        self.assertEqual(events["count"], 2)
        self.assertEqual([e["scenario"] for e in events["events"]], ["fix-00", "fix-32"])
        self.assertEqual(events["events"][1]["scenariosSincePrevious"], 32, "fix-00 passed after its recovery, then 31 more")
        self.assertIn("at", events["events"][1])

    def test_a_survey_is_cut_by_a_second_degradation_too_for_the_environment_not_for_a_failed_scenario(self):
        behaviours = {"fix-00": [with_log(), "pass"], "fix-05": [with_log(), "pass"], "fix-02": ["fail:assertion"]}
        ws = Workspace(self, numbered(8), {"behaviours": behaviours})
        run = ws.diagnose("ios", "--survey")
        self.assertEqual(run.code, 3, run.out)
        self.assertIn("degraded again after", run.out)
        self.assertIn("FAIL  fix-02", run.out, "a scenario that fails does not cut a survey, the second degradation does")
        self.assertEqual(len(ws.calls("recover")), 1)

    def test_the_boundary_is_thirty_greens_exactly(self):
        # fix-00 degrades and then passes (1 green), fix-01..fix-29 pass: 30 greens since the recovery when fix-30 degrades.
        ws = Workspace(self, numbered(32), {"behaviours": {"fix-00": [with_log(), "pass"], "fix-30": [with_log(), "pass"]}})
        self.assertEqual(ws.evidence().code, 0)
        short = Workspace(self, numbered(32), {"behaviours": {"fix-00": [with_log(), "pass"], "fix-29": [with_log(), "pass"]}})
        self.assertEqual(short.evidence().code, 3)

    def test_other_environment_failures_keep_the_one_recovery_rule(self):
        ws = Workspace(self, numbered(3), {"behaviours": {"fix-00": ["env", "pass"], "fix-01": ["env"]}})
        run = ws.evidence()
        self.assertEqual(run.code, 3, run.out)
        self.assertEqual(len(ws.calls("recover")), 1)
        self.assertEqual(ws.manifest()["deviceDegradation"]["count"], 0)

    def test_a_degradation_does_not_spend_the_recovery_of_other_environment_failures(self):
        ws = Workspace(self, numbered(3), {"behaviours": {"fix-00": [with_log(), "pass"], "fix-01": ["env", "pass"]}})
        run = ws.evidence()
        self.assertEqual(run.code, 0, run.out)
        self.assertEqual(len(ws.calls("recover")), 2)

    def test_a_failed_recover_stops_the_run(self):
        ws = Workspace(self, numbered(2), {"behaviours": {"fix-00": [with_log(), "pass"]}, "recover_fails": True})
        self.assertEqual(ws.evidence().code, 3)


if __name__ == "__main__":
    unittest.main()
