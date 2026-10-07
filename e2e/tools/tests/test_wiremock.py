"""WireMock ownership, foreign listeners and the log (plan F15). A fake server stands in for WireMock."""

import os
import shlex
import shutil
import socket
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

import support  # noqa: F401  (puts the harness on sys.path)
from harness.config import Config, EnvironmentRefused
from harness.wiremock import WireMock

FAKE_SERVER = os.path.join(os.path.dirname(os.path.abspath(__file__)), "fake_wiremock.py")


def free_port():
    with socket.socket() as probe:
        probe.bind(("127.0.0.1", 0))
        return probe.getsockname()[1]


class WireMockCase(unittest.TestCase):
    def setUp(self):
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-wiremock-test-"))
        self.addCleanup(shutil.rmtree, self.dir, True)
        self.ports = {"android": free_port(), "ios": free_port()}
        env = dict(os.environ, E2E_TMP_ROOT=os.path.join(self.dir, "tmp"), E2E_ANDROID_WIREMOCK_PORT=str(self.ports["android"]),
            E2E_IOS_WIREMOCK_PORT=str(self.ports["ios"]),
            E2E_FAKE_WIREMOCK_START_CMD="%s %s RunMockEnvironment" % (shlex.quote(sys.executable), shlex.quote(FAKE_SERVER)))
        env.pop("E2E_FAKE_WIREMOCK_DIR", None)
        self.cfg = Config(Path(self.dir), env)

    def wiremock(self, platform="ios", run="run-1"):
        run_dir = os.path.join(self.dir, run)
        os.makedirs(run_dir, exist_ok=True)
        instance = WireMock(self.cfg, platform, run_dir)
        self.addCleanup(instance.stop)
        return instance

    def owner_file(self, platform="ios"):
        return os.path.join(self.dir, "tmp", platform, "wiremock", "lock", "owner.json")


class OwnershipTests(WireMockCase):
    def test_a_started_wiremock_answers_and_stop_releases_the_lock(self):
        mock = self.wiremock()
        mock.start()
        self.assertTrue(mock.health()[0])
        self.assertTrue(os.path.exists(self.owner_file()))
        mock.stop()
        self.assertFalse(os.path.exists(self.owner_file()))
        self.assertFalse(mock.health()[0])

    def test_a_listener_the_harness_did_not_start_is_never_killed(self):
        foreign = subprocess.Popen([sys.executable, "-c",
            "import socket, time\ns = socket.socket()\ns.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)\n"
            "s.bind(('127.0.0.1', %d))\ns.listen()\nprint('up', flush=True)\ntime.sleep(60)" % self.ports["ios"]],
            stdout=subprocess.PIPE, universal_newlines=True)
        self.addCleanup(foreign.stdout.close)
        self.addCleanup(foreign.wait)
        self.addCleanup(foreign.kill)
        self.assertEqual(foreign.stdout.readline().strip(), "up")
        mock = self.wiremock()
        with self.assertRaises(EnvironmentRefused) as caught:
            mock.start()
        self.assertIn("a listener the harness did not start; it is never killed", str(caught.exception))
        self.assertIsNone(foreign.poll(), "the foreign listener must still be alive")
        self.assertFalse(os.path.exists(self.owner_file()), "a refused start must not keep the lock")

    def test_a_second_run_is_refused_naming_the_owner_and_each_platform_has_its_own_lock(self):
        first = self.wiremock(run="run-first")
        first.start()
        with self.assertRaises(EnvironmentRefused) as caught:
            self.wiremock(run="run-second").start()
        self.assertIn("owned by run run-first", str(caught.exception))
        self.assertTrue(first.health()[0], "the owner's WireMock must be untouched")
        other = self.wiremock("android", run="run-android")
        other.start()
        self.assertTrue(other.health()[0])

    def test_a_stale_lock_is_reclaimed(self):
        dead = subprocess.Popen(["true"])
        dead.wait()
        os.makedirs(os.path.dirname(self.owner_file()))
        with open(self.owner_file(), "w") as handle:
            handle.write('{"pid": %d, "pgid": %d, "port": 1, "runId": "old", "harnessPid": %d}' % (dead.pid, dead.pid, dead.pid))
        mock = self.wiremock()
        mock.start()
        self.assertTrue(mock.health()[0])

    def test_a_live_pid_that_is_not_wiremock_does_not_keep_the_lock(self):
        os.makedirs(os.path.dirname(self.owner_file()))
        with open(self.owner_file(), "w") as handle:  # this very process: alive, but not a RunMockEnvironment
            handle.write('{"pid": %d, "pgid": %d, "port": 1, "runId": "reused", "harnessPid": 1}' % (os.getpid(), os.getpgid(0)))
        mock = self.wiremock()
        mock.start()
        self.assertTrue(mock.health()[0])


class LogTests(WireMockCase):
    def test_the_log_is_not_truncated_by_a_second_start(self):
        mock = self.wiremock()
        mock.start()
        mock.stop()
        again = self.wiremock()  # the same run directory
        again.start()
        again.stop()
        with open(os.path.join(self.dir, "run-1", "wiremock.log")) as handle:
            text = handle.read()
        self.assertEqual(text.count("fake wiremock listening"), 2, text)


if __name__ == "__main__":
    unittest.main()
