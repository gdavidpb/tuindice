"""Behaviour of the orchestrator against the fake adapter (plan F14)."""

import json
import os
import re
import shlex
import subprocess
import sys
import threading
import time
import unittest

import support
from support import FP_A, Workspace, scenario, text
from test_wiremock import FAKE_SERVER, free_port


def write_metrics(path, load, idle=80.0):
    with open(path, "w") as handle:
        json.dump({"load": load, "ncpu": 4, "cpuIdle": idle}, handle)


BUSY = [40.0, 30.0, 20.0]  # load1/ncpu = 10


def real_wiremock_env(ws):
    """Environment that swaps the WireMock directory for the real fake server of the harness tests, on a free port."""
    port = free_port()
    return {"E2E_FAKE_WIREMOCK_DIR": "", "E2E_IOS_WIREMOCK_PORT": str(port),
        "E2E_FAKE_WIREMOCK_START_CMD": "%s %s RunMockEnvironment" % (shlex.quote(sys.executable), shlex.quote(FAKE_SERVER))}


def three(timeout=30):
    return [scenario("fix-a", timeout), scenario("fix-b", timeout), scenario("fix-c", timeout)]


class AccumulationTests(unittest.TestCase):
    def test_second_invocation_runs_only_what_is_not_green(self):
        ws = Workspace(self, three(), {"behaviours": {"fix-b": ["fail:typed", "pass"]}})
        first = ws.evidence()
        self.assertEqual(first.code, 5, first.out)
        self.assertEqual(ws.executed(), ["fix-a", "fix-b"])
        second = ws.evidence()
        self.assertEqual(second.code, 0, second.out)
        self.assertEqual(ws.executed(), ["fix-a", "fix-b", "fix-b", "fix-c"])
        self.assertIn("scope: 3 scenarios; 1 already green for fp %s; 2 to run (1 previously failed, 1 pending)." % FP_A[:12],
            second.out)
        third = ws.evidence()
        self.assertEqual(third.code, 0)
        self.assertEqual(len(ws.calls("ensure-device")), 2, "nothing pending must not touch the device")

    def test_retry_reruns_only_the_failed_scenario(self):
        ws = Workspace(self, three(), {"behaviours": {"fix-a": ["fail:assertion", "pass"]}})
        result = ws.evidence()
        self.assertEqual(result.code, 0, result.out)
        self.assertEqual(ws.executed(), ["fix-a", "fix-a", "fix-b", "fix-c"])
        self.assertEqual(ws.ledger()["scenarios"]["fix-a"]["passedAttempt"], 2)

    def test_log_lines_are_exact(self):
        ws = Workspace(self, [scenario("fix-a", 30), scenario("fix-b", 40)],
            {"behaviours": {"fix-a": ["fail:assertion", "pass"], "fix-b": ["fail:assertion", "fail:assertion"]}})
        result = ws.evidence()
        self.assertEqual(result.code, 5, result.out)
        fp = FP_A[:12]
        expected = [
            r"\[e2e ios\] scope: 2 scenarios; 0 already green for fp %s; 2 to run \(0 previously failed, 2 pending\)\.$" % fp,
            r"\[e2e ios\] START 1/2 fix-a attempt 1/2 \(timeout 30s\)$",
            r"\[e2e ios\] FAIL  fix-a attempt 1 in \d+s class=product_assertion: "
            r"step 3 WaitVisible\(tag:record_container\): not visible after 5000ms$",
            r"\[e2e ios\] RETRY fix-a: rerunning only this scenario \(attempt 2 of 2\)\. No other scenario is rerun\.$",
            r"\[e2e ios\] START 1/2 fix-a attempt 2/2 \(timeout 30s\)$",
            r"\[e2e ios\] PASS  fix-a attempt 2 in \d+s$",
            r"\[e2e ios\] START 2/2 fix-b attempt 1/2 \(timeout 40s\)$",
        ]
        lines = result.lines
        cursor = 0
        for pattern in expected:
            while cursor < len(lines) and not re.match(pattern, lines[cursor]):
                cursor += 1
            self.assertLess(cursor, len(lines), "missing log line /%s/ in\n%s" % (pattern, result.out))
        self.assertIn("[e2e ios] STOP  fix-b failed twice with class product_assertion. Another retry is not a remedy.",
            result.out)
        self.assertRegex(result.out, r"\[e2e ios\] RESULT stopped: 1/2 green for fp %s; failed: fix-b \(product_assertion\); "
            r"not run: 0; exit 5\.$" % fp)


