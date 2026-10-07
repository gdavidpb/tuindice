"""The ledger: what each scenario did under one (platform, fingerprint, catalog).

build/e2e/ledger/<platform>/<fingerprint>/ledger.json. A file is read only when its
platform, fingerprint and catalogSha256 equal the computed ones. Writes are atomic and a
run holds an exclusive flock on ledger.lock for its whole duration.
"""

import datetime
import fcntl
import json
import os

from .config import EnvironmentRefused, UsageError

SCHEMA = "tuindice-e2e-ledger/1"


def now():
    return datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


class Ledger:
    """`directory=None` is an in-memory ledger: diagnose runs never write one."""

    def __init__(self, directory, data):
        self.directory = directory
        self.data = data
        self.lock_fd = None

    @classmethod
    def memory(cls, platform):
        return cls(None, cls._fresh(platform, "diagnose", "", "diagnose"))

    @staticmethod
    def _fresh(platform, fingerprint, catalog_sha, version):
        return {
            "schema": SCHEMA, "platform": platform, "fingerprint": fingerprint, "fingerprintVersion": version,
            "catalogSha256": catalog_sha, "createdAt": now(), "updatedAt": now(), "scenarios": {}, "publications": [],
        }

    @classmethod
    def open(cls, state_root, platform, fingerprint, catalog_sha, version, lock=True, create=True):
        directory = os.path.join(str(state_root), "ledger", platform, fingerprint)
        path = os.path.join(directory, "ledger.json")
        if os.path.exists(path):
            try:
                data = json.load(open(path))
            except ValueError:
                raise UsageError("ledger %s is not valid JSON; move it away deliberately" % path)
            expected = (platform, fingerprint, catalog_sha)
            found = (data.get("platform"), data.get("fingerprint"), data.get("catalogSha256"))
            if found != expected:
                raise UsageError(
                    "ledger %s was written for platform/fingerprint/catalog %s, not %s; it is not read"
                    % (path, "/".join(str(v)[:12] for v in found), "/".join(v[:12] for v in expected)))
        elif create:
            data = cls._fresh(platform, fingerprint, catalog_sha, version)
        else:
            return cls(directory, cls._fresh(platform, fingerprint, catalog_sha, version))
        ledger = cls(directory, data)
        if lock:
            os.makedirs(directory, exist_ok=True)
            ledger.lock_fd = os.open(os.path.join(directory, "ledger.lock"), os.O_CREAT | os.O_RDWR)
            try:
                fcntl.flock(ledger.lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
            except OSError:
                os.close(ledger.lock_fd)
                ledger.lock_fd = None
                raise EnvironmentRefused("another run holds the ledger of %s fingerprint %s" % (platform, fingerprint[:12]))
        return ledger

    def release(self):
        if self.lock_fd is not None:
            os.close(self.lock_fd)
            self.lock_fd = None

    def save(self):
        if self.directory is None:
            return
        self.data["updatedAt"] = now()
        os.makedirs(self.directory, exist_ok=True)
        target = os.path.join(self.directory, "ledger.json")
        temporary = target + ".tmp"
        with open(temporary, "w") as handle:
            json.dump(self.data, handle, indent=2, sort_keys=True)
            handle.flush()
            os.fsync(handle.fileno())
        os.replace(temporary, target)

    def entry(self, scenario_id):
        return self.data["scenarios"].setdefault(scenario_id, {"status": "pending", "attempts": [], "overrides": []})

    def attempts(self, scenario_id):
        return self.data["scenarios"].get(scenario_id, {}).get("attempts", [])

    def counted(self, scenario_id):
        return [a for a in self.attempts(scenario_id) if a.get("countsAgainstCap")]

    def passed(self, scenario_id):
        return self.data["scenarios"].get(scenario_id, {}).get("status") == "passed"

    def allowance(self, scenario_id, cap):
        return cap + len(self.data["scenarios"].get(scenario_id, {}).get("overrides", []))

    def exhausted(self, scenario_id, cap):
        return not self.passed(scenario_id) and len(self.counted(scenario_id)) >= self.allowance(scenario_id, cap)

    def has_failed_attempts(self, scenario_id):
        return any(a.get("outcome") == "failed" for a in self.attempts(scenario_id))

    def record_attempt(self, scenario_id, attempt, cap):
        entry = self.entry(scenario_id)
        attempt["n"] = len(entry["attempts"]) + 1
        entry["attempts"].append(attempt)
        if attempt["outcome"] == "passed":
            entry["status"] = "passed"
            entry["passedAttempt"] = attempt["n"]
        elif attempt.get("countsAgainstCap"):
            entry["status"] = "exhausted" if self.exhausted(scenario_id, cap) else "failed"
        return attempt["n"]

    def add_override(self, scenario_id, reason, user):
        entry = self.entry(scenario_id)
        entry["overrides"].append({"at": now(), "reason": reason, "by": user})
        if entry["status"] == "exhausted":
            entry["status"] = "failed"

    def add_publication(self, record):
        self.data["publications"].append(record)

    def publication(self, sha, context, description):
        for item in self.data["publications"]:
            if (item["sha"], item["context"], item["description"]) == (sha, context, description):
                return item
        return None

    def update_index(self, state_root, sha, evidence_seconds):
        index_path = os.path.join(str(state_root), "ledger", self.data["platform"], "index.json")
        try:
            entries = json.load(open(index_path))
        except (OSError, ValueError):
            entries = []
        entries = [e for e in entries if e.get("fingerprint") != self.data["fingerprint"]]
        entries.append({"fingerprint": self.data["fingerprint"], "completeAtSha": sha, "completedAt": now(),
            "evidenceSeconds": int(evidence_seconds)})
        os.makedirs(os.path.dirname(index_path), exist_ok=True)
        with open(index_path + ".tmp", "w") as handle:
            json.dump(entries, handle, indent=2)
        os.replace(index_path + ".tmp", index_path)
