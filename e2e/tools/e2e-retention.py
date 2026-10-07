#!/usr/bin/env python3
"""Retention of the E2E harness artifacts (build/e2e): what a run leaves behind, trimmed to fixed limits.

By default this is a SIMULATION: it prints what it would delete and how much that frees, and changes nothing.

  e2e-retention.py                          simulate the policy on the state root
  e2e-retention.py --apply                  carry the policy out (the harness runs this when a run ends)
  e2e-retention.py --purge-legacy           also list the leftovers of the Maestro harness (still a simulation)
  e2e-retention.py --purge-legacy --yes     delete those leftovers and carry the policy out (the owner's command)

The policy (sizes are sums of file sizes; symbolic links are never followed):
  * a passing attempt keeps result.json, classification.json and the last 1 MB of runner.log;
  * a failing attempt keeps everything, up to 200 MB: past that attempt.xcresult goes first, then videos and
    screenshots, then the biggest remaining files; logs and the verdict files always stay;
  * a platform keeps its last 10 runs; a run that the ledger of a published fingerprint (or the one given with
    --fingerprint) names, or a run still going, is never deleted;
  * the managed content (runs, ledgers, profiles, certification manifests) is capped at 5 GB: the heavy files of the
    oldest failing attempts go first, then the oldest runs;
  * the last 20 ledgers of a platform stay; one with publications, in use or unreadable is never touched.

Never touched: anything outside the state root and the legacy locations below, the run in progress (--current-run,
or a manifest that says it is running), and every path that is not recognised (those are only listed).
Legacy paths: build/e2e/checkpoints, build/e2e/maestro-*.log, build/e2e/certifications/*/*/*/maestro-* and the
tuindice-e2e* entries beside the temp root and in /tmp (the temp root itself is the harness's and stays). They go only
with --purge-legacy --yes, and never while a run is going.

Exit codes: 0 done (or nothing to do); 1 an action failed; 2 bad usage.
"""

import argparse
import fcntl
import glob
import json
import os
import shutil
import sys
import time

MB = 1024 * 1024
GB = 1024 * MB

RUN_SCHEMA = "tuindice-e2e-run/1"
KEEP_RUNS = 10
KEEP_LEDGERS = 20
FAILED_ATTEMPT_CAP_MB = 200
RUNNER_LOG_TAIL = 1 * MB
# A manifest that says "running" and was written less than this long ago belongs to a live run.
IN_PROGRESS_SECONDS = 12 * 3600

GREEN_KEEP = ("result.json", "classification.json")
RUNNER_LOG = "runner.log"
# What a failing attempt keeps whatever its size: the verdict, the logs, the request journal.
ESSENTIAL = frozenset(("result.json", "classification.json", "runner.log", "crash.txt", "driver.log",
    "wiremock-requests.json", "app.log", "logcat.txt"))
KNOWN_TOP = frozenset(("runs", "ledger", "certifications", "profiles", "retention.lock"))
LEGACY_TOP_DIRS = ("checkpoints",)
LEGACY_TOP_GLOBS = ("maestro-*.log",)
LEGACY_CERTIFICATION_GLOB = os.path.join("certifications", "*", "*", "*", "maestro-*")
LEGACY_TMP_GLOB = "tuindice-e2e*"
VIDEO_SUFFIXES = (".mp4", ".mov", ".webm")
IMAGE_SUFFIXES = (".png", ".jpg", ".jpeg", ".heic")
SLIM_KINDS = ("drop-green", "cut-log", "trim-failed")


# -- sizes and files ------------------------------------------------------------------------------

def tree_size(path):
    """Bytes in the files under `path` (a file counts as itself); symbolic links are counted, not followed."""
    try:
        info = os.lstat(path)
    except OSError:
        return 0
    if not os.path.isdir(path) or os.path.islink(path):
        return info.st_size
    total, stack = 0, [path]
    while stack:
        try:
            entries = list(os.scandir(stack.pop()))
        except OSError:
            continue
        for entry in entries:
            try:
                if entry.is_dir(follow_symlinks=False):
                    stack.append(entry.path)
                else:
                    total += entry.stat(follow_symlinks=False).st_size
            except OSError:
                continue
    return total


def read_json(path):
    try:
        with open(path) as handle:
            data = json.load(handle)
        return data if isinstance(data, dict) else None
    except (OSError, ValueError):
        return None


