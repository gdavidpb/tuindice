"""Environment check, toolchain locks and the parallel decision (plan F15)."""

import json
import os
import threading
import unittest
from pathlib import Path

import support
from harness import envcheck, parallel, toolchain
from harness.config import Config
from support import Workspace, scenario

HEALTHY = {"load": 0.10, "diskFreeGb": 300.0, "uptimeDays": 3.0, "memAvailableGb": 40.0, "procs": [], "daemons": 1,
    "foreignDevices": []}


def rows(evidence=True, overrides=(), **changes):
    measures = dict(HEALTHY, **changes)
    return {r["id"]: r for r in envcheck.evaluate(measures, evidence, list(overrides))}


class ThresholdTests(unittest.TestCase):
    def test_a_healthy_host_is_all_ok(self):
        self.assertEqual({r["level"] for r in rows().values()}, {"ok"})

    def test_load_warns_from_half_and_refuses_from_one(self):
        for load, level in ((0.49, "ok"), (0.50, "warn"), (0.99, "warn"), (1.00, "refuse"), (7.0, "refuse")):
            self.assertEqual(rows(load=load)["load"]["level"], level, load)

    def test_disk_warns_below_40_and_refuses_below_15(self):
        for free, level in ((40.0, "ok"), (39.9, "warn"), (15.0, "warn"), (14.9, "refuse")):
            self.assertEqual(rows(diskFreeGb=free)["disk"]["level"], level, free)

    def test_the_other_checks_only_ever_warn(self):
        cases = (("uptime", "uptimeDays", 13.9, 14.0), ("memory", "memAvailableGb", 8.0, 7.9), ("daemons", "daemons", 3, 4))
        for ident, key, good, bad in cases:
            self.assertEqual(rows(**{key: good})[ident]["level"], "ok", ident)
            self.assertEqual(rows(**{key: bad})[ident]["level"], "warn", ident)
        busy = [{"pid": 7, "cpuPct": 180.0, "command": "WindowServer"}]
        self.assertEqual(rows(procs=busy)["procs"]["level"], "warn")
        self.assertEqual(rows(foreignDevices=["simulator iPhone 17"])["foreign_device"]["level"], "warn")
        worst = rows(uptimeDays=900.0, memAvailableGb=0.1, daemons=40, procs=busy, foreignDevices=["emulator x"])
        self.assertNotIn("refuse", {r["level"] for r in worst.values()})

    def test_only_evidence_refuses(self):
        self.assertEqual(rows(evidence=False, load=9.0, diskFreeGb=1.0)["load"]["level"], "warn")
        self.assertEqual(rows(evidence=False, load=9.0, diskFreeGb=1.0)["disk"]["level"], "warn")

    def test_an_override_downgrades_a_refusal_and_is_flagged(self):
        result = rows(overrides=["load"], load=9.0, diskFreeGb=1.0)
        self.assertEqual((result["load"]["level"], result["load"]["overridden"]), ("warn", True))
        self.assertEqual((result["disk"]["level"], result["disk"]["overridden"]), ("refuse", False))


