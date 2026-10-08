#!/usr/bin/env python3
"""Where the time of an E2E run went: per scenario, per primitive, and run against run. Reads the run directories only.

  e2e-profile.py                         the latest run of each platform
  e2e-profile.py --platform ios          the latest iOS run
  e2e-profile.py --run <runId|dir> ...   those runs
  e2e-profile.py --last 5                the primitives of the last 5 runs of each platform, together (more samples for p95)
  e2e-profile.py --compare 1             the latest run of each platform against the 1 run before it
  e2e-profile.py --compare 3             ... against the 3 runs before it (the table lists all four; the deltas use the previous)

Reads <state root>/runs/<runId>/manifest.json and scenarios/<id>/attempt-<n>/result.json; the state root is
--state-root, else E2E_STATE_ROOT, else build/e2e of this repository. A single-run profile is also written to
<state root>/profiles/<runId>.json (--no-write: do not).

Terms (milliseconds in the JSON):
  wall        what the harness spent on the attempt: health, reset-app, runner, crash probe, journal, classification
  runner      the run-scenario invocation alone (xcodebuild or am instrument): wall minus harness overhead
  in-process  the scenario itself, from result.json startedAt..finishedAt
  invocation  runner minus in-process: what one runner invocation costs before and after the scenario (the number that
              decides whether scenarios should be batched; the consultation threshold is 40 s on iOS)
  harness     wall minus runner
  prepare     prepareBackendMs of result.json: what the scenario spent preparing the backend before its first step
  launch      launchMs of result.json: what it spent starting the app
Both are left out (`-`, never 0) for a result.json that does not carry them.
Runs written before the manifest listed its attempts have no runner time: only wall (of the last attempt, from the
manifest results) and in-process are known for them, and `invocation` shows `-`.
A primitive's time is that of its steps that ran; OnPlatform, Group and IfVisible are not counted because
their steps are listed on their own. Percentiles are nearest-rank: p95 of 20 samples is the 19th smallest.

Exit codes: 0 done; 1 no runs found; 2 bad usage.
"""

import argparse
import datetime
import json
import math
import os
import re
import sys

RUN_SCHEMA = "tuindice-e2e-run/1"
CONTAINERS = frozenset(("OnPlatform", "Group", "IfVisible"))
INVOCATION_THRESHOLD_MS = 40000
ATTEMPT_DIR = re.compile(r"^attempt-(\d+)(?:-r(\d+))?$")
STAMP = re.compile(r"^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})(?:\.(\d+))?Z$")


# -- reading --------------------------------------------------------------------------------------

def read_json(path):
    try:
        with open(path) as handle:
            data = json.load(handle)
        return data if isinstance(data, dict) else None
    except (OSError, ValueError):
        return None


def parse_time(text):
    match = STAMP.match(text or "")
    if not match:
        return None
    moment = datetime.datetime.strptime(match.group(1), "%Y-%m-%dT%H:%M:%S")
    return moment + datetime.timedelta(microseconds=int((match.group(2) or "0")[:6].ljust(6, "0")))


def percentile(values, percent):
    """Nearest rank: the smallest sample with at least `percent` % of the samples at or below it."""
    ordered = sorted(values)
    if not ordered:
        return None
    return ordered[max(0, int(math.ceil(percent / 100.0 * len(ordered))) - 1)]


def children(path):
    try:
        return sorted(os.listdir(path))
    except OSError:
        return []


class Run:
    def __init__(self, path, manifest):
        self.path, self.manifest = path, manifest
        self.id = manifest.get("runId") or os.path.basename(path)
        self.platform = manifest.get("platform") or "unknown"


def find_runs(state_root):
    runs = []
    base = os.path.join(state_root, "runs")
    for name in children(base):
        manifest = read_json(os.path.join(base, name, "manifest.json"))
        if manifest and manifest.get("schema") == RUN_SCHEMA:
            runs.append(Run(os.path.join(base, name), manifest))
    return sorted(runs, key=lambda r: r.id)


def locate(state_root, wanted):
    """A run id or a run directory."""
    path = wanted if os.path.isdir(wanted) else os.path.join(state_root, "runs", wanted)
    manifest = read_json(os.path.join(path, "manifest.json"))
    if not manifest:
        raise SystemExit(usage("%s is not a run directory (no manifest.json)" % wanted))
    return Run(path, manifest)


def usage(message):
    sys.stderr.write("e2e-profile: %s\n" % message)
    return 2


# -- profiling one run ----------------------------------------------------------------------------

