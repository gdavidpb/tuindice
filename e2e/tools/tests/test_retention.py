"""e2e/tools/e2e-retention.py on synthetic state trees, and the harness hook that runs it at the end of a run (plan F27)."""

import fcntl
import json
import os
import shutil
import subprocess
import sys
import tempfile
import unittest

import support
from support import Workspace, scenario

RETENTION = os.path.join(support.TESTS, "..", "e2e-retention.py")
MB = 1024 * 1024


def line_text(size, prefix="line"):
    """About `size` bytes of numbered lines, so a cut at a line boundary can be told from a cut in the middle of one."""
    lines, total, number = [], 0, 0
    while total < size:
        line = "%s %07d\n" % (prefix, number)
        lines.append(line)
        total += len(line)
        number += 1
    return "".join(lines)


class State:
    """A throwaway state root with the layout the harness leaves: runs, ledgers, certifications and legacy leftovers."""

    def __init__(self, test):
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-retention-test-"))
        test.addCleanup(shutil.rmtree, self.dir, True)
        self.root = os.path.join(self.dir, "state")
        self.tmp = os.path.join(self.dir, "tmp")
        os.makedirs(self.root)
        os.makedirs(self.tmp)
        self.count = 0

    def put(self, relative, content="", base=None):
        path = os.path.join(base or self.root, relative)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "wb") as handle:
            handle.write(content if isinstance(content, bytes) else content.encode())
        return path

    def blob(self, relative, size, base=None):
        return self.put(relative, b"x" * size, base)

    def run(self, platform="ios", mode="evidence", outcome="passed", parent=None, age_days=0):
        """A run directory with a manifest; ids sort by creation order. Returns its id."""
        self.count += 1
        run_id = "20260101T%06dZ-%s-%s-abc1234" % (self.count, platform, mode)
        manifest = {"schema": "tuindice-e2e-run/1", "runId": run_id, "platform": platform, "mode": mode, "outcome": outcome,
            "parentRunId": parent}
        path = self.put("runs/%s/manifest.json" % run_id, json.dumps(manifest))
        if age_days:
            old = os.path.getmtime(path) - age_days * 86400
            os.utime(path, (old, old))
        return run_id

    def attempt(self, run_id, scenario_id="auth-login", n=1, passed=True, runner_log=2000, xcresult=0, screenshot=0, extra=None):
        """An attempt directory the way the harness fills it. Returns its path."""
        base = "runs/%s/scenarios/%s/attempt-%d" % (run_id, scenario_id, n)
        self.put(base + "/result.json", json.dumps({"scenarioId": scenario_id, "outcome": "passed" if passed else "failed"}))
        self.put(base + "/runner.log", line_text(runner_log))
        self.put(base + "/wiremock-requests.json", "{}")
        self.put(base + "/driver.log", "[0] step\n")
        self.put(base + "/results/%s/result.json" % scenario_id, "{}")
        if not passed:
            self.put(base + "/classification.json", json.dumps({"class": "product_assertion"}))
            self.put(base + "/crash.txt", "FATAL\n")
        if xcresult:
            self.blob(base + "/attempt.xcresult/Data/blob", xcresult)
        if screenshot:
            self.blob(base + "/fallback-screen.png", screenshot)
        for name, size in (extra or {}).items():
            self.blob(base + "/" + name, size)
        return os.path.join(self.root, base)

    def ledger(self, platform, fingerprint, run_ids=(), published=False, age_seconds=0):
        data = {"schema": "tuindice-e2e-ledger/1", "platform": platform, "fingerprint": fingerprint,
            "scenarios": {"auth-login": {"status": "passed", "attempts": [{"runId": r, "outcome": "passed"} for r in run_ids]}},
            "publications": [{"sha": "s", "context": "c", "description": "d", "runId": run_ids[0] if run_ids else "publish"}] if published else []}
        path = self.put("ledger/%s/%s/ledger.json" % (platform, fingerprint), json.dumps(data))
        if age_seconds:
            old = os.path.getmtime(path) - age_seconds
            os.utime(path, (old, old))
        return os.path.dirname(path)

    def snapshot(self, base=None):
        """{relative path: size} of every file and symlink under `base`."""
        base = base or self.dir
        found = {}
        for directory, _names, files in os.walk(base):
            for name in files:
                path = os.path.join(directory, name)
                found[os.path.relpath(path, base)] = os.lstat(path).st_size
        return found

    def exists(self, relative, base=None):
        return os.path.lexists(os.path.join(base or self.root, relative))

    def retention(self, *args, **env):
        """Runs the tool against this state; `--legacy-tmp-parent` defaults to the fixture's temp directory only."""
        argv = [sys.executable, RETENTION, "--state-root", self.root, "--tmp-root", os.path.join(self.tmp, "tuindice-e2e")]
        if "--legacy-tmp-parent" not in args:
            argv += ["--legacy-tmp-parent", self.tmp]
        done = subprocess.run(argv + list(args), env=dict(os.environ, PYTHONDONTWRITEBYTECODE="1", **env), stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, universal_newlines=True, timeout=120)
        done.summary = json.loads(done.stdout) if "--json" in args and done.stdout.strip().startswith("{") else None
        return done