class EnvironmentRunTests(unittest.TestCase):
    def test_evidence_is_refused_under_load_before_any_build_or_boot(self):
        ws = Workspace(self, [support.scenario("fix-a")])
        ws.set_metrics(load=[10.0, 9.0, 8.0], ncpu=10)
        result = ws.evidence()
        self.assertEqual(result.code, 3, result.out)
        self.assertIn("load = 1.0 (load1/ncpu: warn >= 0.50, refuse >= 1.00)", result.out)
        self.assertEqual({c[1] for c in ws.calls()}, {"toolchain"}, "nothing may boot or build when the environment is refused")
        manifest = ws.manifest()
        self.assertEqual(manifest["outcome"], "environment_refused")
        self.assertEqual({r["id"]: r["level"] for r in manifest["envCheck"]}["load"], "refuse")

    def test_a_refusing_disk_is_refused_too_and_the_busy_processes_are_listed(self):
        ws = Workspace(self, [support.scenario("fix-a")])
        ws.set_metrics(diskFreeGb=12.0, procs=[{"pid": 42, "cpuPct": 250.0, "command": "java -jar thing"}])
        result = ws.evidence()
        self.assertEqual(result.code, 3, result.out)
        self.assertIn("disk = 12.0", result.out)
        self.assertIn("busy process: pid 42 250% java -jar thing", result.out)
        self.assertEqual(ws.manifest()["competingProcesses"]["start"][0]["pid"], 42)

    def test_diagnose_only_warns(self):
        ws = Workspace(self, [support.scenario("fix-a")])
        ws.set_metrics(load=[6.0, 6.0, 6.0], ncpu=10)  # 0.60: warns, and is below the per-scenario load gate
        result = ws.diagnose("ios")
        self.assertIn("ENV   WARN load=0.6", result.out)
        self.assertEqual(result.code, 0, result.out)
        ws.set_metrics(load=[20.0, 20.0, 20.0], ncpu=10)  # evidence would refuse this; diagnose never does
        self.assertIn("ENV   WARN load=2.0", ws.diagnose("ios", "--dry-run").out)

    def test_the_override_lets_evidence_run_and_is_recorded_in_the_manifest(self):
        ws = Workspace(self, [support.scenario("fix-a")])
        ws.set_metrics(load=[10.0, 9.0, 8.0], ncpu=10)
        # the host calms down once the check is done, so that the per-scenario load gate stops waiting
        threading.Timer(3.0, ws.set_metrics).start()
        result = ws.evidence(E2E_ENV_OVERRIDE="load", E2E_FAKE_LOAD_POLL_SECONDS="0.1")
        self.assertEqual(result.code, 0, result.out)
        manifest = ws.manifest()
        self.assertEqual(manifest["overrides"]["env"], ["load"])
        load = {r["id"]: r for r in manifest["envCheck"]}["load"]
        self.assertEqual((load["level"], load["overridden"]), ("warn", True))
        self.assertIn("ENV   OVERRIDDEN by E2E_ENV_OVERRIDE load=1.0", result.out)

    def test_an_unknown_or_unrefusable_override_id_exits_2(self):
        ws = Workspace(self, [support.scenario("fix-a")])
        for value in ("loud", "uptime", "load,memory"):
            self.assertEqual(ws.evidence(E2E_ENV_OVERRIDE=value).code, 2, value)
        self.assertEqual(ws.calls(), [])

    def test_the_override_is_recorded_even_when_the_environment_was_fine(self):
        ws = Workspace(self, [support.scenario("fix-a")])
        self.assertEqual(ws.evidence(E2E_ENV_OVERRIDE="load,disk").code, 0)
        manifest = ws.manifest()
        self.assertEqual(manifest["overrides"]["env"], ["load", "disk"])
        self.assertEqual([r["overridden"] for r in manifest["envCheck"] if r["id"] in ("load", "disk")], [False, False])

    def test_env_check_prints_and_exits_3_when_evidence_would_be_refused(self):
        ws = Workspace(self, [support.scenario("fix-a")])
        ws.set_metrics(load=[10.0, 9.0, 8.0], ncpu=10)
        text = ws.run("env-check")
        self.assertEqual(text.code, 3)
        self.assertIn("load", text.out)
        as_json = ws.run("env-check", "--json", E2E_ENV_OVERRIDE="load")
        self.assertEqual(as_json.code, 0, as_json.out)
        data = json.loads(as_json.out)
        self.assertEqual({c["id"]: c["level"] for c in data["checks"]}["load"], "warn")
        self.assertEqual(data["parallel"]["decision"], "sequential")


