"""The per-platform run: scope, ledger, device preparation, scenario loop, finalisation."""

import json
import os
import shlex
import shutil
import subprocess
import sys
import time
import traceback
from contextlib import nullcontext
from pathlib import Path

from . import catalog as catalog_mod
from . import classify as cl
from . import envcheck, hostlock, junit, proc, publish, report, toolchain, tolerances
from .config import DEGRADATION_MIN_GREEN, EXIT_HARNESS_ERROR, SUITE_ID, VERB_TIMEOUTS, EnvironmentRefused, UsageError
from .gitstate import GitState
from .ledger import Ledger, now
from .manifest import Manifest, make_run_dir, read_load
from .wiremock import WireMock

# Step 10 of the run: e2e/tools/e2e-retention.py trims what earlier runs left. It lives outside e2e/scripts, so it is
# not part of the evidence fingerprint; E2E_RETENTION_CMD replaces it in tests.
RETENTION_SCRIPT = Path(__file__).resolve().parents[3] / "tools" / "e2e-retention.py"
RETENTION_TIMEOUT_SECONDS = 600
# A failed iOS attempt waits this long for the crash report of the app, which lands some seconds after the crash.
CRASH_REPORT_WAIT_SECONDS = 15
# The logs a failed attempt leaves are searched for the signs of a degraded simulator through their last bytes only.
FAILURE_LOGS = ("app.log", "logcat.txt")
FAILURE_LOG_TAIL_BYTES = 4 * 1024 * 1024
# What the iOS health verb says when the simulator is booted but does not serve preferences.
DEGRADED_HEALTH_TEXT = "simulator degraded"

# HOOKS["toolchain"](run) compares the adapter's toolchain with the lock, HOOKS["env_check"](run) measures
# the environment. Each raises EnvironmentRefused to refuse.
HOOKS = {"toolchain": toolchain.check, "env_check": envcheck.check}


class StopRun(Exception):
    def __init__(self, outcome, code, reason, scenario=None, klass=None, diagnosis=None):
        Exception.__init__(self, reason)
        self.outcome, self.code, self.reason = outcome, code, reason
        self.scenario, self.klass, self.diagnosis = scenario, klass, diagnosis


class Options:
    def __init__(self, mode, scenarios=None, tag=None, changed_since=None, survey=False, repeat=1,
            trace=False, dry_run=False, force=False, child_of=None, stop_device=False, driver_contract=False):
        self.mode = mode
        self.scenarios = scenarios or []
        self.tag, self.changed_since, self.survey = tag, changed_since, survey
        self.repeat, self.trace, self.dry_run, self.force = repeat, trace, dry_run, force
        self.driver_contract = driver_contract  # diagnose runs the driver contract only when asked; evidence always does
        # Set for a platform of `--platform all`: the parent run id, whether to stop a device this run booted,
        # and the parent's (decision, reason).
        self.child_of, self.stop_device, self.parallel = child_of, stop_device, None


class Adapter:
    def __init__(self, config, platform):
        self.config, self.platform = config, platform
        self.base = config.adapter_command(platform)
        self.verbs_dir = None  # set with the run directory: the full output of every verb that failed goes there
        self.failures = {}

    def call(self, verb, *args, **kwargs):
        env = dict(self.config.env)
        env.update(kwargs.get("env") or {})
        timeout = kwargs.get("timeout") or VERB_TIMEOUTS[verb]
        argv = self.base + [verb] + [str(a) for a in args]
        result = proc.run(argv, timeout, cwd=str(self.config.root), env=env)
        result.json = proc.parse_json(result.stdout)
        result.log = self._keep_failure(verb, argv, result) if not result.ok else None
        return result

    def _keep_failure(self, verb, argv, result):
        """Writes the complete stdout and stderr of a failed verb to verbs/<verb>-<n>.log and returns that relative path."""
        if not self.verbs_dir:
            return None
        self.failures[verb] = self.failures.get(verb, 0) + 1
        name = "verbs/%s-%d.log" % (verb, self.failures[verb])
        os.makedirs(self.verbs_dir, exist_ok=True)
        with open(os.path.join(os.path.dirname(self.verbs_dir), name), "w") as handle:
            handle.write("$ %s\nexit %s%s after %.1fs\n--- stdout ---\n%s\n--- stderr ---\n%s\n" % (
                " ".join(argv), result.returncode, " (timed out)" if result.timed_out else "", result.seconds, result.stdout, result.stderr))
        return name


