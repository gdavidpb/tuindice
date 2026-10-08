"""The iOS crash-probe waits for a crash report only when the app can have crashed (C-10): a scenario that failed with the
app alive must not cost the 15 s the harness asks for on every failed attempt."""

import json
import os
import threading
import time
import unittest

import support  # puts e2e/scripts/shared on sys.path
from test_adapters import Adapter, crash_report


class IosCrashProbeWaitTests(unittest.TestCase):
    WAIT = "30"

    def setUp(self):
        self.box = Adapter(self, "ios")
        self.attempt = os.path.join(self.box.dir, "attempt")
        os.makedirs(self.attempt)
        self.since = str(int(time.time()) - 60)
        self.until = str(int(time.time()) + 600)

    def result(self, failure_kind=None):
        failure = {"kind": failure_kind, "stepIndex": 3} if failure_kind else None
        with open(os.path.join(self.attempt, "result.json"), "w") as handle:
            json.dump({"scenarioId": "x", "outcome": "failed", "steps": [], "failure": failure}, handle)

    def probe(self):
        began = time.monotonic()
        done = self.box.run("crash-probe", self.since, self.attempt, self.until, self.WAIT)
        return done, time.monotonic() - began

    def late_report(self, delay=1.5):
        name = time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()) + ".1 +0000"
        timer = threading.Timer(delay, crash_report, [self.box.reports, "TuIndiceHost-late.ips", name])
        timer.start()
        self.addCleanup(timer.cancel)

    def test_a_scenario_that_failed_with_the_app_alive_does_not_wait_for_a_report(self):
        for kind in ("STEP_TIMEOUT", "ASSERTION", "TYPED_TEXT_MISMATCH"):
            self.result(kind)
            done, elapsed = self.probe()
            self.assertEqual(done.returncode, 0, done.stderr)
            self.assertEqual(done.json["kind"], "none")
            self.assertIn("not waiting", done.stderr)
            self.assertLess(elapsed, 20, "%s: it must not spend the %s s of the wait" % (kind, self.WAIT))

    def test_a_scenario_that_failed_because_the_app_was_not_running_waits_for_the_report(self):
        self.result("APP_NOT_RUNNING")
        self.late_report()
        done, _ = self.probe()
        self.assertEqual(done.json["kind"], "app_crash", done.stderr)
        self.assertNotIn("not waiting", done.stderr)

    def test_an_attempt_without_a_readable_result_waits_for_the_report(self):
        self.late_report()
        done, _ = self.probe()  # the runner was killed or died: there is no result.json
        self.assertEqual(done.json["kind"], "app_crash", done.stderr)
        with open(os.path.join(self.attempt, "result.json"), "w") as handle:
            handle.write("{not json")
        os.remove(os.path.join(self.box.reports, "TuIndiceHost-late.ips"))
        self.late_report()
        done, _ = self.probe()
        self.assertEqual(done.json["kind"], "app_crash", done.stderr)


if __name__ == "__main__":
    unittest.main()
