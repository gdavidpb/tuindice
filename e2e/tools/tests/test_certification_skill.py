"""inspect_certification_state.py: the verdicts of `e2e.py status --json`, the session counters and the stop conditions."""

import contextlib
import importlib.util
import io
import json
import os
import shutil
import sys
import tempfile
import unittest
from pathlib import Path

TESTS = os.path.dirname(os.path.abspath(__file__))
SCRIPT = os.path.join(TESTS, "..", "..", "..", ".codex", "skills", "certify-tuindice-pr", "scripts",
    "inspect_certification_state.py")
HEAD = "a" * 40
FP = "c" * 64


def load_module():
    spec = importlib.util.spec_from_file_location("inspect_certification_state", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module  # dataclasses look their module up here
    spec.loader.exec_module(module)
    return module


inspector = load_module()


def stamp(seconds):
    """Timestamps of the manifests: an hour-aligned base plus an offset."""
    import datetime
    base = datetime.datetime(2026, 10, 1, 8, 0, 0, tzinfo=datetime.timezone.utc) + datetime.timedelta(seconds=seconds)
    return base.strftime("%Y-%m-%dT%H:%M:%SZ")


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as handle:
        json.dump(data, handle)


def info(verdict, **extra):
    base = {"platform": "ios", "fingerprint": FP, "verdict": verdict, "inScope": 4, "green": ["a", "b"],
        "pending": ["c", "d"], "failed": [], "exhausted": [], "evidence": None,
        "remote": {"reachable": True, "checked": 1, "truncated": False}}
    base.update(extra)
    return base


class SessionFixtureMixin:
    def setUp(self):
        self.root = Path(tempfile.mkdtemp(prefix="e2e-skill-"))
        self.addCleanup(shutil.rmtree, str(self.root), True)

    def manifest(self, run_id, platform="ios", exit_code=0, start=0, seconds=600, sha=HEAD, mode="evidence",
            outcome="passed", lock=True):
        write_json(self.root / "runs" / run_id / "manifest.json", {
            "runId": run_id, "platform": platform, "mode": mode, "exitCode": exit_code, "outcome": outcome,
            "startedAt": stamp(start), "durationSeconds": seconds, "commitSha": sha,
            "toolchain": {"lockMatches": lock}})

    def ledger(self, platform, fingerprint, created, scenarios, sha=HEAD):
        data = {"platform": platform, "fingerprint": fingerprint, "createdAt": stamp(created), "scenarios": {}}
        for scenario_id, status in scenarios.items():
            attempt = {"outcome": "passed" if status == "passed" else "failed", "countsAgainstCap": True, "sha": sha}
            data["scenarios"][scenario_id] = {"status": status, "attempts": [attempt]}
        write_json(self.root / "ledger" / platform / fingerprint / "ledger.json", data)


class SessionCounterTests(SessionFixtureMixin, unittest.TestCase):
    def test_only_evidence_invocations_of_this_branch_count(self):
        self.manifest("r1", start=0)
        self.manifest("r2", start=1000, mode="diagnose")
        self.manifest("r3", start=2000, exit_code=2, outcome="failed")
        self.manifest("r4", start=3000, outcome="not_required")
        self.manifest("r5", start=4000, sha="b" * 40)
        self.manifest("r6", start=5000, platform="android")
        write_json(self.root / "runs" / "bad" / "manifest.json", ["not", "a", "manifest"])
        runs = inspector.evidence_runs(self.root, {HEAD})
        self.assertEqual([r["runId"] for r in runs], ["r1", "r6"])
        self.assertEqual([r["runId"] for r in inspector.evidence_runs(self.root, {HEAD}, "ios")], ["r1"])
        self.assertEqual([r["runId"] for r in inspector.evidence_runs(self.root, None, "ios")], ["r1", "r5"])

    def test_a_run_that_was_killed_before_it_wrote_its_exit_still_counts(self):
        self.manifest("r1", exit_code=None, outcome="running")
        self.assertEqual(len(inspector.evidence_runs(self.root, {HEAD})), 1)

    def test_evidence_hours_count_platforms_that_ran_together_once(self):
        self.manifest("a", platform="android", start=0, seconds=3600)
        self.manifest("i", platform="ios", start=600, seconds=3000)
        runs = inspector.evidence_runs(self.root, {HEAD})
        self.assertAlmostEqual(inspector.evidence_hours(runs), 1.0)

    def test_evidence_hours_count_a_partial_overlap_once(self):
        self.manifest("a", platform="android", start=0, seconds=3600)
        self.manifest("i", platform="ios", start=1800, seconds=3600)
        self.assertAlmostEqual(inspector.evidence_hours(inspector.evidence_runs(self.root, {HEAD})), 1.5)

    def test_evidence_hours_add_runs_that_did_not_overlap(self):
        self.manifest("a", start=0, seconds=3600)
        self.manifest("b", start=7200, seconds=1800)
        self.assertAlmostEqual(inspector.evidence_hours(inspector.evidence_runs(self.root, {HEAD})), 1.5)

    def test_a_scenario_failing_under_the_two_latest_fingerprints_is_reported(self):
        self.ledger("ios", "1" * 64, 0, {"x": "failed", "y": "failed", "z": "passed"})
        self.ledger("ios", "2" * 64, 100, {"x": "failed", "y": "passed", "z": "failed"})
        self.ledger("ios", "3" * 64, 200, {"x": "passed"}, sha="b" * 40)
        self.assertEqual(inspector.repeated_failures(self.root, "ios", {HEAD}), ["x"])

    def test_a_single_fingerprint_or_a_fix_that_worked_reports_nothing(self):
        self.ledger("ios", "1" * 64, 0, {"x": "failed"})
        self.assertEqual(inspector.repeated_failures(self.root, "ios", {HEAD}), [])
        self.ledger("ios", "2" * 64, 100, {"x": "passed"})
        self.assertEqual(inspector.repeated_failures(self.root, "ios", {HEAD}), [])

    def test_the_drift_warning_reads_the_last_complete_run(self):
        write_json(self.root / "ledger" / "ios" / "index.json", [
            {"fingerprint": "1" * 64, "completeAtSha": "old", "completedAt": stamp(0)},
            {"fingerprint": "2" * 64, "completeAtSha": "new", "completedAt": stamp(500)}])
        asked = []

        def fake_git(*args):
            asked.append(args)
            if args[0] == "cat-file":
                return inspector.GitResult(0, "", "")
            if args[0] == "rev-list":
                return inspector.GitResult(0, "23", "")
            return inspector.GitResult(0, "\n".join("f%d" % i for i in range(7)), "")

        original, inspector.run_git = inspector.run_git, fake_git
        self.addCleanup(setattr, inspector, "run_git", original)
        self.assertEqual(inspector.drift_since_last_complete_run(self.root, "ios", HEAD), (23, 7))
        self.assertIn(("rev-list", "--count", "new.." + HEAD), asked)
        self.assertIsNone(inspector.drift_since_last_complete_run(self.root, "android", HEAD))


class StopConditionTests(unittest.TestCase):
    def run_of(self, exit_code, run_id="r"):
        return {"runId": run_id, "platform": "ios", "exitCode": exit_code, "start": 0.0, "seconds": 60.0, "lockMatches": True}

    def test_a_platform_that_is_done_never_stops(self):
        for verdict in ("current", "reusable"):
            self.assertEqual(inspector.stop_reasons(verdict, [self.run_of(5)] * 5, ["x"]), [])

    def test_exit_5_and_exit_7_as_the_last_run_stop(self):
        for code in (5, 7):
            reasons = inspector.stop_reasons("partial", [self.run_of(1), self.run_of(code, "last")], [])
            self.assertEqual(len(reasons), 1, reasons)
            self.assertIn("exit %d" % code, reasons[0])
        self.assertEqual(inspector.stop_reasons("partial", [self.run_of(5), self.run_of(1)], []), [])

    def test_an_exhausted_ledger_stops_even_when_the_last_run_was_something_else(self):
        self.assertEqual(len(inspector.stop_reasons("exhausted", [self.run_of(1)], [])), 1)
        self.assertEqual(len(inspector.stop_reasons("exhausted", [self.run_of(7)], [])), 1)

    def test_the_same_scenario_under_two_fingerprints_stops(self):
        reasons = inspector.stop_reasons("partial", [], ["auth-login-success"])
        self.assertIn("two consecutive fingerprints: auth-login-success", reasons[0])

    def test_three_invocations_without_a_result_stop(self):
        self.assertEqual(inspector.stop_reasons("partial", [self.run_of(1)] * 2, []), [])
        self.assertEqual(len(inspector.stop_reasons("partial", [self.run_of(1)] * 3, [])), 1)

    def test_two_environment_exits_or_four_hours_stop_the_session(self):
        self.assertEqual(inspector.session_stop_reasons([self.run_of(3), self.run_of(1)], 3.9), [])
        self.assertEqual(len(inspector.session_stop_reasons([self.run_of(3), self.run_of(3)], 1.0)), 1)
        self.assertEqual(len(inspector.session_stop_reasons([], 4.0)), 1)


class ReportTests(SessionFixtureMixin, unittest.TestCase):
    def test_a_current_platform_passes_and_names_where_the_evidence_is(self):
        lines, reasons = inspector.report_platform("ios", info("current", evidence={"sha": HEAD, "source": "remote"}), [], [], None)
        self.assertTrue(lines[0].startswith("[PASS] ios fp=cccccccccccc verdict=current"), lines)
        self.assertIn("evidence from aaaaaaa (remote)", lines[0])
        self.assertEqual(reasons, [])

    def test_every_verdict_that_is_not_done_fails_and_says_what_to_do(self):
        expected = {"unpublished": "publish --platform ios", "partial": "./gradlew e2eEvidenceIos",
            "rerun": "./gradlew e2eEvidenceIos", "exhausted": "refused with exit 7"}
        for verdict, action in expected.items():
            lines, _ = inspector.report_platform("ios", info(verdict), [], [], None)
            self.assertTrue(lines[0].startswith("[FAIL]"), lines)
            self.assertIn(action, "\n".join(lines))

    def test_failed_scenarios_show_their_class_and_summary(self):
        failed = [{"id": "auth-login-success", "class": "typed_text_mismatch", "attempts": 1, "exhausted": False,
            "summary": "typed '123456' but the backend received '12456'"}]
        lines, _ = inspector.report_platform("ios", info("partial", failed=failed), [], [], None)
        self.assertIn("failed: auth-login-success class=typed_text_mismatch attempts=1: typed '123456'", "\n".join(lines))

    def test_the_fallback_to_the_ledger_and_a_truncated_scan_are_said(self):
        lines, _ = inspector.report_platform("ios", info("rerun", remote={"reachable": False, "checked": 1, "truncated": False}), [], [], None)
        self.assertIn("GitHub could not be read", "\n".join(lines))
        lines, _ = inspector.report_platform("ios", info("rerun", remote={"reachable": True, "checked": 100, "truncated": True}), [], [], None)
        self.assertIn("only 100 candidate commits", "\n".join(lines))

    def test_the_drift_warning_appears_only_past_either_limit(self):
        quiet, _ = inspector.report_platform("ios", info("partial"), [], [], (20, 150))
        self.assertNotIn("WARN", "\n".join(quiet))
        for drift in ((21, 3), (3, 151)):
            lines, _ = inspector.report_platform("ios", info("partial"), [], [], drift)
            self.assertIn("certify in increments", "\n".join(lines))

    def test_the_toolchain_lock_of_the_last_run_is_reported(self):
        run = {"runId": "r", "platform": "ios", "exitCode": 3, "start": 0.0, "seconds": 5.0, "lockMatches": False}
        lines, _ = inspector.report_platform("ios", info("rerun"), [run], [], None)
        self.assertIn("toolchain lock DIFFERS", "\n".join(lines))

    def test_a_platform_the_status_could_not_compute_fails(self):
        lines, _ = inspector.report_platform("ios", {"error": "ledger is not valid JSON"}, [], [], None)
        self.assertTrue(lines[0].startswith("[FAIL] ios - status could not be computed"), lines)

    def run_report(self, status, platforms=("ios",)):
        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            result = inspector.report_evidence(list(platforms), status, HEAD, {HEAD}, self.root)
        return result, out.getvalue()

    def test_report_evidence_counts_failures_and_stops(self):
        self.manifest("r1", exit_code=5, outcome="stopped")
        (failures, stops), out = self.run_report({"platforms": {"ios": info("partial")}})
        self.assertEqual((failures, stops), (1, 1))
        self.assertIn("STOP: the last evidence run (r1) ended with exit 5", out)
        self.assertIn("do not try again", out)

    def test_report_evidence_is_quiet_when_every_platform_is_done(self):
        self.manifest("r1", exit_code=5, outcome="stopped")
        status = {"platforms": {"ios": info("current"), "android": info("reusable", platform="android")}}
        (failures, stops), out = self.run_report(status, ("ios", "android"))
        self.assertEqual((failures, stops), (0, 0))
        self.assertNotIn("STOP", out)

    def test_both_platforms_pending_suggest_the_aggregate_task(self):
        status = {"platforms": {"ios": info("rerun"), "android": info("rerun", platform="android")}}
        (failures, stops), out = self.run_report(status, ("android", "ios"))
        self.assertEqual((failures, stops), (2, 0))
        self.assertIn("./gradlew e2eEvidence\n", out)

    def test_session_limits_stop_while_a_platform_is_unfinished(self):
        self.manifest("a", platform="android", start=0, seconds=3 * 3600, exit_code=3, outcome="environment_refused")
        self.manifest("b", platform="ios", start=4 * 3600, seconds=3700, exit_code=3, outcome="environment_refused")
        (_failures, stops), out = self.run_report({"platforms": {"ios": info("rerun")}})
        self.assertEqual(stops, 2)
        self.assertIn("2 evidence runs ended with exit 3", out)
        self.assertIn("h of evidence in this session", out)


class MainExitCodeTests(SessionFixtureMixin, unittest.TestCase):
    def run_main(self, status, manifests=()):
        for args in manifests:
            self.manifest(*args[0], **args[1])
        scope = inspector.CertificationScope(requires_e2e=True, e2e_scope="ios", missing_version_bump="")

        def fake_git(*args):
            answers = {("branch", "--show-current"): "feat/x", ("rev-parse", "HEAD"): HEAD, ("status", "--porcelain"): "",
                ("rev-parse", "--abbrev-ref", "--symbolic-full-name", "@{u}"): "origin/feat/x", ("rev-parse", "@{u}"): HEAD}
            return inspector.GitResult(0, answers.get(args, ""), "")

        replaced = {"run_git": fake_git, "detect_certification_scope": lambda head: scope,
            "required_platforms": lambda: (["ios"], None), "load_status": lambda: (status, ""),
            "branch_commits": lambda head: {HEAD}, "state_root": lambda: self.root}
        saved = {name: getattr(inspector, name) for name in replaced}
        for name, value in replaced.items():
            setattr(inspector, name, value)
        self.addCleanup(lambda: [setattr(inspector, name, value) for name, value in saved.items()])
        with contextlib.redirect_stdout(io.StringIO()):
            return inspector.main()

    def test_exit_0_when_every_required_platform_is_current_or_reusable(self):
        self.assertEqual(self.run_main({"platforms": {"ios": info("reusable")}}), 0)

    def test_exit_1_when_evidence_is_still_to_be_produced(self):
        self.assertEqual(self.run_main({"platforms": {"ios": info("partial")}}), 1)

    def test_exit_2_when_a_stop_condition_holds(self):
        code = self.run_main({"platforms": {"ios": info("exhausted")}})
        self.assertEqual(code, 2)

    def test_exit_1_when_status_cannot_be_read(self):
        self.assertEqual(self.run_main(None), 1)


if __name__ == "__main__":
    unittest.main()