def recorded_timings(manifest):
    """{(scenario, attempt directory): (wall ms, runner ms)} from manifest.attempts. A `--repeat` run repeats (scenario, n) in
    every repetition, so the directory (`attempt-1-r2`) is the key; a manifest without `attemptDir` is a single repetition."""
    return {(a.get("scenario"), a.get("attemptDir") or "attempt-%s" % a.get("n")): (a.get("durationMs"), a.get("runnerDurationMs"))
        for a in manifest.get("attempts") or [] if isinstance(a, dict)}


def number_or_none(value):
    return value if isinstance(value, (int, float)) and not isinstance(value, bool) else None


def attempt_rows(run):
    recorded = recorded_timings(run.manifest)
    last_wall = {r.get("id"): r.get("durationMs") for r in run.manifest.get("results") or []
        if isinstance(r, dict) and r.get("status") in ("passed", "failed") and r.get("durationMs")}
    rows = []
    base = os.path.join(run.path, "scenarios")
    for scenario in children(base):
        attempts = sorted(n for n in children(os.path.join(base, scenario)) if n.startswith("attempt-"))
        for name in attempts:
            named = ATTEMPT_DIR.match(name)
            number, repetition = (int(named.group(1)), int(named.group(2) or 1)) if named else (0, 1)
            directory = os.path.join(base, scenario, name)
            result = read_json(os.path.join(directory, "result.json")) or {}
            klass = (read_json(os.path.join(directory, "classification.json")) or {}).get("class")
            wall, runner = recorded.get((scenario, name), (None, None))
            if wall is None and name == attempts[-1]:
                wall = last_wall.get(scenario)  # only the last attempt's wall time is in an older manifest
            start, end = parse_time(result.get("startedAt")), parse_time(result.get("finishedAt"))
            inside = int((end - start).total_seconds() * 1000) if start and end else None
            rows.append({
                "scenario": scenario, "attempt": number, "repetition": repetition, "outcome": result.get("outcome") or "no result", "failureClass": klass,
                "wallMs": wall, "runnerMs": runner, "inProcessMs": inside,
                "invocationMs": runner - inside if runner is not None and inside is not None else None,
                "harnessMs": wall - runner if wall is not None and runner is not None else None,
                "prepareBackendMs": number_or_none(result.get("prepareBackendMs")), "launchMs": number_or_none(result.get("launchMs")),
                "steps": [s for s in result.get("steps") or [] if isinstance(s, dict)],
            })
    return rows


def primitive_stats(rows):
    samples = {}
    for row in rows:
        for step in row["steps"]:
            primitive, millis = step.get("primitive"), step.get("durationMs")
            if primitive and primitive not in CONTAINERS and step.get("outcome") != "skipped" and isinstance(millis, (int, float)):
                samples.setdefault(primitive, []).append(millis)
    return {name: {"count": len(v), "p50Ms": percentile(v, 50), "p95Ms": percentile(v, 95), "maxMs": max(v), "totalMs": sum(v)}
        for name, v in sorted(samples.items())}


def overhead_summary(rows):
    values = [r["invocationMs"] for r in rows if r["invocationMs"] is not None]
    if not values:
        return None
    return {"attempts": len(values), "p50Ms": percentile(values, 50), "maxMs": max(values),
        "overThreshold": percentile(values, 50) > INVOCATION_THRESHOLD_MS}


def start_summary(rows, key):
    values = [r[key] for r in rows if r[key] is not None]
    return {"attempts": len(values), "p50Ms": percentile(values, 50), "maxMs": max(values)} if values else None


def profile_run(run):
    rows = attempt_rows(run)
    manifest = run.manifest
    failures = {}
    for result in manifest.get("results") or []:
        for klass in result.get("failureClasses") or []:
            failures[klass] = failures.get(klass, 0) + 1
    return {
        "runId": run.id, "platform": run.platform, "mode": manifest.get("mode"), "outcome": manifest.get("outcome"),
        "exitCode": manifest.get("exitCode"), "durationSeconds": manifest.get("durationSeconds"),
        "parallel": (manifest.get("parallel") or {}).get("decision"), "maxLoad1m": (manifest.get("load") or {}).get("max1m"),
        "phases": {p.get("name"): p.get("durationSeconds") for p in manifest.get("phases") or []},
        "scenarios": manifest.get("scenarios") or {}, "failuresByClass": failures,
        "attempts": [{k: v for k, v in row.items() if k != "steps"} for row in rows],
        "primitives": primitive_stats(rows), "invocationOverhead": overhead_summary(rows),
        "prepareOverhead": start_summary(rows, "prepareBackendMs"), "launchOverhead": start_summary(rows, "launchMs"),
    }


# -- text -----------------------------------------------------------------------------------------

def seconds(millis):
    return "-" if millis is None else "%.1fs" % (millis / 1000.0)


def number(value, template="%.1f"):
    return "-" if value is None else template % value