class ToolchainTests(unittest.TestCase):
    def test_a_lock_that_differs_exits_3_in_evidence_with_the_differences(self):
        ws = Workspace(self, [support.scenario("fix-a")], {"toolchain": {"ios": {"XCODE_VERSION": "26.4"}}})
        result = ws.evidence()
        self.assertEqual(result.code, 3, result.out)
        self.assertIn("TOOLCHAIN DIFFERS XCODE_VERSION: lock 27.0, actual 26.4", result.out)
        self.assertNotIn("ensure-device", [c[1] for c in ws.calls()])
        manifest = ws.manifest()
        self.assertEqual((manifest["toolchain"]["lockMatches"], manifest["outcome"]), (False, "environment_refused"))

    def test_the_same_difference_only_warns_in_diagnose(self):
        ws = Workspace(self, [support.scenario("fix-a")], {"toolchain": {"ios": {"XCODE_VERSION": "26.4"}}})
        result = ws.diagnose("ios")
        self.assertEqual(result.code, 0, result.out)
        self.assertIn("TOOLCHAIN DIFFERS XCODE_VERSION", result.out)
        self.assertFalse(ws.manifest()["toolchain"]["lockMatches"])

    def test_a_key_the_adapter_does_not_report_is_a_difference_and_null_is_verified_later(self):
        ws = Workspace(self, [support.scenario("fix-a")], {"toolchain": {"ios": {"IOS_LANGUAGE": None}}})
        self.assertEqual(ws.evidence().code, 0)
        manifest = ws.manifest()
        self.assertEqual(manifest["toolchain"]["unverified"], ["IOS_LANGUAGE"])
        self.assertTrue(manifest["toolchain"]["lockMatches"])
        self.assertEqual(manifest["toolchain"]["lockFile"], "e2e/toolchain/ios.lock")
        self.assertEqual(manifest["toolchain"]["libs"]["kotlin"], "2.4.20")
        lock = {"A": "1", "B": "2"}
        self.assertEqual(toolchain.compare(lock, {"A": "1"}), ([("B", "2", "<not reported>")], []))
        self.assertEqual(toolchain.compare(lock, {"A": "1", "B": None, "extra": "x"}), ([], ["B"]))

    def test_every_android_key_is_strict(self):
        for key in toolchain.LOCK_KEYS["android"]:
            ws = Workspace(self, [support.scenario("fix-a")], {"toolchain": {"android": {key: "different"}}})
            self.assertEqual(ws.evidence("android").code, 3, key)

    def test_a_malformed_lock_is_a_usage_error(self):
        ws = Workspace(self, [support.scenario("fix-a")])
        path = os.path.join(ws.repo, "e2e", "toolchain", "ios.lock")
        with open(path, "a") as handle:
            handle.write("SURPRISE=1\n")
        ws.git("commit", "-q", "-am", "bad lock")
        self.assertEqual(ws.evidence().code, 2)

    def test_the_repository_locks_carry_the_keys_the_harness_requires(self):
        cfg = Config(Path(support.SHARED).resolve().parents[2], {})
        self.assertEqual(set(toolchain.read_lock(cfg, "android")), set(toolchain.LOCK_KEYS["android"]))
        self.assertEqual(set(toolchain.read_lock(cfg, "ios")), set(toolchain.LOCK_KEYS["ios"]))
        self.assertEqual(toolchain.read_lock(cfg, "android")["ANDROID_EMULATOR_HEADLESS"], "1")


class ParallelDecisionTests(unittest.TestCase):
    def decide(self, ncpu=10, load1=1.0, mem=64, **env):
        cfg = Config(Path("."), dict(env))
        return parallel.decide(cfg, {"ncpu": ncpu, "memGb": mem}, [load1, 0, 0])

    def test_parallel_only_with_cores_headroom_and_memory(self):
        decision, reason = self.decide()
        self.assertEqual(decision, "parallel")
        self.assertIn("ncpu=10", reason)
        for kwargs, fragment in (({"ncpu": 7}, "ncpu=7 < 8"), ({"load1": 5.0}, "load1/ncpu=0.50 >= 0.50"),
                ({"mem": 16}, "memory=16 GB < 32 GB")):
            decision, reason = self.decide(**kwargs)
            self.assertEqual(decision, "sequential", kwargs)
            self.assertIn(fragment, reason)
        self.assertEqual(self.decide(load1=4.9)[0], "parallel")
        self.assertEqual(self.decide(ncpu=8, mem=32)[0], "parallel")

    def test_every_blocker_is_named(self):
        decision, reason = self.decide(ncpu=4, load1=4.0, mem=8)
        self.assertEqual(decision, "sequential")
        self.assertEqual(reason.count(";"), 2)

    def test_the_variable_forces_either_way_and_says_so(self):
        self.assertEqual(self.decide(ncpu=2, mem=4, E2E_PARALLEL="always"), ("parallel", "E2E_PARALLEL=always overrides the policy"))
        self.assertEqual(self.decide(E2E_PARALLEL="never"), ("sequential", "E2E_PARALLEL=never"))

    def test_sequential_order_puts_the_platform_with_failures_first_then_android(self):
        self.assertEqual(parallel.sequence(["android", "ios"], set()), ["android", "ios"])
        self.assertEqual(parallel.sequence(["android", "ios"], {"ios"}), ["ios", "android"])
        self.assertEqual(parallel.sequence(["ios", "android"], {"android", "ios"}), ["android", "ios"])