class CapTests(unittest.TestCase):
    def test_variable_limits_exit_2(self):
        ws = Workspace(self, three())
        for name, value in (("E2E_MAX_RETRIES", "3"), ("E2E_MAX_RETRIES", "-1"), ("E2E_BUDGET_MINUTES", "9"),
                ("E2E_BUDGET_MINUTES", "241"), ("E2E_PARALLEL", "sometimes"), ("E2E_ANDROID_TUNNEL", "wormhole")):
            self.assertEqual(ws.evidence(**{name: value}).code, 2, "%s=%s" % (name, value))
        self.assertEqual(ws.calls(), [])

    def test_the_cap_is_one_plus_retries(self):
        different = ["fail:assertion", "fail:driver", {"do": "fail:assertion", "journal": "unmatched"}]
        for retries, attempts in (("0", 1), ("1", 2), ("2", 3)):
            ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": different}})
            result = ws.evidence(E2E_MAX_RETRIES=retries)
            self.assertEqual(result.code, 1, result.out)
            self.assertEqual(len(ws.executed()), attempts, "retries=%s" % retries)

    def test_same_class_twice_stops_with_5_and_runs_nothing_else(self):
        ws = Workspace(self, three(), {"behaviours": {"fix-a": ["fail:assertion", "fail:assertion"]}})
        result = ws.evidence()
        self.assertEqual(result.code, 5, result.out)
        self.assertEqual(ws.executed(), ["fix-a", "fix-a"])
        self.assertEqual(ws.manifest()["stop"]["failureClass"], "product_assertion")

    def test_typed_text_mismatch_and_crash_stop_at_the_first_attempt(self):
        for behaviour, klass in (("fail:typed", "typed_text_mismatch"), ("crash", "app_crash")):
            ws = Workspace(self, three(), {"behaviours": {"fix-a": [behaviour, "pass"]}})
            result = ws.evidence()
            self.assertEqual(result.code, 5, result.out)
            self.assertEqual(ws.executed(), ["fix-a"], behaviour)
            self.assertEqual(ws.manifest()["stop"]["failureClass"], klass)
            self.assertIn("is never retried", result.out)

    def test_different_classes_keep_going_and_exit_1(self):
        ws = Workspace(self, three(), {"behaviours": {"fix-a": ["fail:assertion", "fail:driver"]}})
        result = ws.evidence()
        self.assertEqual(result.code, 1, result.out)
        self.assertEqual(ws.executed(), ["fix-a", "fix-a", "fix-b", "fix-c"])
        self.assertEqual(ws.ledger()["scenarios"]["fix-a"]["status"], "failed")
        self.assertIn("failed: fix-a (tooling_error)", result.out)

    def test_survey_runs_each_once_and_never_stops(self):
        ws = Workspace(self, three(), {"behaviours": {"fix-a": ["fail:typed"], "fix-b": ["crash"]}})
        result = ws.diagnose("ios", "--survey")
        self.assertEqual(result.code, 1, result.out)
        self.assertEqual(ws.executed(), ["fix-a", "fix-b", "fix-c"])
        self.assertIn("failed: fix-a (typed_text_mismatch), fix-b (app_crash)", result.out)

    def test_an_invocation_after_the_cap_is_refused_before_touching_devices(self):
        ws = Workspace(self, three(), {"behaviours": {"fix-b": ["fail:assertion", "fail:driver"]}})
        first = ws.evidence()
        self.assertEqual(first.code, 1, first.out)
        self.assertEqual(ws.ledger()["scenarios"]["fix-b"]["status"], "failed")
        before = len(ws.calls())
        second = ws.evidence()
        self.assertEqual(second.code, 7, second.out)
        self.assertEqual(len(ws.calls()), before, "exit 7 must not call the adapter at all")
        self.assertIn("EXHAUSTED fix-b used 2 of 2 attempts", second.out)
        self.assertEqual(ws.manifest()["outcome"], "exhausted")

    def test_reset_scenario_grants_one_more_attempt_once(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": [
            "fail:assertion", "fail:driver", {"do": "fail:assertion", "journal": "unmatched"}]}})
        self.assertEqual(ws.evidence().code, 1)
        self.assertEqual(ws.evidence().code, 7)
        refused = ws.run("reset-scenario", "--platform", "ios", "--id", "fix-b", "--reason", "x")
        self.assertEqual(refused.code, 2)
        done = ws.run("reset-scenario", "--platform", "ios", "--id", "fix-a", "--reason", "emulator was swapped")
        self.assertEqual(done.code, 0, done.out + done.err)
        again = ws.run("reset-scenario", "--platform", "ios", "--id", "fix-a", "--reason", "once more")
        self.assertEqual(again.code, 2)
        self.assertEqual(ws.evidence().code, 1)
        self.assertEqual(len(ws.executed()), 3)
        entry = ws.ledger()["scenarios"]["fix-a"]
        self.assertEqual(len(entry["overrides"]), 1)
        self.assertEqual(entry["overrides"][0]["reason"], "emulator was swapped")
        self.assertEqual(ws.evidence().code, 7)
        self.assertEqual(ws.manifest()["overrides"]["scenarioResets"][0]["scenario"], "fix-a")


