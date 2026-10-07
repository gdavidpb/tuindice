"""Environment check: measure the host and refuse (evidence only) or warn before any build or boot."""

import json
import os
import re
import subprocess
import time

from . import manifest as manifest_mod
from . import toolchain
from .config import PLATFORMS, EnvironmentRefused

# Provisional thresholds; revise from `e2e-profile.py --compare`.
LOAD_WARN = 0.50  # load1 / ncpu: informational only. macOS counts threads that wake for an instant, so it overstates contention.
# Host CPU, measured as the idle percentage over a short window (`top -l 2 -n 0 -s 1`, about 1.6 s). Every CPU number is here.
LOAD_MEASURE_RATIO = 1.0  # per-scenario gate: below this load1/ncpu the CPU is not even measured
CPU_IDLE_WAIT_BELOW, CPU_IDLE_WAIT_UNTIL = 20, 35  # per-scenario gate: wait when idle < 20 % and until it reaches 35 %
LOAD_WAIT_PER_SCENARIO_SECONDS, LOAD_WAIT_PER_RUN_SECONDS = 300, 900
CPU_IDLE_WARN, CPU_IDLE_REFUSE = 35, 15  # env check: refusal needs both samples below CPU_IDLE_REFUSE
CPU_RESAMPLE_SECONDS = 5  # gap before the second sample that confirms a low reading
PARALLEL_MIN_CPU_IDLE = 50
DISK_WARN_GB, DISK_REFUSE_GB = 40, 15
UPTIME_WARN_DAYS = 14
MEMORY_WARN_GB = 8
PROCESS_CPU_PERCENT = 100
DAEMONS_WARN_ABOVE = 3
TOP_PROCESSES = 5

# id, measure, warns, refuses (None: never refuses), limits text.
CHECKS = (
    ("load", lambda m: m["load"], lambda v: v >= LOAD_WARN, None, "load1/ncpu: warn >= %.2f, informational" % LOAD_WARN),
    ("cpu", lambda m: m["cpuIdle"], lambda v: v < CPU_IDLE_WARN, lambda v: v < CPU_IDLE_REFUSE,
        "CPU idle %%: warn < %d, refuse < %d in two samples" % (CPU_IDLE_WARN, CPU_IDLE_REFUSE)),
    ("disk", lambda m: m["diskFreeGb"], lambda v: v < DISK_WARN_GB, lambda v: v < DISK_REFUSE_GB,
        "free GB: warn < %d, refuse < %d" % (DISK_WARN_GB, DISK_REFUSE_GB)),
    ("uptime", lambda m: m["uptimeDays"], lambda v: v >= UPTIME_WARN_DAYS, None, "days up: warn >= %d" % UPTIME_WARN_DAYS),
    ("memory", lambda m: m["memAvailableGb"], lambda v: v < MEMORY_WARN_GB, None, "available GB: warn < %d" % MEMORY_WARN_GB),
    ("procs", lambda m: len(m["procs"]), lambda v: v > 0, None, "foreign processes at >= %d%% CPU: warn on any" % PROCESS_CPU_PERCENT),
    ("daemons", lambda m: m["daemons"], lambda v: v > DAEMONS_WARN_ABOVE, None, "Gradle/Kotlin daemons: warn > %d" % DAEMONS_WARN_ABOVE),
    ("foreign_device", lambda m: len(m["foreignDevices"]), lambda v: v > 0, None, "other emulators or simulators: warn on any"),
)


def _output(argv):
    try:
        return subprocess.run(argv, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, universal_newlines=True,
            timeout=30).stdout
    except (OSError, subprocess.SubprocessError):
        return ""


def cpu_idle(cfg, sample=0):
    """Idle CPU percent over the last second, or None when it cannot be read. With E2E_FAKE_HOST_METRICS set,
    `cpuIdle` is a number or a list indexed by `sample`; nothing is probed."""
    fake = manifest_mod.fake_metrics(cfg)
    if fake:
        value = fake["cpuIdle"]
        return float(value[min(sample, len(value) - 1)] if isinstance(value, list) else value)
    found = re.findall(r"CPU usage:.*?([\d.]+)% idle", _output(["top", "-l", "2", "-n", "0", "-s", "1"]))
    return float(found[-1]) if found else None  # the second report covers the interval; the first is since boot


def _process_table():
    """[(pid, cpu percent, command)] for every process."""
    rows = []
    for line in _output(["ps", "-Ao", "pid=,pcpu=,command="]).splitlines():
        parts = line.split(None, 2)
        if len(parts) == 3 and parts[0].isdigit():
            try:
                rows.append((int(parts[0]), float(parts[1]), parts[2]))
            except ValueError:
                continue
    return rows


def _memory_available_gb():
    out = _output(["vm_stat"])
    try:
        page = int(re.search(r"page size of (\d+) bytes", out).group(1))
        pages = sum(int(re.search(r"%s:\s+(\d+)" % key, out).group(1))
            for key in ("Pages free", "Pages inactive", "Pages purgeable"))
    except (AttributeError, ValueError):
        return None
    return round(pages * page / 2 ** 30, 1)


def _foreign_devices(cfg, platforms, table):
    """Emulators and simulators running that are not the pinned device of a platform of this run."""
    found = []
    avd = toolchain.read_lock(cfg, "android")["ANDROID_AVD_NAME"]
    for _pid, _cpu, command in table:
        match = re.search(r"qemu-system\S*.* -avd (\S+)", command)
        if match and ("android" not in platforms or match.group(1) != avd):
            found.append("emulator %s" % match.group(1))
    name = toolchain.read_lock(cfg, "ios")["IOS_SIMULATOR_NAME"]
    try:
        booted = json.loads(_output(["xcrun", "simctl", "list", "devices", "booted", "-j"]) or "{}")
    except ValueError:
        booted = {}
    for devices in booted.get("devices", {}).values():
        found.extend("simulator %s" % d["name"] for d in devices if "ios" not in platforms or d["name"] != name)
    return found