class PlatformRun:
    def __init__(self, config, platform, options):
        self.cfg, self.platform, self.opts = config, platform, options
        self.evidence = options.mode == "evidence"
        self.log = report.Log(platform)
        self.adapter = Adapter(config, platform)
        self.started = time.monotonic()
        self.recoveries = 0
        self.health_degraded = False
        self.series = {}  # (repetition, scenario id) -> how its last attempt ended: passed | failed | environment
        self.environment_attempts = 0
        self.boot_started = time.monotonic()  # the last time the device was started or recovered by this run, as far as it knows
        self.greens_since_recovery = 0
        self.attempts_since_recovery = 0
        self.load_waited = 0.0
        self.load_gate_spent = False
        self.ledger = None
        self.catalog = None
        self.runnable, self.quarantined = [], []
        self.run_dir = None
        self.run_id = None
        self.wiremock = None
        self.fingerprint = options.mode if not self.evidence else None
        self.context = None
        self.failed_overall = {}
        self.passed_overall = {}  # scenario id -> the attempt that passed, in the last repetition that finished it green
        self.repetitions_done = 0
        self.repetition = 0
        self.executed = set()
        self.stop = None
        self.device_booted = False
        self.env_checked = False
        self.device_health = {}
        self.cap = config.attempt_cap

    # -- entry point -------------------------------------------------------------------------

    def execute(self):
        self.git = GitState(self.cfg.root)
        if not self.opts.dry_run:
            self.run_id, self.run_dir = make_run_dir(self.cfg.state_root, self.platform, self.opts.mode, self.git.sha7)
            self.log.attach(os.path.join(self.run_dir, "run.log"))
            self.adapter.verbs_dir = os.path.join(self.run_dir, "verbs")
        self.manifest = Manifest(self.run_dir or "", self.run_id or "dry-run", self.opts.mode, self.platform, self.cfg,
            enabled=not self.opts.dry_run)
        self.manifest.update(commitSha=self.git.sha, branch=self.git.branch, upstreamSha=self.git.upstream_sha,
            treeClean=self.git.tree_clean, headEqualsUpstream=self.git.head_equals_upstream)
        if self.opts.parallel:
            self.manifest.data["parallel"].update(decision=self.opts.parallel[0], reason=self.opts.parallel[1])
        self.manifest.data["parentRunId"] = self.opts.child_of
        self.manifest.write()
        outcome, code, signum = "failed", 1, None
        try:
            outcome, code = self._pipeline()
        except UsageError as error:
            self.log.say("ERROR %s" % error)
            self.manifest.data["stop"]["reason"] = str(error)
            outcome, code = "failed", 2
        except EnvironmentRefused as error:
            self.log.say("ENVIRONMENT %s" % error)
            self.manifest.data["stop"]["reason"] = str(error)
            outcome, code = "environment_refused", 3
        except proc.Interrupted as error:
            signum = error.signum
            self.log.say("INTERRUPTED by signal %d" % signum)
            self.manifest.data["stop"]["reason"] = "signal %d" % signum
            outcome, code = "interrupted", 128 + signum
        except Exception as error:  # a defect of the harness itself: its own exit code, never the 1 of "scenarios failed"
            self.log.say("HARNESS ERROR %s: %s (traceback in harness-error.txt)" % (type(error).__name__, error))
            self.manifest.data["stop"]["reason"] = "%s: %s" % (type(error).__name__, error)
            if self.run_dir:
                with open(os.path.join(self.run_dir, "harness-error.txt"), "w") as handle:
                    handle.write(traceback.format_exc())
            outcome, code = "harness_error", EXIT_HARNESS_ERROR
        finally:
            # The cheap, valuable part first (results, JUnit, manifest); the slow cleanup (WireMock up to 20 s, the device up to
            # 120 s) after it, so that a kill during the cleanup still leaves a finalised manifest. A failure of the finish never
            # skips the cleanup.
            try:
                outcome, code = self._finish(outcome, code)
                if signum is not None:
                    self.log.say("the manifest is finalised; stopping WireMock and the device now")
            finally:
                self._cleanup()
                self.log.close()
        return code

    def _cleanup(self):
        if self.wiremock:
            self.wiremock.stop()
        if self.opts.stop_device and self.device_booted and not self.opts.dry_run:
            self.log.say("DEVICE stopping the device this run booted before the next platform starts")
            self.adapter.call("stop-device")
        if self.ledger:
            self.ledger.release()

    # -- pipeline ----------------------------------------------------------------------------

    def _pipeline(self):
        cfg, git, log, m = self.cfg, self.git, self.log, self.manifest
        required = cfg.publish_required(self.opts.mode)
        m.data["published"]["required"] = required
        if self.evidence:
            cfg.require_no_seams("running evidence")
            git.require_clean()
            if required:
                git.require_publishable(publish.gh_command(cfg))
            lines = self._scope_lines()
            if not lines and not self.opts.force:
                log.say("scope: no E2E evidence is required for this change; pass --force to run anyway.")
                return "not_required", 0
            if required:
                self.context = publish.status_context(cfg, self.platform)
                self._reconcile_contexts(lines)
                m.update(statusContext=self.context)
            self.fingerprint = self._fingerprint()
        else:
            self.fingerprint = "diagnose"
        m.update(fingerprint=self.fingerprint)
        self.catalog = catalog_mod.load(cfg.catalog_path)
        self.runnable, self.quarantined = self.catalog.in_scope(self.platform)
        self.runnable = self._select(self.runnable)
        if self.evidence:
            self.ledger = Ledger.open(cfg.state_root, self.platform, self.fingerprint, self.catalog.sha256,
                cfg.layout["E2E_FINGERPRINT_VERSION"], lock=not self.opts.dry_run, attempt_cap=cfg.attempt_cap)
            cfg.require_clean_ledger(self.ledger.directory, self.ledger.data["seams"])
            if not self.opts.dry_run:
                self.ledger.note_overrides(cfg.run_overrides())
                self.ledger.note_seams(cfg.seams())
                self.ledger.save()
        else:
            self.ledger = Ledger.memory(self.platform)
        cap = self.cap = self.ledger.cap(cfg.attempt_cap)
        pending = [s for s in self.runnable if not self.ledger.passed(s.id)]
        exhausted = [s for s in pending if self.ledger.exhausted(s.id, cap)]
        if exhausted:
            for s in exhausted:
                log.say("EXHAUSTED %s used %d of %d attempts for fp %s. Fix the cause; rerunning is not a remedy. "
                    "One more attempt needs: e2e.py reset-scenario --platform %s --id %s --reason <why>"
                    % (s.id, len(self.ledger.counted(s.id)), self.ledger.allowance(s.id, cap), self.fingerprint[:12],
                        self.platform, s.id))
            m.data["stop"].update(reason="a scenario exhausted its attempts", scenario=exhausted[0].id)
            return "exhausted", 7
        if self.evidence or not (self.opts.scenarios or cfg.scenario_filter):
            pending = self._order(pending)  # an explicit --scenario list keeps the order it was given in
        green = len(self.runnable) - len(pending)
        failed_before = sum(1 for s in pending if self.ledger.has_failed_attempts(s.id))
        log.scope(len(self.runnable), green, len(pending), failed_before, self.fingerprint)
        m.data["scenarios"].update(inScope=len(self.runnable), quarantined=len(self.quarantined), alreadyGreen=green)
        if self.opts.dry_run:
            self._dry_run_checks()
            log.say("DRY-RUN would run: %s" % (", ".join(s.id for s in pending) or "nothing"))
            return "not_required", 0
        if pending:
            try:
                self._prepare()
                with m.phase("scenarios"):
                    self._run_all(pending)
            except StopRun as stop:
                self.stop = stop
        return self._decide(required)

    def _decide(self, required):
        stop, m = self.stop, self.manifest
        if stop:
            m.data["stop"].update(reason=stop.reason, scenario=stop.scenario, failureClass=stop.klass, diagnosis=stop.diagnosis)
            return stop.outcome, stop.code
        failed = self._failed_list()
        if failed:
            return "failed", 1
        if not required:
            return "passed", 0
        return self._publish()

    # -- git, scope, fingerprint -------------------------------------------------------------

    def _tool(self, seam, script_key, *args):
        command = shlex.split(self.cfg.seam(seam)) if self.cfg.seam(seam) \
            else ["bash", str(self.cfg.layout_path(script_key))]
        result = subprocess.run(command + list(args), cwd=str(self.cfg.root), stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, universal_newlines=True)
        if result.returncode != 0:
            raise UsageError("%s failed: %s" % (" ".join(command), result.stderr.strip()[:300]))
        return result.stdout

    def _scope_lines(self):
        out = self._tool("E2E_SCOPE_CMD", "E2E_SCOPE_SCRIPT", self.platform)
        return [tuple(line.split(",")) for line in out.splitlines() if line.strip()]

    def _reconcile_contexts(self, lines):
        """The scope names a suite per platform; this harness certifies one. The status context has one definition, in shell."""
        for fields in lines:
            suite = fields[1] if len(fields) > 1 else SUITE_ID
            if suite != SUITE_ID:
                raise UsageError("the scope requires the suite %s but this harness can only publish %s (%s)"
                    % (suite, SUITE_ID, self.context))

    def _fingerprint(self):
        value = self._tool("E2E_FINGERPRINT_CMD", "E2E_FINGERPRINT_SCRIPT", self.platform, SUITE_ID, "HEAD").strip()
        if not value or not all(c in "0123456789abcdef" for c in value) or len(value) < 12:
            raise UsageError("the fingerprint command printed %r" % value[:80])
        return value

    def _select(self, runnable):
        if self.evidence:
            return runnable
        wanted = self.opts.scenarios or self.cfg.scenario_filter
        if wanted:
            known = {s.id: s for s in runnable}
            missing = [i for i in wanted if i not in known]
            if missing:
                raise UsageError("scenarios not in the %s catalog scope: %s" % (self.platform, ", ".join(missing)))
            runnable = [known[i] for i in wanted]
        if self.opts.tag:
            runnable = [s for s in runnable if self.opts.tag in s.tags or self.opts.tag == s.module]
        if self.opts.changed_since:
            changed = catalog_mod.changed_ids(self.cfg, self.catalog, self.opts.changed_since)
            runnable = [s for s in runnable if s.id in changed]
        if not runnable:
            raise UsageError("no scenario matches the selection")
        return runnable

    def _order(self, pending):
        """Failed first, then changed since the base (stepsHash), then catalog order."""
        ref = self.opts.changed_since or catalog_mod.default_base_ref(self.cfg)
        changed = catalog_mod.changed_ids(self.cfg, self.catalog, ref)
        index = {s.id: i for i, s in enumerate(self.catalog.scenarios)}
        return sorted(pending, key=lambda s: (
            0 if self.ledger.has_failed_attempts(s.id) else 1, 0 if s.id in changed else 1, index[s.id]))

    # -- preparation -------------------------------------------------------------------------

    def _dry_run_checks(self):
        """The read-only checks of the preparation: they report and never refuse; nothing boots or builds."""
        for name in ("toolchain", "env_check"):
            if HOOKS[name]:
                HOOKS[name](self)
        if self._runs_driver_contract():
            self.log.say("DRY-RUN would run the driver contract once, after enumerate and before the first scenario")

    def _prepare(self):
        m, adapter, cfg = self.manifest, self.adapter, self.cfg
        for name in ("toolchain", "env_check"):
            with m.phase("env-check" if name == "env_check" else name):
                if HOOKS[name]:
                    HOOKS[name](self)
        lock = hostlock.prepare_lock(cfg, self.platform, self.run_id, self.log.say, m.data["prepareLock"].update) \
            if self.opts.child_of else nullcontext()  # `--platform all`: one platform prepares its device at a time
        with lock:
            # The platform's WireMock lock is the claim on this platform: a second run on it learns that before the device is touched.
            self.wiremock = WireMock(cfg, self.platform, self.run_dir)
            self.wiremock.claim()
            with m.phase("device"):
                result = adapter.call("ensure-device")
                if not result.ok:
                    raise EnvironmentRefused("ensure-device failed (full output in %s): %s"
                        % (result.log, result.stderr.strip()[-300:] or result.returncode))
                m.update(device=result.json)
                self.device_booted = bool(result.json.get("bootedByHarness"))
                self.boot_started = time.monotonic()
                if result.json.get("recoveredAtEnsure") is True:  # a simulator that was up but refused its settings
                    self.log.say("ENV   the simulator refused its settings at ensure and was recovered; recorded as a degradation")
                    self._degradation_event(None, "ensure recovered a booted simulator that did not accept its settings", {}, {})
            with m.phase("wiremock"):
                self.wiremock.start()
                m.data["wiremock"]["log"] = "wiremock.log"
            with m.phase("build"):
                result = adapter.call("build", cfg.ports[self.platform])
                if not result.ok:
                    raise StopRun("failed", 1, "build failed (full output in %s): %s" % (result.log, result.stderr.strip()[-300:]))
                self._require_unchanged("after the build")
            with m.phase("install"):
                result = adapter.call("install")
                if not result.ok:
                    raise EnvironmentRefused("install failed (full output in %s)" % result.log)
        with m.phase("enumerate"):
            result = adapter.call("enumerate")
            if not result.ok:
                raise UsageError("enumerate failed: %s" % result.stderr.strip()[-300:])
            tests = result.json.get("tests", [])
            bad = ["%s (%d tests)" % (s.id, tests.count(s.runner_identifier(self.platform))) for s in self.runnable
                if tests.count(s.runner_identifier(self.platform)) != 1]
            if bad:
                raise UsageError("every catalog id needs exactly one runner test; mismatches: %s" % ", ".join(bad))
        if self._runs_driver_contract():
            with m.phase("driver-contract"):
                self._driver_contract()

    def _runs_driver_contract(self):
        return self.evidence or self.opts.driver_contract

    def _driver_contract(self):
        """The driver's own contract, once per platform before the first scenario (B-5). What cannot be executed or read is
        never taken as good: a missing verb, a timeout, an exit status or an answer the harness does not understand is exit 3
        as much as a red probe."""
        record, directory = self.manifest.data["driverContract"], os.path.join(self.run_dir, "driver-contract")
        os.makedirs(directory, exist_ok=True)
        deadline = self.cfg.seam("E2E_FAKE_DRIVER_CONTRACT_TIMEOUT_SECONDS")  # the tests' way to reach the deadline quickly
        result = self.adapter.call("driver-contract", directory, self.cfg.ports[self.platform], timeout=float(deadline) if deadline else None)
        answer = result.json
        record.update(ran=True, artifacts="driver-contract")
        shape = isinstance(answer.get("ok"), bool) and isinstance(answer.get("passed"), list) and isinstance(answer.get("failed"), list)
        if shape:
            record.update(ok=answer["ok"], passed=answer["passed"], failed=answer["failed"],
                skipped=answer["skipped"] if isinstance(answer.get("skipped"), list) else [])
        where = "(full output in %s, artifacts in driver-contract/)" % (result.log or "the adapter output")
        if result.timed_out or not shape:
            record["ok"] = False
            raise EnvironmentRefused("the driver contract gave no answer the harness understands: %s %s"
                % ("it timed out" if result.timed_out else "exit %s, %s" % (result.returncode, result.stderr.strip()[-200:] or "no valid JSON"), where))
        if not answer["ok"] or answer["failed"]:
            record["ok"] = False
            raise EnvironmentRefused("the driver contract failed, so no scenario runs on this driver; failed probes: %s %s"
                % (", ".join(str(n) for n in answer["failed"]) or "none named", where))
        if not result.ok or not answer["passed"]:
            record["ok"] = False
            raise EnvironmentRefused("the driver contract answered ok but %s: it is not taken as good %s"
                % ("exited %s" % result.returncode if not result.ok else "no probe ran", where))
        self.log.say("CONTRACT driver contract green: %d probes" % len(answer["passed"]))

    def _require_unchanged(self, moment):
        """Evidence comes from one commit and a clean tree: HEAD and the tree are read again, and a change is exit 2."""
        if self.evidence:
            self.git.require_unchanged(moment)

    # -- the scenario loop -------------------------------------------------------------------

    def _run_all(self, pending):
        for repetition in range(self.opts.repeat if not self.evidence else 1):
            self.repetition = repetition
            if repetition:
                self.ledger = Ledger.memory(self.platform)
            for index, scenario in enumerate(pending, 1):
                self._scenario(scenario, index, len(pending))
            for s in pending:
                if not self.ledger.passed(s.id):
                    self.failed_overall[s.id] = self._last_attempt(s) or {}
                else:
                    self.passed_overall[s.id] = [a for a in self.ledger.attempts(s.id) if a["outcome"] == "passed"][-1]
            self.repetitions_done += 1

    def _series_summary(self):
        """Environment failures are not a signal about the product: the series says how many of its runs were valid, so that
        '92 of 111' is not read as a failure rate. Written at the end of the run, whatever cut it."""
        done = self.repetitions_done if self.repetitions_done == self.opts.repeat else "%d of %d" % (self.repetitions_done, self.opts.repeat)
        self.log.say("REPEAT %s runs: %d scenarios failed in at least one" % (done, len(self._failed_list())))
        ends = list(self.series.values())
        counts = {kind: ends.count(kind) for kind in ("passed", "failed", "environment")}
        valid = counts["passed"] + counts["failed"]
        self.manifest.data["series"] = {"repetitions": self.opts.repeat, "repetitionsCompleted": self.repetitions_done,
            "scenarioRuns": len(ends), "valid": valid, **counts, "environmentAttempts": self.environment_attempts}
        if self.environment_attempts:
            self.log.say("SERIES %s repetitions, %d scenario runs: %d valid (%d passed, %d failed), %d lost to the environment; %s"
                % (done, len(ends), valid, counts["passed"], counts["failed"], counts["environment"],
                    self._rerun_text(self.environment_attempts, counts["environment"])))

    @staticmethod
    def _rerun_text(attempts, lost):
        rerun = attempts - lost
        parts = []
        if rerun:
            parts.append("%d environment attempt%s %s rerun" % (rerun, "" if rerun == 1 else "s", "was" if rerun == 1 else "were"))
        if lost:
            parts.append("%d environment attempt%s %s not rerun" % (lost, "" if lost == 1 else "s", "was" if lost == 1 else "were"))
        return "; ".join(parts)

    def _last_attempt(self, scenario):
        """The last counted attempt; a scenario that only had environment failures has no counted one. None when there is none."""
        attempts = self.ledger.counted(scenario.id) or self.ledger.attempts(scenario.id)
        return attempts[-1] if attempts else None

    def _last_class(self, scenario):
        attempt = self._last_attempt(scenario)
        return attempt.get("failureClass") if attempt else None

    def _budget_check(self, scenario):
        elapsed = time.monotonic() - self.started
        if elapsed + scenario.timeout > self.cfg.budget_seconds:
            text = "%s needs up to %ds but only %ds of the %d minute budget remain; stopping. Greens are kept; run again to continue." \
                % (scenario.id, scenario.timeout, max(0, self.cfg.budget_seconds - elapsed), self.cfg.budget_minutes)
            self.log.say("BUDGET %s" % text)
            raise StopRun("budget_exhausted", 4, text, scenario.id)

    def _wait_for_load(self, scenario):
        """The host gate before a scenario, in two steps: load1/ncpu is the cheap filter of a diagnosis (the CPU is measured only
        above it) and evidence skips the filter and measures every time; the run waits only while the CPU is busy, within a cap per scenario and one per run. When the run's cap is
        spent the gate says so once and the rest of the run starts without waiting. Returns (seconds waited, CPU idle percent)."""
        cfg, m = self.cfg, self.manifest
        load, ncpu = read_load(cfg)
        m.sample_load(load)
        if not self.evidence and load[0] / ncpu < envcheck.LOAD_MEASURE_RATIO:
            return 0, None
        idle = envcheck.cpu_idle(cfg)
        if idle is None or idle >= envcheck.CPU_IDLE_WAIT_BELOW:
            return 0, idle
        poll = float(cfg.seam("E2E_FAKE_LOAD_POLL_SECONDS") or 10)
        per_scenario, per_run = (float(v) for v in (cfg.seam("E2E_FAKE_LOAD_WAIT_CAPS") or "%d,%d" % (
            envcheck.LOAD_WAIT_PER_SCENARIO_SECONDS, envcheck.LOAD_WAIT_PER_RUN_SECONDS)).split(","))
        if self.load_waited >= per_run:
            self._load_gate_spent(scenario, per_run, idle)
            return 0, idle
        began = time.monotonic()
        self.log.say("LOAD  load1/ncpu is %.2f and the CPU is %.0f%% idle; waiting for %d%% idle"
            % (load[0] / ncpu, idle, envcheck.CPU_IDLE_WAIT_UNTIL))
        while idle is not None and idle < envcheck.CPU_IDLE_WAIT_UNTIL:
            waited = time.monotonic() - began
            if waited >= per_scenario or self.load_waited + waited >= per_run:
                self.log.say("LOAD  gave up after %ds: the CPU is %.0f%% idle, short of %d%%; the scenario starts anyway"
                    % (waited, idle, envcheck.CPU_IDLE_WAIT_UNTIL))
                break
            time.sleep(poll)
            load, ncpu = read_load(cfg)
            m.sample_load(load)
            idle = envcheck.cpu_idle(cfg)
        waited = time.monotonic() - began
        self.load_waited += waited
        m.data["load"]["waitSeconds"] = round(self.load_waited, 1)
        if self.load_waited >= per_run:
            self._load_gate_spent(scenario, per_run, idle)
        return round(waited, 1), idle

    def _load_gate_spent(self, scenario, per_run, idle):
        if self.load_gate_spent:
            return
        self.load_gate_spent = True
        self.manifest.data["load"]["gateExhaustedAt"] = {"scenario": scenario.id, "at": now(), "waitedSeconds": round(self.load_waited, 1)}
        self.log.say("LOAD  gate exhausted after %ds of waiting in this run (at %s, CPU %s%% idle): the remaining scenarios start "
            "without waiting" % (per_run, scenario.id, "unmeasured" if idle is None else "%.0f" % idle))

    def _scenario(self, scenario, index, total):
        cap, ledger, log = self.cap, self.ledger, self.log
        single = self.opts.survey or self.opts.repeat > 1  # a survey or a repeated run samples: one attempt each, no retry
        while True:
            self._budget_check(scenario)
            waited, idle = self._wait_for_load(scenario)
            before = ledger.counted(scenario.id)
            environment_before = ledger.environment_events(scenario.id)
            degraded_before = ledger.degraded_events(scenario.id)
            allowed = len(before) + 1 if single else ledger.allowance(scenario.id, cap)
            log.start(index, total, scenario, len(before) + 1, allowed)
            attempt, verdict = self._attempt(scenario)
            attempt.update(loadWaitSeconds=waited, cpuIdle=idle)
            self._require_unchanged("before recording the attempt of %s" % scenario.id)
            ledger.record_attempt(scenario.id, attempt)
            ledger.save()
            self.manifest.data["tolerances"] = tolerances.merge(self.manifest.data["tolerances"], attempt["tolerances"])
            self.manifest.data["refusals"] = tolerances.merge(self.manifest.data["refusals"], attempt["refusals"])
            self.manifest.data["attempts"].append({"scenario": scenario.id, "n": attempt["n"], "attemptDir": attempt["attemptDir"],
                "repetition": attempt["repetition"], "outcome": attempt["outcome"],
                "failureClass": attempt["failureClass"], "durationMs": attempt["durationMs"],
                "runnerDurationMs": attempt["runnerDurationMs"], "loadWaitSeconds": waited, "load1": attempt["load"]["start"][0],
                "cpuIdle": idle, "deviceLoad1": attempt["deviceLoad1"], "unmatchedRequests": attempt["unmatchedRequests"]["count"],
                "tolerances": attempt["tolerances"], "refusals": attempt["refusals"], **self._timings(attempt)})
            self.executed.add(scenario.id)
            self.series[(self.repetition, scenario.id)] = "passed" if verdict.passed \
                else "environment" if verdict.klass == cl.ENVIRONMENT else "failed"
            self.environment_attempts += 1 if verdict.klass == cl.ENVIRONMENT else 0
            self.attempts_since_recovery += 1
            self.greens_since_recovery += 1 if verdict.passed else 0
            self.manifest.write()
            seconds = attempt["durationMs"] / 1000.0
            if attempt["unmatchedRequests"]["count"]:
                log.say("NOTE  %s attempt %d: %d request(s) had no stub: %s" % (scenario.id, attempt["n"],
                    attempt["unmatchedRequests"]["count"], ", ".join(attempt["unmatchedRequests"]["paths"])))
            if verdict.passed:
                log.passed(scenario, attempt["n"], seconds)
                if verdict.note:
                    log.say("NOTE  %s attempt %d: %s" % (scenario.id, attempt["n"], verdict.note))
                return
            log.failed(scenario, attempt["n"], seconds, verdict.klass, verdict.summary)
            if verdict.klass == cl.ENVIRONMENT:
                if self.opts.survey:  # the scenario is not measured again, but the next ones need a simulator that can be read
                    if verdict.degraded:
                        self._recover(scenario, verdict)
                    else:
                        log.say("ENV   %s: noted; the survey goes on" % scenario.id)
                    return
                if environment_before:
                    text = "the simulator degraded twice on this scenario: it is the scenario, or the state it leaves behind, that " \
                        "brings the simulator down. Another retry is not a remedy." \
                        if verdict.degraded and degraded_before else \
                        "failed with class environment twice for this fingerprint. Another retry is not a remedy: something in " \
                        "the environment, or a mock state the scenario declares, breaks it every time."
                    log.stop(scenario, text)
                    raise StopRun("stopped", 5, text, scenario.id, verdict.klass, report.same_class_diagnosis(verdict.klass))
                self._recover(scenario, verdict)
                continue
            previous = [a["failureClass"] for a in before if a.get("failureClass")]
            if not self.opts.survey:
                if verdict.klass in cl.NON_RETRYABLE:
                    text = "class=%s is never retried. %s" % (verdict.klass, report.DIAGNOSIS[verdict.klass])
                    log.stop(scenario, text)
                    raise StopRun("stopped", 5, text, scenario.id, verdict.klass, report.DIAGNOSIS[verdict.klass])
                if verdict.klass in previous:
                    text = "failed twice with class %s. Another retry is not a remedy." % verdict.klass
                    log.stop(scenario, text)
                    raise StopRun("stopped", 5, text, scenario.id, verdict.klass, report.same_class_diagnosis(verdict.klass))
            used = len(ledger.counted(scenario.id))
            if single or used >= allowed:
                return
            log.retry(scenario, used + 1, allowed)

    def _recover(self, scenario, verdict):
        if verdict.degraded:
            self._note_degradation(scenario, verdict)
            self.log.say("ENV   %s: the simulator degraded; recovering it%s; this attempt does not count"
                % (scenario.id, "" if self.opts.survey else " and rerunning"))
            return self._recover_device(scenario, verdict)
        if self.recoveries >= 1:
            text = "a second environment failure in this run (%s)" % verdict.summary
            self.log.stop(scenario, text)
            raise StopRun("environment_refused", 3, text, scenario.id, cl.ENVIRONMENT, verdict.summary)
        self.recoveries += 1
        if not self.wiremock.health()[0]:
            # A dead WireMock is the harness's to restart; rebooting the device would find it dead again.
            self.log.say("ENV   %s: WireMock is down; restarting it (the device is not touched) and rerunning; this attempt does not count"
                % scenario.id)
            try:
                self.wiremock.restart()
            except EnvironmentRefused as error:
                raise StopRun("environment_refused", 3, "WireMock could not be restarted: %s" % error, scenario.id, cl.ENVIRONMENT,
                    verdict.summary)
            return
        self.log.say("ENV   %s: recovering the device once and rerunning; this attempt does not count" % scenario.id)
        self._recover_device(scenario, verdict)

    def _note_degradation(self, scenario, verdict):
        """Records a recovery caused by a degraded simulator, with the data that helps to find why the preferences daemon stops
        answering. The first one is always allowed; another one only after DEGRADATION_MIN_GREEN green scenarios since the previous."""
        record = self.manifest.data["deviceDegradation"]
        if record["events"] and self.greens_since_recovery < DEGRADATION_MIN_GREEN:
            text = "the simulator degraded again after %d green scenarios since the previous recovery (%d are needed); a simulator " \
                "that degrades twice in a row is not usable (%s)" % (self.greens_since_recovery, DEGRADATION_MIN_GREEN, verdict.summary)
            self.log.stop(scenario, text)
            raise StopRun("environment_refused", 3, text, scenario.id, cl.ENVIRONMENT, verdict.summary)
        self._degradation_event(scenario.id, verdict.summary, verdict.markers, verdict.marker_files)

    def _degradation_event(self, scenario_id, summary, markers, marker_files):
        record = self.manifest.data["deviceDegradation"]
        record["events"].append({"at": now(), "scenario": scenario_id, "scenariosSincePrevious": self.greens_since_recovery,
            "attemptsSincePrevious": self.attempts_since_recovery,
            "minutesSincePrevious": round((time.monotonic() - self.boot_started) / 60, 1),
            "previousIsHarnessBoot": not record["events"], "deviceBootedByHarness": self.device_booted, "summary": summary[:300],
            "markers": markers, "markersByFile": marker_files})
        record["count"] = len(record["events"])

    def _recover_device(self, scenario, verdict):
        recovered = self.adapter.call("recover")
        if not recovered.ok:
            raise StopRun("environment_refused", 3, "recover failed (full output in %s)" % recovered.log, scenario.id, cl.ENVIRONMENT,
                verdict.summary)
        self.greens_since_recovery = self.attempts_since_recovery = 0
        self.boot_started = time.monotonic()

    def _pre_attempt(self, scenario, env):
        healthy = self.adapter.call("health", env=env)
        self.health_degraded = not healthy.ok and DEGRADED_HEALTH_TEXT in healthy.stderr
        if not healthy.ok:
            return "health failed: %s" % (healthy.stderr.strip()[-200:] or "exit %d" % healthy.returncode)
        self.device_health = healthy.json
        ok, message = self.wiremock.health()
        if not ok:
            return "WireMock is not healthy: %s" % message
        if not self.adapter.call("reset-app", env=env).ok:
            return "reset-app failed"
        return None

    def _attempt(self, scenario):
        cfg, p, ledger = self.cfg, self.platform, self.ledger
        n = len(ledger.attempts(scenario.id)) + 1
        directory = "attempt-%d%s" % (n, "-r%d" % (self.repetition + 1) if self.repetition else "")
        adir = os.path.join(self.run_dir, "scenarios", scenario.id, directory)
        os.makedirs(adir)
        env = {"E2E_CURRENT_SCENARIO": scenario.id, "E2E_TRACE": "1" if self.opts.trace else "0"}
        evidence = cl.Evidence(scenario, self.catalog.account(scenario), p)
        since = int(time.time())
        started_at, began = now(), time.monotonic()
        load_start = read_load(cfg)[0]
        runner_ms = 0
        errors = []
        self.device_health = {}
        evidence.pre_failure = self._pre_attempt(scenario, env)
        evidence.degraded_health = self.health_degraded
        if not evidence.pre_failure:
            result = self.adapter.call("run-scenario", scenario.id, adir, cfg.ports[p],
                timeout=scenario.timeout + cfg.overhead_seconds(p), env=env)
            runner_ms = int(result.seconds * 1000)
            if result.timed_out:
                evidence.killed_after = scenario.timeout
            evidence.native_ok = result.json.get("nativeOk")
            evidence.tests_executed = result.json.get("testsExecuted")
            evidence.runner_log = self._read(os.path.join(adir, "runner.log")) or (result.stdout + result.stderr)
            self._read_result(adir, evidence)
            errors += self._probe(evidence, since, adir, env)
            evidence.journal = self.wiremock.journal()
            if self.wiremock.journal_error:
                errors.append(self.wiremock.journal_error)
                evidence.backend_down = not self.wiremock.health()[0]
            with open(os.path.join(adir, "wiremock-requests.json"), "w") as handle:
                json.dump({"requests": evidence.journal}, handle, indent=2)
        verdict = cl.classify(evidence)
        if not verdict.passed:
            self.adapter.call("collect-failure", adir, since, env=env)
            evidence.logs = {name: proc.tail_text(os.path.join(adir, name), FAILURE_LOG_TAIL_BYTES) for name in FAILURE_LOGS}
            evidence.driver_log = proc.tail_text(os.path.join(adir, "driver.log"), FAILURE_LOG_TAIL_BYTES)  # collect-failure may have brought it just now
            evidence.runner_log = evidence.runner_log[-FAILURE_LOG_TAIL_BYTES:]
            verdict = cl.classify(evidence)  # again: the logs only exist now, and a degraded simulator is the environment's
            with open(os.path.join(adir, "classification.json"), "w") as handle:
                json.dump({"class": verdict.klass, "summary": verdict.summary}, handle, indent=2)
        tolerated, refused = tolerances.read(os.path.join(adir, "driver.log"))  # after collect-failure: a killed run's log is home
        attempt = {
            "runId": self.run_id, "sha": self.git.sha, "startedAt": started_at, "finishedAt": now(),
            "durationMs": int((time.monotonic() - began) * 1000), "runnerDurationMs": runner_ms,
            "outcome": "passed" if verdict.passed else "failed", "failureClass": verdict.klass,
            "failureSummary": verdict.summary, "countsAgainstCap": verdict.klass != cl.ENVIRONMENT, "degraded": verdict.degraded,
            "load": {"start": load_start, "end": read_load(cfg)[0]}, "loadWaitSeconds": 0, "cpuIdle": None,
            "deviceLoad1": self.device_health.get("deviceLoad1"),
            "deviceLoadWaitSeconds": self.device_health.get("loadWaitSeconds"),
            "attemptDir": directory, "repetition": self.repetition + 1,
            "unmatchedRequests": cl.unmatched_summary(evidence.journal), "notes": [verdict.note] if verdict.note else [],
            "probeErrors": errors, "tolerances": tolerated if verdict.passed else {}, "refusals": refused,
            **self._timings(evidence.result),
            "artifacts": os.path.relpath(adir, str(cfg.root)) if adir.startswith(str(cfg.root)) else adir,
        }
        return attempt, verdict

    def _probe(self, evidence, since, adir, env):
        """The crash probe of the finished attempt. A crash report lands seconds after the crash, so a failed attempt waits for it
        (iOS); the report is attributed by when the crash happened, between the attempt's start and now. Returns the errors."""
        failed = evidence.killed_after is not None or not (evidence.result and evidence.result.get("outcome") == "passed")
        until = int(time.time()) + 1
        probe = self.adapter.call("crash-probe", since, adir, until, CRASH_REPORT_WAIT_SECONDS if failed else 0, env=env)
        if probe.ok and probe.json.get("kind"):
            evidence.crash = probe.json
        if not probe.ok or not probe.json.get("kind"):
            return ["crash-probe gave no answer (exit %s, %s); a crash would have gone unseen" % (probe.returncode, probe.log or "no output")]
        return []

    @staticmethod
    def _timings(source):
        """prepareBackendMs and launchMs when the runner's result.json (or the attempt built from it) carries them; absent is not zero."""
        return {key: source[key] for key in ("prepareBackendMs", "launchMs")
            if isinstance(source, dict) and isinstance(source.get(key), (int, float)) and not isinstance(source.get(key), bool)}

    @staticmethod
    def _read(path):
        try:
            return open(path, errors="replace").read()
        except OSError:
            return ""

    def _read_result(self, adir, evidence):
        path = os.path.join(adir, "result.json")
        if not os.path.exists(path):
            return
        try:
            result = json.load(open(path))
        except ValueError as error:
            evidence.result_error = "result.json is not valid JSON: %s" % error
            return
        if isinstance(result, dict):
            evidence.result = result
        else:
            evidence.result_error = "result.json is not a JSON object (it holds %s)" % type(result).__name__

    # -- finalisation ------------------------------------------------------------------------

    def _failed_list(self):
        failed = []
        for s in self.runnable:
            if s.id in self.failed_overall:
                failed.append((s.id, self.failed_overall[s.id].get("failureClass")))
            elif not self.ledger.passed(s.id) and (self.ledger.counted(s.id) or (self.opts.survey and s.id in self.executed)):
                if self._last_attempt(s) is None:
                    continue  # cut before its attempt was recorded: it was not run, which is not a failure
                failed.append((s.id, self._last_class(s)))  # a survey does not recover: what it could not measure is not a pass
        return failed

    def _publish(self):
        cfg, m, ledger, git = self.cfg, self.manifest, self.ledger, self.git
        pending = [s.id for s in self.runnable if not ledger.passed(s.id)]
        if pending or self._failed_list():
            raise UsageError("refusing to publish: %d scenario(s) are not green (%s)" % (len(pending), ", ".join(pending)))
        cfg.require_no_seams("publishing evidence", ledger.data["seams"])
        self._require_unchanged("before publishing")
        text = publish.description(self.platform, self.runnable, self.quarantined, git.sha, self.fingerprint, ledger)
        m.data["published"].update(description=text, attempted=True)
        with m.phase("publish"):
            try:
                publish.publish_once(cfg, self.platform, git, self.context, text, ledger, self.run_id, self.log)
            except publish.PublishError as error:
                m.data["published"]["ok"] = False
                self.log.say("PUBLISH FAILED %s. The ledger is intact: rerun `e2e.py publish --platform %s`." % (error, self.platform))
                m.data["stop"]["reason"] = str(error)
                return "publish_failed", 6
        m.data["published"]["ok"] = True
        return "passed", 0

    def _results(self):
        results, failed = [], dict(self._failed_list())
        for s in self.runnable:
            entries = self.ledger.attempts(s.id)
            if s.id in failed:
                # The attempt that failed: the one a repetition kept when the scenario failed in it, or the last of this one.
                last = self.failed_overall.get(s.id) or self._last_attempt(s) or {}
                results.append({"id": s.id, "status": "failed", "class": failed[s.id], "summary": last.get("failureSummary", ""),
                    "seconds": last.get("durationMs", 0) / 1000.0, "tolerances": {}, "refusals": last.get("refusals", {})})
            elif self.ledger.passed(s.id) or s.id in self.passed_overall:
                # Passed in this repetition, or in an earlier one when the cut came before this scenario's turn.
                won = ([a for a in entries if a["outcome"] == "passed"] or [self.passed_overall.get(s.id)])[-1]
                results.append({"id": s.id, "status": "passed", "seconds": won["durationMs"] / 1000.0,
                    "producedByRunId": won["runId"] if won["runId"] != self.run_id else None, "producedBySha": won["sha"],
                    "tolerances": won.get("tolerances", {})})
            else:
                results.append({"id": s.id, "status": "skipped", "summary": "not run", "seconds": 0.0})
        for s in self.quarantined:
            results.append({"id": s.id, "status": "skipped", "seconds": 0.0,
                "summary": "quarantined until %s: %s" % (s.quarantine["until"], s.quarantine["reason"])})
        return results

    def _finish(self, outcome, code):
        """Results, JUnit and the finalised manifest first; the retention (up to 600 s) after, so that a kill during it still
        leaves a finalised run. A defect while the results are written is a harness error: the manifest, the RESULT line and the
        exit code say so (an outcome that was already a failure keeps its own code). Returns the outcome and the code."""
        try:
            self._write_results(outcome, code)
        except Exception as error:
            self.log.say("HARNESS ERROR %s: %s (traceback in harness-error.txt)" % (type(error).__name__, error))
            self.manifest.data["stop"]["reason"] = "%s: %s" % (type(error).__name__, error)
            if self.run_dir:
                with open(os.path.join(self.run_dir, "harness-error.txt"), "a") as handle:
                    handle.write(traceback.format_exc())
            if code == 0:
                outcome, code = "harness_error", EXIT_HARNESS_ERROR
            green = sum(1 for s in self.runnable if self.ledger is not None and self.ledger.passed(s.id))
            self.log.result(outcome, green, len(self.runnable), [], 0, code, self.fingerprint or "")
        finally:
            self.manifest.finalize(outcome, code)
        try:
            if self.run_dir and outcome != "interrupted":
                self._retention()
                self.manifest.write()
        finally:
            self.log.emit_result()
        self._archive(outcome)
        return outcome, code

    def _archive(self, outcome):
        """The manifest of a green evidence run is also kept under certifications/<sha>/<platform>."""
        if self.run_dir and outcome == "passed" and self.evidence and not self.stop and self.ledger is not None \
                and self.catalog is not None:
            target = os.path.join(str(self.cfg.state_root), "certifications", self.git.sha, self.platform)
            os.makedirs(target, exist_ok=True)
            shutil.copyfile(self.manifest.path, os.path.join(target, "manifest.json"))

    def _write_results(self, outcome, code):
        m = self.manifest
        if not self.evidence and self.opts.repeat > 1 and self.series:
            self._series_summary()
        if self.ledger is not None and self.catalog is not None and self.run_dir:
            results = self._results()
            junit.write(os.path.join(self.run_dir, "junit.xml"), self.platform, results)
            m.data["results"] = [self._manifest_result(r) for r in results]
            m.data["overrides"]["scenarioResets"] = [
                dict(o, scenario=s.id) for s in self.runnable
                for o in self.ledger.data["scenarios"].get(s.id, {}).get("overrides", [])]
            passed_run = [r for r in results if r["status"] == "passed" and r.get("producedByRunId") is None]
            failed = [r for r in results if r["status"] == "failed"]
            m.data["scenarios"].update(
                executed=len(self.executed), passed=len(passed_run), failed=len(failed),
                passedOnRetry=sum(1 for s in self.runnable if s.id in self.executed and self.ledger.passed(s.id)
                    and any(a["outcome"] == "failed" for a in self.ledger.attempts(s.id))),
                notRun=sum(1 for r in results if r["status"] == "skipped" and r["summary"] == "not run"))
            green = sum(1 for r in results if r["status"] == "passed")
            if m.data["tolerances"]:
                self.log.say("TOLERANCES %s" % " ".join("%s=%d" % item for item in sorted(m.data["tolerances"].items())))
            line = self.log.result(outcome, green, len(self.runnable), self._failed_list(), m.data["scenarios"]["notRun"],
                code, self.fingerprint)
            report.write_summary(os.path.join(self.run_dir, "summary.txt"), [line] + [
                "%s %s %s" % (r["status"].upper(), r["id"], r.get("summary", "")) for r in results]
                + ["tolerances: %s" % json.dumps(m.data["tolerances"], sort_keys=True),
                    "refusals: %s" % json.dumps(m.data["refusals"], sort_keys=True)])
            if outcome == "passed" and self.evidence and not self.stop:
                self.ledger.update_index(self.cfg.state_root, self.git.sha, time.monotonic() - self.started)
        if self.env_checked and self.run_dir and not self.cfg.seam("E2E_FAKE_HOST_METRICS"):
            m.data["competingProcesses"]["end"] = envcheck.top_processes(envcheck._process_table())

    def _retention_fingerprint(self):
        """The fingerprint of HEAD, whose ledger and runs the retention keeps; a diagnose run asks for it too, or the
        failures of the evidence in progress would be trimmed away by diagnostic runs."""
        if self.evidence:
            return self.fingerprint
        try:
            return self._fingerprint()
        except UsageError:
            return None

    def _retention(self):
        """Trims what earlier runs left (this run is named, so it is never touched). A failure is logged and recorded in
        the manifest and changes neither the outcome nor the exit code."""
        cfg, m = self.cfg, self.manifest
        command = shlex.split(cfg.seam("E2E_RETENTION_CMD")) if cfg.seam("E2E_RETENTION_CMD") else [sys.executable, str(RETENTION_SCRIPT)]
        argv = command + ["--apply", "--json", "--state-root", str(cfg.state_root), "--tmp-root", str(cfg.tmp_root),
            "--current-run", self.run_id, "--max-gb", str(cfg.artifacts_max_gb)]
        fingerprint = self._retention_fingerprint()
        if fingerprint:
            argv += ["--fingerprint", fingerprint]
        record = {"ran": True, "ok": False, "exitCode": None, "freedBytes": 0, "actions": 0, "error": None}
        try:
            result = proc.run(argv, RETENTION_TIMEOUT_SECONDS, cwd=str(cfg.root), env=cfg.env)
            summary = proc.parse_json(result.stdout)
            record.update(exitCode=result.returncode, freedBytes=summary.get("freedBytes", 0), actions=summary.get("actions", 0))
            if summary.get("busy"):  # another retention is trimming right now: nothing for this run to do
                record.update(ok=True, busy=True)
            else:
                record["ok"] = result.ok and not summary.get("errors") and bool(summary)
            if not record["ok"]:
                record["error"] = "timed out" if result.timed_out \
                    else "; ".join(summary.get("errors") or []) or result.stderr.strip()[-300:] or "exit %s" % result.returncode
            if summary.get("currentRunBytes") is not None:
                m.data["artifactsBytes"] = summary["currentRunBytes"]
        except (OSError, proc.Interrupted) as error:
            record["error"] = "interrupted" if isinstance(error, proc.Interrupted) else str(error)
        m.data["retention"] = record
        self.log.say("RETENTION %s" % ("skipped, another retention is running" if record.get("busy") else
            "freed %d bytes in %d action(s)" % (record["freedBytes"], record["actions"]) if record["ok"]
            else "FAILED, the run result is unchanged: %s" % record["error"]))

    @staticmethod
    def _manifest_result(item):
        return {"id": item["id"], "status": item["status"], "durationMs": int(item["seconds"] * 1000),
            "producedBySha": item.get("producedBySha"), "producedByRunId": item.get("producedByRunId"), "failureClasses": [item["class"]] if item.get("class") else [],
            "tolerances": item.get("tolerances", {}), "refusals": item.get("refusals", {})}
