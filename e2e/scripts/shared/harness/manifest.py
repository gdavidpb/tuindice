"""Run manifest (build/e2e/runs/<runId>/manifest.json) and the host facts it records.

The manifest is written at start, after every attempt, and finalised in a `finally`
block and by the signal handlers, so a failed or killed run still leaves one.
"""

import datetime
import json
import os
import platform as host_platform
import shutil
import subprocess
import time
from contextlib import contextmanager

from .ledger import now

SCHEMA = "tuindice-e2e-run/1"
MAX_LOAD_SAMPLES = 200


def _sysctl(name):
    try:
        return subprocess.check_output(["sysctl", "-n", name], stderr=subprocess.DEVNULL, universal_newlines=True).strip()
    except (OSError, subprocess.CalledProcessError):
        return ""


FAKE_HOST_DEFAULTS = {"diskFreeGb": 500.0, "uptimeDays": 1.0, "memGb": 64, "memAvailableGb": 64.0, "procs": [],
    "daemons": 0, "foreignDevices": []}


def fake_metrics(config):
    """E2E_FAKE_HOST_METRICS is a JSON file re-read on every call: {"load": [...], "ncpu": n} plus optional
    diskFreeGb, uptimeDays, memGb, memAvailableGb, procs, daemons, foreignDevices (healthy defaults).
    None when the seam is unset; with it set nothing about the host is probed."""
    path = config.seam("E2E_FAKE_HOST_METRICS")
    if not path:
        return None
    try:
        data = json.load(open(path))
    except (OSError, ValueError):
        data = {}
    return dict(FAKE_HOST_DEFAULTS, **data)


def read_load(config):
    """(load averages, ncpu), from the fake metrics when the seam is set."""
    fake = fake_metrics(config)
    if fake and "load" in fake and "ncpu" in fake:
        return list(fake["load"]), int(fake["ncpu"])
    try:
        return list(os.getloadavg()), os.cpu_count() or 1
    except OSError:
        return [0.0, 0.0, 0.0], os.cpu_count() or 1


def collect_host(config):
    load, ncpu = read_load(config)
    uptime_days = None
    boot = _sysctl("kern.boottime")
    if "sec =" in boot:
        try:
            uptime_days = round((time.time() - int(boot.split("sec =")[1].split(",")[0])) / 86400, 1)
        except ValueError:
            pass
    mem = _sysctl("hw.memsize")
    usage = shutil.disk_usage(str(config.root))
    host = {
        "model": _sysctl("hw.model") or host_platform.node(), "cpu": _sysctl("machdep.cpu.brand_string"), "ncpu": ncpu,
        "memGb": round(int(mem) / 2 ** 30) if mem.isdigit() else None,
        "os": "%s %s" % (host_platform.system(), host_platform.release()), "uptimeDays": uptime_days,
        "diskFreeGb": round(usage.free / 2 ** 30, 1),
    }
    fake = fake_metrics(config)
    if fake:
        host.update({key: fake[key] for key in ("memGb", "uptimeDays", "diskFreeGb")})
    return host, load


class Manifest:
    def __init__(self, run_dir, run_id, mode, platform, config, enabled=True):
        self.path = os.path.join(str(run_dir), "manifest.json")
        self.enabled = enabled
        self.started = time.monotonic()
        host, load = collect_host(config)
        self.data = {
            "schema": SCHEMA, "runId": run_id, "mode": mode, "platform": platform, "outcome": "running", "exitCode": None,
            "parentRunId": None, "commitSha": None, "branch": None, "upstreamSha": None, "treeClean": None, "headEqualsUpstream": None,
            "fingerprint": None, "fingerprintVersion": config.layout.get("E2E_FINGERPRINT_VERSION"), "statusContext": None,
            "published": {"required": False, "attempted": False, "ok": None, "description": None},
            "startedAt": now(), "finishedAt": None, "durationSeconds": 0, "phases": [],
            "budget": {"minutes": config.budget_minutes, "usedSeconds": 0},
            "retryPolicy": {"maxRetries": config.max_retries, "nonRetryable": ["typed_text_mismatch", "app_crash"]},
            "parallel": {"requested": config.parallel, "decision": "sequential",
                "reason": "single platform"},
            "overrides": {"env": list(config.env_override), "parallel": None if config.parallel == "auto" else config.parallel,
                "scenarioResets": []},
            "host": host, "load": {"start": load, "end": load, "max1m": load[0], "samples": []},
            "competingProcesses": {"start": [], "end": [], "atFailures": []},
            "envCheck": [], "toolchain": {"lockFile": None, "lockMatches": None, "actual": {}, "informational": {}, "libs": {}},
            "device": {}, "appVersion": {}, "wiremock": {"port": config.ports[platform], "delayProfile": config.delay_profile},
            "scenarios": {"inScope": 0, "quarantined": 0, "alreadyGreen": 0, "executed": 0, "passed": 0,
                "passedOnRetry": 0, "failed": 0, "notRun": 0},
            "results": [], "stop": {"reason": None, "scenario": None, "failureClass": None, "diagnosis": None},
            "artifactsBytes": 0,
        }

    def update(self, **values):
        self.data.update(values)

    def sample_load(self, load):
        samples = self.data["load"]["samples"]
        if len(samples) < MAX_LOAD_SAMPLES:
            samples.append({"at": now(), "load": load})
        self.data["load"]["end"] = load
        self.data["load"]["max1m"] = max(self.data["load"]["max1m"], load[0])

    @contextmanager
    def phase(self, name):
        began = time.monotonic()
        record = {"name": name, "durationSeconds": 0, "ok": False}
        self.data["phases"].append(record)
        try:
            yield
            record["ok"] = True
        finally:
            record["durationSeconds"] = round(time.monotonic() - began, 1)
            self.write()

    def write(self):
        if not self.enabled:
            return
        self.data["durationSeconds"] = int(time.monotonic() - self.started)
        self.data["budget"]["usedSeconds"] = self.data["durationSeconds"]
        temporary = self.path + ".tmp"
        with open(temporary, "w") as handle:
            json.dump(self.data, handle, indent=2)
        os.replace(temporary, self.path)

    def finalize(self, outcome, exit_code):
        self.data["outcome"] = outcome
        self.data["exitCode"] = exit_code
        self.data["finishedAt"] = now()
        self.write()


def make_run_dir(state_root, platform, mode, sha7):
    stamp = datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    base = "%s-%s-%s-%s" % (stamp, platform, mode, sha7)
    run_id, counter = base, 1
    while True:
        path = os.path.join(str(state_root), "runs", run_id)
        try:
            os.makedirs(path)
            return run_id, path
        except FileExistsError:
            counter += 1
            run_id = "%s-%d" % (base, counter)