def table(headers, rows):
    widths = [max(len(str(x)) for x in column) for column in zip(headers, *rows)] if rows else [len(h) for h in headers]
    lines = ["  ".join(str(cell).ljust(width) if index == 0 else str(cell).rjust(width)
        for index, (cell, width) in enumerate(zip(line, widths))) for line in [headers] + rows]
    return ["  " + line.rstrip() for line in lines]


def render_profile(data):
    out = ["Run %s: %s %s, outcome %s (exit %s), %s s, %s, load1 max %s" % (
        data["runId"], data["platform"], data["mode"], data["outcome"], data["exitCode"], data["durationSeconds"],
        data["parallel"] or "sequential", number(data["maxLoad1m"], "%.2f")),
        "  phases: " + (", ".join("%s %ss" % (k, v) for k, v in data["phases"].items()) or "-"),
        "", "Scenarios (one row per attempt)"]
    timed = bool(data.get("prepareOverhead") or data.get("launchOverhead"))
    out += table(["scenario", "att", "outcome", "wall", "runner", "in-process", "invocation", "harness"] + (["prepare", "launch"] if timed else []),
        [[a["scenario"], "%d%s" % (a["attempt"], "-r%d" % a["repetition"] if a["repetition"] > 1 else ""), a["failureClass"] or a["outcome"], seconds(a["wallMs"]), seconds(a["runnerMs"]),
            seconds(a["inProcessMs"]), seconds(a["invocationMs"]), seconds(a["harnessMs"])]
            + ([seconds(a.get("prepareBackendMs")), seconds(a.get("launchMs"))] if timed else []) for a in data["attempts"]])
    for label, key in (("backend preparation", "prepareOverhead"), ("app launch", "launchOverhead")):
        if data.get(key):
            out.append("  %s overhead over %d attempt(s): p50 %s, max %s" % (label, data[key]["attempts"], seconds(data[key]["p50Ms"]),
                seconds(data[key]["maxMs"])))
    overhead = data["invocationOverhead"]
    if overhead:
        out.append("  invocation overhead over %d attempt(s): p50 %s, max %s%s" % (overhead["attempts"], seconds(overhead["p50Ms"]),
            seconds(overhead["maxMs"]), "  <- above the 40 s threshold: batching is the next question, ask before doing it"
                if overhead["overThreshold"] else ""))
    else:
        out.append("  invocation overhead: not recorded for this run (its manifest has no attempts list)")
    out += ["", "Primitives (steps that ran, in ms)"] + primitive_table(data["primitives"])
    return out


def primitive_table(stats):
    return table(["primitive", "count", "p50", "p95", "max", "total"],
        [[name, s["count"], s["p50Ms"], s["p95Ms"], s["maxMs"], s["totalMs"]] for name, s in stats.items()]) or ["  (none)"]


# -- comparing ------------------------------------------------------------------------------------

def last_in_process(data):
    """{scenario: in-process ms of its last attempt}"""
    return {a["scenario"]: a["inProcessMs"] for a in data["attempts"] if a["inProcessMs"] is not None}


def delta(new, old, template="%+.1f"):
    return "-" if new is None or old is None else template % (new - old)


def compare_platform(runs, count):
    """The newest run of one platform against the `count` before it."""
    chosen = list(reversed(runs[-(count + 1):]))
    profiles = [profile_run(r) for r in chosen]
    latest, previous = profiles[0], profiles[1] if len(profiles) > 1 else None
    result = {"platform": latest["platform"], "runs": profiles, "enough": previous is not None}
    if previous:
        old_times, new_times = last_in_process(previous), last_in_process(latest)
        moved = sorted(((new_times[s] - old_times[s], s) for s in new_times if s in old_times), key=lambda x: -abs(x[0]))
        primitives = sorted(((latest["primitives"][p]["p50Ms"] - previous["primitives"][p]["p50Ms"], p)
            for p in latest["primitives"] if p in previous["primitives"]), key=lambda x: -abs(x[0]))
        result.update(durationDeltaSeconds=(latest["durationSeconds"] or 0) - (previous["durationSeconds"] or 0),
            sameMode=latest["mode"] == previous["mode"],
            scenarioDeltas=[{"scenario": s, "deltaMs": d, "beforeMs": old_times[s], "afterMs": new_times[s]} for d, s in moved[:5] if d],
            primitiveP50Deltas=[{"primitive": p, "deltaMs": d} for d, p in primitives[:5] if d])
    return result


