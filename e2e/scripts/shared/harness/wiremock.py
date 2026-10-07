"""WireMock as the harness sees it: ownership, start, health and the request journal.

The interpreter inside each runner resets the backend and fixes the mock states; the harness
never resets. Each platform owns its WireMock through an atomic `mkdir` lock under
`<E2E_TMP_ROOT>/<platform>/wiremock/`. A listener the harness did not start is never killed.
With E2E_FAKE_WIREMOCK_DIR set nothing is started and the "server" is a directory
(`journal.json` is its journal, a `down` file makes it unhealthy).
"""

import json
import os
import shlex
import shutil
import signal
import socket
import subprocess
import time
import urllib.request

from .config import EnvironmentRefused
from .proc import KILL_GRACE_SECONDS, kill_group

OWNER_MARKER = "RunMockEnvironment"  # in the command line of a live WireMock
CLAIM_GRACE_SECONDS = 10  # a lock without owner.json this young is being claimed right now
READY_SECONDS = 45


def _group_commands():
    """{pgid: [command]} of every process."""
    groups = {}
    try:
        out = subprocess.run(["ps", "-Ao", "pid=,pgid=,command="], stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
            universal_newlines=True, timeout=30).stdout
    except (OSError, subprocess.SubprocessError):
        return groups
    for line in out.splitlines():
        parts = line.split(None, 2)
        if len(parts) == 3 and parts[1].isdigit():
            groups.setdefault(int(parts[1]), []).append(parts[2])
    return groups


def _alive(pid):
    try:
        os.kill(pid, 0)
        return True
    except ProcessLookupError:
        return False
    except PermissionError:
        return True


