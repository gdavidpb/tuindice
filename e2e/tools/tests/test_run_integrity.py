"""What a run must not do: record or publish from a checkout that moved, let a corrupt text through as a retry, lose its manifest,
keep a dead WireMock, hide a recoverable failure, or publish through a test seam (the audit corrections C-1 to C-24)."""

import argparse
import json
import os
import shlex
import signal
import subprocess
import sys
import time
import unittest
from pathlib import Path
from unittest import mock

import support
from support import FP_A, Workspace, expect_request, scenario, text
import e2e
from harness import parallel
from harness.config import Config, UsageError
from harness import gitstate
from harness.gitstate import GitState
from harness.ledger import Ledger
from harness.manifest import Manifest
from harness.proc import Interrupted
from harness.runner import Options, PlatformRun
from harness.wiremock import WireMock
from test_publish import gh_posts, publishing
from test_wiremock import FAKE_SERVER, free_port


def real_wiremock(extra=""):
    """Environment that swaps the WireMock directory for the real fake server of the harness tests, on a free port."""
    return {"E2E_FAKE_WIREMOCK_DIR": "", "E2E_IOS_WIREMOCK_PORT": str(free_port()),
        "E2E_FAKE_WIREMOCK_START_CMD": "%s %s RunMockEnvironment %s" % (shlex.quote(sys.executable), shlex.quote(FAKE_SERVER), extra)}


def update_password_scenario():
    """auth-update-password as the catalog declares it: the account holds the old password, the step expects the new one."""
    return scenario("auth-update-password", account="update-password", steps=[expect_request("55-55555:123456")])


class CorruptTextTests(unittest.TestCase):
    def test_a_corrupt_password_on_an_unscripted_endpoint_stops_the_run_at_the_first_attempt(self):
        # The journal of a real attempt: the dialog sent 55-55555:v123456 to /auth/v1/token, which has no stub.
        ws = Workspace(self, [update_password_scenario(), scenario("fix-b")],
            {"behaviours": {"auth-update-password": [{"do": "fail:assertion", "journal": "typed-corrupt-token"}, "pass"]}})
        result = ws.evidence()
        self.assertEqual(result.code, 5, result.out)
        self.assertEqual(ws.executed(), ["auth-update-password"], "no second attempt may turn the defect green")
        self.assertIn("typed '123456' but the backend received 'v123456' on POST /auth/v1/token", result.out)
        self.assertEqual(ws.manifest()["stop"]["failureClass"], "typed_text_mismatch")


class EnvironmentLedgerTests(unittest.TestCase):
    def test_the_same_scenario_hitting_the_environment_twice_under_one_fingerprint_exits_5_across_invocations(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["env", "fail:typed", "env"]}})
        first = ws.evidence()
        self.assertEqual(first.code, 5, first.out)
        entry = ws.ledger()["scenarios"]["fix-a"]
        self.assertEqual([a["failureClass"] for a in entry["attempts"]], ["environment", "typed_text_mismatch"])
        second = ws.evidence()
        self.assertEqual(second.code, 5, second.out)
        self.assertIn("failed with class environment twice", second.out)
        self.assertEqual(len(ws.calls("recover")), 1, "the second event is a diagnosis, not another recovery")
        self.assertEqual(ws.manifest()["stop"]["failureClass"], "environment")

    def test_environment_events_show_in_the_status_description(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-a": ["env", "pass"]}})
        self.assertEqual(publishing(ws).code, 0)
        self.assertIn(" env 1.", ws.ledger()["publications"][0]["description"])

    def test_an_anr_of_another_process_does_not_discard_a_scenario_that_passed(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["pass-system-anr"]}})
        result = ws.evidence()
        self.assertEqual(result.code, 0, result.out)
        attempt = ws.ledger()["scenarios"]["fix-a"]["attempts"][0]
        self.assertEqual(attempt["outcome"], "passed")
        self.assertEqual(len(attempt["notes"]), 1)
        self.assertIn("system ANR", attempt["notes"][0])
        self.assertEqual(len(ws.calls("recover")), 0)

    def test_an_anr_of_another_process_still_explains_a_failure(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["system-anr", "pass"]}})
        self.assertEqual(ws.evidence().code, 0)
        self.assertEqual(ws.ledger()["scenarios"]["fix-a"]["attempts"][0]["failureClass"], "environment")