class PolicyTests(unittest.TestCase):
    def setUp(self):
        self.state = State(self)

    def test_the_default_is_a_simulation_that_changes_nothing_and_says_what_it_would_free(self):
        old = self.state.run()
        self.state.attempt(old, passed=True, runner_log=2 * MB, xcresult=50000)
        for _ in range(10):
            self.state.attempt(self.state.run(), passed=True)
        before = self.state.snapshot()
        for flags in ((), ("--dry-run",)):
            done = self.state.retention(*flags)
            self.assertEqual(done.returncode, 0, done.stderr)
            self.assertIn("SIMULATION", done.stdout)
            self.assertIn("WOULD DELETE-RUN", done.stdout)
            self.assertRegex(done.stdout, r"would free [\d.]+ [KM]B in \d+ action")
            self.assertEqual(self.state.snapshot(), before)

    def test_apply_and_dry_run_contradict_each_other(self):
        done = self.state.retention("--apply", "--dry-run")
        self.assertNotEqual(done.returncode, 0)
        self.assertIn("contradict", done.stderr)

    def test_a_passing_attempt_keeps_only_its_verdict_and_the_last_megabyte_of_the_runner_log(self):
        run = self.state.run()
        attempt = self.state.attempt(run, passed=True, runner_log=int(1.5 * MB), xcresult=40000, screenshot=30000)
        done = self.state.retention("--apply")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(sorted(os.listdir(attempt)), ["result.json", "runner.log"])
        log = support.text(os.path.join(attempt, "runner.log"))
        original = line_text(int(1.5 * MB))
        self.assertLessEqual(len(log), MB)
        self.assertGreater(len(log), MB - 20)
        self.assertEqual(log, original[original.index(log.splitlines()[0] + "\n"):], "a suffix of the log that starts on a line")
        self.assertTrue(log.startswith("line "))

    def test_a_second_pass_finds_nothing_left_to_do(self):
        self.state.attempt(self.state.run(), passed=True, runner_log=2 * MB, xcresult=1000)
        self.assertEqual(self.state.retention("--apply").returncode, 0)
        again = self.state.retention("--apply", "--json")
        self.assertEqual(again.summary["actions"], 0, again.stdout)

    def test_a_failing_attempt_is_kept_complete_when_it_fits_its_cap(self):
        run = self.state.run(outcome="failed")
        attempt = self.state.attempt(run, passed=False, runner_log=5000, xcresult=60000, screenshot=30000)
        before = self.state.snapshot(attempt)
        done = self.state.retention("--apply")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(self.state.snapshot(attempt), before)

    def test_a_failing_attempt_over_its_cap_loses_the_xcresult_first_and_never_its_verdict_or_logs(self):
        attempt = self.state.attempt(self.state.run(outcome="failed"), passed=False, runner_log=50000, xcresult=300000, screenshot=200000)
        self.state.retention("--apply", "--failed-cap-mb", "0.4")
        self.assertFalse(os.path.exists(os.path.join(attempt, "attempt.xcresult")), "the xcresult goes first")
        self.assertTrue(os.path.exists(os.path.join(attempt, "fallback-screen.png")), "the screenshot stays while the cap allows")
        self.state.retention("--apply", "--failed-cap-mb", "0.1")
        self.assertFalse(os.path.exists(os.path.join(attempt, "fallback-screen.png")))
        for name in ("result.json", "classification.json", "runner.log", "crash.txt", "wiremock-requests.json", "driver.log"):
            self.assertTrue(os.path.exists(os.path.join(attempt, name)), name)
        done = self.state.retention("--apply", "--failed-cap-mb", "0.01")
        self.assertTrue(os.path.exists(os.path.join(attempt, "runner.log")))
        self.assertIn("only verdict files and logs remain", done.stdout)

    def test_a_platform_keeps_its_last_ten_runs_and_the_other_platform_is_counted_apart(self):
        ios = [self.state.run("ios") for _ in range(13)]
        android = [self.state.run("android") for _ in range(3)]
        for run in ios + android:
            self.state.attempt(run, passed=True)
        self.state.attempt(ios[0], "auth-other", passed=False)
        done = self.state.retention("--apply")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual([r for r in ios if self.state.exists("runs/" + r)], ios[3:])
        self.assertEqual([r for r in android if self.state.exists("runs/" + r)], android)

    def test_the_failing_attempts_of_the_recent_runs_are_never_deleted(self):
        runs = [self.state.run("ios") for _ in range(12)]
        failed = [self.state.attempt(run, passed=False, xcresult=1000) for run in runs]
        self.state.retention("--apply")
        for index in range(2, 12):
            self.assertTrue(os.path.isdir(failed[index]), "attempt of run %d" % index)
            self.assertTrue(os.path.exists(os.path.join(failed[index], "attempt.xcresult")))
        self.assertFalse(os.path.exists(failed[0]))

    def test_runs_named_by_a_published_ledger_or_by_the_given_fingerprint_survive_the_count(self):
        runs = [self.state.run("ios") for _ in range(16)]
        self.state.ledger("ios", "pub" * 8, [runs[0]], published=True)
        self.state.ledger("ios", "unp" * 8, [runs[1]])
        self.state.ledger("ios", "head" * 6, [runs[2]])
        self.state.retention("--apply", "--fingerprint", "head" * 6)
        survivors = [r for r in runs if self.state.exists("runs/" + r)]
        self.assertEqual(survivors, [runs[0], runs[2]] + runs[6:])

    def test_only_the_latest_published_fingerprints_keep_protecting_their_runs(self):
        runs = [self.state.run("ios") for _ in range(20)]
        for index in range(7):  # seven published fingerprints, each naming one of the oldest runs; the oldest published is the oldest ledger
            self.state.ledger("ios", ("pub%d" % index) * 8, [runs[index]], published=True, age_seconds=(7 - index) * 100)
        self.state.retention("--apply")
        survivors = [r for r in runs if self.state.exists("runs/" + r)]
        self.assertEqual(survivors, runs[2:7] + runs[10:], "the 5 newest publications protect their run; the 2 oldest no longer do")

    def test_a_retention_that_finds_another_one_running_answers_json_not_text(self):
        lock = os.open(os.path.join(self.state.root, "retention.lock"), os.O_CREAT | os.O_RDWR)
        self.addCleanup(os.close, lock)
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        done = self.state.retention("--apply", "--json")
        self.assertEqual(done.returncode, 0)
        self.assertTrue(done.summary["busy"])
        self.assertEqual((done.summary["freedBytes"], done.summary["errors"]), (0, []))
        self.assertIn("another retention holds", self.state.retention("--apply").stdout)

    def test_the_run_in_progress_is_not_touched_even_when_it_is_the_oldest(self):
        runs = [self.state.run("ios") for _ in range(12)]
        live = self.state.attempt(runs[0], passed=True, xcresult=5000)
        before = self.state.snapshot(live)
        self.state.retention("--apply", "--current-run", runs[0])
        self.assertTrue(self.state.exists("runs/" + runs[0]))
        self.assertEqual(self.state.snapshot(live), before, "not even slimmed")
        self.assertFalse(self.state.exists("runs/" + runs[1]))

    def test_a_manifest_that_says_running_protects_its_run_until_it_is_stale(self):
        fresh = self.state.run("ios", outcome="running")
        stale = self.state.run("ios", outcome="running", age_days=2)
        for _ in range(10):
            self.state.run("ios")
        self.state.retention("--apply")
        self.assertTrue(self.state.exists("runs/" + fresh))
        self.assertFalse(self.state.exists("runs/" + stale))

    def test_the_directory_of_an_all_invocation_goes_with_its_last_run(self):
        gone_parent = "20260101T000000Z-all-evidence-abc1234"
        kept_parent = "20260101T000099Z-all-evidence-abc1234"
        self.state.put("runs/%s/summary.json" % gone_parent, "{}")
        self.state.put("runs/%s/summary.json" % kept_parent, "{}")
        old = [self.state.run("ios", parent=gone_parent) for _ in range(2)]
        recent = [self.state.run("ios", parent=kept_parent)] + [self.state.run("ios") for _ in range(9)]
        self.state.retention("--apply")
        self.assertFalse(any(self.state.exists("runs/" + r) for r in old))
        self.assertFalse(self.state.exists("runs/" + gone_parent))
        self.assertTrue(self.state.exists("runs/" + kept_parent))
        self.assertTrue(self.state.exists("runs/" + recent[0]))

    def test_an_all_directory_without_a_summary_is_a_run_being_set_up_and_is_not_touched(self):
        os.makedirs(os.path.join(self.state.root, "runs", "20260101T000000Z-all-evidence-abc1234"))
        self.state.retention("--apply")
        self.assertTrue(self.state.exists("runs/20260101T000000Z-all-evidence-abc1234"))


