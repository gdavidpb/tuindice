"""The evidence verdict of `e2e.py status`: current, reusable, unpublished, partial, rerun, exhausted."""

import json
import os
import subprocess
import unittest

from support import FP_A, FP_B, Workspace, scenario

CONTEXT = "local-e2e/ios/local-certification-suite"


def three():
    return [scenario("fix-a"), scenario("fix-b"), scenario("fix-c")]


def commit(ws, message):
    subprocess.run(["git", "commit", "-q", "--allow-empty", "-m", message], cwd=ws.repo, check=True)
    return head(ws)


def head(ws):
    return subprocess.run(["git", "rev-parse", "HEAD"], cwd=ws.repo, stdout=subprocess.PIPE, universal_newlines=True,
        check=True).stdout.strip()


def success(fingerprint=FP_A, creator="owner", state="success", context=CONTEXT):
    return {"context": context, "state": state, "creator": {"login": creator},
        "description": "Local E2E ios 3/3 passed for 1234567 fp %s." % fingerprint[:12]}


class VerdictTests(unittest.TestCase):
    def setUp(self):
        self.ws = Workspace(self, three())
        self.statuses = os.path.join(self.ws.dir, "statuses")
        os.makedirs(self.statuses)
        self.ws.env.update(E2E_FAKE_GH_STATUSES_DIR=self.statuses)
        self.ws.git("branch", "production")

    def remote(self, sha, *statuses):
        with open(os.path.join(self.statuses, sha + ".json"), "w") as handle:
            json.dump(list(statuses), handle)

    def status(self, platform="ios", **env):
        result = self.ws.run("status", "--json", **env)
        self.assertEqual(result.code, 0, result.err)
        return json.loads(result.out)["platforms"][platform]

    def green(self, **env):
        self.assertEqual(self.ws.evidence(**env).code, 0)

    def test_the_text_status_says_when_the_history_was_cut_short(self):
        for number in range(105):
            commit(self.ws, "c%d" % number)
        text = self.ws.run("status")
        self.assertEqual(text.code, 0, text.err)
        self.assertIn("the history was cut short", text.out)
        quiet = Workspace(self, three())
        self.assertNotIn("cut short", quiet.run("status").out)

    def test_nothing_green_is_rerun(self):
        info = self.status()
        self.assertEqual(info["verdict"], "rerun")
        self.assertIsNone(info["evidence"])
        self.assertEqual(len(info["pending"]), 3)

    def test_some_green_and_a_failure_that_still_has_attempts_is_partial(self):
        self.ws.write_script({"behaviours": {"fix-b": ["fail:typed"]}})
        self.assertEqual(self.ws.evidence().code, 5)
        info = self.status()
        self.assertEqual(info["verdict"], "partial")
        self.assertEqual(info["green"], ["fix-a"])
        self.assertEqual(info["failed"], [{"id": "fix-b", "class": "typed_text_mismatch", "attempts": 1,
            "exhausted": False, "summary": info["failed"][0]["summary"]}])
        self.assertIn("typed", info["failed"][0]["summary"])

    def test_only_a_failure_and_nothing_green_is_rerun_and_lists_the_failure(self):
        self.ws.write_script({"behaviours": {"fix-a": ["fail:typed"]}})
        self.assertEqual(self.ws.evidence().code, 5)
        info = self.status()
        self.assertEqual(info["verdict"], "rerun")
        self.assertEqual([f["id"] for f in info["failed"]], ["fix-a"])

    def test_a_scenario_that_used_all_its_attempts_is_exhausted(self):
        self.ws.write_script({"behaviours": {"fix-b": ["fail:typed"]}})
        self.assertEqual(self.ws.evidence().code, 5)
        self.assertEqual(self.ws.evidence().code, 5)
        info = self.status()
        self.assertEqual(info["verdict"], "exhausted")
        self.assertEqual(info["exhausted"], ["fix-b"])
        self.assertTrue(info["failed"][0]["exhausted"])

    def test_green_everywhere_without_a_status_is_unpublished(self):
        self.green()
        info = self.status()
        self.assertEqual(info["verdict"], "unpublished")
        self.assertTrue(info["complete"])
        self.assertEqual(info["remote"], {"reachable": True, "checked": 1, "truncated": False, "incomplete": []})

    def test_a_trusted_status_naming_the_fingerprint_on_head_is_current(self):
        self.remote(head(self.ws), success())
        info = self.status()
        self.assertEqual(info["verdict"], "current")
        self.assertEqual(info["evidence"], {"sha": head(self.ws), "source": "remote"})

    def test_the_remote_wins_over_an_incomplete_ledger(self):
        self.ws.write_script({"behaviours": {"fix-b": ["fail:typed"]}})
        self.assertEqual(self.ws.evidence().code, 5)
        self.remote(head(self.ws), success())
        self.assertEqual(self.status()["verdict"], "current")

    def test_a_status_on_an_earlier_branch_commit_is_reusable(self):
        first = commit(self.ws, "first")
        self.remote(first, success())
        commit(self.ws, "second")
        info = self.status()
        self.assertEqual(info["verdict"], "reusable")
        self.assertEqual(info["evidence"], {"sha": first, "source": "remote"})

    def test_a_status_on_the_base_itself_is_reusable(self):
        base = head(self.ws)
        self.remote(base, success())
        commit(self.ws, "first")
        self.assertEqual(self.status()["evidence"], {"sha": base, "source": "remote"})
        self.assertEqual(self.status()["verdict"], "reusable")

    def test_a_status_before_the_base_is_not_a_candidate(self):
        before = head(self.ws)
        self.remote(before, success())
        commit(self.ws, "advance production past it")
        self.ws.git("branch", "-f", "production", "HEAD")
        commit(self.ws, "first")
        self.assertEqual(self.status()["verdict"], "rerun")

    def test_an_untrusted_creator_a_foreign_fingerprint_or_a_non_success_do_not_count(self):
        sha = head(self.ws)
        for status in (success(creator="stranger"), success(fingerprint=FP_B), success(state="failure"),
                success(context="local-e2e/android/local-certification-suite")):
            self.remote(sha, status)
            self.assertEqual(self.status()["verdict"], "rerun", status)

    def test_the_bot_is_a_trusted_creator(self):
        self.remote(head(self.ws), success(creator="github-actions[bot]"))
        self.assertEqual(self.status()["verdict"], "current")

    def test_the_trusted_creators_are_the_ones_the_preflight_reads(self):
        # D-7: E2E_TRUSTED_STATUS_CREATORS replaces the default list for the verdict exactly as for the preflight.
        self.remote(head(self.ws), success(creator="release-bot"))
        self.assertEqual(self.status()["verdict"], "rerun")
        self.assertEqual(self.status(E2E_TRUSTED_STATUS_CREATORS="release-bot")["verdict"], "current")
        self.remote(head(self.ws), success(creator="owner"))
        self.assertEqual(self.status(E2E_TRUSTED_STATUS_CREATORS="release-bot,other")["verdict"], "rerun")
        # An empty value (an unset repository variable in a workflow) keeps the default: the owner.
        self.assertEqual(self.status(E2E_TRUSTED_STATUS_CREATORS="")["verdict"], "current")

    def test_the_base_is_origin_production_when_both_exist(self):
        # D-7: every tool reads origin/production first (the local branch may lag). A status on a commit that lies
        # between the two bases is a candidate only under origin/production... the other way round it is not.
        old = head(self.ws)
        ahead = commit(self.ws, "ahead of the local production")
        self.remote(old, success())
        self.ws.git("update-ref", "refs/remotes/origin/production", ahead)
        commit(self.ws, "first")
        self.assertEqual(self.status()["verdict"], "rerun")  # `old` lies before origin/production
        self.ws.git("update-ref", "-d", "refs/remotes/origin/production")
        self.assertEqual(self.status()["evidence"], {"sha": old, "source": "remote"})  # only `production` is left

    def test_only_the_newest_status_of_the_context_counts(self):
        self.remote(head(self.ws), success(state="failure"), success())
        self.assertEqual(self.status()["verdict"], "rerun")
        self.remote(head(self.ws), success(), success(state="failure"))
        self.assertEqual(self.status()["verdict"], "current")

    def test_the_two_platforms_have_their_own_verdicts(self):
        self.remote(head(self.ws), success())
        data = json.loads(self.ws.run("status", "--json").out)["platforms"]
        self.assertEqual((data["ios"]["verdict"], data["android"]["verdict"]), ("current", "rerun"))

    def test_a_ledger_publication_is_not_evidence_while_the_remote_can_be_read(self):
        # A published record without the status on GitHub (deleted, or never landed) is only `unpublished`.
        self.green(E2E_PUBLISH_GITHUB_STATUS="auto")
        self.assertEqual(len(self.ws.ledger()["publications"]), 1)
        self.assertEqual(self.status()["verdict"], "unpublished")

    def test_with_github_unreachable_the_ledgers_publications_stand_in(self):
        self.green(E2E_PUBLISH_GITHUB_STATUS="auto")
        published = head(self.ws)
        info = self.status(E2E_FAKE_GH_UNREACHABLE="1")
        self.assertEqual((info["verdict"], info["evidence"], info["remote"]["reachable"]),
            ("current", {"sha": published, "source": "ledger"}, False))
        commit(self.ws, "later")
        info = self.status(E2E_FAKE_GH_UNREACHABLE="1")
        self.assertEqual((info["verdict"], info["evidence"]), ("reusable", {"sha": published, "source": "ledger"}))

    def test_without_an_owner_the_remote_counts_as_unreachable(self):
        self.green()
        self.assertEqual(self.status(E2E_FAKE_GH_OWNER="")["remote"]["reachable"], False)
        self.assertEqual(self.status(E2E_FAKE_GH_OWNER="")["verdict"], "unpublished")

    def test_each_commit_is_asked_about_once_and_a_hit_on_head_asks_for_no_older_one(self):
        commit(self.ws, "first")
        commit(self.ws, "second")
        self.remote(head(self.ws), success())
        open(self.ws.gh_log, "w").close()
        self.assertEqual(self.status()["verdict"], "current")
        asked = [line for line in open(self.ws.gh_log).read().splitlines() if line.endswith("/statuses")]
        # HEAD, then (for Android, which has nothing there) the two older commits; iOS reuses what was fetched.
        self.assertEqual(len(asked), 3, asked)
        self.assertEqual(len(set(asked)), len(asked), "a commit was fetched twice")

    def test_a_head_github_does_not_know_is_unreachable_not_rerun(self):
        # An unpushed HEAD answers 422: nothing was asked successfully, so the ledger is the only source.
        self.green()
        self.assertEqual(self.status(E2E_FAKE_GH_MISSING_SHAS=head(self.ws))["remote"]["reachable"], False)
        self.assertEqual(self.status(E2E_FAKE_GH_MISSING_SHAS=head(self.ws))["verdict"], "unpublished")

    def test_a_failed_query_for_one_commit_does_not_hide_evidence_on_another(self):
        first = commit(self.ws, "first")
        self.remote(first, success())
        commit(self.ws, "second")
        info = self.status(E2E_FAKE_GH_FAIL_SHAS=head(self.ws))
        self.assertEqual((info["verdict"], info["evidence"]), ("reusable", {"sha": first, "source": "remote"}))

    def test_a_failed_query_for_the_candidate_that_holds_the_evidence_is_incomplete_not_rerun(self):
        first = commit(self.ws, "first")
        self.remote(first, success())
        second = commit(self.ws, "second")
        info = self.status(E2E_FAKE_GH_FAIL_SHAS=first)
        self.assertEqual((info["verdict"], info["evidence"]), ("incomplete", None))
        self.assertEqual(info["remote"]["incomplete"], [first])
        self.assertTrue(info["remote"]["reachable"])  # other commits did answer, and that is not enough to say "absent"
        self.assertNotIn(second, info["remote"]["incomplete"])
        text = self.ws.run("status", E2E_FAKE_GH_FAIL_SHAS=first)
        self.assertIn("ios: incomplete;", text.out)
        self.assertIn("GitHub did not answer for 1 commit(s) after asking twice: %s" % first[:12], text.out)

    def test_a_failed_query_is_asked_once_more_and_only_then_given_up(self):
        first = commit(self.ws, "first")
        open(self.ws.gh_log, "w").close()
        self.status(E2E_FAKE_GH_FAIL_SHAS=first)
        with open(self.ws.gh_log) as handle:
            asked = [line for line in handle.read().splitlines() if line.endswith("commits/%s/statuses" % first)]
        self.assertEqual(len(asked), 2, "asked once and once more, not a third time")

    def test_a_query_that_answers_on_the_second_try_is_a_normal_answer(self):
        first = commit(self.ws, "first")
        self.remote(first, success())
        commit(self.ws, "second")
        info = self.status(E2E_FAKE_GH_FAIL_ONCE_SHAS=first)
        self.assertEqual((info["verdict"], info["evidence"]), ("reusable", {"sha": first, "source": "remote"}))
        self.assertEqual(info["remote"]["incomplete"], [])

    def test_a_commit_github_does_not_know_is_not_incomplete(self):
        self.green()
        self.assertEqual(self.status(E2E_FAKE_GH_MISSING_SHAS=head(self.ws))["remote"]["incomplete"], [])

    def test_a_hit_still_counts_when_another_lookup_failed(self):
        first = commit(self.ws, "first")
        second = commit(self.ws, "second")
        self.remote(second, success())
        info = self.status(E2E_FAKE_GH_FAIL_SHAS=first)
        self.assertEqual((info["verdict"], info["evidence"]), ("current", {"sha": second, "source": "remote"}))

    def test_the_text_form_names_the_verdict(self):
        self.remote(head(self.ws), success())
        lines = self.ws.run("status").lines
        self.assertTrue(lines[0].startswith("android: rerun; fp "), lines)
        self.assertTrue(lines[1].startswith("ios: current; fp "), lines)

    def test_status_reports_the_git_facts_the_inspector_needs(self):
        data = json.loads(self.ws.run("status", "--json").out)
        self.assertEqual(data["head"], head(self.ws))
        self.assertEqual(data["upstream"], head(self.ws))
        self.assertTrue(data["treeClean"])
        self.assertTrue(data["headEqualsUpstream"])
        commit(self.ws, "unpushed")
        data = json.loads(self.ws.run("status", "--json").out)
        self.assertFalse(data["headEqualsUpstream"])


if __name__ == "__main__":
    unittest.main()
