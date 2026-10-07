#!/usr/bin/env python3
"""Local E2E certification harness: run, status, env-check, list, publish, reset-scenario, contexts, stop-devices."""

import argparse
import json
import os
import signal
import sys
import traceback

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from harness import catalog as catalog_mod  # noqa: E402
from harness import publish as publish_mod  # noqa: E402
from harness import envcheck, parallel, report, runner, verdict  # noqa: E402
from harness.config import EXIT_HARNESS_ERROR, PLATFORMS, Config, EnvironmentRefused, UsageError, find_repo_root  # noqa: E402
from harness.gitstate import GitState  # noqa: E402
from harness.ledger import Ledger, now  # noqa: E402
from harness.proc import Interrupted  # noqa: E402
from harness.wiremock import WireMock  # noqa: E402

EXIT_PRECEDENCE = (EXIT_HARNESS_ERROR, 2, 3, 5, 7, 1, 4, 6)


def combine(codes):
    for code in EXIT_PRECEDENCE:
        if code in codes:
            return code
    return max(codes) if codes else 0


def install_signal_handlers():
    state = {"seen": False}

    def handler(signum, _frame):
        if state["seen"]:
            return
        state["seen"] = True
        raise Interrupted(signum)

    for name in ("SIGINT", "SIGTERM", "SIGHUP"):
        signal.signal(getattr(signal, name), handler)


def fingerprint_for(cfg, platform):
    holder = runner.PlatformRun(cfg, platform, runner.Options("evidence"))
    return holder._fingerprint()


def open_ledger(cfg, platform, lock, create):
    catalog = catalog_mod.load(cfg.catalog_path)
    fingerprint = fingerprint_for(cfg, platform)
    ledger = Ledger.open(cfg.state_root, platform, fingerprint, catalog.sha256, cfg.layout["E2E_FINGERPRINT_VERSION"],
        lock=lock, create=create)  # no attempt cap asked: these commands read the one the ledger stores
    return catalog, fingerprint, ledger


def cmd_run(cfg, args):
    if args.mode == "evidence":
        rejected = [name for name, value in (("--scenario", args.scenario), ("--tag", args.tag), ("--repeat", args.repeat != 1),
            ("--trace", args.trace), ("E2E_SCENARIOS", cfg.scenario_filter)) if value]
        if rejected:
            raise UsageError("evidence mode runs the whole catalog; it rejects %s" % ", ".join(rejected))
        if args.survey:
            raise UsageError("--survey is a diagnostic: it retries nothing and never stops, so it cannot produce evidence")
    if not 1 <= args.repeat <= 50:
        raise UsageError("--repeat must be between 1 and 50")
    options = runner.Options(
        args.mode, scenarios=[i for i in (args.scenario or "").split(",") if i], tag=args.tag,
        changed_since=args.changed_since, survey=args.survey, repeat=args.repeat, trace=args.trace,
        dry_run=args.dry_run, force=args.force, child_of=args.child_of, stop_device=args.stop_device)
    if args.parallel_decision:
        options.parallel = (args.parallel_decision, args.parallel_reason or "")
    platforms = cfg.platforms_for(args.platform)
    if len(platforms) == 1:
        return runner.PlatformRun(cfg, platforms[0], options).execute()
    failed = set() if args.mode == "diagnose" else {p for p in platforms if has_failed_attempts(cfg, p)}
    return parallel.run_all(cfg, platforms, args, options, failed, combine)


def has_failed_attempts(cfg, platform):
    """True when the platform's ledger for the current fingerprint holds a failed attempt (it then runs first)."""
    try:
        catalog, _fingerprint, ledger = open_ledger(cfg, platform, lock=False, create=False)
        runnable, _quarantined = catalog.in_scope(platform)
        return any(ledger.has_failed_attempts(s.id) for s in runnable)
    except (UsageError, EnvironmentRefused):
        return False


def failed_scenarios(ledger, ids, cap):
    """Scenarios that are not green but have counted attempts: the class and summary of the last one."""
    failed = []
    for scenario_id in ids:
        counted = ledger.counted(scenario_id)
        if counted and not ledger.passed(scenario_id):
            failed.append({"id": scenario_id, "class": counted[-1].get("failureClass"),
                "summary": counted[-1].get("failureSummary"), "attempts": len(counted),
                "exhausted": ledger.exhausted(scenario_id, cap)})
    return failed


