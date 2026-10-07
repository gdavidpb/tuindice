"""Loads and validates the generated scenario catalog (e2e/catalog/scenarios.json)."""

import datetime
import hashlib
import json
import re
import subprocess

from .config import PLATFORMS, UsageError

SCHEMA = "tuindice-e2e-catalog/1"
ID_PATTERN = re.compile(r"^[a-z0-9]+(-[a-z0-9]+)*$")


class Scenario:
    def __init__(self, raw):
        self.raw = raw
        self.id = raw["id"]
        self.module = raw.get("module", "")
        self.tags = list(raw.get("tags") or [])
        self.platforms = list(raw["platforms"])
        self.account_id = raw.get("account")
        self.signs_in = bool(raw.get("signsIn"))
        self.timeout = int(raw.get("timeoutSeconds", 180))
        self.steps_hash = raw.get("stepsHash", "")
        self.quarantine = raw.get("quarantine")
        self.ios_only_testing = (raw.get("ios") or {}).get("onlyTesting")
        self.android_arg = (raw.get("android") or {}).get("scenarioArg")

    def runner_identifier(self, platform):
        return self.android_arg if platform == "android" else self.ios_only_testing


class Catalog:
    def __init__(self, path, data, sha256):
        self.path = path
        self.data = data
        self.sha256 = sha256
        self.accounts = {a["id"]: a for a in data.get("accounts", [])}
        self.scenarios = [Scenario(raw) for raw in data["scenarios"]]
        self.by_id = {s.id: s for s in self.scenarios}

    def account(self, scenario):
        return self.accounts.get(scenario.account_id) if scenario.account_id else None

    def in_scope(self, platform, today=None):
        """Returns (runnable, quarantined). An expired quarantine is a precondition error."""
        today = today or datetime.date.today()
        runnable, quarantined = [], []
        for scenario in self.scenarios:
            if platform not in scenario.platforms:
                continue
            q = scenario.quarantine
            if q:
                if datetime.date.fromisoformat(q["until"]) < today:
                    raise UsageError("quarantine of %s expired on %s (%s)" % (scenario.id, q["until"], q["reason"]))
                quarantined.append(scenario)
            else:
                runnable.append(scenario)
        return runnable, quarantined


def _validate(data):
    if data.get("schema") != SCHEMA:
        raise UsageError("catalog schema is %r, expected %r" % (data.get("schema"), SCHEMA))
    seen = set()
    account_ids = {a.get("id") for a in data.get("accounts", [])}
    for raw in data.get("scenarios", []):
        sid = raw.get("id", "")
        if not ID_PATTERN.match(sid) or sid in seen:
            raise UsageError("catalog scenario id %r is invalid or duplicated" % sid)
        seen.add(sid)
        if not raw.get("platforms") or not set(raw["platforms"]) <= set(PLATFORMS):
            raise UsageError("catalog scenario %s has invalid platforms" % sid)
        if int(raw.get("timeoutSeconds", 180)) <= 0:
            raise UsageError("catalog scenario %s has a non-positive timeout" % sid)
        if raw.get("account") and raw["account"] not in account_ids:
            raise UsageError("catalog scenario %s names the unknown account %s" % (sid, raw["account"]))
        q = raw.get("quarantine")
        if q:
            try:
                datetime.date.fromisoformat(q["until"])
            except (KeyError, ValueError):
                raise UsageError("quarantine of %s needs a reason and an ISO until date" % sid)
            if not q.get("reason"):
                raise UsageError("quarantine of %s needs a reason" % sid)


def load(path):
    try:
        raw = open(str(path), "rb").read()
    except OSError as error:
        raise UsageError("cannot read the catalog %s: %s" % (path, error))
    try:
        data = json.loads(raw.decode("utf-8"))
    except ValueError as error:
        raise UsageError("catalog %s is not valid JSON: %s" % (path, error))
    _validate(data)
    return Catalog(path, data, hashlib.sha256(raw).hexdigest())


def changed_ids(config, catalog, ref):
    """Scenarios whose stepsHash differs from the catalog committed at `ref`."""
    if not ref:
        return set()
    relative = config.layout["E2E_CATALOG_JSON"]
    result = subprocess.run(
        ["git", "show", "%s:%s" % (ref, relative)], cwd=str(config.root),
        stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, universal_newlines=True,
    )
    if result.returncode != 0:
        return set()
    try:
        old = {s["id"]: s.get("stepsHash") for s in json.loads(result.stdout)["scenarios"]}
    except (ValueError, KeyError):
        return set()
    return {s.id for s in catalog.scenarios if old.get(s.id) != s.steps_hash}


def default_base_ref(config):
    for ref in ("origin/production", "production"):
        result = subprocess.run(
            ["git", "merge-base", "HEAD", ref], cwd=str(config.root),
            stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, universal_newlines=True,
        )
        if result.returncode == 0 and result.stdout.strip():
            return result.stdout.strip()
    return None
