"""Host lock for the heavy preparation of `--platform all`: one platform boots a device and builds at a time.

An flock on a file under the harness temporary root. The kernel drops it when its holder dies, so a killed run
cannot leave it held; the file's content only records the holder for whoever waits.
"""

import fcntl
import json
import os
import time
from contextlib import contextmanager

from .config import EnvironmentRefused
from .ledger import now

# More than the sum of the ensure-device, build and install deadlines (420 + 1800 + 120 s): a holder cannot
# legitimately keep it longer.
PREPARE_LOCK_TIMEOUT_SECONDS = 2400
POLL_SECONDS = 0.5


def _owner(handle):
    handle.seek(0)
    try:
        return json.loads(handle.read() or "{}")
    except ValueError:
        return {}


@contextmanager
def prepare_lock(cfg, platform, run_id, say, record):
    """Holds the lock for the body. `record(waitSeconds=, waitedFor=)` reports how long and behind whom this waited."""
    timeout = float(cfg.seam("E2E_FAKE_PREPARE_LOCK_TIMEOUT_SECONDS") or PREPARE_LOCK_TIMEOUT_SECONDS)
    os.makedirs(str(cfg.tmp_root), exist_ok=True)
    handle = open(os.path.join(str(cfg.tmp_root), "prepare.lock"), "a+")
    held, began, waited, announced = False, time.monotonic(), None, False
    try:
        while not held:
            try:
                fcntl.flock(handle, fcntl.LOCK_EX | fcntl.LOCK_NB)
                held = True
            except OSError:
                waited = waited or "unknown"
                owner = _owner(handle)  # empty for an instant between the holder's flock and its write
                if owner.get("platform"):
                    waited = owner["platform"]
                if not announced and (owner or time.monotonic() - began > 1):
                    announced = True
                    say("LOCK  %s (pid %s) is preparing its device; waiting for it" % (waited, owner.get("pid", "?")))
                if time.monotonic() - began >= timeout:
                    raise EnvironmentRefused("the host preparation lock stayed with %s (pid %s, since %s) for %ds"
                        % (waited, owner.get("pid", "?"), owner.get("since", "?"), timeout))
                time.sleep(POLL_SECONDS)
        handle.seek(0)
        handle.truncate(0)
        handle.write(json.dumps({"platform": platform, "pid": os.getpid(), "runId": run_id, "since": now()}))
        handle.flush()
        record(waitSeconds=round(time.monotonic() - began, 1), waitedFor=waited)
        yield
    finally:
        if held:
            handle.truncate(0)
        handle.close()