class CapTests(unittest.TestCase):
    def setUp(self):
        self.state = State(self)
        self.runs = [self.state.run("ios", outcome="failed") for _ in range(6)]
        self.attempts = [self.state.attempt(run, passed=False, runner_log=10000, xcresult=100000) for run in self.runs]

    def total(self):
        return sum(self.state.snapshot(self.state.root).values())

    def test_the_heavy_files_of_the_oldest_failing_attempts_go_before_any_run(self):
        cap_gb = (self.total() - 130000) / float(1024 ** 3)
        done = self.state.retention("--apply", "--max-gb", repr(cap_gb))
        self.assertEqual(done.returncode, 0, done.stderr)
        gone = [os.path.exists(os.path.join(a, "attempt.xcresult")) for a in self.attempts]
        self.assertEqual(gone, [False, False, True, True, True, True])
        self.assertTrue(all(self.state.exists("runs/" + r) for r in self.runs), "no run is deleted while trimming is enough")
        self.assertLessEqual(self.total(), cap_gb * 1024 ** 3)

    def test_when_trimming_is_not_enough_the_oldest_runs_go_and_protected_ones_stay(self):
        self.state.ledger("ios", "pub" * 8, [self.runs[1]], published=True)
        done = self.state.retention("--apply", "--max-gb", repr(140000 / float(1024 ** 3)))
        self.assertEqual(done.returncode, 0, done.stderr)
        survivors = [r for r in self.runs if self.state.exists("runs/" + r)]
        # The protected run keeps its 100 kB xcresult, so three of the five others have to go, oldest first.
        self.assertEqual(survivors, [self.runs[1], self.runs[4], self.runs[5]])
        for index in (4, 5):
            self.assertFalse(os.path.exists(os.path.join(self.attempts[index], "attempt.xcresult")), "the heavy files went before any run")
        self.assertTrue(os.path.exists(os.path.join(self.attempts[1], "attempt.xcresult")), "a protected run is not trimmed by the cap")

    def test_a_cap_that_protected_runs_alone_exceed_is_reported_not_forced(self):
        self.state.ledger("ios", "pub" * 8, self.runs, published=True)
        done = self.state.retention("--apply", "--max-gb", "0.000001")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertTrue(all(self.state.exists("runs/" + r) for r in self.runs))
        self.assertIn("only protected runs and ledgers remain", done.stdout)


