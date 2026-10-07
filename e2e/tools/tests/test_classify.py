"""Failure classification: unit tests on the evidence, plus the audit case end to end."""

import base64
import json
import os
import unittest

import support
from support import ACCOUNTS, Workspace, scenario, text
from harness import classify as cl
from harness.catalog import Scenario

RETRY = ACCOUNTS[1]
CANONICAL = ACCOUNTS[0]


def fixture_journal(name):
    with open(os.path.join(support.TESTS, "fixtures", "journals", name + ".json")) as handle:
        return json.load(handle)["requests"]


def result(outcome="failed", kind="ASSERTION", step=3, scenario_id="fix-a", message="not visible after 5000ms"):
    failure = None if outcome == "passed" else {
        "kind": kind, "stepIndex": step, "primitive": "WaitVisible", "target": "tag:x", "message": message,
        "expected": "123456", "actual": "12456"}
    return {"scenarioId": scenario_id, "outcome": outcome, "steps": [
        {"index": 0, "primitive": "Tap", "target": "tag:a", "outcome": "passed"}], "failure": failure}


def evidence(res=None, native_ok=None, tests=1, account=None, **fields):
    ev = cl.Evidence(Scenario(scenario("fix-a")), account)
    ev.result = res
    ev.native_ok = (res or {}).get("outcome") == "passed" if native_ok is None else native_ok
    ev.tests_executed = tests
    for name, value in fields.items():
        setattr(ev, name, value)
    return ev


class RuleOrderTests(unittest.TestCase):
    def test_a_pass_needs_result_runner_and_one_test(self):
        self.assertTrue(cl.classify(evidence(result("passed"))).passed)
        self.assertEqual(cl.classify(evidence(result("passed"), tests=0)).klass, cl.TOOLING)
        self.assertEqual(cl.classify(evidence(result("passed"), native_ok=False)).klass, cl.TOOLING)
        self.assertEqual(cl.classify(evidence(result("failed"), native_ok=True)).klass, cl.TOOLING)
        self.assertEqual(cl.classify(evidence(result("passed", scenario_id="other"))).klass, cl.TOOLING)

    def test_missing_or_unparsable_result_is_tooling_error(self):
        verdict = cl.classify(evidence(None, native_ok=False, runner_log="a\nerror: Testing failed: boom\nz\n"))
        self.assertEqual((verdict.klass, verdict.summary), (cl.TOOLING, "error: Testing failed: boom"))
        broken = cl.classify(evidence(None, result_error="result.json is not valid JSON: x"))
        self.assertIn("not valid JSON", broken.summary)

    def test_a_previous_step_failure_wins_over_everything(self):
        ev = evidence(result(), pre_failure="reset-app failed", crash={"kind": "app_crash", "excerpt": "boom"})
        self.assertEqual(cl.classify(ev).klass, cl.ENVIRONMENT)

    def test_crash_and_anr_evidence(self):
        self.assertEqual(cl.classify(evidence(result(), crash={"kind": "app_crash", "excerpt": "x"})).klass, cl.CRASH)
        self.assertEqual(cl.classify(evidence(result(), crash={"kind": "app_anr", "excerpt": "x"})).klass, cl.CRASH)
        self.assertEqual(cl.classify(evidence(result(), crash={"kind": "system_anr", "excerpt": "x"})).klass, cl.ENVIRONMENT)

    def test_killed_runner_is_a_timeout_that_names_the_last_step(self):
        verdict = cl.classify(evidence(result(), killed_after=180))
        self.assertEqual((verdict.klass, verdict.summary),
            (cl.TIMEOUT, "scenario exceeded 180s; last completed step 0 Tap(tag:a)"))

    def test_result_kinds(self):
        self.assertEqual(cl.classify(evidence(result(kind="SYSTEM_DIALOG"))).klass, cl.ENVIRONMENT)
        self.assertEqual(cl.classify(evidence(result(kind="BACKEND_UNAVAILABLE"))).klass, cl.ENVIRONMENT)
        self.assertEqual(cl.classify(evidence(result(kind="DRIVER_ERROR"))).klass, cl.TOOLING)
        self.assertEqual(cl.classify(evidence(result(kind="APP_NOT_RUNNING", step=-1))).klass, cl.ENVIRONMENT)
        self.assertEqual(cl.classify(evidence(result(kind="APP_NOT_RUNNING", step=4))).klass, cl.CRASH)
        typed = cl.classify(evidence(result(kind="TYPED_TEXT_MISMATCH")))
        self.assertEqual(typed.klass, cl.TYPED)
        self.assertIn("typed '123456' but the field held '12456'", typed.summary)

    def test_the_summary_is_taken_from_result_json(self):
        verdict = cl.classify(evidence(result(message="snackbar not shown"), runner_log="last log line\n"))
        self.assertEqual((verdict.klass, verdict.summary), (cl.PRODUCT, "step 3 WaitVisible(tag:x): snackbar not shown"))


