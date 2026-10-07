"""The per-platform run: scope, ledger, device preparation, scenario loop, finalisation."""

import json
import os
import shlex
import shutil
import subprocess
import sys
import time
from contextlib import nullcontext
from pathlib import Path

from . import catalog as catalog_mod
from . import classify as cl
from . import envcheck, hostlock, junit, proc, publish, report, toolchain
from .config import SUITE_ID, VERB_TIMEOUTS, EnvironmentRefused, UsageError
from .gitstate import GitState
from .ledger import Ledger, now
from .manifest import Manifest, make_run_dir, read_load
from .wiremock import WireMock

# Step 10 of the run: e2e/tools/e2e-retention.py trims what earlier runs left. It lives outside e2e/scripts, so it is
# not part of the evidence fingerprint; E2E_RETENTION_CMD replaces it in tests.
RETENTION_SCRIPT = Path(__file__).resolve().parents[3] / "tools" / "e2e-retention.py"
RETENTION_TIMEOUT_SECONDS = 600

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
            trace=False, dry_run=False, force=False, child_of=None, stop_device=False):
        self.mode = mode
        self.scenarios = scenarios or []
        self.tag, self.changed_since, self.survey = tag, changed_since, survey
        self.repeat, self.trace, self.dry_run, self.force = repeat, trace, dry_run, force
        # Set for a platform of `--platform all`: the parent run id, whether to stop a device this run booted,
        # and the parent's (decision, reason).
        self.child_of, self.stop_device, self.parallel = child_of, stop_device, None


class Adapter:
    def __init__(self, config, platform):
        self.config, self.platform = config, platform
        self.base = config.adapter_command(platform)

    def call(self, verb, *args, **kwargs):
        env = dict(self.config.env)
        env.update(kwargs.get("env") or {})
        timeout = kwargs.get("timeout") or VERB_TIMEOUTS[verb]
        result = proc.run(self.base + [verb] + [str(a) for a in args], timeout, cwd=str(self.config.root), env=env)
        result.json = proc.parse_json(result.stdout)
        return result