def measure(cfg, platforms=PLATFORMS, devices=True):
    """The host measures every check needs. With E2E_FAKE_HOST_METRICS set, nothing is probed."""
    host, load = manifest_mod.collect_host(cfg)
    ratio = load[0] / host["ncpu"]
    result = {"loadAvg": load, "ncpu": host["ncpu"], "load": ratio, "diskFreeGb": host["diskFreeGb"],
        "uptimeDays": host["uptimeDays"], "memTotalGb": host["memGb"]}
    idle = cpu_idle(cfg)
    result["cpuSamples"] = [idle]
    if idle is not None and idle < CPU_IDLE_REFUSE:  # confirm with a second sample so that a burst cannot refuse
        if not manifest_mod.fake_metrics(cfg):
            time.sleep(CPU_RESAMPLE_SECONDS)
        result["cpuSamples"].append(cpu_idle(cfg, 1))
    known = [v for v in result["cpuSamples"] if v is not None]
    result["cpuIdle"] = max(known) if known else None
    fake = manifest_mod.fake_metrics(cfg)
    if fake:
        result.update({key: fake[key] for key in ("memAvailableGb", "procs", "daemons", "foreignDevices")})
        if not devices:
            result["foreignDevices"] = []
        return result
    table = _process_table()
    result["memAvailableGb"] = _memory_available_gb()
    result["procs"] = top_processes(table)
    result["daemons"] = sum(1 for _p, _c, cmd in table if "GradleDaemon" in cmd or "KotlinCompileDaemon" in cmd)
    result["foreignDevices"] = _foreign_devices(cfg, platforms, table) if devices else []
    return result


def top_processes(table):
    ours = {os.getpid(), os.getppid()}
    busy = sorted((r for r in table if r[1] >= PROCESS_CPU_PERCENT and r[0] not in ours), key=lambda r: -r[1])
    return [{"pid": pid, "cpuPct": cpu, "command": command[:120]} for pid, cpu, command in busy[:TOP_PROCESSES]]


def evaluate(measures, evidence, overrides):
    """One row per check: level ok|warn|refuse. Diagnose never refuses; an override downgrades a refusal."""
    rows = []
    for ident, measure_of, warns, refuses, limits in CHECKS:
        value = measure_of(measures)
        level, overridden = "ok", False
        if value is None:
            level = "warn"
        elif refuses and refuses(value):
            level = "refuse"
            if ident in overrides:
                level, overridden = "warn", True
            elif not evidence:
                level = "warn"
        elif warns(value):
            level = "warn"
        rows.append({"id": ident, "value": value if not isinstance(value, float) else round(value, 2), "level": level,
            "overridden": overridden, "limits": limits})
    return rows


def describe(rows, measures):
    lines = ["%s = %s (%s)" % (r["id"], r["value"], r["limits"]) for r in rows if r["level"] == "refuse"]
    lines += ["busy process: pid %d %.0f%% %s" % (p["pid"], p["cpuPct"], p["command"]) for p in measures["procs"]]
    return "\n".join(lines)


def check(run):
    """Hook of the runner: records the check in the manifest and raises EnvironmentRefused to refuse."""
    platforms = (run.platform,) if run.opts.child_of is None else PLATFORMS
    measures = measure(run.cfg, platforms, devices=run.opts.child_of is None)
    rows = evaluate(measures, run.evidence, run.cfg.env_override)
    run.manifest.update(envCheck=rows)
    run.manifest.data["competingProcesses"]["start"] = measures["procs"]
    run.env_checked = True
    for row in rows:
        if row["level"] != "ok":
            note = "OVERRIDDEN by E2E_ENV_OVERRIDE" if row["overridden"] else row["level"].upper()
            run.log.say("ENV   %s %s=%s (%s)" % (note, row["id"], row["value"], row["limits"]))
    refused = [r for r in rows if r["level"] == "refuse"]
    run.log.say("ENV   %d checks, %d not ok, %d refused" % (len(rows), sum(1 for r in rows if r["level"] != "ok"), len(refused)))
    if refused:
        text = "the environment is refused before any build or boot; fix it or, knowingly, set E2E_ENV_OVERRIDE=%s\n%s" \
            % (",".join(r["id"] for r in refused), describe(rows, measures))
        if run.opts.dry_run:
            run.log.say("ENV   WOULD REFUSE: %s" % text)
        else:
            raise EnvironmentRefused(text)


def main(cfg, args):
    """`e2e.py env-check`: the evidence thresholds against this host; exit 3 when evidence would be refused."""
    from . import parallel
    measures = measure(cfg)
    rows = evaluate(measures, True, cfg.env_override)
    host, _load = manifest_mod.collect_host(cfg)
    decision, reason = parallel.decide(cfg, host, measures["cpuIdle"])
    if args.json:
        print(json.dumps({"checks": rows, "measures": measures, "parallel": {"decision": decision, "reason": reason}},
            indent=2, sort_keys=True))
    else:
        for row in rows:
            suffix = " (overridden)" if row["overridden"] else ""
            print("%-15s %-9s %-7s %s%s" % (row["id"], row["value"], row["level"], row["limits"], suffix))
        for proc in measures["procs"]:
            print("  busy: pid %d %.0f%% %s" % (proc["pid"], proc["cpuPct"], proc["command"]))
        for device in measures["foreignDevices"]:
            print("  foreign: %s" % device)
        print("parallel: %s (%s)" % (decision, reason))
    return 3 if any(r["level"] == "refuse" for r in rows) else 0