class InterruptionTests(unittest.TestCase):
    def start(self, ws, env):
        return subprocess.Popen([sys.executable, support.E2E_PY, "run", "--platform", "ios", "--mode", "evidence"], cwd=ws.repo, env=env,
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)

    def test_a_kill_during_the_slow_cleanup_still_leaves_a_finalised_manifest(self):
        ws = Workspace(self, [scenario("fix-a", 60)], {"behaviours": {"fix-a": ["hang"]}})
        env = dict(ws.env, **real_wiremock("ignore-term"))  # a WireMock that ignores SIGTERM makes the cleanup take 10 s
        owner = os.path.join(ws.dir, "tmp", "ios", "wiremock", "lock", "owner.json")

        def reap():
            try:
                os.killpg(json.load(open(owner))["pgid"], signal.SIGKILL)
            except (OSError, ValueError, ProcessLookupError):
                pass
        self.addCleanup(reap)
        process = self.start(ws, env)
        pid_file = os.path.join(ws.fake, "ios", "hang.pid")
        deadline = time.monotonic() + 30
        while not os.path.exists(pid_file) and time.monotonic() < deadline:
            time.sleep(0.1)
        self.assertTrue(os.path.exists(pid_file), "the hanging runner never started")
        time.sleep(0.3)
        process.terminate()
        # The kill must land inside the cleanup (WireMock was asked to stop and ignores it for 10 s), which starts when the harness
        # says its manifest is finalised: wait for that line, however long a loaded machine takes to get there.
        deadline = time.monotonic() + 60
        while time.monotonic() < deadline and not any("the manifest is finalised" in text(os.path.join(d, "run.log"))
                for d in ws.run_dirs() if os.path.exists(os.path.join(d, "run.log"))):
            time.sleep(0.05)
        time.sleep(0.5)
        process.kill()
        process.communicate()
        manifest = ws.manifest()
        self.assertEqual((manifest["outcome"], manifest["exitCode"]), ("interrupted", 143))
        self.assertIsNotNone(manifest["finishedAt"])
        self.assertTrue(os.path.exists(os.path.join(ws.run_dirs()[-1], "junit.xml")))

    def test_a_kill_during_the_retention_still_leaves_a_finalised_run_with_its_results(self):
        # The retention may take 10 minutes; the results, the JUnit file and the manifest do not wait for it (dC-10).
        ws = Workspace(self, [scenario("fix-a")])
        tool, started = os.path.join(ws.dir, "retention-slow.sh"), os.path.join(ws.dir, "retention.pid")
        with open(tool, "w") as handle:
            handle.write("#!/bin/sh\necho $$ > '%s'\nexec sleep 60\n" % started)
        os.chmod(tool, 0o755)

        def reap():
            try:
                os.kill(int(text(started)), signal.SIGKILL)
            except (OSError, ValueError):
                pass
        self.addCleanup(reap)
        process = subprocess.Popen([sys.executable, support.E2E_PY, "run", "--platform", "ios", "--mode", "diagnose"], cwd=ws.repo,
            env=dict(ws.env, E2E_RETENTION_CMD=tool), stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        self.addCleanup(process.kill)
        deadline = time.monotonic() + 60
        while not os.path.exists(started) and time.monotonic() < deadline:
            time.sleep(0.05)
        self.assertTrue(os.path.exists(started), "the retention never started")
        process.kill()
        process.communicate()
        manifest = ws.manifest()
        self.assertEqual((manifest["outcome"], manifest["exitCode"]), ("passed", 0))
        self.assertIsNotNone(manifest["finishedAt"])
        for name in ("junit.xml", "summary.txt"):
            self.assertTrue(os.path.exists(os.path.join(ws.run_dirs()[-1], name)), name)

    def test_an_interruption_between_the_starts_of_platform_all_stops_the_child_already_running(self):
        cfg = Config(Path(os.getcwd()), dict(os.environ))
        child = subprocess.Popen(["sleep", "60"], start_new_session=True)
        self.addCleanup(child.kill)
        with mock.patch.object(parallel.subprocess, "Popen", side_effect=[child, Interrupted(signal.SIGTERM)]):
            with self.assertRaises(Interrupted):
                parallel._launch_and_wait(cfg, {"android": ["x"], "ios": ["y"]})
        self.assertIsNotNone(child.poll(), "the first child must not outlive the interruption")


class CheckoutChangeTests(unittest.TestCase):
    def assert_nothing_recorded(self, ws, result):
        self.assertEqual(result.code, 2, result.out)
        self.assertIn("the checkout changed during the run", result.out)
        self.assertEqual(gh_posts(ws), [], "nothing is published from a checkout that moved")
        recorded = ws.ledger()["scenarios"] if os.path.exists(os.path.join(ws.state, "ledger", "ios", FP_A, "ledger.json")) else {}
        self.assertEqual(recorded, {}, "the attempt of the new tree must not be recorded")
        self.assertEqual(ws.manifest()["exitCode"], 2)

    def test_a_file_that_appears_during_the_build_is_exit_2(self):
        ws = Workspace(self, [scenario("fix-a")], {"mutate": {"ios:build": "file"}})
        result = publishing(ws)
        self.assert_nothing_recorded(ws, result)
        self.assertEqual(ws.executed(), [], "no scenario runs on a tree that already changed")

    def test_a_commit_during_a_scenario_is_exit_2_and_that_attempt_is_not_recorded(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"mutate": {"ios:run-scenario": "commit"}})
        result = publishing(ws)
        self.assert_nothing_recorded(ws, result)
        self.assertIn("HEAD moved", result.out)
        self.assertEqual(ws.executed(), ["fix-a"])

    def test_the_ledger_keeps_what_was_recorded_before_the_change(self):
        behaviours = {"fix-b": ["fail:typed", "pass"]}
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": behaviours})
        self.assertEqual(ws.evidence().code, 5)
        ws.write_script({"behaviours": behaviours, "mutate": {"ios:run-scenario": "file"}})
        result = ws.evidence()
        self.assertEqual(result.code, 2, result.out)
        entry = ws.ledger()["scenarios"]
        self.assertEqual(entry["fix-a"]["status"], "passed", "the green recorded before stays")
        self.assertEqual(len(entry["fix-b"]["attempts"]), 1, "the attempt made on the changed tree is not added")

    def test_publishing_reads_the_checkout_again(self):
        ws = Workspace(self, [scenario("fix-a")])
        cfg = Config(ws.repo, ws.env)
        run = PlatformRun(cfg, "ios", Options("evidence"))
        run.git = GitState(ws.repo)
        run.manifest = Manifest("", "x", "evidence", "ios", cfg, enabled=False)
        run.ledger, run.runnable, run.quarantined = Ledger.memory("ios"), [], []
        run.fingerprint, run.context = FP_A, "local-e2e/ios/local-certification-suite"
        with open(os.path.join(ws.repo, "stray.txt"), "w") as handle:
            handle.write("x")
        with self.assertRaises(UsageError) as caught:
            run._publish()
        self.assertIn("before publishing", str(caught.exception))
        self.assertFalse(os.path.exists(ws.gh_log))

    def test_the_publication_invariant_is_checked_where_it_is_made(self):
        ws = Workspace(self, [scenario("fix-a")])
        cfg = Config(ws.repo, ws.env)
        run = PlatformRun(cfg, "ios", Options("evidence"))
        run.git = GitState(ws.repo)
        run.manifest = Manifest("", "x", "evidence", "ios", cfg, enabled=False)
        run.ledger, run.quarantined = Ledger.memory("ios"), []
        run.runnable = [mock.Mock(id="fix-a")]  # not green: the ledger has no pass for it
        with self.assertRaises(UsageError) as caught:
            run._publish()
        self.assertIn("not green", str(caught.exception))