class WireMock:
    def __init__(self, config, platform, run_dir):
        self.config = config
        self.platform = platform
        self.port = config.ports[platform]
        self.run_dir = run_dir
        self.run_id = os.path.basename(str(run_dir))
        self.fake_dir = config.seam("E2E_FAKE_WIREMOCK_DIR")
        self.process = None
        self.state_dir = config.tmp_root / platform / "wiremock"
        self.lock_dir = self.state_dir / "lock"
        self.marker = config.seam("E2E_FAKE_WIREMOCK_MARKER") or OWNER_MARKER
        self.log_path = os.path.join(str(run_dir), "wiremock.log")
        self.claimed = False
        self.journal_error = None

    @property
    def base_url(self):
        return "http://127.0.0.1:%d" % self.port

    # -- ownership ---------------------------------------------------------------------------

    def _owner(self):
        try:
            with open(str(self.lock_dir / "owner.json")) as handle:
                return json.load(handle)
        except (OSError, ValueError):
            return None

    def _write_owner(self, pid=None, pgid=None):
        owner = {"pid": pid, "pgid": pgid, "port": self.port, "runId": self.run_id, "harnessPid": os.getpid()}
        temporary = str(self.lock_dir / "owner.json.tmp")
        with open(temporary, "w") as handle:
            json.dump(owner, handle)
        os.replace(temporary, str(self.lock_dir / "owner.json"))

    def _running(self, owner):
        if owner["pid"] is None:
            return _alive(owner["harnessPid"])
        return _alive(owner["pid"]) and any(self.marker in command for command in _group_commands().get(owner["pgid"], []))

    def _orphan(self, owner):
        """A WireMock this harness started whose harness is gone: the process is alive and is still the one that was started
        (pid, process group and marker agree), so it is ours to stop and not somebody else's."""
        return owner["pid"] is not None and owner.get("harnessPid") is not None and not _alive(owner["harnessPid"]) \
            and self._running(owner)

    def _claim(self):
        os.makedirs(str(self.state_dir), exist_ok=True)
        for _ in range(2):
            try:
                os.mkdir(str(self.lock_dir))
                self._write_owner()
                return
            except FileExistsError:
                owner = self._owner()
                young = owner is None and time.time() - os.stat(str(self.lock_dir)).st_mtime < CLAIM_GRACE_SECONDS
                if owner and self._orphan(owner):
                    self._kill_orphan(owner)
                elif young or (owner and self._running(owner)):
                    raise EnvironmentRefused("the %s WireMock is owned by run %s (pid %s); the harness does not start a second one"
                        % (self.platform, owner["runId"] if owner else "unknown", owner["pid"] if owner else "unknown"))
                shutil.rmtree(str(self.lock_dir), ignore_errors=True)
        raise EnvironmentRefused("could not claim the WireMock lock %s" % self.lock_dir)

    @staticmethod
    def _kill_orphan(owner):
        for signum in (signal.SIGTERM, signal.SIGKILL):
            try:
                os.killpg(owner["pgid"], signum)
            except (ProcessLookupError, PermissionError):
                return
            deadline = time.monotonic() + KILL_GRACE_SECONDS
            while time.monotonic() < deadline and _alive(owner["pid"]):
                time.sleep(0.1)
            if not _alive(owner["pid"]):
                return

    def _release(self):
        owner = self._owner()
        if owner is not None and owner.get("harnessPid") == os.getpid():
            shutil.rmtree(str(self.lock_dir), ignore_errors=True)

    def _listening(self):
        with socket.socket() as probe:
            probe.settimeout(2)
            return probe.connect_ex(("127.0.0.1", self.port)) == 0

    def _listener_description(self):
        try:
            out = subprocess.run(["lsof", "-nP", "-iTCP:%d" % self.port, "-sTCP:LISTEN"], stdout=subprocess.PIPE,
                stderr=subprocess.DEVNULL, universal_newlines=True, timeout=15).stdout.strip()
        except (OSError, subprocess.SubprocessError):
            out = ""
        return out.splitlines()[-1] if out else "(lsof reports nothing)"

    # -- lifecycle ---------------------------------------------------------------------------

    def claim(self):
        """Takes the platform's WireMock lock without starting anything: the run does it before it touches the device."""
        if not self.fake_dir and not self.claimed:
            self._claim()
            self.claimed = True

    def start(self):
        if self.fake_dir:
            return
        self.claim()
        try:
            if self._listening():
                raise EnvironmentRefused("port %d already has a listener the harness did not start; it is never killed. "
                    "Stop it yourself or choose another port.\n%s" % (self.port, self._listener_description()))
            self._spawn()
        except BaseException:
            self.stop()
            raise

    def _spawn(self):
        env = dict(self.config.env)
        env.update({
            "PORT": str(self.port),
            "WIREMOCK_DELAY_PROFILE": self.config.delay_profile,
            "WIREMOCK_GENERATED_ROOT": str(self.state_dir / "generated"),
        })
        custom = self.config.seam("E2E_FAKE_WIREMOCK_START_CMD")
        command = shlex.split(custom) if custom else ["bash", str(self.config.layout_path("E2E_MOCK_START_SCRIPT"))]
        with open(self.log_path, "ab") as log:  # append: a second start in the same run directory keeps the first log
            self.process = subprocess.Popen(command, cwd=str(self.config.root), env=env, stdout=log,
                stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL, start_new_session=True)
        self._write_owner(self.process.pid, self.process.pid)
        deadline = time.monotonic() + READY_SECONDS
        while time.monotonic() < deadline:
            if self.process.poll() is not None:
                break
            if self.health()[0]:
                return
            time.sleep(0.5)
        with open(self.log_path, errors="replace") as handle:
            tail = "\n".join(handle.read().splitlines()[-40:])
        raise EnvironmentRefused("WireMock did not become ready on port %d:\n%s" % (self.port, tail))

    def restart(self):
        """Stops this run's WireMock and starts a new one on the same port and lock; the device is not touched."""
        self.stop()
        self.start()

    def health(self):
        if self.fake_dir:
            down = os.path.exists(os.path.join(self.fake_dir, "down"))
            return (not down, "fake WireMock is down" if down else "ok")
        try:
            with urllib.request.urlopen(self.base_url + "/__admin", timeout=3) as response:
                return (200 <= response.status < 300, "HTTP %d" % response.status)
        except Exception as error:
            return (False, str(error))

    def journal(self):
        """The request journal as WireMock lists it; an empty list when unreadable, with the reason in `journal_error`."""
        self.journal_error = None
        try:
            if self.fake_dir:
                with open(os.path.join(self.fake_dir, "journal.json")) as handle:
                    text = handle.read()
            else:
                with urllib.request.urlopen(self.base_url + "/__admin/requests", timeout=10) as response:
                    text = response.read().decode("utf-8")
            return json.loads(text).get("requests", [])
        except (OSError, ValueError, AttributeError) as error:
            self.journal_error = "the WireMock journal could not be read: %s" % error
            return []

    def stop(self):
        process, self.process = self.process, None
        if process is not None and process.poll() is None:
            kill_group(process, KILL_GRACE_SECONDS)
        elif process is not None:
            try:
                os.killpg(process.pid, signal.SIGTERM)
            except (ProcessLookupError, PermissionError):
                pass
        if not self.fake_dir:
            self._release()
            self.claimed = False
