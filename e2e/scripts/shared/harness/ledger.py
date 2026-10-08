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
        return cls(None, cls._fresh(platform, "diagnose", "", "diagnose", None))

    @staticmethod
    def _fresh(platform, fingerprint, catalog_sha, version, attempt_cap):
        return {
            "schema": SCHEMA, "platform": platform, "fingerprint": fingerprint, "fingerprintVersion": version,
            "catalogSha256": catalog_sha, "attemptCap": attempt_cap, "runOverrides": [], "seams": [],
            "createdAt": now(), "updatedAt": now(), "scenarios": {}, "publications": [],
        }

    @classmethod
    def open(cls, state_root, platform, fingerprint, catalog_sha, version, lock=True, create=True, attempt_cap=None):
        """The ledger of one (platform, fingerprint). With `lock` the lock is taken before the file is read, so that
        what a finishing run saved is never overwritten. `attempt_cap` is stored when the ledger is created; a later
        invocation that asks for another one is refused (None reads whatever is stored)."""
        directory = os.path.join(str(state_root), "ledger", platform, fingerprint)
        path = os.path.join(directory, "ledger.json")
        if not os.path.exists(path) and (not lock or not create):
            return cls(directory, cls._fresh(platform, fingerprint, catalog_sha, version, attempt_cap))
        lock_fd = None
        if lock:
            os.makedirs(directory, exist_ok=True)
            lock_fd = os.open(os.path.join(directory, "ledger.lock"), os.O_CREAT | os.O_RDWR)
            try:
                fcntl.flock(lock_fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
            except OSError:
                os.close(lock_fd)
                raise EnvironmentRefused("another run holds the ledger of %s fingerprint %s" % (platform, fingerprint[:12]))
        try:
            ledger = cls(directory, cls._read(path, platform, fingerprint, catalog_sha, version, attempt_cap))
        except BaseException:
            if lock_fd is not None:
                os.close(lock_fd)
            raise
        ledger.lock_fd = lock_fd
        return ledger

    @classmethod
    def _read(cls, path, platform, fingerprint, catalog_sha, version, attempt_cap):
        if not os.path.exists(path):
            return cls._fresh(platform, fingerprint, catalog_sha, version, attempt_cap)
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
        stored = data.get("attemptCap")
        if attempt_cap is not None and stored is not None and stored != attempt_cap:
            raise UsageError("the attempt cap of this fingerprint is %d, fixed when its ledger was created; this invocation asks "
                "for %d (E2E_MAX_RETRIES=%d). Rerun with E2E_MAX_RETRIES=%d; an extra attempt needs e2e.py reset-scenario"
                % (stored, attempt_cap, attempt_cap - 1, stored - 1))
        if stored is None:
            data["attemptCap"] = attempt_cap
        data.setdefault("runOverrides", [])
        data.setdefault("seams", [])
        return data

    def cap(self, default):
        """The attempt cap of this ledger: the stored one, else `default`."""
        return self.data.get("attemptCap") or default

    def note_overrides(self, overrides):
        """Records the overrides a run used (environment checks, parallelism, retries); the status description counts them."""
        self.data["runOverrides"] = sorted(set(self.data["runOverrides"]) | set(overrides))

    def note_seams(self, seams):
        """Records the test seams a run used; a ledger that has any cannot be published (the harness tests' key apart)."""
        self.data["seams"] = sorted((set(self.data["seams"]) | set(seams)) - {"E2E_TEST_ALLOW_SEAMS"})

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

    def environment_events(self, scenario_id):
        return [a for a in self.attempts(scenario_id) if a.get("failureClass") == "environment"]

    def degraded_events(self, scenario_id):
        return [a for a in self.environment_events(scenario_id) if a.get("degraded")]

    def has_failed_attempts(self, scenario_id):
        return any(a.get("outcome") == "failed" for a in self.attempts(scenario_id))

    def record_attempt(self, scenario_id, attempt):
        entry = self.entry(scenario_id)
        attempt["n"] = len(entry["attempts"]) + 1
        entry["attempts"].append(attempt)
        if attempt["outcome"] == "passed":
            entry["status"] = "passed"
            entry["passedAttempt"] = attempt["n"]
        elif attempt.get("countsAgainstCap"):
            entry["status"] = "failed"  # whether the cap is spent is computed from the attempts and the overrides, never stored
        return attempt["n"]

    def add_override(self, scenario_id, reason, user):
        entry = self.entry(scenario_id)
        entry["overrides"].append({"at": now(), "reason": reason, "by": user})

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