class LedgerTests(unittest.TestCase):
    def test_the_last_twenty_ledgers_stay_and_one_with_publications_or_in_use_or_unreadable_is_never_touched(self):
        state = State(self)
        dirs = [state.ledger("ios", "fp%02d" % i, age_seconds=(30 - i) * 100) for i in range(23)]
        state.ledger("ios", "fp00", published=True, age_seconds=30 * 100)
        locked = os.open(os.path.join(dirs[1], "ledger.lock"), os.O_CREAT | os.O_RDWR)
        self.addCleanup(os.close, locked)
        fcntl.flock(locked, fcntl.LOCK_EX | fcntl.LOCK_NB)
        state.put("ledger/ios/fp-broken/ledger.json", "not json")
        state.put("ledger/ios/index.json", "[]")
        done = state.retention("--apply")
        self.assertEqual(done.returncode, 0, done.stderr)
        alive = [os.path.isdir(d) for d in dirs]
        self.assertEqual(alive, [True, True, False] + [True] * 20)
        self.assertTrue(state.exists("ledger/ios/fp-broken/ledger.json"))
        self.assertTrue(state.exists("ledger/ios/index.json"))
        self.assertIn("fp-broken", done.stdout)


class LegacyTests(unittest.TestCase):
    def setUp(self):
        self.state = State(self)
        self.parent = self.state.tmp
        self.legacy = [
            "checkpoints/Android/auth-suite/state.json", "maestro-ios.log", "certifications/sha1/ios/local-certification-suite/maestro-output/a.png",
        ]
        for name in self.legacy:
            self.state.blob(name, 100)
        self.state.blob("certifications/sha1/ios/local-certification-suite/manifest.json", 10)
        self.state.blob("certifications/sha1/ios/manifest.json", 10)
        self.state.blob("tuindice-e2e-scope.AbC123/state", 10, self.parent)
        self.state.blob("tuindice-e2e/ios/build.json", 10, self.parent)
        self.state.blob("unrelated/file", 10, self.parent)
        self.state.blob("poc/report.md", 10)
        self.state.blob("my-tuindice-e2e-notes/file", 10, self.parent)

    def kept(self):
        return ["certifications/sha1/ios/local-certification-suite/manifest.json", "certifications/sha1/ios/manifest.json", "poc/report.md"]

    def everything_there(self):
        return all(self.state.exists(p) for p in self.legacy + self.kept()) \
            and self.state.exists("tuindice-e2e-scope.AbC123/state", self.parent)

    def test_without_the_two_flags_no_legacy_path_is_ever_deleted(self):
        before = self.state.snapshot()
        for flags in ((), ("--apply",), ("--purge-legacy",), ("--apply", "--purge-legacy"), ("--yes",), ("--apply", "--yes")):
            done = self.state.retention(*flags)
            self.assertEqual(done.returncode, 2 if flags in (("--yes",), ("--apply", "--yes")) else 0, (flags, done.stderr))
            after = {k: v for k, v in self.state.snapshot().items() if not k.endswith("retention.lock")}
            self.assertEqual(after, before, flags)

    def test_purge_legacy_alone_lists_what_it_would_delete(self):
        done = self.state.retention("--purge-legacy")
        self.assertIn("WOULD DELETE-LEGACY", done.stdout)
        self.assertIn("checkpoints", done.stdout)
        self.assertIn("tuindice-e2e-scope.AbC123", done.stdout)

    def test_purge_legacy_with_yes_deletes_the_legacy_paths_and_nothing_else(self):
        done = self.state.retention("--purge-legacy", "--yes")
        self.assertEqual(done.returncode, 0, done.stderr)
        for name in self.legacy:
            self.assertFalse(self.state.exists(name), name)
        self.assertFalse(self.state.exists("tuindice-e2e-scope.AbC123", self.parent))
        for name in self.kept():
            self.assertTrue(self.state.exists(name), name)
        self.assertTrue(self.state.exists("tuindice-e2e/ios/build.json", self.parent), "the harness's own temp root stays")
        self.assertTrue(self.state.exists("unrelated/file", self.parent))
        self.assertTrue(self.state.exists("my-tuindice-e2e-notes/file", self.parent), "the pattern starts at the name")

    def test_legacy_is_left_alone_while_a_run_is_in_progress(self):
        self.state.run("ios", outcome="running")
        done = self.state.retention("--purge-legacy", "--yes")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertTrue(self.everything_there())
        self.assertIn("a run is in progress", done.stdout)

    def test_a_legacy_symbolic_link_is_not_followed(self):
        outside = os.path.join(self.state.dir, "outside")
        os.makedirs(outside)
        self.state.blob("precious.txt", 10, outside)
        os.symlink(outside, os.path.join(self.state.root, "checkpoints-link"))
        os.symlink(outside, os.path.join(self.parent, "tuindice-e2e-link"))
        self.state.retention("--purge-legacy", "--yes")
        self.assertTrue(os.path.exists(os.path.join(outside, "precious.txt")))


