"""The ledger is keyed by platform, fingerprint and catalog; anything else is not read."""

import fcntl
import os
import shutil
import unittest

from support import FP_A, FP_B, Workspace, scenario


def ledger_dir(ws, platform, fingerprint):
    return os.path.join(ws.state, "ledger", platform, fingerprint)


class LedgerKeyTests(unittest.TestCase):
    def test_a_ledger_of_another_fingerprint_is_not_read(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence().code, 0)
        self.assertEqual(ws.executed(), ["fix-a"])
        again = ws.evidence(E2E_FAKE_FP=FP_B)
        self.assertEqual(again.code, 0, again.out)
        self.assertEqual(ws.executed(), ["fix-a", "fix-a"], "a new fingerprint starts from nothing")
        self.assertTrue(os.path.exists(os.path.join(ledger_dir(ws, "ios", FP_A), "ledger.json")))
        self.assertTrue(os.path.exists(os.path.join(ledger_dir(ws, "ios", FP_B), "ledger.json")))

    def test_a_ledger_that_names_another_fingerprint_is_refused(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence().code, 0)
        os.makedirs(ledger_dir(ws, "ios", FP_B))
        shutil.copy(os.path.join(ledger_dir(ws, "ios", FP_A), "ledger.json"), os.path.join(ledger_dir(ws, "ios", FP_B), "ledger.json"))
        result = ws.evidence(E2E_FAKE_FP=FP_B)
        self.assertEqual(result.code, 2, result.out)
        self.assertIn("it is not read", result.out)
        self.assertEqual(ws.executed(), ["fix-a"])

    def test_a_ledger_of_another_platform_is_refused(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence("android").code, 0)
        os.makedirs(ledger_dir(ws, "ios", FP_A))
        shutil.copy(os.path.join(ledger_dir(ws, "android", FP_A), "ledger.json"), os.path.join(ledger_dir(ws, "ios", FP_A), "ledger.json"))
        result = ws.evidence("ios")
        self.assertEqual(result.code, 2, result.out)
        self.assertEqual(ws.executed("ios"), [])

    def test_the_same_fingerprint_with_another_catalog_is_refused(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence().code, 0)
        ws.write_catalog(scenario("fix-a"), scenario("fix-b"))
        result = ws.evidence()
        self.assertEqual(result.code, 2, result.out)
        self.assertIn("it is not read", result.out)
        self.assertEqual(ws.executed(), ["fix-a"])

    def test_a_second_run_on_the_same_ledger_exits_3(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence().code, 0)
        descriptor = os.open(os.path.join(ledger_dir(ws, "ios", FP_A), "ledger.lock"), os.O_RDWR)
        self.addCleanup(os.close, descriptor)
        fcntl.flock(descriptor, fcntl.LOCK_EX | fcntl.LOCK_NB)
        os.remove(os.path.join(ledger_dir(ws, "ios", FP_A), "ledger.json"))
        result = ws.evidence()
        self.assertEqual(result.code, 3, result.out)
        self.assertIn("another run holds the ledger", result.out)

    def test_writes_are_atomic_and_leave_no_temporary_file(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")])
        self.assertEqual(ws.evidence().code, 0)
        self.assertEqual(sorted(os.listdir(ledger_dir(ws, "ios", FP_A))), ["ledger.json", "ledger.lock"])
        data = ws.ledger()
        self.assertEqual((data["platform"], data["fingerprint"]), ("ios", FP_A))
        self.assertEqual(len(data["catalogSha256"]), 64)
        attempt = data["scenarios"]["fix-a"]["attempts"][0]
        for key in ("sha", "durationMs", "failureClass", "failureSummary", "countsAgainstCap", "artifacts", "load"):
            self.assertIn(key, attempt)


if __name__ == "__main__":
    unittest.main()
