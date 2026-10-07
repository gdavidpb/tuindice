"""Publication of the platform status and its single context."""

import os
import subprocess
import unittest

from support import FP_A, Workspace, scenario, text


def gh_posts(ws):
    if not os.path.exists(ws.gh_log):
        return []
    return [line for line in text(ws.gh_log).splitlines() if line.startswith("api -X POST")]


def publishing(ws, **env):
    return ws.evidence(E2E_PUBLISH_GITHUB_STATUS="auto", **env)


class PublishTests(unittest.TestCase):
    def test_a_green_platform_publishes_one_success_with_the_shared_context(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")])
        result = publishing(ws)
        self.assertEqual(result.code, 0, result.out)
        posts = gh_posts(ws)
        self.assertEqual(len(posts), 1)
        sha = ws.manifest()["commitSha"]
        self.assertIn("statuses/%s" % sha, posts[0])
        self.assertIn("context=local-e2e/ios/local-certification-suite", posts[0])
        self.assertIn("description=Local E2E ios 2/2 passed for %s fp %s." % (sha[:7], FP_A[:12]), posts[0])
        self.assertTrue(ws.manifest()["published"]["ok"])
        self.assertEqual(len(ws.ledger()["publications"]), 1)
        again = publishing(ws)
        self.assertEqual(again.code, 0, again.out)
        self.assertEqual(len(gh_posts(ws)), 1, "a second run must not post the same status again")

    def test_the_context_is_defined_once_and_both_platforms_use_it(self):
        ws = Workspace(self, [scenario("fix-a")])
        shell = {}
        for platform in ("android", "ios"):
            shell[platform] = subprocess.run(
                ["bash", "-c", 'source .github/scripts/common.sh; e2e_status_context "$1"', "_", platform],
                cwd=ws.repo, stdout=subprocess.PIPE, universal_newlines=True, check=True).stdout.strip()
        self.assertEqual(shell["android"], "local-e2e/android/local-certification-suite")
        self.assertNotEqual(shell["android"], shell["ios"])
        listed = ws.run("contexts").lines
        self.assertEqual(listed, ["android %s" % shell["android"], "ios %s" % shell["ios"]])
        self.assertEqual(publishing(ws).code, 0)
        self.assertEqual(ws.evidence("android", E2E_PUBLISH_GITHUB_STATUS="auto").code, 0)
        contexts = sorted(line.split("context=")[1].split(" ")[0] for line in gh_posts(ws))
        self.assertEqual(contexts, sorted(shell.values()))

    def test_a_required_context_the_harness_cannot_publish_exits_2(self):
        ws = Workspace(self, [scenario("fix-a")])
        result = publishing(ws, E2E_FAKE_SCOPE_SUITE="local-maestro-suite")
        self.assertEqual(result.code, 2, result.out)
        self.assertIn("requires the suite local-maestro-suite but this harness can only publish local-certification-suite", result.out)
        self.assertEqual(ws.calls(), [])
        self.assertEqual(gh_posts(ws), [])

    def test_a_failed_publication_exits_6_and_publish_later_never_touches_the_adapter(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")])
        open(ws.gh_fail, "w").close()
        failed = publishing(ws)
        self.assertEqual(failed.code, 6, failed.out)
        self.assertIn("PUBLISH FAILED", failed.out)
        self.assertEqual(ws.manifest()["outcome"], "publish_failed")
        self.assertFalse(ws.manifest()["published"]["ok"])
        self.assertEqual(ws.ledger()["publications"], [])
        self.assertTrue(all(s["status"] == "passed" for s in ws.ledger()["scenarios"].values()))
        calls_before = len(ws.calls())
        os.remove(ws.gh_fail)
        done = ws.run("publish", "--platform", "ios")
        self.assertEqual(done.code, 0, done.out + done.err)
        self.assertEqual(len(ws.calls()), calls_before, "publish must not call the adapter")
        self.assertEqual(len(gh_posts(ws)), 2)
        self.assertEqual(len(ws.ledger()["publications"]), 1)
        rerun = ws.run("publish", "--platform", "ios")
        self.assertEqual(rerun.code, 0)
        self.assertEqual(len(gh_posts(ws)), 2)

    def test_publish_refuses_when_something_is_not_green(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-b": ["fail:assertion"]}})
        self.assertEqual(publishing(ws, E2E_MAX_RETRIES="0").code, 1)
        self.assertEqual(gh_posts(ws), [], "nothing is published while something failed")
        result = ws.run("publish", "--platform", "ios")
        self.assertEqual(result.code, 2)
        self.assertIn("1 scenarios are not green", result.err)

    def test_publishing_needs_head_to_equal_the_upstream(self):
        ws = Workspace(self, [scenario("fix-a")])
        ws.git("commit", "-q", "--allow-empty", "-m", "unpushed")
        result = publishing(ws)
        self.assertEqual(result.code, 2, result.out)
        self.assertIn("HEAD == @{u}", result.out)
        self.assertEqual(ws.calls(), [])

    def test_ledger_only_mode_never_calls_gh(self):
        ws = Workspace(self, [scenario("fix-a")])
        ws.git("commit", "-q", "--allow-empty", "-m", "unpushed")
        self.assertEqual(ws.evidence().code, 0)
        self.assertFalse(os.path.exists(ws.gh_log))

    def test_the_description_counts_retries_and_stays_within_140_characters(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-q", quarantine={"reason": "x", "until": "2999-01-01"})],
            {"behaviours": {"fix-a": ["fail:assertion", "pass"]}})
        self.assertEqual(publishing(ws).code, 0)
        description = ws.ledger()["publications"][0]["description"]
        self.assertTrue(description.endswith(" retried 1. quarantined 1."), description)
        self.assertLessEqual(len(description), 140)


if __name__ == "__main__":
    unittest.main()