class JournalTests(unittest.TestCase):
    def test_the_audit_case_recordretry_ass_instead_of_the_password(self):
        verdict = cl.classify(evidence(result(), account=RETRY, journal=fixture_journal("typed-mismatch")))
        self.assertEqual(verdict.klass, cl.TYPED)
        self.assertTrue(verdict.summary.startswith("typed 'record-retry-pass' but the backend received 'recordretry-ass' (diff at "),
            verdict.summary)

    def test_an_equal_credential_rejected_is_a_backend_mismatch(self):
        verdict = cl.classify(evidence(result(), account=RETRY, journal=fixture_journal("equal-credential-401")))
        self.assertEqual(verdict.klass, cl.BACKEND)
        self.assertIn("rejected the declared credential", verdict.summary)

    def test_an_unmatched_request_is_a_backend_mismatch_that_lists_the_url(self):
        verdict = cl.classify(evidence(result(), account=RETRY, journal=fixture_journal("unmatched")))
        self.assertEqual(verdict.klass, cl.BACKEND)
        self.assertIn("/record/v5/terms", verdict.summary)

    def test_a_bearer_that_is_not_the_accounts_token(self):
        journal = [{"request": {"url": "/record/v5/terms", "headers": {"authorization": "Bearer stale"}},
                    "response": {"status": 401}, "wasMatched": True}]
        self.assertEqual(cl.classify(evidence(result(), account=CANONICAL, journal=journal)).klass, cl.BACKEND)
        same = [dict(journal[0], request={"url": "/record/v5/terms", "headers": {"authorization": "Bearer canon.token"}})]
        self.assertEqual(cl.classify(evidence(result(), account=CANONICAL, journal=same)).klass, cl.PRODUCT)

    def test_the_email_account_is_compared_by_its_local_part(self):
        header = "Basic " + base64.b64encode(b"mail:123456").decode()
        journal = [{"request": {"url": "/auth/v2/bootstrap", "headers": {"Authorization": header}},
                    "response": {"status": 401}, "wasMatched": True}]
        account = dict(CANONICAL, usbId="mail@usb.ve")
        self.assertEqual(cl.classify(evidence(result(), account=account, journal=journal)).klass, cl.BACKEND)


class EndToEndAuditCaseTests(unittest.TestCase):
    def test_the_audit_case_stops_the_run_with_the_decoded_credential(self):
        ws = Workspace(self, [scenario("auth-login-retry", account="record-retry"), scenario("fix-b")],
            {"behaviours": {"auth-login-retry": [{"do": "fail:assertion", "journal": "typed-mismatch"}, "pass"]}})
        result = ws.evidence()
        self.assertEqual(result.code, 5, result.out)
        self.assertEqual(ws.executed(), ["auth-login-retry"])
        self.assertIn("class=typed_text_mismatch: typed 'record-retry-pass' but the backend received 'recordretry-ass'",
            result.out)
        attempt = ws.ledger()["scenarios"]["auth-login-retry"]["attempts"][0]
        self.assertEqual(attempt["failureClass"], "typed_text_mismatch")
        journal_copy = os.path.join(ws.run_dirs()[-1], "scenarios", "auth-login-retry", "attempt-1", "wiremock-requests.json")
        header = json.loads(text(journal_copy))["requests"][0]["request"]["headers"]["Authorization"]
        self.assertEqual(base64.b64decode(header[6:]).decode(), "11-11111:recordretry-ass")

    def test_the_summary_in_the_ledger_comes_from_result_json_not_the_log(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:assertion"]}})
        ws.evidence(E2E_MAX_RETRIES="0")
        summary = ws.ledger()["scenarios"]["fix-a"]["attempts"][0]["failureSummary"]
        self.assertEqual(summary, "step 3 WaitVisible(tag:record_container): not visible after 5000ms")
        self.assertNotIn("fake runner", summary)

    def test_zero_tests_and_disagreement_are_tooling_errors(self):
        for behaviour in ("zero-tests", "disagree", "no-result"):
            ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": [behaviour]}})
            ws.evidence(E2E_MAX_RETRIES="0")
            attempt = ws.ledger()["scenarios"]["fix-a"]["attempts"][0]
            self.assertEqual(attempt["failureClass"], "tooling_error", behaviour)


if __name__ == "__main__":
    unittest.main()
