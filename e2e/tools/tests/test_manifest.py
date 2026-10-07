"""Manifest, JUnit counts and the certification copy."""

import os
import unittest
import xml.etree.ElementTree as ET

from support import Workspace, scenario


def suite_of(ws, index=-1):
    return ET.parse(os.path.join(ws.run_dirs()[index], "junit.xml")).getroot()


class ManifestTests(unittest.TestCase):
    def test_the_manifest_is_written_on_failure(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-b": ["fail:assertion"]}})
        self.assertEqual(ws.evidence(E2E_MAX_RETRIES="0").code, 1)
        manifest = ws.manifest()
        self.assertEqual((manifest["outcome"], manifest["exitCode"], manifest["mode"], manifest["platform"]),
            ("failed", 1, "evidence", "ios"))
        self.assertEqual(manifest["fingerprint"], "a" * 64)
        self.assertEqual(manifest["retryPolicy"], {"maxRetries": 0, "nonRetryable": ["typed_text_mismatch", "app_crash"]})
        self.assertEqual(manifest["scenarios"]["inScope"], 2)
        self.assertEqual((manifest["scenarios"]["passed"], manifest["scenarios"]["failed"]), (1, 1))
        self.assertEqual([p["name"] for p in manifest["phases"]],
            ["toolchain", "env-check", "device", "wiremock", "build", "install", "enumerate", "scenarios"])
        self.assertIn("ncpu", manifest["host"])
        self.assertEqual(manifest["budget"]["minutes"], 120)
        self.assertEqual({r["id"]: r["status"] for r in manifest["results"]}, {"fix-a": "passed", "fix-b": "failed"})
        self.assertEqual(manifest["results"][1]["failureClasses"], ["product_assertion"])

    def test_the_manifest_records_a_precondition_failure_too(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence(E2E_FAKE_SCOPE_SUITE="other-suite", E2E_PUBLISH_GITHUB_STATUS="auto").code, 2)
        manifest = ws.manifest()
        self.assertEqual((manifest["outcome"], manifest["exitCode"]), ("failed", 2))
        self.assertIn("the scope requires the status", manifest["stop"]["reason"])

    def test_a_green_platform_leaves_a_certification_copy_and_an_index(self):
        ws = Workspace(self, [scenario("fix-a")])
        self.assertEqual(ws.evidence().code, 0)
        sha = ws.manifest()["commitSha"]
        copy = os.path.join(ws.state, "certifications", sha, "ios", "manifest.json")
        self.assertEqual(ws.read(copy)["outcome"], "passed")
        index = ws.read(os.path.join(ws.state, "ledger", "ios", "index.json"))
        self.assertEqual((index[0]["fingerprint"], index[0]["completeAtSha"]), ("a" * 64, sha))

    def test_run_directories_never_collide(self):
        ws = Workspace(self, [scenario("fix-a")])
        for _ in range(3):
            ws.evidence(E2E_FAKE_FP="c" * 64)
        self.assertEqual(len(set(ws.run_dirs())), len(ws.run_dirs()))
        self.assertEqual(len(ws.run_dirs()), 3)


class JunitTests(unittest.TestCase):
    def test_counts_are_real(self):
        quarantined = scenario("fix-q", quarantine={"reason": "flaky", "until": "2999-01-01"})
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b"), scenario("fix-c"), scenario("fix-d"), scenario("fix-e"), quarantined],
            {"behaviours": {"fix-b": ["fail:assertion"], "fix-c": ["fail:driver"], "fix-d": ["fail:typed"]}})
        self.assertEqual(ws.evidence(E2E_MAX_RETRIES="0").code, 5)
        root = suite_of(ws)
        self.assertEqual(root.get("name"), "local-certification-suite.ios")
        counts = {name: root.get(name) for name in ("tests", "failures", "errors", "skipped")}
        self.assertEqual(counts, {"tests": "6", "failures": "2", "errors": "1", "skipped": "2"})
        by_name = {case.get("name"): [child.tag for child in case] for case in root}
        self.assertEqual(by_name["fix-a"], [])
        self.assertEqual(by_name["fix-b"], ["failure"])
        self.assertEqual(by_name["fix-c"], ["error"])
        self.assertEqual(by_name["fix-d"], ["failure"])
        self.assertEqual(by_name["fix-e"], ["skipped"])
        self.assertEqual(by_name["fix-q"], ["skipped"])

    def test_greens_from_earlier_runs_carry_their_run_id(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-b": ["fail:assertion", "pass"]}})
        self.assertEqual(ws.evidence(E2E_MAX_RETRIES="0").code, 1)
        first_run = ws.manifest()["runId"]
        self.assertEqual(ws.evidence().code, 0)
        root = suite_of(ws)
        self.assertEqual((root.get("tests"), root.get("failures"), root.get("errors"), root.get("skipped")), ("2", "0", "0", "0"))
        properties = {case.get("name"): [p.get("value") for p in case.iter("property")] for case in root}
        self.assertEqual(properties["fix-a"], [first_run])
        self.assertEqual(properties["fix-b"], [])


if __name__ == "__main__":
    unittest.main()