def human(count):
    for unit, size in (("GB", GB), ("MB", MB), ("KB", 1024)):
        if abs(count) >= size:
            return "%.1f %s" % (count / float(size), unit)
    return "%d B" % count


def children(path):
    try:
        return sorted(os.listdir(path))
    except OSError:
        return []


def inside(path, root):
    real = os.path.realpath(path)
    return real != root and real.startswith(root.rstrip(os.sep) + os.sep)


# -- the plan -------------------------------------------------------------------------------------

class Action:
    """One thing the policy does: delete a path, or cut a log to its last bytes. `root` is the only place it may touch."""

    def __init__(self, kind, path, freed, why, root, keep_bytes=None):
        self.kind, self.path, self.freed, self.why, self.root, self.keep_bytes = kind, path, freed, why, root, keep_bytes
        self.done = False

    @property
    def managed(self):
        return self.kind != "delete-legacy"

    def as_dict(self):
        return {"kind": self.kind, "path": self.path, "freedBytes": self.freed, "why": self.why, "done": self.done}

    def carry_out(self):
        """Raises OSError on anything wrong; never follows a link and never leaves `self.root`."""
        if os.path.islink(self.path):  # the link itself goes; what it points to is never looked at
            if not inside(os.path.join(os.path.dirname(self.path), "x"), self.root):
                raise OSError("%s is outside %s" % (self.path, self.root))
            os.unlink(self.path)
            self.done = True
            return
        if not inside(self.path, self.root):
            raise OSError("%s is outside %s" % (self.path, self.root))
        if self.kind == "cut-log":
            with open(self.path, "rb") as handle:
                handle.seek(-self.keep_bytes, os.SEEK_END)
                tail = handle.read()
            newline = tail.find(b"\n")
            temporary = self.path + ".retention-tmp"
            with open(temporary, "wb") as handle:
                handle.write(tail[newline + 1:] if newline >= 0 else tail)
            os.replace(temporary, self.path)
        elif os.path.isdir(self.path):
            shutil.rmtree(self.path)
        else:
            os.unlink(self.path)
        self.done = True


class Run:
    def __init__(self, name, path, manifest):
        self.name, self.path, self.manifest = name, path, manifest
        self.platform = manifest.get("platform") or "unknown"
        self.size = tree_size(path)
        self.protected = None  # why this run is never deleted, or None
        self.in_progress = False


