"""`--platform all`: the parallel-or-sequential decision and one independent child process per platform."""

import copy
import datetime
import json
import os
import signal
import subprocess
import sys
import time

from . import envcheck, runner
from .config import PLATFORMS
from .gitstate import GitState
from .manifest import collect_host, make_run_dir
from .proc import Interrupted, kill_group
from .report import Log

# Provisional; revise from `e2e-profile.py --compare`.
PARALLEL_MIN_CPUS = 8
PARALLEL_MAX_LOAD_RATIO = envcheck.LOAD_WARN
PARALLEL_MIN_MEMORY_GB = 32
CHILD_FINALISE_SECONDS = 60


def decide(cfg, host, load):
    """("parallel"|"sequential", reason) for the platforms of one invocation."""
    if cfg.parallel == "never":
        return "sequential", "E2E_PARALLEL=never"
    if cfg.parallel == "always":
        return "parallel", "E2E_PARALLEL=always overrides the policy"
    ratio, memory = load[0] / host["ncpu"], host["memGb"]
    blockers = []
    if host["ncpu"] < PARALLEL_MIN_CPUS:
        blockers.append("ncpu=%d < %d" % (host["ncpu"], PARALLEL_MIN_CPUS))
    if ratio >= PARALLEL_MAX_LOAD_RATIO:
        blockers.append("load1/ncpu=%.2f >= %.2f" % (ratio, PARALLEL_MAX_LOAD_RATIO))
    if memory is None or memory < PARALLEL_MIN_MEMORY_GB:
        blockers.append("memory=%s GB < %d GB" % (memory, PARALLEL_MIN_MEMORY_GB))
    if blockers:
        return "sequential", "; ".join(blockers)
    return "parallel", "ncpu=%d, load1/ncpu=%.2f, memory=%d GB" % (host["ncpu"], ratio, memory)


def sequence(platforms, failed):
    """Sequential order: the platform with failed attempts in its ledger first, then Android, then iOS."""
    return sorted(platforms, key=lambda p: (0 if p in failed else 1, PLATFORMS.index(p)))


def _child_argv(args, platform, parent, decision, reason, stop_device):
    argv = [sys.executable, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "e2e.py"),
        "run", "--platform", platform, "--mode", args.mode, "--child-of", parent, "--parallel-decision", decision,
        "--parallel-reason", reason]
    for flag, value in (("--scenario", args.scenario), ("--tag", args.tag), ("--changed-since", args.changed_since)):
        if value:
            argv += [flag, value]
    argv += ["--repeat", str(args.repeat)]
    argv += [flag for flag, on in (("--survey", args.survey), ("--trace", args.trace), ("--force", args.force),
        ("--stop-device", stop_device)) if on]
    return argv


def _stop_children(children):
    for child in children.values():
        if child.poll() is None:
            child.send_signal(signal.SIGTERM)
    deadline = time.monotonic() + CHILD_FINALISE_SECONDS
    for child in children.values():
        while child.poll() is None and time.monotonic() < deadline:
            time.sleep(0.2)
        if child.poll() is None:
            kill_group(child, 10)


def _wait(children):
    """Waits for every child, whatever any of them returns; forwards an interruption and re-raises it."""
    try:
        while any(child.poll() is None for child in children.values()):
            time.sleep(0.2)
    except Interrupted:
        _stop_children(children)
        raise
    return {p: (child.returncode if child.returncode >= 0 else 128 - child.returncode) for p, child in children.items()}


def _write_summary(cfg, run_dir, run_id, args, decision, reason, order, codes):
    children = {}
    runs = os.path.join(str(cfg.state_root), "runs")
    for name in sorted(os.listdir(runs)):
        try:
            manifest = json.load(open(os.path.join(runs, name, "manifest.json")))
        except (OSError, ValueError):
            continue
        if manifest.get("parentRunId") == run_id:
            children[manifest["platform"]] = {"runId": name, "outcome": manifest["outcome"], "exitCode": manifest["exitCode"]}
    summary = {"runId": run_id, "mode": args.mode, "decision": decision, "reason": reason, "order": order,
        "exitCodes": codes, "children": children, "finishedAt": datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")}
    with open(os.path.join(run_dir, "summary.json"), "w") as handle:
        json.dump(summary, handle, indent=2)


def run_all(cfg, platforms, args, options, failed, combine):
    """Runs the platforms; one failing never cancels the other. Returns the combined exit code."""
    host, load = collect_host(cfg)
    decision, reason = decide(cfg, host, load)
    order = sequence(platforms, failed)
    log = Log("all")
    log.say("%s: %s; %s" % (decision, reason, "both at once" if decision == "parallel" else "order " + ", ".join(order)))
    if options.dry_run:
        options = copy.copy(options)
        options.parallel = (decision, reason)
        options.child_of = "dry-run"  # a platform of an `all` run does not see its sibling's device as foreign
        codes = {p: runner.PlatformRun(cfg, p, options).execute() for p in order}
    else:
        sha7 = GitState(cfg.root).sha7
        run_id, run_dir = make_run_dir(cfg.state_root, "all", args.mode, sha7)
        codes = {}
        if decision == "parallel":
            children = {p: subprocess.Popen(_child_argv(args, p, run_id, decision, reason, False), cwd=str(cfg.root),
                env=cfg.env, stdin=subprocess.DEVNULL, start_new_session=True) for p in order}
            codes = _wait(children)
        else:
            for platform in order:
                child = subprocess.Popen(_child_argv(args, platform, run_id, decision, reason, True), cwd=str(cfg.root),
                    env=cfg.env, stdin=subprocess.DEVNULL, start_new_session=True)
                codes.update(_wait({platform: child}))
        _write_summary(cfg, run_dir, run_id, args, decision, reason, order, codes)
    print("[e2e] exit codes: %s" % " ".join("%s=%d" % (p, codes[p]) for p in platforms))
    return max(codes.values()) if any(code >= 128 for code in codes.values()) else combine(list(codes.values()))