class GitStatusTests(unittest.TestCase):
    """A `git status` that fails says nothing about the tree: an empty answer is not a clean one (dC-12)."""

    @staticmethod
    def failing_status():
        real = gitstate.run_git
        return mock.patch.object(gitstate, "run_git", lambda root, *args: (128, "") if args[0] == "status" else real(root, *args))

    def test_the_checkout_is_not_read_as_clean_when_git_status_fails_at_the_start(self):
        ws = Workspace(self, [scenario("fix-a")])
        with self.failing_status():
            with self.assertRaises(UsageError) as caught:
                GitState(ws.repo)
        self.assertIn("git status failed", str(caught.exception))

    def test_nor_when_it_is_read_again_during_the_run(self):
        ws = Workspace(self, [scenario("fix-a")])
        git = GitState(ws.repo)
        git.require_unchanged("before the test")
        with self.failing_status():
            with self.assertRaises(UsageError) as caught:
                git.require_unchanged("after the build")
        self.assertIn("git status failed", str(caught.exception))


class CapTests(unittest.TestCase):
    def test_another_attempt_cap_for_the_same_fingerprint_is_refused_before_the_device(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-b": ["fail:typed", "pass"]}})
        self.assertEqual(ws.evidence().code, 5)
        self.assertEqual(ws.ledger()["attemptCap"], 2)
        calls = len(ws.calls())
        for retries in ("0", "2"):
            refused = ws.evidence(E2E_MAX_RETRIES=retries)
            self.assertEqual(refused.code, 2, refused.out)
            self.assertIn("the attempt cap of this fingerprint is 2", refused.out)
            self.assertIn("reset-scenario", refused.out)
        self.assertEqual(len(ws.calls()), calls, "a refused cap must not touch the device")
        self.assertEqual(ws.evidence().code, 0)

    def test_reading_commands_use_the_stored_cap_whatever_the_variable_says(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:typed"]}})
        self.assertEqual(ws.evidence().code, 5)  # one attempt of a cap of 2: not exhausted
        status = json.loads(ws.run("status", "--json", E2E_MAX_RETRIES="0").out)["platforms"]["ios"]
        self.assertEqual(status["exhausted"], [], "a cap of 1 read from the variable would call it exhausted")
        done = ws.run("reset-scenario", "--platform", "ios", "--id", "fix-a", "--reason", "swapped", E2E_MAX_RETRIES="0")
        self.assertEqual(done.code, 2, done.out + done.err)
        self.assertIn("has not exhausted its attempts", done.err)


class SeamTests(unittest.TestCase):
    def test_which_variables_count_as_seams(self):
        cfg = Config(Path("."), {"E2E_FAKE_HOST_METRICS": "x", "E2E_ADAPTER_IOS_CMD": "x", "E2E_GH_CMD": "gh", "E2E_CATALOG_FILE": "c",
            "E2E_SCOPE_FILE": "s", "E2E_STATE_ROOT": "/s", "E2E_TMP_ROOT": "/t", "E2E_ENV_OVERRIDE": "cpu", "E2E_MAX_RETRIES": "1",
            "E2E_FAKE_EMPTY": ""})
        self.assertEqual(cfg.seams(), ["E2E_ADAPTER_IOS_CMD", "E2E_CATALOG_FILE", "E2E_FAKE_HOST_METRICS", "E2E_GH_CMD", "E2E_SCOPE_FILE"])

    def test_real_publication_refuses_every_seam_unless_the_test_key_allows_it(self):
        ws = Workspace(self, [scenario("fix-a")])
        refused = publishing(ws, E2E_TEST_ALLOW_SEAMS="")
        self.assertEqual(refused.code, 2, refused.out)
        self.assertIn("unset the test seams", refused.out)
        self.assertIn("E2E_CATALOG_FILE", refused.out)
        self.assertEqual(ws.calls(), [])
        self.assertEqual(gh_posts(ws), [])
        self.assertEqual(ws.run("publish", "--platform", "ios", E2E_TEST_ALLOW_SEAMS="").code, 2)

    def test_evidence_refuses_seams_even_when_it_publishes_nothing(self):
        ws = Workspace(self, [scenario("fix-a")])
        refused = ws.evidence(E2E_PUBLISH_GITHUB_STATUS="0", E2E_TEST_ALLOW_SEAMS="")
        self.assertEqual(refused.code, 2, refused.out)
        self.assertIn("unset the test seams", refused.out)
        self.assertEqual(ws.calls(), [])

    def test_the_fingerprint_root_is_a_seam(self):
        cfg = Config(Path("."), {"E2E_FINGERPRINT_REPO_ROOT": "/elsewhere", "E2E_STATE_ROOT": "/s"})
        self.assertEqual(cfg.seams(), ["E2E_FINGERPRINT_REPO_ROOT"])

    def test_the_ledger_keeps_the_seams_of_each_run(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence().code, 0)
        self.assertIn("E2E_ADAPTER_IOS_CMD", ws.ledger()["seams"])
        self.assertNotIn("E2E_TEST_ALLOW_SEAMS", ws.ledger()["seams"])

    def test_publish_refuses_a_ledger_that_recorded_seams_before_it_asks_github(self):
        ws = Workspace(self, [scenario("fix-a")])
        cfg = Config(ws.repo, {"E2E_STATE_ROOT": ws.state})  # a real environment: no seam of its own, no test key
        ledger = Ledger.memory("ios")
        ledger.data["seams"] = ["E2E_FAKE_HOST_METRICS"]
        catalog = mock.Mock(in_scope=mock.Mock(return_value=([], [])))
        with mock.patch.object(e2e, "open_ledger", return_value=(catalog, FP_A, ledger)), \
                mock.patch.object(GitState, "require_publishable") as asks_github, \
                mock.patch.object(e2e.publish_mod, "publish_success") as posts:  # never the real gh
            with self.assertRaises(UsageError) as caught:
                e2e.cmd_publish(cfg, argparse.Namespace(platform="ios"))
        self.assertIn("E2E_FAKE_HOST_METRICS", str(caught.exception))
        asks_github.assert_not_called()
        posts.assert_not_called()

    def test_the_run_refuses_to_publish_a_ledger_with_seams(self):
        ws = Workspace(self, [scenario("fix-a")])
        cfg = Config(ws.repo, {"E2E_STATE_ROOT": ws.state})
        run = PlatformRun(cfg, "ios", Options("evidence"))
        run.git = GitState(ws.repo)
        run.manifest = Manifest("", "x", "evidence", "ios", cfg, enabled=False)
        run.ledger, run.runnable, run.quarantined = Ledger.memory("ios"), [], []
        run.ledger.data["seams"] = ["E2E_ADAPTER_IOS_CMD"]
        run.fingerprint, run.context = FP_A, "local-e2e/ios/local-certification-suite"
        with self.assertRaises(UsageError) as caught:
            run._publish()
        self.assertIn("E2E_ADAPTER_IOS_CMD", str(caught.exception))
        self.assertFalse(os.path.exists(ws.gh_log))

    def test_a_ledger_that_recorded_seams_is_refused_without_the_key_and_the_message_says_where_it_is(self):
        # Not "unset the seams": the environment has none, the ledger remembers them.
        cfg = Config(Path("."), {"E2E_STATE_ROOT": "/s"})
        with self.assertRaises(UsageError) as caught:
            cfg.require_clean_ledger("/s/ledger/ios/abc", ["E2E_ADAPTER_IOS_CMD"])
        message = str(caught.exception)
        self.assertIn("/s/ledger/ios/abc", message)
        self.assertIn("E2E_ADAPTER_IOS_CMD", message)
        self.assertIn("set it aside", message)
        self.assertNotIn("unset the test seams", message)
        cfg.require_clean_ledger("/s/ledger/ios/abc", [])
        Config(Path("."), {"E2E_TEST_ALLOW_SEAMS": "1"}).require_clean_ledger("/s/x", ["E2E_ADAPTER_IOS_CMD"])

    def test_evidence_asks_the_ledger_about_its_seams_before_anything_runs(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence().code, 0)
        calls = len(ws.calls())
        cfg = Config(ws.repo, ws.env)
        run = PlatformRun(cfg, "ios", Options("evidence"))
        run.git, run.manifest = GitState(ws.repo), Manifest("", "x", "evidence", "ios", cfg, enabled=False)
        with mock.patch.object(Config, "require_clean_ledger", side_effect=UsageError("sentinel")) as asked, \
                mock.patch.object(PlatformRun, "_fingerprint", return_value=FP_A):
            with self.assertRaises(UsageError) as caught:
                run._pipeline()
        self.assertEqual(str(caught.exception), "sentinel")
        directory, recorded = asked.call_args[0]
        self.assertEqual(directory, os.path.join(ws.state, "ledger", "ios", FP_A))
        self.assertIn("E2E_ADAPTER_IOS_CMD", recorded)
        self.assertEqual(len(ws.calls()), calls)

    def test_evidence_that_ends_with_a_scenario_neither_green_nor_failed_is_an_internal_error_even_when_it_publishes_nothing(self):
        ws = Workspace(self, [scenario("fix-a")])
        cfg = Config(ws.repo, ws.env)
        run = PlatformRun(cfg, "ios", Options("evidence"))
        run.manifest = Manifest("", "x", "evidence", "ios", cfg, enabled=False)
        run.ledger, run.runnable = Ledger.memory("ios"), [mock.Mock(id="fix-a")]  # nothing recorded for it, no stop, no failure
        with self.assertRaises(RuntimeError) as caught:
            run._decide(False)
        self.assertIn("fix-a", str(caught.exception))
        diagnose = PlatformRun(cfg, "ios", Options("diagnose"))
        diagnose.manifest, diagnose.ledger, diagnose.runnable = run.manifest, run.ledger, run.runnable
        self.assertEqual(diagnose._decide(False), ("passed", 0), "a diagnosis may leave scenarios unrun")

    def test_a_state_root_alone_is_not_a_seam_and_diagnosis_may_use_seams(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.diagnose("ios", E2E_TEST_ALLOW_SEAMS="").code, 0)
        seams = ws.manifest()["seams"]
        self.assertIn("E2E_ADAPTER_IOS_CMD", seams)
        self.assertNotIn("E2E_STATE_ROOT", seams)
        self.assertNotIn("E2E_TMP_ROOT", seams)

    def test_the_status_counts_the_overrides_the_runs_used_and_publish_words_it_the_same_way(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(publishing(ws, E2E_ENV_OVERRIDE="cpu", E2E_PARALLEL="never").code, 0)
        description = ws.ledger()["publications"][0]["description"]
        self.assertTrue(description.endswith(" overrides 2."), description)
        os.remove(ws.gh_log)
        self.assertEqual(ws.run("publish", "--platform", "ios").code, 0)
        self.assertFalse(os.path.exists(ws.gh_log) and gh_posts(ws), "the same words: already published")


class PublishFailureTests(unittest.TestCase):
    def test_a_gh_that_hangs_is_exit_6_with_the_ledger_intact(self):
        ws = Workspace(self, [scenario("fix-a")])
        result = publishing(ws, E2E_FAKE_GH_SLEEP="20", E2E_FAKE_PUBLISH_TIMEOUT_SECONDS="1")
        self.assertEqual(result.code, 6, result.out)
        self.assertIn("PUBLISH FAILED gh did not answer within 1s", result.out)
        self.assertEqual(ws.ledger()["scenarios"]["fix-a"]["status"], "passed")
        self.assertEqual(ws.manifest()["outcome"], "publish_failed")

    def test_a_missing_gh_is_a_usage_error_before_anything_runs(self):
        ws = Workspace(self, [scenario("fix-a")])
        result = publishing(ws, E2E_GH_CMD="/no/such/gh")
        self.assertEqual(result.code, 2, result.out)
        self.assertIn("cannot run /no/such/gh", result.out)
        self.assertEqual(ws.calls(), [])

    def test_a_defect_of_the_harness_has_its_own_code_and_manifest(self):
        ws = Workspace(self, [scenario("fix-a")], {"enumerate": {"raw": 5}})  # the adapter answers something the harness cannot read
        result = ws.evidence()
        self.assertEqual(result.code, 70, result.out)
        manifest = ws.manifest()
        self.assertEqual((manifest["outcome"], manifest["exitCode"]), ("harness_error", 70))
        self.assertTrue(os.path.exists(os.path.join(ws.run_dirs()[-1], "harness-error.txt")))
        self.assertIn("HARNESS ERROR", result.out)


class SamplingTests(unittest.TestCase):
    def test_repeat_runs_do_not_retry_so_a_failed_sample_is_not_hidden(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:assertion", "pass"]}})
        result = ws.diagnose("ios", "--repeat", "2")
        self.assertEqual(ws.executed(), ["fix-a", "fix-a"], "one attempt per repetition")
        self.assertEqual(result.code, 1, result.out)
        self.assertIn("REPEAT 2 runs: 1 scenarios failed in at least one", result.out)

    def test_a_survey_notes_environment_events_and_goes_on(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b"), scenario("fix-c")],
            {"behaviours": {"fix-a": ["env"], "fix-b": ["env"]}})
        result = ws.diagnose("ios", "--survey")
        self.assertEqual(len(ws.calls("health")), 3, "every scenario is tried, none ends the survey")
        self.assertEqual(ws.executed(), ["fix-c"], result.out)
        self.assertEqual(result.code, 1, "scenarios that could not be measured are not a clean survey")
        self.assertIn("failed: fix-a (environment), fix-b (environment)", result.out)
        self.assertEqual(ws.calls("recover"), [])

    def test_the_manifest_names_the_directory_and_repetition_of_every_attempt(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.diagnose("ios", "--repeat", "3").code, 0)
        attempts = ws.manifest()["attempts"]
        self.assertEqual([(a["attemptDir"], a["repetition"]) for a in attempts], [("attempt-1", 1), ("attempt-1-r2", 2), ("attempt-1-r3", 3)])


class DiagnosticsTests(unittest.TestCase):
    def test_requests_without_a_stub_are_kept_on_a_green_attempt(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": [{"do": "pass", "journal": "unmatched"}]}})
        result = ws.evidence()
        self.assertEqual(result.code, 0, result.out)
        attempt = ws.ledger()["scenarios"]["fix-a"]["attempts"][0]
        self.assertEqual(attempt["unmatchedRequests"], {"count": 1, "paths": ["/record/v5/terms"]})
        self.assertEqual(ws.manifest()["attempts"][0]["unmatchedRequests"], 1)
        self.assertIn("NOTE  fix-a attempt 1: 1 request(s) had no stub: /record/v5/terms", result.out)

    def test_an_unreadable_journal_and_a_failed_crash_probe_are_recorded_not_swallowed(self):
        ws = Workspace(self, [scenario("fix-a")], {"failVerbs": ["ios:crash-probe"],
            "behaviours": {"fix-a": [{"do": "pass", "journal_raw": "{not json"}]}})
        self.assertEqual(ws.evidence().code, 0)
        errors = ws.ledger()["scenarios"]["fix-a"]["attempts"][0]["probeErrors"]
        self.assertTrue(any("crash-probe gave no answer" in e for e in errors), errors)
        self.assertTrue(any("journal could not be read" in e for e in errors), errors)

    def test_a_result_that_is_valid_json_but_not_an_object_is_a_tooling_error(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["result-list"]}})
        result = ws.evidence(E2E_MAX_RETRIES="0")
        self.assertEqual(result.code, 1, result.out)
        attempt = ws.ledger()["scenarios"]["fix-a"]["attempts"][0]
        self.assertEqual(attempt["failureClass"], "tooling_error")
        self.assertIn("not a JSON object", attempt["failureSummary"])

    def test_the_full_output_of_a_failed_verb_is_kept_in_the_run_directory(self):
        ws = Workspace(self, [scenario("fix-a")], {"failVerbs": ["ios:ensure-device"]})
        result = ws.evidence()
        self.assertEqual(result.code, 3, result.out)
        log = text(os.path.join(ws.run_dirs()[-1], "verbs", "ensure-device-1.log"))
        self.assertIn("fake ios:ensure-device failed", log)
        self.assertIn("exit 1", log)
        self.assertIn("verbs/ensure-device-1.log", result.out)

    def test_the_crash_probe_gets_the_end_of_the_attempt_and_waits_only_after_a_failure(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-b": ["fail:assertion", "pass"]}})
        self.assertEqual(ws.evidence().code, 0)
        probes = ws.calls("crash-probe")
        self.assertEqual(len(probes[0]), 6, probes[0])  # platform verb since dir until wait
        self.assertEqual([p[5] for p in probes], ["0", "15", "0"])
        self.assertGreaterEqual(int(probes[0][4]), int(probes[0][2]))


class LoadGateTests(unittest.TestCase):
    def test_an_exhausted_gate_says_so_once_and_the_manifest_records_where(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b"), scenario("fix-c")])
        support_metrics = {"load": [40.0, 30.0, 20.0], "ncpu": 4, "cpuIdle": 10.0}
        with open(ws.metrics, "w") as handle:
            json.dump(support_metrics, handle)
        result = ws.evidence(E2E_FAKE_LOAD_POLL_SECONDS="0.2", E2E_ENV_OVERRIDE="cpu", E2E_FAKE_LOAD_WAIT_CAPS="1,1.5")
        self.assertEqual(result.code, 0, result.out)
        self.assertEqual(result.out.count("gate exhausted"), 1, result.out)
        self.assertEqual(result.out.count("waiting for 35% idle"), 2, "the third scenario must not claim to wait")
        gate = ws.manifest()["load"]["gateExhaustedAt"]
        self.assertEqual(gate["scenario"], "fix-b")
        self.assertIn("gave up after", result.out)


class DeviceClaimTests(unittest.TestCase):
    def test_a_second_run_on_the_platform_is_refused_before_it_touches_the_device(self):
        ws = Workspace(self, [scenario("fix-a")])
        extra = real_wiremock()
        holder_dir = os.path.join(ws.dir, "holder-run")
        os.makedirs(holder_dir)
        holder = WireMock(Config(Path(ws.repo), dict(ws.env, **extra)), "ios", holder_dir)
        holder.start()
        self.addCleanup(holder.stop)
        result = ws.evidence(**extra)
        self.assertEqual(result.code, 3, result.out)
        self.assertIn("is owned by run holder-run", result.out)
        self.assertEqual(ws.calls("ensure-device"), [], "the claim comes before the device")


class StopDevicesAndRetentionTests(unittest.TestCase):
    def test_stop_devices_stops_the_pinned_devices_and_refuses_while_a_run_owns_one(self):
        ws = Workspace(self, [scenario("fix-a")])
        done = ws.run("stop-devices")
        self.assertEqual(done.code, 0, done.out + done.err)
        self.assertEqual([c[:2] for c in ws.calls("stop-device")], [["android", "stop-device"], ["ios", "stop-device"]])
        extra = real_wiremock()
        holder_dir = os.path.join(ws.dir, "holder-run")
        os.makedirs(holder_dir)
        holder = WireMock(Config(Path(ws.repo), dict(ws.env, **extra)), "ios", holder_dir)
        holder.start()
        self.addCleanup(holder.stop)
        before = len(ws.calls("stop-device"))
        busy = ws.run("stop-devices", **extra)
        self.assertEqual(busy.code, 3, busy.out + busy.err)
        self.assertEqual(len(ws.calls("stop-device")), before)

    def test_a_retention_that_found_another_retention_running_is_not_a_failure(self):
        ws = Workspace(self, [scenario("fix-a")])
        busy = json.dumps({"mode": "apply", "busy": True, "freedBytes": 0, "actions": 0, "errors": []})
        tool = os.path.join(ws.dir, "retention-busy.sh")
        with open(tool, "w") as handle:
            handle.write("#!/bin/sh\necho '%s'\n" % busy)
        os.chmod(tool, 0o755)
        result = ws.evidence(E2E_RETENTION_CMD=tool)
        self.assertEqual(result.code, 0, result.out)
        self.assertTrue(ws.manifest()["retention"]["ok"])
        self.assertIn("RETENTION skipped, another retention is running", result.out)


class ChangedSinceTests(unittest.TestCase):
    def commit_catalog(self, ws, *scenarios):
        path = os.path.join(ws.repo, "e2e", "catalog")
        os.makedirs(path)
        with open(os.path.join(path, "scenarios.json"), "w") as handle:
            json.dump(support.catalog(*scenarios), handle)
        ws.git("add", "-A")
        ws.git("commit", "-q", "-m", "catalog")

    def test_a_ref_that_is_not_a_commit_is_exit_2(self):
        ws = Workspace(self, [scenario("fix-a")])
        done = ws.run("list", "--changed-since", "no-such-ref")
        self.assertEqual(done.code, 2, done.out)
        self.assertIn("not a commit of this repository", done.err)
        self.assertEqual(ws.diagnose("ios", "--changed-since", "no-such-ref").code, 2)

    def test_a_base_without_the_catalog_changes_every_scenario(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")])
        done = ws.run("list", "--changed-since", "HEAD")  # the catalog is not committed at HEAD
        self.assertEqual(done.code, 0, done.err)
        self.assertEqual([line.split()[0] for line in done.lines], ["fix-a", "fix-b"])

    def test_only_the_scenarios_whose_steps_changed_since_the_base_are_listed(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")])
        self.commit_catalog(ws, scenario("fix-a"), scenario("fix-b"))
        changed = scenario("fix-b")
        changed["stepsHash"] = "other"
        ws.write_catalog(scenario("fix-a"), changed)
        done = ws.run("list", "--changed-since", "HEAD")
        self.assertEqual([line.split()[0] for line in done.lines], ["fix-b"])


if __name__ == "__main__":
    unittest.main()