class PlatformsAllTests(unittest.TestCase):
    FAIL_ANDROID = {"behaviours": {"android:fix-a": ["fail:assertion"]}}

    def test_parallel_runs_both_independently_and_a_failing_platform_does_not_cancel_the_other(self):
        ws = Workspace(self, [scenario("fix-a")], self.FAIL_ANDROID)
        result = ws.run("run", "--platform", "all", "--mode", "evidence", E2E_MAX_RETRIES="0")
        self.assertEqual(result.code, 1, result.out)
        self.assertIn("[e2e all] parallel: ncpu=10", result.out)
        manifests = ws.manifests()
        self.assertEqual((manifests["android"]["exitCode"], manifests["ios"]["exitCode"]), (1, 0))
        self.assertEqual(manifests["android"]["outcome"], "failed")
        self.assertEqual(manifests["ios"]["outcome"], "passed")
        for manifest in manifests.values():
            self.assertEqual(manifest["parallel"]["decision"], "parallel")
            self.assertIn("ncpu=10", manifest["parallel"]["reason"])
            self.assertIsNotNone(manifest["parentRunId"])
        self.assertEqual([c[1] for c in ws.calls("stop-device")], [], "parallel runs leave the devices alone")
        summary = ws.read(os.path.join(ws.state, "runs", manifests["ios"]["parentRunId"], "summary.json"))
        self.assertEqual(summary["exitCodes"], {"android": 1, "ios": 0})
        self.assertEqual(summary["decision"], "parallel")
        self.assertEqual(set(summary["children"]), {"android", "ios"})

    def test_sequential_records_why_and_stops_the_device_it_booted_before_the_next_platform(self):
        ws = Workspace(self, [scenario("fix-a")], self.FAIL_ANDROID)
        ws.set_metrics(load=[8.0, 8.0, 8.0], ncpu=10)
        result = ws.run("run", "--platform", "all", "--mode", "evidence", E2E_MAX_RETRIES="0")
        self.assertIn("[e2e all] sequential: load1/ncpu=0.80 >= 0.50", result.out)
        manifests = ws.manifests()
        self.assertEqual((manifests["android"]["exitCode"], manifests["ios"]["exitCode"]), (1, 0))
        for manifest in manifests.values():
            self.assertEqual(manifest["parallel"]["decision"], "sequential")
            self.assertIn("load1/ncpu=0.80 >= 0.50", manifest["parallel"]["reason"])
        order = [(c[0], c[1]) for c in ws.calls() if c[1] in ("ensure-device", "stop-device")]
        self.assertEqual(order, [("android", "ensure-device"), ("android", "stop-device"), ("ios", "ensure-device"),
            ("ios", "stop-device")])

    def test_sequential_runs_first_the_platform_that_has_failed_attempts(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"ios:fix-a": ["fail:assertion", "pass"]}})
        self.assertEqual(ws.evidence("ios", E2E_MAX_RETRIES="0").code, 1)
        result = ws.run("run", "--platform", "all", "--mode", "evidence", E2E_PARALLEL="never")
        self.assertEqual(result.code, 0, result.out)
        self.assertIn("order ios, android", result.out)
        starts = [c[0] for c in ws.calls("ensure-device")]
        self.assertEqual(starts, ["ios", "ios", "android"])
        self.assertEqual(ws.manifests()["ios"]["parallel"]["reason"], "E2E_PARALLEL=never")

    def test_the_dry_run_decides_and_touches_no_device(self):
        ws = Workspace(self, [scenario("fix-a")])
        result = ws.run("run", "--platform", "all", "--mode", "diagnose", "--dry-run")
        self.assertEqual(result.code, 0, result.out)
        self.assertIn("[e2e all] parallel:", result.out)
        self.assertIn("TOOLCHAIN matches ios.lock", result.out)
        self.assertIn("ENV   7 checks, 0 not ok, 0 refused", result.out)
        self.assertEqual({c[1] for c in ws.calls()}, {"toolchain"})
        self.assertFalse(os.path.exists(ws.state))


if __name__ == "__main__":
    unittest.main()