class SafetyTests(unittest.TestCase):
    def setUp(self):
        self.state = State(self)

    def test_paths_it_does_not_recognise_are_listed_and_never_deleted(self):
        self.state.blob("poc/report.md", 10)
        self.state.blob("emu-config/x", 10)
        self.state.blob("runs/not-a-run/file", 10)
        self.state.blob("runs/stray.txt", 10)
        run = self.state.run()
        self.state.blob("runs/%s/scenarios/auth-login/notes.txt" % run, 10)
        self.state.attempt(run)
        done = self.state.retention("--apply", "--json")
        listed = sorted(os.path.basename(item["path"]) for item in done.summary["unrecognised"])
        self.assertEqual(listed, ["emu-config", "not-a-run", "notes.txt", "poc", "stray.txt"])
        for name in ("poc/report.md", "emu-config/x", "runs/not-a-run/file", "runs/stray.txt",
                "runs/%s/scenarios/auth-login/notes.txt" % run):
            self.assertIn(os.path.join("state", name), self.state.snapshot())

    def test_nothing_outside_the_state_root_is_touched(self):
        for run in [self.state.run() for _ in range(12)]:
            self.state.attempt(run, passed=True, xcresult=100)
        self.state.blob("elsewhere/precious.bin", 5000)
        self.state.blob("state-sibling/runs/20250101T000000Z-ios-evidence-abc1234/manifest.json", 10)
        outside = {k: v for k, v in self.state.snapshot().items() if not k.startswith("state" + os.sep)}
        self.assertEqual(self.state.retention("--apply", "--max-gb", "0.000001").returncode, 0)
        self.assertEqual({k: v for k, v in self.state.snapshot().items() if not k.startswith("state" + os.sep)}, outside)

    def test_a_symbolic_link_inside_an_attempt_is_removed_but_never_followed(self):
        outside = os.path.join(self.state.dir, "outside")
        self.state.blob("precious.txt", 10, outside)
        attempt = self.state.attempt(self.state.run(), passed=True)
        os.symlink(outside, os.path.join(attempt, "link-to-outside"))
        self.assertEqual(self.state.retention("--apply").returncode, 0)
        self.assertTrue(os.path.exists(os.path.join(outside, "precious.txt")))
        self.assertFalse(os.path.lexists(os.path.join(attempt, "link-to-outside")))

    def test_a_run_directory_that_is_a_symbolic_link_is_left_alone(self):
        outside = os.path.join(self.state.dir, "outside-run")
        self.state.put("manifest.json", json.dumps({"schema": "tuindice-e2e-run/1", "platform": "ios"}), outside)
        os.makedirs(os.path.join(self.state.root, "runs"))
        os.symlink(outside, os.path.join(self.state.root, "runs", "20250101T000000Z-ios-evidence-abc1234"))
        self.assertEqual(self.state.retention("--apply", "--keep-runs", "0").returncode, 0)
        self.assertTrue(os.path.exists(os.path.join(outside, "manifest.json")))

    def test_two_retentions_never_work_at_once(self):
        self.state.attempt(self.state.run(), passed=True, xcresult=1000)
        before = self.state.snapshot()
        holder = os.open(os.path.join(self.state.root, "retention.lock"), os.O_CREAT | os.O_RDWR)
        self.addCleanup(os.close, holder)
        fcntl.flock(holder, fcntl.LOCK_EX | fcntl.LOCK_NB)
        done = self.state.retention("--apply")
        self.assertEqual(done.returncode, 0)
        self.assertIn("another retention holds", done.stdout)
        after = self.state.snapshot()
        self.assertEqual({k: v for k, v in after.items() if not k.endswith("retention.lock")}, {k: v for k, v in before.items() if not k.endswith("retention.lock")})

    def test_a_missing_state_root_is_nothing_to_do(self):
        shutil.rmtree(self.state.root)
        done = self.state.retention("--apply")
        self.assertEqual(done.returncode, 0)
        self.assertIn("nothing to do", done.stdout)

    def test_the_limits_come_from_the_command_line_or_the_environment(self):
        self.assertNotEqual(self.state.retention("--keep-runs", "-1").returncode, 0)
        self.assertNotEqual(self.state.retention(E2E_ARTIFACTS_MAX_GB="lots").returncode, 0)
        done = self.state.retention("--json", E2E_ARTIFACTS_MAX_GB="7")
        self.assertEqual(done.summary["capBytes"], 7 * 1024 ** 3)


