"""Publishing of the platform's success status. The context has one definition, in shell."""

import json
import shlex
import subprocess

from .config import UsageError


PUBLISH_TIMEOUT_SECONDS = 120


class PublishError(Exception):
    pass


def gh_command(config):
    return shlex.split(config.seam("E2E_GH_CMD")) if config.seam("E2E_GH_CMD") else ["gh"]


def status_context(config, platform):
    """e2e_status_context from the shared shell library: the detector and the harness read the same name."""
    source = config.root / config.layout["E2E_STATUS_CONTEXT_SOURCE"]
    if not source.exists():
        raise UsageError("%s does not exist" % source)
    result = subprocess.run(
        ["bash", "-c", 'source "$1"; e2e_status_context "$2"', "_", str(source), platform], cwd=str(config.root),
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True,
    )
    context = result.stdout.strip()
    if result.returncode != 0 or not context:
        raise UsageError("e2e_status_context is not defined in %s for %s: %s"
            % (source, platform, result.stderr.strip()[:200]))
    return context


def description(platform, runnable, quarantined, sha, fingerprint, ledger):
    """The status text. Every number comes from the ledger, so a run and a later `publish` word it the same way:
    retried = scenarios that failed a counted attempt, env = scenarios that had an environment failure (they do not count
    against the cap), overrides = scenario resets plus the environment, parallelism and retry overrides the runs used."""
    total = len(runnable)
    retried = sum(1 for s in runnable if any(a["outcome"] == "failed" and a["countsAgainstCap"] for a in ledger.attempts(s.id)))
    env = sum(1 for s in runnable if ledger.environment_events(s.id))
    overrides = sum(len(ledger.data["scenarios"].get(s.id, {}).get("overrides", [])) for s in runnable) \
        + len(ledger.data.get("runOverrides", []))
    text = "Local E2E %s %d/%d passed for %s fp %s." % (platform, total, total, sha[:7], fingerprint[:12])
    for count, word in ((retried, "retried"), (env, "env"), (len(quarantined), "quarantined"), (overrides, "overrides")):
        if count:
            text += " %s %d." % (word, count)
    return text[:140]


def publish_success(config, platform, sha, context, text):
    command = gh_command(config) + [
        "api", "-X", "POST", "repos/{owner}/{repo}/statuses/%s" % sha,
        "-f", "state=success", "-f", "context=%s" % context, "-f", "description=%s" % text,
    ]
    timeout = float(config.seam("E2E_FAKE_PUBLISH_TIMEOUT_SECONDS") or PUBLISH_TIMEOUT_SECONDS)
    try:
        result = subprocess.run(
            command, cwd=str(config.root), stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True,
            timeout=timeout,
        )
    except subprocess.TimeoutExpired:
        raise PublishError("gh did not answer within %ds" % timeout)
    except OSError as error:
        raise PublishError("gh could not be run: %s" % error)
    if result.returncode != 0:
        raise PublishError("gh could not publish the status: %s" % result.stderr.strip()[:300])
    try:
        reply = json.loads(result.stdout)
    except ValueError:
        raise PublishError("gh answered something that is not JSON: %s" % result.stdout[:200])
    if reply.get("state") != "success" or reply.get("context") != context:
        raise PublishError("GitHub recorded %s/%s instead of success/%s" % (reply.get("state"), reply.get("context"), context))
