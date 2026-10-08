"""Publication of the platform status and its single context."""

import json
import os
import subprocess
import unittest

from support import FP_A, FP_B, Workspace, scenario, text


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
        result = publishing(ws, E2E_FAKE_SCOPE_SUITE="local-legacy-suite")
        self.assertEqual(result.code, 2, result.out)
        self.assertIn("requires the suite local-legacy-suite but this harness can only publish local-certification-suite", result.out)
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


CONTEXT = "local-e2e/ios/local-certification-suite"


def trusted_status(fingerprint=FP_A, creator="owner", state="success"):
    return {"context": CONTEXT, "state": state, "creator": {"login": creator},
        "description": "Local E2E ios 3/3 passed for 1234567 fp %s." % fingerprint[:12]}


class PublishFromAnAncestorTests(unittest.TestCase):
    """`publish` without a complete ledger for the fingerprint: it cites the ancestor that holds trusted evidence for the
    same fingerprint (option C: the workflow of a pull request never writes, so the owner does it from here)."""

    def setUp(self):
        self.ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")])
        self.statuses = os.path.join(self.ws.dir, "statuses")
        os.makedirs(self.statuses)
        self.ws.env.update(E2E_FAKE_GH_STATUSES_DIR=self.statuses)
        self.ws.git("branch", "production")
        self.evidence = self.commit("evidence here")
        self.commit("changed after the evidence")

    def commit(self, message):
        self.ws.git("commit", "-q", "--allow-empty", "-m", message)
        sha = subprocess.run(["git", "rev-parse", "HEAD"], cwd=self.ws.repo, stdout=subprocess.PIPE,
            universal_newlines=True, check=True).stdout.strip()
        self.ws.git("update-ref", "refs/remotes/origin/main", sha)  # pushed: HEAD == @{u}
        return sha

    def remote(self, sha, *statuses):
        with open(os.path.join(self.statuses, sha + ".json"), "w") as handle:
            json.dump(list(statuses), handle)

    def publish(self, **env):
        return self.ws.run("publish", "--platform", "ios", **env)

    def test_the_trusted_ancestor_with_the_same_fingerprint_is_cited_on_head(self):
        self.remote(self.evidence, trusted_status())
        result = self.publish()
        self.assertEqual(result.code, 0, result.out + result.err)
        posts = gh_posts(self.ws)
        self.assertEqual(len(posts), 1, posts)
        head = self.commit_sha()
        self.assertIn("statuses/%s" % head, posts[0])
        self.assertIn("context=%s" % CONTEXT, posts[0])
        self.assertIn("description=Local E2E ios reused from %s fp %s." % (self.evidence[:7], FP_A[:12]), posts[0])
        self.assertEqual(self.ws.calls(), [], "publish never runs the adapter")

    def commit_sha(self):
        return subprocess.run(["git", "rev-parse", "HEAD"], cwd=self.ws.repo, stdout=subprocess.PIPE,
            universal_newlines=True, check=True).stdout.strip()

    def test_a_head_that_already_has_the_evidence_is_not_published_again(self):
        self.remote(self.commit_sha(), trusted_status())
        result = self.publish()
        self.assertEqual(result.code, 0, result.out + result.err)
        self.assertEqual(gh_posts(self.ws), [])
        self.assertIn("already", result.out)

    def test_an_ancestor_with_another_fingerprint_is_not_cited(self):
        self.remote(self.evidence, trusted_status(fingerprint=FP_B))
        result = self.publish()
        self.assertEqual(result.code, 2, result.out)
        self.assertEqual(gh_posts(self.ws), [])
        self.assertIn("2 scenarios are not green", result.err)
        self.assertIn("naming that fingerprint", result.err)

    def test_an_ancestor_created_by_someone_untrusted_is_not_cited(self):
        for creator in ("stranger", "github-actions[bot]"):
            self.remote(self.evidence, trusted_status(creator=creator))
            result = self.publish()
            self.assertEqual(result.code, 2, creator + result.out)
            self.assertEqual(gh_posts(self.ws), [], creator)
            self.assertIn("created by owner", result.err)

    def test_a_failure_status_or_a_lookup_that_failed_cites_nothing(self):
        self.remote(self.evidence, trusted_status(state="failure"))
        self.assertEqual(self.publish().code, 2)
        self.remote(self.evidence, trusted_status())
        failed = self.publish(E2E_FAKE_GH_FAIL_SHAS=self.evidence)
        self.assertEqual(failed.code, 2, failed.out)
        self.assertIn("did not answer", failed.err)
        self.assertEqual(gh_posts(self.ws), [])

    def test_a_failed_precondition_never_reaches_gh(self):
        self.remote(self.evidence, trusted_status())
        with open(os.path.join(self.ws.repo, "stray.txt"), "w") as handle:
            handle.write("x")
        dirty = self.publish()
        self.assertEqual(dirty.code, 2, dirty.out)
        self.assertFalse(os.path.exists(self.ws.gh_log), "gh was called with a dirty tree")
        os.remove(os.path.join(self.ws.repo, "stray.txt"))
        self.ws.git("commit", "-q", "--allow-empty", "-m", "unpushed")
        unpushed = self.publish()
        self.assertEqual(unpushed.code, 2, unpushed.out)
        self.assertIn("HEAD == @{u}", unpushed.err)
        self.assertFalse(os.path.exists(self.ws.gh_log), "gh was called with HEAD ahead of its upstream")


if __name__ == "__main__":
    unittest.main()
