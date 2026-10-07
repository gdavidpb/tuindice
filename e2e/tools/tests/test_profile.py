"""e2e/tools/e2e-profile.py on synthetic run directories (plan F27)."""

import importlib.util
import json
import os
import shutil
import subprocess
import sys
import tempfile
import unittest

import support

PROFILE = os.path.join(support.TESTS, "..", "e2e-profile.py")


def load_module():
    spec = importlib.util.spec_from_file_location("e2e_profile", PROFILE)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


profile = load_module()


def step(primitive, millis, outcome="passed", index=0):
    return {"index": index, "primitive": primitive, "target": "tag:x", "durationMs": millis, "outcome": outcome}


class Fixture:
    def __init__(self, test):
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-profile-test-"))
        test.addCleanup(shutil.rmtree, self.dir, True)
        self.root = os.path.join(self.dir, "state")
        self.count = 0

    def run(self, platform="ios", mode="diagnose", seconds=100, load=1.5, attempts=(), parallel="sequential", record_attempts=True,
            failed_classes=None):
        """`attempts`: (scenario, passed, seconds in process, steps, wall ms, runner ms) tuples, in execution order."""
        self.count += 1
        run_id = "20260101T%06dZ-%s-%s-abc1234" % (self.count, platform, mode)
        path = os.path.join(self.root, "runs", run_id)
        results, recorded, numbers = {}, [], {}
        for scenario, passed, inside, steps, wall, runner in attempts:
            n = numbers[scenario] = numbers.get(scenario, 0) + 1
            directory = os.path.join(path, "scenarios", scenario, "attempt-%d" % n)
            os.makedirs(directory)
            end = "2026-01-01T00:%02d:%02dZ" % (inside // 60, inside % 60)
            with open(os.path.join(directory, "result.json"), "w") as handle:
                json.dump({"scenarioId": scenario, "outcome": "passed" if passed else "failed", "startedAt": "2026-01-01T00:00:00Z",
                    "finishedAt": end, "steps": steps, "failure": None}, handle)
            if not passed:
                with open(os.path.join(directory, "classification.json"), "w") as handle:
                    json.dump({"class": (failed_classes or {}).get(scenario, "product_assertion")}, handle)
            recorded.append({"scenario": scenario, "n": n, "durationMs": wall, "runnerDurationMs": runner})
            results[scenario] = {"id": scenario, "status": "passed" if passed else "failed", "durationMs": wall,
                "failureClasses": [] if passed else [(failed_classes or {}).get(scenario, "product_assertion")]}
        manifest = {"schema": "tuindice-e2e-run/1", "runId": run_id, "mode": mode, "platform": platform, "outcome": "passed",
            "exitCode": 0, "durationSeconds": seconds, "parallel": {"decision": parallel}, "load": {"max1m": load},
            "phases": [{"name": "build", "durationSeconds": 6.5, "ok": True}], "scenarios": {"executed": len(results), "failed": 0},
            "results": list(results.values())}
        if record_attempts:
            manifest["attempts"] = recorded
        os.makedirs(path, exist_ok=True)
        with open(os.path.join(path, "manifest.json"), "w") as handle:
            json.dump(manifest, handle)
        return run_id

    def cli(self, *args, **env):
        done = subprocess.run([sys.executable, PROFILE, "--state-root", self.root] + list(args),
            env=dict(os.environ, PYTHONDONTWRITEBYTECODE="1", **env), stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            universal_newlines=True, timeout=60)
        done.json = json.loads(done.stdout) if "--json" in args and done.stdout.strip() else None
        return done


class HarnessRunTests(unittest.TestCase):
    """The profile of a run the harness itself wrote, with the fake adapter."""

    def test_a_real_run_profiles_with_runner_time_from_the_manifest(self):
        ws = support.Workspace(self, [support.scenario("fix-a"), support.scenario("fix-b")],
            {"behaviours": {"fix-b": ["fail:assertion", "pass"]}})
        self.assertEqual(ws.diagnose("ios").code, 0)
        done = subprocess.run([sys.executable, PROFILE, "--state-root", ws.state, "--json", "--no-write"], stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, universal_newlines=True, timeout=60)
        self.assertEqual(done.returncode, 0, done.stderr)
        data = json.loads(done.stdout)
        self.assertEqual([(a["scenario"], a["attempt"], a["outcome"]) for a in data["attempts"]],
            [("fix-a", 1, "passed"), ("fix-b", 1, "failed"), ("fix-b", 2, "passed")])
        for row in data["attempts"]:
            self.assertIsNotNone(row["wallMs"])
            self.assertIsNotNone(row["runnerMs"])
            self.assertEqual(row["inProcessMs"], 1000, "the fake runner reports one second")
        self.assertEqual(data["primitives"]["WaitVisible"]["count"], 3)
        self.assertEqual(data["primitives"]["Tap"]["totalMs"], 21)


class PercentileTests(unittest.TestCase):
    def test_nearest_rank(self):
        self.assertEqual(profile.percentile([40, 10, 30, 20], 50), 20)
        self.assertEqual(profile.percentile(list(range(1, 101)), 95), 95)
        self.assertEqual(profile.percentile(list(range(1, 21)), 95), 19)
        self.assertEqual(profile.percentile(list(range(1, 11)), 95), 10)
        self.assertEqual(profile.percentile([7], 95), 7)
        self.assertEqual(profile.percentile([7], 50), 7)
        self.assertIsNone(profile.percentile([], 50))

    def test_times_parse_with_or_without_fractions(self):
        whole = profile.parse_time("2026-10-07T04:21:10Z")
        fraction = profile.parse_time("2026-10-07T04:21:25.469043Z")
        long = profile.parse_time("2026-10-07T04:21:25.469043123Z")
        self.assertEqual((fraction - whole).total_seconds(), 15.469043)
        self.assertEqual(long, fraction)
        self.assertIsNone(profile.parse_time("yesterday"))


class RunProfileTests(unittest.TestCase):
    def setUp(self):
        self.fx = Fixture(self)

    def profile(self, **kwargs):
        run_id = self.fx.run(**kwargs)
        return profile.profile_run(profile.locate(self.fx.root, run_id))

    def test_per_primitive_counts_and_percentiles_skip_containers_and_skipped_steps(self):
        steps = [step("Tap", 100), step("Tap", 300), step("Tap", 200), step("Tap", 9000, "skipped"),
            step("WaitVisible", 50), step("OnPlatform", 700), step("Group", 800), step("IfVisible", 5), step("Retry", 6)]
        data = self.profile(attempts=[("a", True, 30, steps, 45000, 40000)])
        self.assertEqual(data["primitives"], {
            "Tap": {"count": 3, "p50Ms": 200, "p95Ms": 300, "maxMs": 300, "totalMs": 600},
            "WaitVisible": {"count": 1, "p50Ms": 50, "p95Ms": 50, "maxMs": 50, "totalMs": 50}})

    def test_overhead_is_the_runner_minus_the_scenario_and_the_harness_the_wall_minus_the_runner(self):
        data = self.profile(attempts=[("a", True, 30, [step("Tap", 1)], 60000, 50000)])
        row = data["attempts"][0]
        self.assertEqual((row["wallMs"], row["runnerMs"], row["inProcessMs"]), (60000, 50000, 30000))
        self.assertEqual((row["invocationMs"], row["harnessMs"]), (20000, 10000))
        self.assertEqual(data["invocationOverhead"], {"attempts": 1, "p50Ms": 20000, "maxMs": 20000, "overThreshold": False})

    def test_a_median_invocation_overhead_over_forty_seconds_is_flagged(self):
        data = self.profile(attempts=[("a", True, 10, [], 90000, 60000), ("b", True, 10, [], 90000, 55000), ("c", True, 10, [], 20000, 15000)])
        self.assertEqual(data["invocationOverhead"]["p50Ms"], 45000)
        self.assertTrue(data["invocationOverhead"]["overThreshold"])
        self.assertIn("above the 40 s threshold", "\n".join(profile.render_profile(data)))

    def test_a_run_of_an_older_manifest_still_profiles_with_what_it_has(self):
        data = self.profile(attempts=[("a", False, 9, [step("Tap", 5)], 23000, 21000), ("a", False, 10, [step("Tap", 6)], 24000, 22000)],
            record_attempts=False)
        first, second = data["attempts"]
        self.assertIsNone(first["wallMs"], "an older manifest only keeps the wall time of the last attempt")
        self.assertEqual((second["wallMs"], second["runnerMs"], second["invocationMs"]), (24000, None, None))
        self.assertEqual((first["inProcessMs"], second["inProcessMs"]), (9000, 10000))
        self.assertIsNone(data["invocationOverhead"])
        self.assertIn("not recorded for this run", "\n".join(profile.render_profile(data)))
        self.assertEqual(data["primitives"]["Tap"]["count"], 2)

    def test_a_skipped_result_does_not_pass_for_a_wall_time_of_zero(self):
        run_id = self.fx.run(attempts=[("a", True, 5, [], 1, 1)], record_attempts=False)
        path = os.path.join(self.fx.root, "runs", run_id, "manifest.json")
        manifest = json.loads(support.text(path))
        manifest["results"] = [{"id": "a", "status": "skipped", "durationMs": 0, "failureClasses": []}]
        with open(path, "w") as handle:
            json.dump(manifest, handle)
        data = profile.profile_run(profile.locate(self.fx.root, run_id))
        self.assertIsNone(data["attempts"][0]["wallMs"])

    def test_failures_are_counted_by_class(self):
        data = self.profile(attempts=[("a", False, 5, [], 1, 1), ("b", False, 5, [], 1, 1), ("c", True, 5, [], 1, 1)],
            failed_classes={"b": "timeout"})
        self.assertEqual(data["failuresByClass"], {"product_assertion": 1, "timeout": 1})

    def test_an_attempt_without_result_json_is_listed_as_no_result(self):
        run_id = self.fx.run(attempts=[("a", True, 5, [], 1, 1)])
        os.remove(os.path.join(self.fx.root, "runs", run_id, "scenarios", "a", "attempt-1", "result.json"))
        data = profile.profile_run(profile.locate(self.fx.root, run_id))
        self.assertEqual((data["attempts"][0]["outcome"], data["attempts"][0]["inProcessMs"]), ("no result", None))


class CommandLineTests(unittest.TestCase):
    def setUp(self):
        self.fx = Fixture(self)

    def test_the_default_profiles_the_latest_run_of_each_platform_and_writes_it_down(self):
        self.fx.run("ios", attempts=[("old", True, 5, [step("Tap", 1)], 10, 8)])
        newest = self.fx.run("ios", attempts=[("a", True, 30, [step("Tap", 700), step("Tap", 500)], 60000, 50000)])
        android = self.fx.run("android", attempts=[("a", True, 20, [step("Swipe", 90)], 30000, 25000)])
        done = self.fx.cli()
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertIn("Run %s" % newest, done.stdout)
        self.assertIn("Run %s" % android, done.stdout)
        self.assertNotIn("old ", done.stdout)
        self.assertIn("Tap", done.stdout)
        self.assertIn("invocation overhead over 1 attempt(s): p50 20.0s", done.stdout)
        written = json.loads(support.text(os.path.join(self.fx.root, "profiles", newest + ".json")))
        self.assertEqual(written["primitives"]["Tap"]["totalMs"], 1200)

    def test_no_write_leaves_the_state_alone_and_an_unwritable_profile_directory_is_only_a_warning(self):
        self.fx.run("ios", attempts=[("a", True, 5, [], 10, 8)])
        self.assertEqual(self.fx.cli("--no-write").returncode, 0)
        self.assertFalse(os.path.exists(os.path.join(self.fx.root, "profiles")))
        with open(os.path.join(self.fx.root, "profiles"), "w") as handle:
            handle.write("a file where the directory should be")
        done = self.fx.cli()
        self.assertEqual(done.returncode, 0)
        self.assertIn("the profile was not written", done.stderr)

    def test_a_named_run_by_id_or_by_directory(self):
        first = self.fx.run("ios", attempts=[("a", True, 5, [step("Tap", 11)], 10, 8)])
        self.fx.run("ios", attempts=[("b", True, 5, [step("Tap", 22)], 10, 8)])
        by_id = self.fx.cli("--run", first, "--json", "--no-write")
        self.assertEqual(by_id.json["runId"], first)
        by_dir = self.fx.cli("--run", os.path.join(self.fx.root, "runs", first), "--json", "--no-write")
        self.assertEqual(by_dir.json["primitives"]["Tap"]["totalMs"], 11)
        self.assertEqual(self.fx.cli("--run", "no-such-run").returncode, 2)

    def test_last_pools_the_samples_of_several_runs(self):
        for millis in (100, 200, 300, 400):
            self.fx.run("ios", attempts=[("a", True, 5, [step("Tap", millis)], 10, 8)])
        done = self.fx.cli("--last", "3", "--json")
        self.assertEqual(done.json["primitives"]["Tap"], {"count": 3, "p50Ms": 300, "p95Ms": 400, "maxMs": 400, "totalMs": 900})
        self.assertEqual(len(done.json["runs"]), 3)

    def test_no_runs_is_exit_1_and_contradictory_questions_are_exit_2(self):
        self.assertEqual(self.fx.cli().returncode, 1)
        self.fx.run("ios", attempts=[("a", True, 5, [], 10, 8)])
        self.assertEqual(self.fx.cli("--compare", "0").returncode, 2)
        self.assertEqual(self.fx.cli("--compare", "1", "--last", "2").returncode, 2)
        self.assertEqual(self.fx.cli("--platform", "android").returncode, 1)


class CompareTests(unittest.TestCase):
    def setUp(self):
        self.fx = Fixture(self)

    def three(self):
        a = self.fx.run("ios", mode="evidence", seconds=100, load=1.0, attempts=[
            ("slow", True, 30, [step("Tap", 400)], 1, 1), ("same", True, 10, [step("Tap", 500)], 1, 1)])
        self.fx.run("android", mode="evidence", seconds=40, attempts=[("x", True, 5, [], 1, 1)])
        b = self.fx.run("ios", mode="evidence", seconds=130, load=4.0, parallel="parallel", attempts=[
            ("slow", True, 55, [step("Tap", 700)], 1, 1), ("same", True, 10, [step("Tap", 500)], 1, 1), ("new", False, 3, [], 1, 1)])
        return a, b

    def test_the_latest_run_is_compared_with_the_one_before_on_the_same_platform(self):
        a, b = self.three()
        done = self.fx.cli("--compare", "1", "--platform", "ios", "--json")
        self.assertEqual(done.returncode, 0, done.stderr)
        data = done.json[0]
        self.assertEqual([r["runId"] for r in data["runs"]], [b, a])
        self.assertEqual(data["durationDeltaSeconds"], 30)
        self.assertTrue(data["sameMode"])
        self.assertEqual(data["scenarioDeltas"], [{"scenario": "slow", "deltaMs": 25000, "beforeMs": 30000, "afterMs": 55000}])
        self.assertEqual(data["primitiveP50Deltas"], [{"primitive": "Tap", "deltaMs": 100}])

    def test_the_table_lists_mode_parallel_decision_duration_load_and_failures_by_class(self):
        a, b = self.three()
        done = self.fx.cli("--compare", "1", "--platform", "ios")
        self.assertEqual(done.returncode, 0, done.stderr)
        text = done.stdout
        for expected in (a, b, "parallel", "130s", "4.00", "product_assertion=1", "duration +30 s against the previous run", "slow"):
            self.assertIn(expected, text)

    def test_compare_without_a_number_is_one_and_n_reaches_n_runs_back(self):
        for _ in range(4):
            self.fx.run("ios", attempts=[("a", True, 5, [], 10, 8)])
        bare = self.fx.cli("--compare", "--platform", "ios", "--json")
        self.assertEqual(len(bare.json[0]["runs"]), 2)
        three = self.fx.cli("--compare", "3", "--platform", "ios", "--json")
        self.assertEqual(len(three.json[0]["runs"]), 4)
        more = self.fx.cli("--compare", "9", "--platform", "ios", "--json")
        self.assertEqual(len(more.json[0]["runs"]), 4, "only four runs exist")

    def test_both_platforms_are_compared_apart_when_none_is_named(self):
        self.three()
        self.fx.run("android", mode="evidence", seconds=55, attempts=[("x", True, 5, [], 1, 1)])
        done = self.fx.cli("--compare", "1", "--json")
        self.assertEqual(sorted(c["platform"] for c in done.json), ["android", "ios"])
        android = [c for c in done.json if c["platform"] == "android"][0]
        self.assertEqual(android["durationDeltaSeconds"], 15)

    def test_a_platform_with_one_run_says_there_is_nothing_to_compare(self):
        self.fx.run("ios", attempts=[("a", True, 5, [], 10, 8)])
        done = self.fx.cli("--compare", "1")
        self.assertEqual(done.returncode, 0)
        self.assertIn("nothing to compare", done.stdout)

    def test_runs_of_different_modes_are_flagged_as_not_comparable(self):
        self.fx.run("ios", mode="diagnose", attempts=[("a", True, 5, [], 10, 8)])
        self.fx.run("ios", mode="evidence", attempts=[("a", True, 5, [], 10, 8)])
        self.assertIn("the modes differ", self.fx.cli("--compare", "1").stdout)


if __name__ == "__main__":
    unittest.main()
