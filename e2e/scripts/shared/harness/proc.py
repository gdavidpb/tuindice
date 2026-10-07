"""Child processes with deadlines. Every child gets its own process group, killed as a whole."""

import json
import os
import signal
import subprocess
import time

KILL_GRACE_SECONDS = 10.0


def parse_json(text):
    text = (text or "").strip()
    for candidate in (text, text.splitlines()[-1] if text else ""):
        try:
            value = json.loads(candidate)
            return value if isinstance(value, dict) else {}
        except ValueError:
            continue
    return {}


class Interrupted(BaseException):
    """Raised by the signal handler so that cleanup runs in `finally` blocks."""

    def __init__(self, signum):
        BaseException.__init__(self, signum)
        self.signum = signum


class Result:
    def __init__(self, returncode, stdout, stderr, timed_out, seconds):
        self.returncode = returncode
        self.stdout = stdout
        self.stderr = stderr
        self.timed_out = timed_out
        self.seconds = seconds

    @property
    def ok(self):
        return self.returncode == 0 and not self.timed_out


def kill_group(process, grace=KILL_GRACE_SECONDS):
    """SIGTERM to the group, SIGKILL after `grace` seconds. Returns the collected output."""
    try:
        os.killpg(process.pid, signal.SIGTERM)
    except (ProcessLookupError, PermissionError):
        pass
    try:
        return process.communicate(timeout=grace)
    except subprocess.TimeoutExpired:
        pass
    try:
        os.killpg(process.pid, signal.SIGKILL)
    except (ProcessLookupError, PermissionError):
        pass
    try:
        return process.communicate(timeout=grace)
    except subprocess.TimeoutExpired:
        return "", ""


def run(argv, timeout, cwd=None, env=None, grace=KILL_GRACE_SECONDS):
    started = time.monotonic()
    process = subprocess.Popen(
        argv, cwd=cwd, env=env, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
        universal_newlines=True, start_new_session=True,
    )
    deadline = started + timeout
    try:
        while True:
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                out, err = kill_group(process, grace)
                return Result(process.returncode, out or "", err or "", True, time.monotonic() - started)
            try:
                out, err = process.communicate(timeout=min(0.2, remaining))
                return Result(process.returncode, out or "", err or "", False, time.monotonic() - started)
            except subprocess.TimeoutExpired:
                continue
    except BaseException:
        if process.poll() is None:
            kill_group(process, grace)
        raise