def platform_status(cfg, platform, head, candidates, github):
    info = {"platform": platform}
    try:
        catalog, fingerprint, ledger = open_ledger(cfg, platform, lock=False, create=False)
        runnable, quarantined = catalog.in_scope(platform)
        cap = ledger.cap(cfg.attempt_cap)
        ids = [s.id for s in runnable]
        info.update(
            fingerprint=fingerprint, ledgerPresent=os.path.exists(os.path.join(ledger.directory, "ledger.json")),
            inScope=len(ids), quarantined=len(quarantined), green=[i for i in ids if ledger.passed(i)],
            pending=[i for i in ids if not ledger.passed(i)],
            exhausted=[i for i in ids if ledger.exhausted(i, cap)], failed=failed_scenarios(ledger, ids, cap),
            publications=ledger.data["publications"])
        info["complete"] = not info["pending"]
        remote = github.find(platform, fingerprint, [head] + candidates)
        info["verdict"], info["evidence"] = verdict.decide(info, head, candidates, remote)
        info["remote"] = {key: remote[key] for key in ("reachable", "checked", "truncated", "incomplete")}
    except (UsageError, EnvironmentRefused) as error:
        info["error"] = str(error)
    return info


def cmd_status(cfg, args):
    git = GitState(cfg.root)
    candidates, github = verdict.reuse_candidates(cfg.root, git.sha), verdict.Remote(cfg)
    data = {"head": git.sha, "branch": git.branch, "upstream": git.upstream_sha, "treeClean": git.tree_clean,
        "headEqualsUpstream": git.head_equals_upstream,
        "platforms": {p: platform_status(cfg, p, git.sha, candidates, github) for p in PLATFORMS}}
    if args.json:
        print(json.dumps(data, indent=2, sort_keys=True))
        return 0
    for platform, info in data["platforms"].items():
        if "error" in info:
            print("%s: %s" % (platform, info["error"]))
            continue
        print("%s: %s; fp %s; %d in scope; %d green; %d pending; %d exhausted; %d publications"
            % (platform, info["verdict"], info["fingerprint"][:12], info["inScope"], len(info["green"]),
                len(info["pending"]), len(info["exhausted"]), len(info["publications"])))
        if info["remote"]["incomplete"]:
            print("  GitHub did not answer for %d commit(s) after asking twice: %s; the evidence on them is neither present nor "
                "absent, ask again with `status`" % (len(info["remote"]["incomplete"]), " ".join(c[:12] for c in info["remote"]["incomplete"])))
        if info["remote"]["truncated"]:
            print("  note: the history was cut short, so evidence on older commits was not looked for")
    return 0


def cmd_list(cfg, args):
    catalog = catalog_mod.load(cfg.catalog_path)
    changed = catalog_mod.changed_ids(cfg, catalog, args.changed_since) if args.changed_since else None
    for scenario in catalog.scenarios:
        if args.platform and args.platform not in scenario.platforms:
            continue
        if changed is not None and scenario.id not in changed:
            continue
        quarantine = " quarantined until %s" % scenario.quarantine["until"] if scenario.quarantine else ""
        print("%s  [%s]  tags=%s%s" % (scenario.id, ",".join(scenario.platforms), ",".join(scenario.tags) or "-", quarantine))
    return 0


def cmd_publish(cfg, args):
    log = report.Log(args.platform)
    git = GitState(cfg.root)
    git.require_clean()
    cfg.require_no_seams("publishing a status")
    git.require_publishable(publish_mod.gh_command(cfg))
    catalog, fingerprint, ledger = open_ledger(cfg, args.platform, lock=True, create=False)
    try:
        runnable, quarantined = catalog.in_scope(args.platform)
        pending = [s.id for s in runnable if not ledger.passed(s.id)]
        if pending:
            raise UsageError("%d scenarios are not green for fp %s: %s" % (len(pending), fingerprint[:12], ", ".join(pending)))
        context = publish_mod.status_context(cfg, args.platform)
        text = publish_mod.description(args.platform, runnable, quarantined, git.sha, fingerprint, ledger)
        if ledger.publication(git.sha, context, text):
            log.say("status %s already published for %s" % (context, git.sha7))
            return 0
        try:
            publish_mod.publish_success(cfg, args.platform, git.sha, context, text)
        except publish_mod.PublishError as error:
            log.say("PUBLISH FAILED %s" % error)
            return 6
        ledger.add_publication({"sha": git.sha, "context": context, "description": text,
            "publishedAt": now(), "runId": "publish"})
        ledger.save()
        log.say("published %s: %s" % (context, text))
        return 0
    finally:
        ledger.release()