def render_compare(data):
    runs = data["runs"]
    out = ["Platform %s: latest run against %d before it" % (data["platform"], len(runs) - 1)]
    out += table(["run", "mode", "parallel", "duration", "load1 max", "executed", "failed", "failures by class"],
        [[r["runId"], r["mode"], r["parallel"] or "sequential", "%ss" % r["durationSeconds"], number(r["maxLoad1m"], "%.2f"),
            r["scenarios"].get("executed", "-"), r["scenarios"].get("failed", "-"),
            ", ".join("%s=%d" % kv for kv in sorted(r["failuresByClass"].items())) or "-"] for r in runs])
    if not data["enough"]:
        out.append("  only one %s run exists: there is nothing to compare it with" % data["platform"])
        return out
    if not data["sameMode"]:
        out.append("  NOTE the modes differ (%s against %s): the durations are not comparable" % (runs[0]["mode"], runs[1]["mode"]))
    out.append("  duration %+d s against the previous run" % data["durationDeltaSeconds"])
    if data["scenarioDeltas"]:
        out += ["", "  Scenarios whose in-process time moved most (previous -> latest)"]
        out += table(["scenario", "before", "after", "delta"], [[d["scenario"], seconds(d["beforeMs"]), seconds(d["afterMs"]),
            "%+.1fs" % (d["deltaMs"] / 1000.0)] for d in data["scenarioDeltas"]])
    if data["primitiveP50Deltas"]:
        out += ["", "  Primitives whose p50 moved most (ms)"]
        out += table(["primitive", "delta"], [[d["primitive"], "%+d" % d["deltaMs"]] for d in data["primitiveP50Deltas"]])
    return out


# -- command line ---------------------------------------------------------------------------------

def build_parser():
    parser = argparse.ArgumentParser(prog="e2e-profile.py", description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--state-root", help="the harness state root (default: E2E_STATE_ROOT, else build/e2e of this repository)")
    parser.add_argument("--platform", choices=("android", "ios"), help="only this platform")
    parser.add_argument("--run", action="append", default=[], metavar="RUN", help="a run id or run directory (repeatable)")
    parser.add_argument("--last", type=int, metavar="N", help="together, the last N runs of each platform")
    parser.add_argument("--compare", nargs="?", type=int, const=1, metavar="N",
        help="the latest run of each platform against the N runs before it (default 1)")
    parser.add_argument("--json", action="store_true", help="print JSON instead of tables")
    parser.add_argument("--no-write", action="store_true", help="do not write profiles/<runId>.json")
    return parser


def write_profile(state_root, data):
    try:
        directory = os.path.join(state_root, "profiles")
        os.makedirs(directory, exist_ok=True)
        with open(os.path.join(directory, data["runId"] + ".json"), "w") as handle:
            json.dump(data, handle, indent=2)
        return True
    except OSError as error:
        sys.stderr.write("e2e-profile: the profile was not written: %s\n" % error)
        return False


def main(argv=None, env=None):
    env = dict(os.environ if env is None else env)
    args = build_parser().parse_args(argv)
    if args.compare is not None and args.compare < 1 or args.last is not None and args.last < 1:
        return usage("--compare and --last take a positive number")
    if sum(1 for option in (args.compare is not None, args.last is not None, bool(args.run)) if option) > 1:
        return usage("--compare, --last and --run are different questions: ask one at a time")
    repo = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    state_root = args.state_root or env.get("E2E_STATE_ROOT") or os.path.join(repo, "build", "e2e")
    every = [r for r in find_runs(state_root) if not args.platform or r.platform == args.platform]
    platforms = sorted(set(r.platform for r in every))
    if args.run:
        chosen = [locate(state_root, wanted) for wanted in args.run]
    elif not every:
        sys.stderr.write("e2e-profile: no runs under %s\n" % os.path.join(state_root, "runs"))
        return 1
    elif args.compare is not None:
        compared = [compare_platform([r for r in every if r.platform == p], args.compare) for p in platforms]
        print(json.dumps(compared, indent=2) if args.json else "\n\n".join("\n".join(render_compare(c)) for c in compared))
        return 0
    elif args.last:
        chosen = [r for p in platforms for r in [x for x in every if x.platform == p][-args.last:]]
    else:
        chosen = [[r for r in every if r.platform == p][-1] for p in platforms]
    profiles = [profile_run(r) for r in chosen]
    if args.last:
        rows = [row for r in chosen for row in attempt_rows(r)]
        merged = {"runs": [p["runId"] for p in profiles], "primitives": primitive_stats(rows), "invocationOverhead": overhead_summary(rows)}
        if args.json:
            print(json.dumps(merged, indent=2))
        else:
            print("Primitives over %d run(s): %s\n" % (len(profiles), ", ".join(merged["runs"])))
            print("\n".join(primitive_table(merged["primitives"])))
        return 0
    if args.json:
        print(json.dumps(profiles if len(profiles) > 1 else profiles[0], indent=2))
    else:
        print("\n\n".join("\n".join(render_profile(p)) for p in profiles))
    if not args.no_write:
        for data in profiles:
            write_profile(state_root, data)
    return 0


if __name__ == "__main__":
    sys.exit(main())