class BudgetAndProcessTests(unittest.TestCase):
    def test_budget_exhausted_keeps_the_greens_and_the_next_run_continues(self):
        ws = Workspace(self, [scenario("fix-a", 100), scenario("fix-b", 700)])
        first = ws.evidence(E2E_BUDGET_MINUTES="10")
        self.assertEqual(first.code, 4, first.out)
        self.assertEqual(ws.executed(), ["fix-a"])
        self.assertTrue(ws.ledger()["scenarios"]["fix-a"]["status"] == "passed")
        self.assertEqual(ws.manifest()["outcome"], "budget_exhausted")
        second = ws.evidence(E2E_BUDGET_MINUTES="20")
        self.assertEqual(second.code, 0, second.out)
        self.assertEqual(ws.executed(), ["fix-a", "fix-b"])

    def test_a_hung_runner_is_killed_with_its_process_group(self):
        ws = Workspace(self, [scenario("fix-a", 1)], {"behaviours": {"fix-a": ["hang", "hang"]}})
        began = time.monotonic()
        result = ws.evidence(E2E_MAX_RETRIES="0")
        self.assertLess(time.monotonic() - began, 100, "the fake runner sleeps 300 s: the harness must have killed it, not waited")
        self.assertEqual(result.code, 1, result.out)
        self.assertIn("class=timeout: scenario exceeded 1s; the driver wrote no step to its log, so the step it was in is not known", result.out)
        pid = int(text(os.path.join(ws.fake, "ios", "hang.pid")))
        with self.assertRaises(ProcessLookupError):
            os.kill(pid, 0)

    def test_environment_failures_do_not_count_and_get_one_recovery(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["env", "pass"]}})
        result = ws.evidence()
        self.assertEqual(result.code, 0, result.out)
        self.assertEqual(len(ws.calls("recover")), 1)
        attempts = ws.ledger()["scenarios"]["fix-a"]["attempts"]
        self.assertEqual([a["failureClass"] for a in attempts], ["environment", None])
        self.assertEqual([a["countsAgainstCap"] for a in attempts], [False, True])

    def test_a_second_environment_failure_exits_3(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")],
            {"behaviours": {"fix-a": ["env", "pass"], "fix-b": ["env"]}})
        result = ws.evidence()
        self.assertEqual(result.code, 3, result.out)
        self.assertEqual(len(ws.calls("recover")), 1)
        self.assertEqual(ws.manifest()["outcome"], "environment_refused")
        fix_b = ws.ledger()["scenarios"]["fix-b"]
        self.assertEqual([a["failureClass"] for a in fix_b["attempts"]], ["environment"])
        self.assertEqual(fix_b["status"], "pending", "an environment event never makes a scenario failed or exhausted")

    def test_a_dead_wiremock_is_restarted_by_the_harness_and_the_device_is_left_alone(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["wiremock-kill", "pass"]}})
        result = ws.evidence(**real_wiremock_env(ws))
        self.assertEqual(result.code, 0, result.out)
        self.assertEqual(ws.calls("recover"), [], "restarting WireMock is not a reason to reboot the device")
        attempts = ws.ledger()["scenarios"]["fix-a"]["attempts"]
        self.assertEqual([a["failureClass"] for a in attempts], ["environment", None])
        self.assertIn("WireMock is not healthy", attempts[0]["failureSummary"])
        self.assertIn("WireMock is down; restarting it", result.out)

    def test_a_wiremock_that_dies_during_the_scenario_makes_the_failed_attempt_the_environments(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": [{"do": "fail:assertion", "killWiremock": True}, "pass"]}})
        result = ws.evidence(**real_wiremock_env(ws))
        self.assertEqual(result.code, 0, result.out)
        attempts = ws.ledger()["scenarios"]["fix-a"]["attempts"]
        self.assertEqual([a["failureClass"] for a in attempts], ["environment", None])
        self.assertFalse(attempts[0]["countsAgainstCap"])
        self.assertIn("WireMock stopped answering during the attempt", attempts[0]["failureSummary"])
        self.assertEqual(ws.calls("recover"), [], "a dead WireMock is restarted by the harness, not by rebooting the device")

    def test_an_unreadable_journal_with_a_healthy_wiremock_leaves_the_failure_as_it_was(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": [{"do": "fail:assertion", "journal_raw": "{not json"}, "pass"]}})
        self.assertEqual(ws.evidence().code, 0)
        self.assertEqual(ws.ledger()["scenarios"]["fix-a"]["attempts"][0]["failureClass"], "product_assertion")

    def test_a_wiremock_that_cannot_be_restarted_stops_the_run(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["wiremock-down", "pass"]}})
        result = ws.evidence()  # a fake WireMock directory has nothing to restart: it stays down
        self.assertEqual(result.code, 5, result.out)
        self.assertIn("failed with class environment twice", result.out)

    def test_app_not_running_before_the_first_step_is_environment(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:app-not-running", "pass"]}})
        result = ws.evidence()
        self.assertEqual(result.code, 0, result.out)
        self.assertEqual(ws.ledger()["scenarios"]["fix-a"]["attempts"][0]["failureClass"], "environment")

    def test_app_not_running_later_needs_crash_evidence_to_be_a_crash(self):
        # No crash-probe evidence: a retryable product_assertion, so the second attempt still runs.
        gone = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:app-not-running-late", "pass"]}})
        self.assertEqual(gone.evidence().code, 0)
        self.assertEqual(gone.ledger()["scenarios"]["fix-a"]["attempts"][0]["failureClass"], "product_assertion")
        # The same failure twice stops the scenario by the product_assertion policy, not by the crash one.
        twice = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:app-not-running-late", "fail:app-not-running-late"]}})
        self.assertEqual(twice.evidence().code, 5)
        self.assertEqual(len(twice.ledger()["scenarios"]["fix-a"]["attempts"]), 2)

    def test_a_high_load_average_with_an_idle_cpu_does_not_wait_and_the_cpu_is_recorded(self):
        ws = Workspace(self, [scenario("fix-a")])
        write_metrics(ws.metrics, BUSY, 70.0)
        result = ws.evidence()
        self.assertEqual(result.code, 0, result.out)
        self.assertNotIn("LOAD ", result.out)
        attempt = ws.ledger()["scenarios"]["fix-a"]["attempts"][0]
        self.assertEqual((attempt["loadWaitSeconds"], attempt["cpuIdle"]), (0, 70.0))
        manifest = ws.manifest()
        self.assertEqual(manifest["load"]["waitSeconds"], 0)
        self.assertEqual((manifest["attempts"][0]["cpuIdle"], manifest["attempts"][0]["load1"]), (70.0, 40.0))
        self.assertIn("deviceLoad1", manifest["attempts"][0])

    def test_a_diagnosis_does_not_measure_the_cpu_below_a_load_of_seven_tenths_per_core(self):
        ws = Workspace(self, [scenario("fix-a")])
        write_metrics(ws.metrics, [2.4, 2.4, 2.4], 5.0)  # 0.6 per core
        result = ws.diagnose("ios")
        self.assertEqual(result.code, 0, result.out)
        self.assertNotIn("LOAD ", result.out)
        self.assertIsNone(ws.manifest()["attempts"][0]["cpuIdle"])

    def test_a_diagnosis_measures_it_from_seven_tenths_on_where_the_old_filter_of_one_did_not(self):
        ws = Workspace(self, [scenario("fix-a")])
        write_metrics(ws.metrics, [3.2, 3.2, 3.2], 70.0)  # 0.8 per core: eight busy threads of ten cores
        self.assertEqual(ws.diagnose("ios").code, 0)
        self.assertEqual(ws.manifest()["attempts"][0]["cpuIdle"], 70.0)

    def test_evidence_measures_the_cpu_before_every_scenario_whatever_the_load(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")])
        write_metrics(ws.metrics, [0.1, 0.1, 0.1], 70.0)
        self.assertEqual(ws.evidence().code, 0)
        self.assertEqual([a["cpuIdle"] for a in ws.manifest()["attempts"]], [70.0, 70.0])
        self.assertEqual([a["cpuIdle"] for a in ws.ledger()["scenarios"]["fix-a"]["attempts"]], [70.0])

    def test_evidence_on_a_busy_cpu_waits_even_when_the_load_per_core_is_low(self):
        ws = Workspace(self, [scenario("fix-a")])
        write_metrics(ws.metrics, [0.4, 0.4, 0.4], 10.0)  # 0.1 per core, and still no room for the emulator
        result = ws.evidence(E2E_FAKE_LOAD_POLL_SECONDS="0.2", E2E_ENV_OVERRIDE="cpu", E2E_FAKE_LOAD_WAIT_CAPS="1,1")
        self.assertEqual(result.code, 0, result.out)
        self.assertIn("LOAD  load1/ncpu is 0.10 and the CPU is 10% idle; waiting for 35% idle", result.out)

    def test_the_run_waits_while_the_cpu_is_busy_and_resumes_at_35_percent(self):
        ws = Workspace(self, [scenario("fix-a")])
        write_metrics(ws.metrics, BUSY, 10.0)

        def recover():  # starts once the run reports that it waits, so process start-up cannot shift the timeline
            deadline = time.monotonic() + 60
            while time.monotonic() < deadline and not any("LOAD " in support.text(os.path.join(d, "run.log")) for d in ws.run_dirs()
                    if os.path.exists(os.path.join(d, "run.log"))):
                time.sleep(0.05)
            write_metrics(ws.metrics, BUSY, 34.0)  # still short of 35: keeps waiting
            time.sleep(1.5)
            write_metrics(ws.metrics, BUSY, 35.0)

        threading.Thread(target=recover, daemon=True).start()
        result = ws.evidence(E2E_FAKE_LOAD_POLL_SECONDS="0.2", E2E_ENV_OVERRIDE="cpu")
        self.assertEqual(result.code, 0, result.out)
        self.assertIn("LOAD  load1/ncpu is 10.00 and the CPU is 10% idle; waiting for 35% idle", result.out)
        attempt = ws.ledger()["scenarios"]["fix-a"]["attempts"][0]
        self.assertGreaterEqual(attempt["loadWaitSeconds"], 1.4)
        self.assertEqual(attempt["cpuIdle"], 35.0)
        manifest = ws.manifest()
        self.assertGreaterEqual(manifest["load"]["waitSeconds"], 1.4)
        self.assertGreaterEqual(manifest["load"]["max1m"], 40.0)

    def test_a_cpu_at_20_percent_does_not_start_a_wait(self):
        ws = Workspace(self, [scenario("fix-a")])
        write_metrics(ws.metrics, BUSY, 20.0)
        result = ws.evidence(E2E_ENV_OVERRIDE="cpu")
        self.assertEqual(result.code, 0, result.out)
        self.assertNotIn("LOAD ", result.out)

    def test_the_wait_is_capped_per_scenario_and_per_run(self):
        ws = Workspace(self, three())
        write_metrics(ws.metrics, BUSY, 10.0)
        result = ws.evidence(E2E_FAKE_LOAD_POLL_SECONDS="0.2", E2E_ENV_OVERRIDE="cpu", E2E_FAKE_LOAD_WAIT_CAPS="1,1.5")
        self.assertEqual(result.code, 0, result.out)
        waits = [a["loadWaitSeconds"] for a in ws.manifest()["attempts"]]
        self.assertGreaterEqual(waits[0], 1.0)  # the per-scenario cap ends the first wait
        self.assertLess(waits[1], waits[0] + 0.01)  # the run cap cuts the second short
        self.assertEqual(waits[2], 0)  # the run cap is spent
        self.assertLess(sum(waits), 2.5)

    def test_enumeration_must_match_exactly_one_test_per_scenario(self):
        ws = Workspace(self, three(), {"enumerate": {"missing": ["fix-a"], "duplicate": ["fix-b", "fix-b"]}})
        result = ws.evidence("android")
        self.assertEqual(result.code, 2, result.out + result.err)
        self.assertEqual(ws.executed("android"), [])
        self.assertIn("fix-a (0 tests)", result.out + result.err)
        self.assertIn("fix-b (3 tests)", result.out + result.err)


class ModeTests(unittest.TestCase):
    def test_diagnose_creates_no_ledger_and_accepts_filters(self):
        ws = Workspace(self, three(), {"behaviours": {"fix-b": ["fail:assertion"]}})
        result = ws.diagnose("ios", "--scenario", "fix-b,fix-a", E2E_MAX_RETRIES="0")
        self.assertEqual(result.code, 1, result.out)
        self.assertEqual(ws.executed(), ["fix-b", "fix-a"], "an explicit list keeps the order it was given in")
        self.assertFalse(os.path.exists(os.path.join(ws.state, "ledger")))
        self.assertEqual(ws.manifest()["fingerprint"], "diagnose")

    def test_tag_selects_by_tag_or_by_module(self):
        catalog = [scenario("alpha-one"), scenario("beta-two", tags=("alpha",)), scenario("gamma-three", tags=("other",))]
        ws = Workspace(self, catalog)
        result = ws.diagnose("ios", "--tag", "alpha", E2E_MAX_RETRIES="0")
        self.assertEqual(result.code, 0, result.out)
        self.assertEqual(sorted(ws.executed()), ["alpha-one", "beta-two"])

    def test_repeat_runs_every_scenario_each_time_in_its_own_attempt_directory(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")])
        result = ws.diagnose("ios", "--repeat", "3", E2E_MAX_RETRIES="0")
        self.assertEqual(result.code, 0, result.out + result.err)
        self.assertEqual(sorted(ws.executed()), ["fix-a"] * 3 + ["fix-b"] * 3)
        attempts = os.listdir(os.path.join(ws.run_dirs()[-1], "scenarios", "fix-a"))
        self.assertEqual(len(attempts), 3, attempts)

    def test_evidence_rejects_filters_and_a_dirty_tree(self):
        ws = Workspace(self, three())
        for flag in (["--scenario", "fix-a"], ["--tag", "x"], ["--repeat", "2"], ["--trace"], ["--survey"]):
            self.assertEqual(ws.run("run", "--platform", "ios", "--mode", "evidence", *flag).code, 2, flag)
        self.assertEqual(ws.evidence(E2E_SCENARIOS="fix-a").code, 2)
        with open(os.path.join(ws.repo, "stray.txt"), "w") as handle:
            handle.write("x")
        dirty = ws.evidence()
        self.assertEqual(dirty.code, 2)
        self.assertIn("clean working tree", dirty.out)
        self.assertEqual(ws.calls(), [])

    def test_no_scope_means_not_required_unless_forced(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence(E2E_FAKE_SCOPE_EMPTY="1").code, 0)
        self.assertEqual(ws.calls(), [])
        self.assertEqual(ws.manifest()["outcome"], "not_required")
        self.assertEqual(ws.run("run", "--platform", "ios", "--mode", "evidence", "--force", E2E_FAKE_SCOPE_EMPTY="1").code, 0)
        self.assertEqual(ws.executed(), ["fix-a"])

    def test_quarantine_is_skipped_and_an_expired_one_exits_2(self):
        active = scenario("fix-q", quarantine={"reason": "flaky", "until": "2999-01-01"})
        ws = Workspace(self, [scenario("fix-a"), active])
        self.assertEqual(ws.evidence().code, 0)
        self.assertEqual(ws.executed(), ["fix-a"])
        self.assertIn("quarantined until 2999-01-01: flaky", text(os.path.join(ws.run_dirs()[-1], "summary.txt")))
        expired = Workspace(self, [scenario("fix-a"), scenario("fix-q", quarantine={"reason": "flaky", "until": "2000-01-01"})])
        self.assertEqual(expired.evidence().code, 2)
        self.assertEqual(expired.calls(), [])

    def test_dry_run_plans_without_touching_anything(self):
        ws = Workspace(self, three())
        result = ws.run("run", "--platform", "ios", "--mode", "evidence", "--dry-run")
        self.assertEqual(result.code, 0, result.out)
        self.assertIn("DRY-RUN would run: fix-a, fix-b, fix-c", result.out)
        self.assertEqual([c[1] for c in ws.calls()], ["toolchain"], "the only adapter call of a dry run is the read-only toolchain")
        self.assertFalse(os.path.exists(ws.state))

    def test_platform_all_runs_both_and_combines_the_codes(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"android:fix-a": ["fail:assertion"]}})
        result = ws.run("run", "--platform", "all", "--mode", "evidence", E2E_MAX_RETRIES="0")
        self.assertEqual(result.code, 1, result.out)
        self.assertIn("[e2e] exit codes: android=1 ios=0", result.out)
        skipped = ws.run("run", "--platform", "all", "--mode", "evidence", E2E_SKIP_ANDROID="1", E2E_MAX_RETRIES="0")
        self.assertEqual(skipped.code, 0, skipped.out)
        self.assertEqual(ws.run("run", "--platform", "all", "--mode", "evidence",
            E2E_SKIP_ANDROID="1", E2E_SKIP_IOS="1").code, 2)

    def test_status_and_list_work_without_devices(self):
        ws = Workspace(self, three())
        self.assertEqual(ws.evidence().code, 0)
        status = json.loads(ws.run("status", "--json").out)
        self.assertEqual(status["platforms"]["ios"]["complete"], True)
        self.assertEqual(status["platforms"]["android"]["pending"], ["fix-a", "fix-b", "fix-c"])
        self.assertEqual(len(ws.run("list").lines), 3)


class SignalTests(unittest.TestCase):
    def test_sigterm_finalises_the_manifest_and_kills_the_runner(self):
        ws = Workspace(self, [scenario("fix-a", 60)], {"behaviours": {"fix-a": ["hang"]}})
        env = dict(ws.env)
        process = subprocess.Popen([sys.executable, support.E2E_PY, "run", "--platform", "ios", "--mode", "evidence"],
            cwd=ws.repo, env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        pid_file = os.path.join(ws.fake, "ios", "hang.pid")
        deadline = time.monotonic() + 20
        while not os.path.exists(pid_file) and time.monotonic() < deadline:
            time.sleep(0.1)
        self.assertTrue(os.path.exists(pid_file), "the hanging runner never started")
        time.sleep(0.3)
        process.terminate()
        out, _ = process.communicate(timeout=30)
        self.assertEqual(process.returncode, 143, out)
        manifest = ws.manifest()
        self.assertEqual(manifest["outcome"], "interrupted")
        self.assertEqual(manifest["exitCode"], 143)
        with self.assertRaises(ProcessLookupError):
            os.kill(int(text(pid_file)), 0)


class CollectFailureTests(unittest.TestCase):
    def test_a_collect_failure_that_fails_is_named_in_the_attempt_and_in_the_log(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:assertion"]}, "failVerbs": ["ios:collect-failure"]})
        result = ws.evidence()
        self.assertEqual(result.code, 5, result.out)
        errors = ws.ledger()["scenarios"]["fix-a"]["attempts"][0]["probeErrors"]
        self.assertEqual(len(errors), 1, errors)
        self.assertIn("collect-failure failed (exit 1", errors[0])
        self.assertIn("NOTE  fix-a attempt 1: collect-failure failed", result.out)


class TimeoutStepTests(unittest.TestCase):
    def test_a_hung_scenario_names_the_last_step_its_driver_logged(self):
        ws = Workspace(self, [scenario("fix-a", 2)], {"behaviours": {"fix-a": [{"do": "hang", "driverlog": "ios-quiet.log"}]}})
        result = ws.diagnose("ios")
        self.assertIn("class=timeout: scenario exceeded 2s; last completed step 0 WaitVisible(tag:auth_screen)", result.out)


if __name__ == "__main__":
    unittest.main()