class PlatformRun:
    def __init__(self, config, platform, options):
        self.cfg, self.platform, self.opts = config, platform, options
        self.evidence = options.mode == "evidence"
        self.log = report.Log(platform)
        self.adapter = Adapter(config, platform)
        self.started = time.monotonic()
        self.recoveries = 0
        self.load_waited = 0.0
        self.ledger = None
        self.catalog = None
        self.runnable, self.quarantined = [], []
        self.run_dir = None
        self.run_id = None
        self.wiremock = None
        self.fingerprint = options.mode if not self.evidence else None
        self.context = None
        self.failed_overall = {}
        self.repetition = 0
        self.executed = set()
        self.stop = None
        self.device_booted = False
        self.env_checked = False
        self.device_health = {}

    # -- entry point -------------------------------------------------------------------------

    def execute(self):
        self.git = GitState(self.cfg.root)
        if not self.opts.dry_run:
            self.run_id, self.run_dir = make_run_dir(self.cfg.state_root, self.platform, self.opts.mode, self.git.sha7)
            self.log.attach(os.path.join(self.run_dir, "run.log"))
        self.manifest = Manifest(self.run_dir or "", self.run_id or "dry-run", self.opts.mode, self.platform, self.cfg,
            enabled=not self.opts.dry_run)
        self.manifest.update(commitSha=self.git.sha, branch=self.git.branch, upstreamSha=self.git.upstream_sha,
            treeClean=self.git.tree_clean, headEqualsUpstream=self.git.head_equals_upstream)
        if self.opts.parallel:
            self.manifest.data["parallel"].update(decision=self.opts.parallel[0], reason=self.opts.parallel[1])
        self.manifest.data["parentRunId"] = self.opts.child_of
        self.manifest.write()
        outcome, code = "failed", 1
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
            self.log.say("INTERRUPTED by signal %d; the manifest is finalised" % error.signum)
            self.manifest.data["stop"]["reason"] = "signal %d" % error.signum
            outcome, code = "interrupted", 128 + error.signum
        finally:
            self._cleanup()
            self._finish(outcome, code)
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
                cfg.layout["E2E_FINGERPRINT_VERSION"], lock=not self.opts.dry_run)
            if not self.opts.dry_run:
                self.ledger.save()
        else:
            self.ledger = Ledger.memory(self.platform)
        cap = cfg.attempt_cap
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
        pending = self._order(pending)
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
        for fields in lines:
            required = "local-e2e/%s/%s" % (self.platform, fields[1] if len(fields) > 1 else SUITE_ID)
            if required != self.context:
                raise UsageError("the scope requires the status %s but this harness can only publish %s"
                    % (required, self.context))

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

    def _prepare(self):
        m, adapter, cfg = self.manifest, self.adapter, self.cfg
        for name in ("toolchain", "env_check"):
            with m.phase("env-check" if name == "env_check" else name):
                if HOOKS[name]:
                    HOOKS[name](self)
        lock = hostlock.prepare_lock(cfg, self.platform, self.run_id, self.log.say, m.data["prepareLock"].update) \
            if self.opts.child_of else nullcontext()  # `--platform all`: one platform prepares its device at a time
        with lock:
            with m.phase("device"):
                result = adapter.call("ensure-device")
                if not result.ok:
                    raise EnvironmentRefused("ensure-device failed: %s" % (result.stderr.strip()[-300:] or result.returncode))
                m.update(device=result.json)
                self.device_booted = bool(result.json.get("bootedByHarness"))
            with m.phase("wiremock"):
                self.wiremock = WireMock(cfg, self.platform, self.run_dir)
                self.wiremock.start()
                m.data["wiremock"]["log"] = "wiremock.log"
            with m.phase("build"):
                result = adapter.call("build", cfg.ports[self.platform])
                if not result.ok:
                    raise StopRun("failed", 1, "build failed: %s" % result.stderr.strip()[-300:])
            with m.phase("install"):
                if not adapter.call("install").ok:
                    raise EnvironmentRefused("install failed")
        with m.phase("enumerate"):
            result = adapter.call("enumerate")
            if not result.ok:
                raise UsageError("enumerate failed: %s" % result.stderr.strip()[-300:])
            tests = result.json.get("tests", [])
            bad = ["%s (%d tests)" % (s.id, tests.count(s.runner_identifier(self.platform))) for s in self.runnable
                if tests.count(s.runner_identifier(self.platform)) != 1]
            if bad:
                raise UsageError("every catalog id needs exactly one runner test; mismatches: %s" % ", ".join(bad))

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
                    self.failed_overall[s.id] = self._last_class(s)
        if not self.evidence and self.opts.repeat > 1:
            self.log.say("REPEAT %d runs: %d scenarios failed in at least one" % (self.opts.repeat, len(self.failed_overall)))

    def _last_class(self, scenario):
        counted = self.ledger.counted(scenario.id)
        return counted[-1].get("failureClass") if counted else None

    def _budget_check(self, scenario):
        elapsed = time.monotonic() - self.started
        if elapsed + scenario.timeout > self.cfg.budget_seconds:
            text = "%s needs up to %ds but only %ds of the %d minute budget remain; stopping. Greens are kept; run again to continue." \
                % (scenario.id, scenario.timeout, max(0, self.cfg.budget_seconds - elapsed), self.cfg.budget_minutes)
            self.log.say("BUDGET %s" % text)
            raise StopRun("budget_exhausted", 4, text, scenario.id)

    def _wait_for_load(self):
        """The host gate before a scenario, in two steps: load1/ncpu is the cheap filter and the CPU is measured only
        above it; the run waits only while the CPU is busy. Returns (seconds waited, CPU idle percent if measured)."""
        cfg, m = self.cfg, self.manifest
        load, ncpu = read_load(cfg)
        m.sample_load(load)
        if load[0] / ncpu < envcheck.LOAD_MEASURE_RATIO:
            return 0, None
        idle = envcheck.cpu_idle(cfg)
        if idle is None or idle >= envcheck.CPU_IDLE_WAIT_BELOW:
            return 0, idle
        poll = float(cfg.seam("E2E_FAKE_LOAD_POLL_SECONDS") or 10)
        per_scenario, per_run = (float(v) for v in (cfg.seam("E2E_FAKE_LOAD_WAIT_CAPS") or "%d,%d" % (
            envcheck.LOAD_WAIT_PER_SCENARIO_SECONDS, envcheck.LOAD_WAIT_PER_RUN_SECONDS)).split(","))
        began = time.monotonic()
        self.log.say("LOAD  load1/ncpu is %.2f and the CPU is %.0f%% idle; waiting for %d%% idle"
            % (load[0] / ncpu, idle, envcheck.CPU_IDLE_WAIT_UNTIL))
        while idle is not None and idle < envcheck.CPU_IDLE_WAIT_UNTIL:
            waited = time.monotonic() - began
            if waited >= per_scenario or self.load_waited + waited >= per_run:
                break
            time.sleep(poll)
            load, ncpu = read_load(cfg)
            m.sample_load(load)
            idle = envcheck.cpu_idle(cfg)
        waited = time.monotonic() - began
        self.load_waited += waited
        m.data["load"]["waitSeconds"] = round(self.load_waited, 1)
        return round(waited, 1), idle

    def _scenario(self, scenario, index, total):
        cap, ledger, log = self.cfg.attempt_cap, self.ledger, self.log
        while True:
            self._budget_check(scenario)
            waited, idle = self._wait_for_load()
            before = ledger.counted(scenario.id)
            allowed = len(before) + 1 if self.opts.survey else ledger.allowance(scenario.id, cap)
            log.start(index, total, scenario, len(before) + 1, allowed)
            attempt, verdict = self._attempt(scenario)
            attempt.update(loadWaitSeconds=waited, cpuIdle=idle)
            ledger.record_attempt(scenario.id, attempt, cap)
            ledger.save()
            self.manifest.data["attempts"].append({"scenario": scenario.id, "n": attempt["n"], "outcome": attempt["outcome"],
                "failureClass": attempt["failureClass"], "durationMs": attempt["durationMs"],
                "runnerDurationMs": attempt["runnerDurationMs"], "loadWaitSeconds": waited, "load1": attempt["load"]["start"][0],
                "cpuIdle": idle, "deviceLoad1": attempt["deviceLoad1"]})
            self.executed.add(scenario.id)
            self.manifest.write()
            seconds = attempt["durationMs"] / 1000.0
            if verdict.passed:
                log.passed(scenario, attempt["n"], seconds)
                return
            log.failed(scenario, attempt["n"], seconds, verdict.klass, verdict.summary)
            if verdict.klass == cl.ENVIRONMENT:
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
            if self.opts.survey or used >= allowed:
                return
            log.retry(scenario, used + 1, allowed)

    def _recover(self, scenario, verdict):
        if self.recoveries >= 1:
            text = "a second environment failure in this run (%s)" % verdict.summary
            self.log.stop(scenario, text)
            raise StopRun("environment_refused", 3, text, scenario.id, cl.ENVIRONMENT, verdict.summary)
        self.recoveries += 1
        self.log.say("ENV   %s: recovering the device once and rerunning; this attempt does not count" % scenario.id)
        if not self.adapter.call("recover").ok:
            raise StopRun("environment_refused", 3, "recover failed", scenario.id, cl.ENVIRONMENT, verdict.summary)

    def _pre_attempt(self, scenario, env):
        healthy = self.adapter.call("health", env=env)
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
        suffix = "-r%d" % (self.repetition + 1) if self.repetition else ""
        adir = os.path.join(self.run_dir, "scenarios", scenario.id, "attempt-%d%s" % (n, suffix))
        os.makedirs(adir)
        env = {"E2E_CURRENT_SCENARIO": scenario.id, "E2E_TRACE": "1" if self.opts.trace else "0"}
        evidence = cl.Evidence(scenario, self.catalog.account(scenario))
        since = int(time.time())
        started_at, began = now(), time.monotonic()
        load_start = read_load(cfg)[0]
        runner_ms = 0
        self.device_health = {}
        evidence.pre_failure = self._pre_attempt(scenario, env)
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
            probe = self.adapter.call("crash-probe", since, adir, env=env)
            if probe.ok and probe.json.get("kind"):
                evidence.crash = probe.json
            evidence.journal = self.wiremock.journal()
            with open(os.path.join(adir, "wiremock-requests.json"), "w") as handle:
                json.dump({"requests": evidence.journal}, handle, indent=2)
        verdict = cl.classify(evidence)
        if not verdict.passed:
            self.adapter.call("collect-failure", adir, since, env=env)
            with open(os.path.join(adir, "classification.json"), "w") as handle:
                json.dump({"class": verdict.klass, "summary": verdict.summary}, handle, indent=2)
        attempt = {
            "runId": self.run_id, "sha": self.git.sha, "startedAt": started_at, "finishedAt": now(),
            "durationMs": int((time.monotonic() - began) * 1000), "runnerDurationMs": runner_ms,
            "outcome": "passed" if verdict.passed else "failed", "failureClass": verdict.klass,
            "failureSummary": verdict.summary, "countsAgainstCap": verdict.klass != cl.ENVIRONMENT,
            "load": {"start": load_start, "end": read_load(cfg)[0]}, "loadWaitSeconds": 0, "cpuIdle": None,
            "deviceLoad1": self.device_health.get("deviceLoad1"),
            "deviceLoadWaitSeconds": self.device_health.get("loadWaitSeconds"),
            "artifacts": os.path.relpath(adir, str(cfg.root)) if adir.startswith(str(cfg.root)) else adir,
        }
        return attempt, verdict

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
            evidence.result = json.load(open(path))
        except ValueError as error:
            evidence.result_error = "result.json is not valid JSON: %s" % error

    # -- finalisation ------------------------------------------------------------------------

    def _failed_list(self):
        failed = []
        for s in self.runnable:
            if s.id in self.failed_overall:
                failed.append((s.id, self.failed_overall[s.id]))
            elif not self.ledger.passed(s.id) and self.ledger.counted(s.id):
                failed.append((s.id, self._last_class(s)))
        return failed

    def _publish(self):
        cfg, m, ledger, git = self.cfg, self.manifest, self.ledger, self.git
        retried = sum(1 for s in self.runnable
            if any(a["outcome"] == "failed" and a["countsAgainstCap"] for a in ledger.attempts(s.id)))
        overrides = sum(len(ledger.data["scenarios"].get(s.id, {}).get("overrides", [])) for s in self.runnable)
        text = publish.description(self.platform, len(self.runnable), git.sha, self.fingerprint, retried,
            len(self.quarantined), overrides)
        m.data["published"].update(description=text, attempted=True)
        with m.phase("publish"):
            if ledger.publication(git.sha, self.context, text):
                self.log.say("status %s already published for %s" % (self.context, git.sha7))
            else:
                try:
                    publish.publish_success(cfg, self.platform, git.sha, self.context, text)
                except publish.PublishError as error:
                    m.data["published"]["ok"] = False
                    self.log.say("PUBLISH FAILED %s. The ledger is intact: rerun `e2e.py publish --platform %s`."
                        % (error, self.platform))
                    m.data["stop"]["reason"] = str(error)
                    return "publish_failed", 6
                ledger.add_publication({"sha": git.sha, "context": self.context, "description": text,
                    "publishedAt": now(), "runId": self.run_id})
                ledger.save()
        m.data["published"]["ok"] = True
        return "passed", 0

    def _results(self):
        results, failed = [], dict(self._failed_list())
        for s in self.runnable:
            entries = self.ledger.attempts(s.id)
            if s.id in failed:
                last = self.ledger.counted(s.id)[-1] if self.ledger.counted(s.id) else entries[-1]
                results.append({"id": s.id, "status": "failed", "class": failed[s.id], "summary": last["failureSummary"],
                    "seconds": last["durationMs"] / 1000.0})
            elif self.ledger.passed(s.id):
                won = [a for a in entries if a["outcome"] == "passed"][-1]
                results.append({"id": s.id, "status": "passed", "seconds": won["durationMs"] / 1000.0,
                    "producedByRunId": won["runId"] if won["runId"] != self.run_id else None, "producedBySha": won["sha"]})
            else:
                results.append({"id": s.id, "status": "skipped", "summary": "not run", "seconds": 0.0})
        for s in self.quarantined:
            results.append({"id": s.id, "status": "skipped", "seconds": 0.0,
                "summary": "quarantined until %s: %s" % (s.quarantine["until"], s.quarantine["reason"])})
        return results

    def _finish(self, outcome, code):
        m = self.manifest
        if self.run_dir and outcome != "interrupted":
            self._retention()  # before the RESULT line, which stays the last one of the run
        if self.ledger is not None and self.catalog is not None and self.run_dir:
            results = self._results()
            counts = junit.write(os.path.join(self.run_dir, "junit.xml"), self.platform, results)
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
            self.log.result(outcome, green, len(self.runnable), self._failed_list(), m.data["scenarios"]["notRun"],
                code, self.fingerprint)
            report.write_summary(os.path.join(self.run_dir, "summary.txt"), [self.log.lines[-1]] + [
                "%s %s %s" % (r["status"].upper(), r["id"], r.get("summary", "")) for r in results])
            if outcome == "passed" and self.evidence and not self.stop:
                self.ledger.update_index(self.cfg.state_root, self.git.sha, time.monotonic() - self.started)
                target = os.path.join(str(self.cfg.state_root), "certifications", self.git.sha, self.platform)
                os.makedirs(target, exist_ok=True)
                m.finalize(outcome, code)
                shutil.copyfile(m.path, os.path.join(target, "manifest.json"))
        if self.env_checked and self.run_dir and not self.cfg.seam("E2E_FAKE_HOST_METRICS"):
            m.data["competingProcesses"]["end"] = envcheck.top_processes(envcheck._process_table())
        m.finalize(outcome, code)
        self.log.close()

    def _retention(self):
        """Trims what earlier runs left (this run is named, so it is never touched). A failure is logged and recorded in
        the manifest and changes neither the outcome nor the exit code."""
        cfg, m = self.cfg, self.manifest
        command = shlex.split(cfg.seam("E2E_RETENTION_CMD")) if cfg.seam("E2E_RETENTION_CMD") else [sys.executable, str(RETENTION_SCRIPT)]
        argv = command + ["--apply", "--json", "--state-root", str(cfg.state_root), "--tmp-root", str(cfg.tmp_root),
            "--current-run", self.run_id, "--max-gb", str(cfg.artifacts_max_gb)]
        if self.evidence and self.fingerprint:
            argv += ["--fingerprint", self.fingerprint]
        record = {"ran": True, "ok": False, "exitCode": None, "freedBytes": 0, "actions": 0, "error": None}
        try:
            result = proc.run(argv, RETENTION_TIMEOUT_SECONDS, cwd=str(cfg.root), env=cfg.env)
            summary = proc.parse_json(result.stdout)
            record.update(exitCode=result.returncode, freedBytes=summary.get("freedBytes", 0), actions=summary.get("actions", 0))
            record["ok"] = result.ok and not summary.get("errors") and bool(summary)
            if not record["ok"]:
                record["error"] = "timed out" if result.timed_out \
                    else "; ".join(summary.get("errors") or []) or result.stderr.strip()[-300:] or "exit %s" % result.returncode
            if summary.get("currentRunBytes") is not None:
                m.data["artifactsBytes"] = summary["currentRunBytes"]
        except (OSError, proc.Interrupted) as error:
            record["error"] = "interrupted" if isinstance(error, proc.Interrupted) else str(error)
        m.data["retention"] = record
        self.log.say("RETENTION %s" % ("freed %d bytes in %d action(s)" % (record["freedBytes"], record["actions"]) if record["ok"]
            else "FAILED, the run result is unchanged: %s" % record["error"]))

    @staticmethod
    def _manifest_result(item):
        return {"id": item["id"], "status": item["status"], "durationMs": int(item["seconds"] * 1000),
            "producedBySha": item.get("producedBySha"), "producedByRunId": item.get("producedByRunId"), "failureClasses": [item["class"]] if item.get("class") else []}
