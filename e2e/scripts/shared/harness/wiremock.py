"""WireMock as the harness sees it: start, health and the request journal.

The interpreter inside each runner resets the backend and fixes the mock states; the
harness never resets. F15 adds the ownership lock and the foreign-listener check around
`start`; with E2E_FAKE_WIREMOCK_DIR set nothing is started and the "server" is a directory
(`journal.json` is its journal, a `down` file makes it unhealthy).
"""

import json
import os
import signal
import subprocess
import time
import urllib.request

from .config import EnvironmentRefused
from .proc import KILL_GRACE_SECONDS, kill_group


class WireMock:
    def __init__(self, config, platform, run_dir):
        self.config = config
        self.port = config.ports[platform]
        self.run_dir = run_dir
        self.fake_dir = config.seam("E2E_FAKE_WIREMOCK_DIR")
        self.process = None
        self.log_path = os.path.join(str(run_dir), "wiremock.log")

    @property
    def base_url(self):
        return "http://127.0.0.1:%d" % self.port

    def start(self):
        if self.fake_dir:
            return
        env = dict(self.config.env)
        env.update({
            "PORT": str(self.port),
            "WIREMOCK_DELAY_PROFILE": self.config.delay_profile,
            "WIREMOCK_GENERATED_ROOT": str(self.config.tmp_root / "wiremock"),
        })
        script = str(self.config.layout_path("E2E_MOCK_START_SCRIPT"))
        log = open(self.log_path, "ab")
        self.process = subprocess.Popen(
            ["bash", script], cwd=str(self.config.root), env=env, stdout=log, stderr=subprocess.STDOUT,
            stdin=subprocess.DEVNULL, start_new_session=True,
        )
        deadline = time.monotonic() + 45
        while time.monotonic() < deadline:
            if self.process.poll() is not None:
                break
            if self.health()[0]:
                return
            time.sleep(0.5)
        tail = "\n".join(open(self.log_path, errors="replace").read().splitlines()[-40:])
        self.stop()
        raise EnvironmentRefused("WireMock did not become ready on port %d:\n%s" % (self.port, tail))

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
        """The request journal as WireMock lists it; an empty list when unreadable."""
        try:
            if self.fake_dir:
                text = open(os.path.join(self.fake_dir, "journal.json")).read()
            else:
                with urllib.request.urlopen(self.base_url + "/__admin/requests", timeout=10) as response:
                    text = response.read().decode("utf-8")
            return json.loads(text).get("requests", [])
        except (OSError, ValueError, AttributeError):
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