def cmd_reset_scenario(cfg, args):
    if not args.reason.strip():
        raise UsageError("reset-scenario needs a reason")
    catalog, fingerprint, ledger = open_ledger(cfg, args.platform, lock=True, create=False)
    try:
        if args.id not in catalog.by_id:
            raise UsageError("%s is not in the catalog" % args.id)
        entry = ledger.data["scenarios"].get(args.id, {})
        if entry.get("overrides"):
            raise UsageError("%s was already reset once for fp %s; it is not reset twice" % (args.id, fingerprint[:12]))
        if not ledger.exhausted(args.id, ledger.cap(cfg.attempt_cap)):
            raise UsageError("%s has not exhausted its attempts for fp %s" % (args.id, fingerprint[:12]))
        ledger.add_override(args.id, args.reason, GitState(cfg.root).user)
        ledger.save()
        print("[e2e %s] reset %s once for fp %s: %s" % (args.platform, args.id, fingerprint[:12], args.reason))
        return 0
    finally:
        ledger.release()


def cmd_contexts(cfg, args):
    for platform in ([args.platform] if args.platform else PLATFORMS):
        print("%s %s" % (platform, publish_mod.status_context(cfg, platform)))
    return 0


def cmd_stop_devices(cfg, args):
    """A device the harness booted stays up after a run of one platform (the next run reuses it); this stops the pinned
    emulator and simulator, and nothing else. It refuses while a run owns the platform."""
    platforms = [args.platform] if args.platform else list(PLATFORMS)
    for platform in platforms:
        mock = WireMock(cfg, platform, "")
        owner = mock._owner()
        if owner and mock._running(owner):
            raise EnvironmentRefused("a run (%s, pid %s) is using the %s device; stop it first" % (owner["runId"], owner["pid"], platform))
    failed = 0
    for platform in platforms:
        result = runner.Adapter(cfg, platform).call("stop-device")
        print("[e2e %s] %s" % (platform, "device stopped" if result.ok else "stop-device failed: %s" % result.stderr.strip()[-300:]))
        failed += 0 if result.ok else 1
    return 3 if failed else 0


def cmd_env_check(cfg, args):
    return envcheck.main(cfg, args)


def build_parser():
    parser = argparse.ArgumentParser(prog="e2e.py", description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)
    run = sub.add_parser("run")
    run.add_argument("--platform", choices=list(PLATFORMS) + ["all"], required=True)
    run.add_argument("--mode", choices=["evidence", "diagnose"], required=True)
    run.add_argument("--scenario")
    run.add_argument("--tag")
    run.add_argument("--changed-since")
    run.add_argument("--survey", action="store_true")
    run.add_argument("--repeat", type=int, default=1)
    run.add_argument("--trace", action="store_true")
    run.add_argument("--dry-run", action="store_true")
    run.add_argument("--force", action="store_true")
    # Internal: how `--platform all` starts each platform as an independent process.
    run.add_argument("--child-of", help=argparse.SUPPRESS)
    run.add_argument("--parallel-decision", help=argparse.SUPPRESS)
    run.add_argument("--parallel-reason", help=argparse.SUPPRESS)
    run.add_argument("--stop-device", action="store_true", help=argparse.SUPPRESS)
    for name in ("status", "env-check"):
        sub.add_parser(name).add_argument("--json", action="store_true")
    listing = sub.add_parser("list")
    listing.add_argument("--platform", choices=PLATFORMS)
    listing.add_argument("--changed-since")
    pub = sub.add_parser("publish")
    pub.add_argument("--platform", choices=PLATFORMS, required=True)
    reset = sub.add_parser("reset-scenario")
    reset.add_argument("--platform", choices=PLATFORMS, required=True)
    reset.add_argument("--id", required=True)
    reset.add_argument("--reason", required=True)
    contexts = sub.add_parser("contexts")
    contexts.add_argument("--platform", choices=PLATFORMS)
    sub.add_parser("stop-devices").add_argument("--platform", choices=PLATFORMS)
    return parser


COMMANDS = {"run": cmd_run, "status": cmd_status, "list": cmd_list, "publish": cmd_publish,
    "reset-scenario": cmd_reset_scenario, "contexts": cmd_contexts, "env-check": cmd_env_check, "stop-devices": cmd_stop_devices}


def main(argv=None, env=None):
    args = build_parser().parse_args(argv)
    try:
        cfg = Config(find_repo_root(), env)
        return COMMANDS[args.command](cfg, args)
    except UsageError as error:
        print("e2e: %s" % error, file=sys.stderr)
        return 2
    except EnvironmentRefused as error:
        print("e2e: %s" % error, file=sys.stderr)
        return 3
    except Interrupted as error:
        return 128 + error.signum
    except Exception as error:  # a defect of the harness: its own exit code, never the 1 of "scenarios failed"
        print("e2e: internal error %s: %s\n%s" % (type(error).__name__, error, traceback.format_exc()), file=sys.stderr)
        return EXIT_HARNESS_ERROR


if __name__ == "__main__":
    install_signal_handlers()
    sys.exit(main())