class HarnessHookTests(unittest.TestCase):
    """The harness runs the tool at the end of every run; its failure is recorded and changes nothing else."""

    def test_a_run_trims_what_earlier_runs_left_and_leaves_itself_whole(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.diagnose("ios", "--scenario", "fix-a").code, 0)
        first = ws.run_dirs()[0]
        first_attempt = os.path.join(first, "scenarios", "fix-a", "attempt-1")
        self.assertTrue(os.path.exists(os.path.join(first_attempt, "wiremock-requests.json")), "its own run is whole until the next run")
        self.assertEqual(ws.diagnose("ios", "--scenario", "fix-a").code, 0)
        self.assertFalse(os.path.exists(os.path.join(first_attempt, "wiremock-requests.json")))
        self.assertTrue(os.path.exists(os.path.join(first_attempt, "result.json")))
        second = ws.run_dirs()[1]
        self.assertTrue(os.path.exists(os.path.join(second, "scenarios", "fix-a", "attempt-1", "wiremock-requests.json")))
        retention = ws.manifest()["retention"]
        self.assertEqual((retention["ran"], retention["ok"], retention["error"]), (True, True, None))
        self.assertGreater(retention["freedBytes"], 0)
        self.assertGreater(ws.manifest()["artifactsBytes"], 0)

    def test_a_failing_retention_is_recorded_and_the_result_of_the_run_stays(self):
        ws = Workspace(self, [scenario("fix-a")])
        done = ws.evidence(E2E_RETENTION_CMD="sh -c 'echo disk on fire >&2; exit 3' _")
        self.assertEqual(done.code, 0, done.out)
        manifest = ws.manifest()
        self.assertEqual(manifest["outcome"], "passed")
        self.assertEqual(manifest["exitCode"], 0)
        self.assertEqual((manifest["retention"]["ok"], manifest["retention"]["exitCode"]), (False, 3))
        self.assertIn("disk on fire", str(manifest["retention"]["error"]))
        self.assertIn("RETENTION FAILED", done.out)

    def test_a_retention_that_hangs_or_does_not_exist_is_a_recorded_failure_too(self):
        ws = Workspace(self, [scenario("fix-a")])
        done = ws.evidence(E2E_RETENTION_CMD="/no/such/retention-tool")
        self.assertEqual(done.code, 0, done.out)
        self.assertFalse(ws.manifest()["retention"]["ok"])

    def test_a_failed_run_still_runs_retention_and_keeps_its_exit_code(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:assertion", "fail:driver"]}})
        done = ws.evidence()
        self.assertEqual(done.code, 1, done.out)
        self.assertTrue(ws.manifest()["retention"]["ok"])
        self.assertTrue(os.path.exists(os.path.join(ws.run_dirs()[0], "scenarios", "fix-a", "attempt-1", "classification.json")),
            "the failing attempts of the run are whole")

    def test_a_diagnostic_run_names_the_fingerprint_of_head_too(self):
        ws = Workspace(self, [scenario("fix-a")])
        log = os.path.join(ws.dir, "retention-args.txt")
        tool = os.path.join(ws.dir, "retention-tool.sh")
        with open(tool, "w") as handle:
            handle.write('#!/bin/sh\necho "$@" > "%s"\necho \'{"freedBytes": 0, "actions": 0, "errors": []}\'\n' % log)
        os.chmod(tool, 0o755)
        self.assertEqual(ws.diagnose("ios", E2E_RETENTION_CMD=tool).code, 0)
        arguments = support.text(log).split()
        self.assertEqual(arguments[arguments.index("--fingerprint") + 1], support.FP_A, "the failures of the evidence in progress stay")
        self.assertEqual(ws.manifest()["fingerprint"], "diagnose", "the manifest still says it was a diagnosis")

    def test_the_hook_names_the_run_and_the_limits_it_passes(self):
        ws = Workspace(self, [scenario("fix-a")])
        log = os.path.join(ws.dir, "retention-args.txt")
        tool = os.path.join(ws.dir, "retention-tool.sh")
        with open(tool, "w") as handle:
            handle.write('#!/bin/sh\necho "$@" > "%s"\necho \'{"freedBytes": 0, "actions": 0, "errors": []}\'\n' % log)
        os.chmod(tool, 0o755)
        self.assertEqual(ws.evidence(E2E_RETENTION_CMD=tool, E2E_ARTIFACTS_MAX_GB="3").code, 0)
        arguments = support.text(log).split()
        run_id = ws.manifest()["runId"]
        self.assertEqual(arguments[arguments.index("--current-run") + 1], run_id)
        self.assertEqual(arguments[arguments.index("--max-gb") + 1], "3")
        self.assertEqual(arguments[arguments.index("--fingerprint") + 1], support.FP_A)
        self.assertEqual(arguments[arguments.index("--state-root") + 1], ws.state)
        self.assertIn("--apply", arguments)


if __name__ == "__main__":
    unittest.main()