class Retention:
    def __init__(self, state_root, options):
        self.root = os.path.realpath(state_root)
        self.opt = options
        self.actions, self.unrecognised, self.notes = [], [], []
        self.runs, self.parents = [], []
        self.legacy_bytes = 0
        self.legacy_blocked = None
        self.managed_before = 0
        self.freed_managed = 0

    def path(self, *parts):
        return os.path.join(self.root, *parts)

    def add(self, kind, path, freed, why, root=None, keep_bytes=None):
        action = Action(kind, path, freed, why, root or self.root, keep_bytes)
        self.actions.append(action)
        self.freed_managed += freed if action.managed else 0
        return action

    @property
    def projected(self):
        """The managed content once every managed action is done."""
        return self.managed_before - self.freed_managed

    # -- discovery ----------------------------------------------------------------------------

    def scan_runs(self):
        runs_dir = self.path("runs")
        for name in children(runs_dir):
            path = os.path.join(runs_dir, name)
            if os.path.islink(path) or not os.path.isdir(path):
                self.unrecognised.append((path, tree_size(path)))
                continue
            manifest = read_json(os.path.join(path, "manifest.json"))
            if manifest and manifest.get("schema") == RUN_SCHEMA:
                self.runs.append(Run(name, path, manifest))
            elif manifest is None and os.path.exists(os.path.join(path, "summary.json")):
                self.parents.append((name, path, tree_size(path)))
            else:
                self.unrecognised.append((path, tree_size(path)))
        for run in self.runs:
            run.in_progress = self._in_progress(run)

    def _in_progress(self, run):
        if run.name == self.opt.current_run:
            return True
        if run.manifest.get("outcome") != "running":
            return False
        try:
            return time.time() - os.path.getmtime(os.path.join(run.path, "manifest.json")) < IN_PROGRESS_SECONDS
        except OSError:
            return True

    def scan_ledgers(self):
        """One dict per readable ledger.json; an unreadable ledger is listed as unrecognised and never touched."""
        found = []
        for platform in children(self.path("ledger")):
            base = self.path("ledger", platform)
            for fingerprint in children(base):
                directory = os.path.join(base, fingerprint)
                if not os.path.isdir(directory) or os.path.islink(directory):
                    continue
                data = read_json(os.path.join(directory, "ledger.json"))
                if data is None:
                    self.unrecognised.append((directory, tree_size(directory)))
                    continue
                ids = set(p.get("runId") for p in data.get("publications") or [] if isinstance(p, dict))
                for entry in (data.get("scenarios") or {}).values():
                    ids.update(a.get("runId") for a in entry.get("attempts", []) if isinstance(a, dict))
                found.append({"platform": platform, "fingerprint": fingerprint, "dir": directory,
                    "mtime": os.path.getmtime(os.path.join(directory, "ledger.json")),
                    "published": bool(data.get("publications")), "runIds": ids - {None}})
        return found

    # -- policy: runs -------------------------------------------------------------------------

    def protect_runs(self, ledgers):
        for run in self.runs:
            if run.in_progress:
                run.protected = "the run in progress"
        for ledger in ledgers:
            if ledger["published"]:
                reason = "named by the ledger of published fingerprint %s" % ledger["fingerprint"][:12]
            elif self.opt.fingerprint and ledger["fingerprint"] == self.opt.fingerprint:
                reason = "named by the ledger of fingerprint %s" % ledger["fingerprint"][:12]
            else:
                continue
            for run in self.runs:
                if run.name in ledger["runIds"] and not run.protected:
                    run.protected = reason

    def plan_run_deletion(self):
        survivors = []
        for platform in sorted(set(r.platform for r in self.runs)):
            ordered = sorted((r for r in self.runs if r.platform == platform), key=lambda r: r.name)
            recent = set(r.name for r in ordered[-self.opt.keep_runs:]) if self.opt.keep_runs else set()
            for run in ordered:
                if run.name in recent or run.protected:
                    survivors.append(run)
                else:
                    self.add("delete-run", run.path, run.size, "older than the last %d %s runs" % (self.opt.keep_runs, platform))
        self.runs = survivors

    def plan_parents(self):
        """The directory of an `all` invocation only holds summary.json: it goes when none of its runs is left."""
        for name, path, size in self.parents:
            if not any(r.manifest.get("parentRunId") == name for r in self.runs):
                self.add("delete-run", path, size, "no run of this invocation is kept")

    # -- policy: attempts ---------------------------------------------------------------------

    @staticmethod
    def attempt_failed(path):
        result = read_json(os.path.join(path, "result.json"))
        return os.path.exists(os.path.join(path, "classification.json")) or not result or result.get("outcome") != "passed"

    def attempts(self, run, note_unrecognised=False):
        base = os.path.join(run.path, "scenarios")
        for scenario in children(base):
            directory = os.path.join(base, scenario)
            for name in children(directory):
                path = os.path.join(directory, name)
                if name.startswith("attempt-") and os.path.isdir(path) and not os.path.islink(path):
                    yield path
                elif note_unrecognised:
                    self.unrecognised.append((path, tree_size(path)))

    def plan_attempts(self):
        for run in self.runs:
            if run.in_progress:
                continue
            for attempt in self.attempts(run, note_unrecognised=True):
                if self.attempt_failed(attempt):
                    self._cap_failed(attempt)
                else:
                    self._slim_green(attempt)

    def _slim_green(self, attempt):
        for name in children(attempt):
            path = os.path.join(attempt, name)
            if name in GREEN_KEEP:
                continue
            if name == RUNNER_LOG and os.path.isfile(path) and not os.path.islink(path):
                size = os.path.getsize(path)
                if size > RUNNER_LOG_TAIL:
                    self.add("cut-log", path, size - RUNNER_LOG_TAIL, "a passing attempt keeps the last 1 MB of runner.log",
                        keep_bytes=RUNNER_LOG_TAIL)
                continue
            self.add("drop-green", path, tree_size(path), "a passing attempt keeps result.json, classification.json and the runner.log tail")

    def heavy_files(self, attempt):
        """[(path, bytes)] a failing attempt may lose, in the order it loses them: xcresult, videos, screenshots, the rest."""
        candidates = []
        for name in children(attempt):
            if name in ESSENTIAL:
                continue
            path = os.path.join(attempt, name)
            lower = name.lower()
            group = 0 if lower.endswith(".xcresult") else 1 if lower.endswith(VIDEO_SUFFIXES) \
                else 2 if lower.endswith(IMAGE_SUFFIXES) else 3
            candidates.append((group, -tree_size(path), name, path))
        return [(path, -negative) for _group, negative, _name, path in sorted(candidates)]

    def _cap_failed(self, attempt):
        cap = int(self.opt.failed_cap_mb * MB)
        size = tree_size(attempt)
        for path, weight in self.heavy_files(attempt):
            if size <= cap:
                return
            self.add("trim-failed", path, weight, "the failing attempt is over its %s cap" % human(cap))
            size -= weight
        if size > cap:
            self.notes.append("%s keeps %s, over its cap: only verdict files and logs remain" % (attempt, human(size)))

    # -- policy: total cap --------------------------------------------------------------------

    def managed_size(self):
        total = sum(r.size for r in self.runs) + sum(size for _n, _p, size in self.parents)
        total += tree_size(self.path("profiles"))
        total += sum(tree_size(self.path("ledger", platform)) for platform in children(self.path("ledger")))
        total += sum(os.path.getsize(f) for f in glob.glob(self.path("certifications", "*", "*", "manifest.json")))
        return total

    def plan_cap(self):
        cap = int(self.opt.max_gb * GB)
        if self.projected <= cap:
            return
        candidates = sorted((r for r in self.runs if not r.protected), key=lambda r: r.name)
        trimmed = set(a.path for a in self.actions)
        for run in candidates:  # 1. the heavy files of failing attempts, oldest run first
            for attempt in self.attempts(run):
                if not self.attempt_failed(attempt):
                    continue
                for path, weight in self.heavy_files(attempt):
                    if self.projected <= cap:
                        return
                    if path not in trimmed:
                        self.add("trim-failed", path, weight, "the managed content is over the %s cap" % human(cap))
        for run in candidates:  # 2. whole runs, oldest first; what was planned inside a run is replaced by deleting it
            if self.projected <= cap:
                return
            inner = [a for a in self.actions if a.path.startswith(run.path + os.sep)]
            self.actions = [a for a in self.actions if a not in inner]
            self.freed_managed -= sum(a.freed for a in inner)
            self.runs.remove(run)
            self.add("delete-run", run.path, run.size, "the managed content is over the %s cap" % human(cap))
        if self.projected > cap:
            self.notes.append("the managed content stays at %s, over the %s cap: only protected runs and ledgers remain"
                % (human(self.projected), human(cap)))

    # -- policy: ledgers ----------------------------------------------------------------------

    @staticmethod
    def lock_held(directory):
        lock = os.path.join(directory, "ledger.lock")
        if not os.path.exists(lock):
            return False
        try:
            fd = os.open(lock, os.O_RDWR)
        except OSError:
            return True
        try:
            fcntl.flock(fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
            return False
        except OSError:
            return True
        finally:
            os.close(fd)

    def plan_ledgers(self, ledgers):
        for platform in sorted(set(l["platform"] for l in ledgers)):
            for ledger in sorted((l for l in ledgers if l["platform"] == platform), key=lambda l: -l["mtime"])[KEEP_LEDGERS:]:
                if ledger["published"] or ledger["fingerprint"] == self.opt.fingerprint or self.lock_held(ledger["dir"]):
                    continue
                self.add("delete-ledger", ledger["dir"], tree_size(ledger["dir"]),
                    "older than the last %d %s ledgers and without publications" % (KEEP_LEDGERS, platform))

    # -- legacy and unrecognised --------------------------------------------------------------

    def legacy_paths(self):
        found = [(self.path(name), self.root) for name in LEGACY_TOP_DIRS if os.path.isdir(self.path(name))]
        for pattern in LEGACY_TOP_GLOBS:
            found += [(p, self.root) for p in sorted(glob.glob(self.path(pattern)))]
        found += [(p, self.root) for p in sorted(glob.glob(self.path(LEGACY_CERTIFICATION_GLOB)))]
        tmp_root = os.path.realpath(self.opt.tmp_root)
        for parent in self.opt.legacy_tmp_parents:
            real_parent = os.path.realpath(parent)
            found += [(p, real_parent) for p in sorted(glob.glob(os.path.join(real_parent, LEGACY_TMP_GLOB)))
                if os.path.realpath(p) != tmp_root]
        return [(p, root) for p, root in found if not os.path.islink(p)]

    def plan_legacy(self):
        found = self.legacy_paths()
        self.legacy_bytes = sum(tree_size(p) for p, _root in found)
        if self.opt.purge_legacy and any(r.in_progress for r in self.runs):
            self.legacy_blocked = "a run is in progress: the legacy paths are left alone"
        elif self.opt.purge_legacy:
            for path, root in found:
                self.add("delete-legacy", path, tree_size(path), "leftover of the Maestro harness", root=root)
        legacy = set(p for p, _root in found)
        for name in children(self.root):
            path = self.path(name)
            if name not in KNOWN_TOP and path not in legacy:
                self.unrecognised.append((path, tree_size(path)))

    # -- the whole thing ----------------------------------------------------------------------

    def build(self):
        self.scan_runs()
        ledgers = self.scan_ledgers()
        self.managed_before = self.managed_size()
        self.protect_runs(ledgers)
        self.plan_run_deletion()
        self.plan_attempts()
        self.plan_cap()
        self.plan_parents()
        self.plan_ledgers(ledgers)
        self.plan_legacy()


# -- command line ---------------------------------------------------------------------------------

def build_parser():
    parser = argparse.ArgumentParser(prog="e2e-retention.py", description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--state-root", help="the harness state root (default: E2E_STATE_ROOT, else build/e2e of this repository)")
    parser.add_argument("--tmp-root", help="the harness temp root, never deleted (default: E2E_TMP_ROOT, else $TMPDIR/tuindice-e2e)")
    parser.add_argument("--dry-run", action="store_true", help="simulate: the default, accepted so scripts can say it")
    parser.add_argument("--apply", action="store_true", help="carry the policy out instead of simulating it")
    parser.add_argument("--purge-legacy", action="store_true", help="include the leftovers of the Maestro harness")
    parser.add_argument("--yes", action="store_true", help="with --purge-legacy: really delete them (and carry the policy out)")
    parser.add_argument("--current-run", default="", help="id of the run in progress: never touched")
    parser.add_argument("--fingerprint", default="", help="fingerprint of HEAD: its ledger and the runs it names stay")
    parser.add_argument("--keep-runs", type=int, default=KEEP_RUNS, help="runs kept per platform (default %(default)s)")
    parser.add_argument("--max-gb", type=float, default=None, help="cap of the managed content (default: E2E_ARTIFACTS_MAX_GB, else 5)")
    parser.add_argument("--failed-cap-mb", type=float, default=FAILED_ATTEMPT_CAP_MB, help="cap of a failing attempt (default %(default)s)")
    parser.add_argument("--legacy-tmp-parent", action="append", default=None, metavar="DIR",
        help="where tuindice-e2e* leftovers are looked for (repeatable; default: /tmp and the parent of the temp root)")
    parser.add_argument("--json", action="store_true", help="print one JSON object instead of the report")
    parser.add_argument("--verbose", action="store_true", help="list every file that is trimmed instead of one line per run")
    return parser


def usage(message):
    sys.stderr.write("e2e-retention: %s\n" % message)
    raise SystemExit(2)


def resolve(args, env):
    if args.dry_run and args.apply:
        usage("--dry-run and --apply contradict each other")
    if args.yes and not args.purge_legacy:
        usage("--yes only confirms --purge-legacy")
    if args.max_gb is None:
        try:
            args.max_gb = float(env.get("E2E_ARTIFACTS_MAX_GB") or 5)
        except ValueError:
            usage("E2E_ARTIFACTS_MAX_GB is not a number")
    if args.keep_runs < 0 or args.max_gb <= 0 or args.failed_cap_mb <= 0:
        usage("the limits must be positive")
    args.tmp_root = args.tmp_root or env.get("E2E_TMP_ROOT") or os.path.join(env.get("TMPDIR") or "/tmp", "tuindice-e2e")
    args.legacy_tmp_parents = args.legacy_tmp_parent if args.legacy_tmp_parent is not None \
        else ["/tmp", os.path.dirname(os.path.abspath(args.tmp_root))]
    repo = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    args.state_root = args.state_root or env.get("E2E_STATE_ROOT") or os.path.join(repo, "build", "e2e")
    args.do_legacy = args.purge_legacy and args.yes
    args.do_regular = args.apply or args.do_legacy
    return args


def summarise(plan, errors):
    carried = [a for a in plan.actions if a.done]
    applying = plan.opt.do_regular
    freed_by = carried if applying else plan.actions
    return {
        "mode": "apply" if applying else "simulation", "stateRoot": plan.root, "capBytes": int(plan.opt.max_gb * GB),
        "managedBytesBefore": plan.managed_before,
        "managedBytesAfter": plan.managed_before - sum(a.freed for a in freed_by if a.managed),
        "freedBytes": sum(a.freed for a in freed_by), "actions": len(plan.actions), "carriedOut": len(carried),
        "currentRunBytes": tree_size(plan.path("runs", plan.opt.current_run)) if plan.opt.current_run else None,
        "legacyBytes": plan.legacy_bytes, "legacyBlocked": plan.legacy_blocked, "notes": plan.notes, "errors": errors,
        "unrecognised": [{"path": p, "bytes": s} for p, s in plan.unrecognised],
        "plan": [a.as_dict() for a in plan.actions],
    }


def run_of(plan, action):
    parts = action.path[len(plan.root) + 1:].split(os.sep)
    return parts[1] if len(parts) > 1 and parts[0] == "runs" else action.path


def report(plan, summary, verbose):
    applying = summary["mode"] == "apply"
    print("[retention] mode: %s" % ("APPLY" if applying else "SIMULATION, nothing is deleted"))
    print("[retention] state root %s: managed content %s of the %s cap" % (plan.root, human(summary["managedBytesBefore"]), human(summary["capBytes"])))
    totals = {}
    for action in plan.actions:
        verb = action.kind.upper() if action.done else "WOULD " + action.kind.upper()
        if action.kind in SLIM_KINDS and not verbose:
            key = (verb, run_of(plan, action))
            count, freed = totals.get(key, (0, 0))
            totals[key] = (count + 1, freed + action.freed)
        else:
            print("[retention] %s %s  %s  (%s)" % (verb, action.path, human(action.freed), action.why))
    for (verb, run), (count, freed) in sorted(totals.items()):
        print("[retention] %s in run %s: %d item(s), %s" % (verb, run, count, human(freed)))
    for note in plan.notes:
        print("[retention] NOTE %s" % note)
    for path, size in plan.unrecognised:
        print("[retention] UNRECOGNISED %s  %s  (not touched)" % (path, human(size)))
    if plan.legacy_blocked:
        print("[retention] LEGACY %s" % plan.legacy_blocked)
    elif not plan.opt.purge_legacy and plan.legacy_bytes:
        print("[retention] LEGACY %s of Maestro leftovers stay: --purge-legacy lists them, --purge-legacy --yes deletes them"
            % human(plan.legacy_bytes))
    for error in summary["errors"]:
        print("[retention] ERROR %s" % error)
    print("[retention] %s %s in %d action(s); managed content %s -> %s" % (
        "freed" if applying else "would free", human(summary["freedBytes"]), summary["carriedOut" if applying else "actions"],
        human(summary["managedBytesBefore"]), human(summary["managedBytesAfter"])))


def main(argv=None, env=None):
    env = dict(os.environ if env is None else env)
    args = resolve(build_parser().parse_args(argv), env)
    if not os.path.isdir(args.state_root):
        print("[retention] %s does not exist: nothing to do" % args.state_root)
        return 0
    plan = Retention(args.state_root, args)
    if args.do_regular:
        try:
            lock = os.open(os.path.join(plan.root, "retention.lock"), os.O_CREAT | os.O_RDWR)
            fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)  # held until this process exits
        except OSError:
            print("[retention] another retention holds %s: nothing done" % os.path.join(plan.root, "retention.lock"))
            return 0
    plan.build()
    errors = []
    for action in plan.actions:
        if args.do_legacy if action.kind == "delete-legacy" else args.do_regular:
            try:
                action.carry_out()
            except OSError as error:
                errors.append("%s %s: %s" % (action.kind, action.path, error))
    summary = summarise(plan, errors)
    if args.json:
        print(json.dumps(summary, indent=2))
    else:
        report(plan, summary, args.verbose)
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
