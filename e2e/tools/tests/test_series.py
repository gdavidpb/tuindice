"""The count of a `--repeat` series: environment failures are not product failures, and the summary says how many runs were valid."""

import unittest

from support import Workspace, scenario


class SeriesCountTests(unittest.TestCase):
    def test_a_series_with_an_environment_failure_counts_valid_and_environment_runs_apart(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b")], {"behaviours": {"fix-b": ["env", "pass"]}})
        result = ws.diagnose("ios", "--repeat", "3")
        self.assertEqual(result.code, 0, result.out)
        self.assertIn("SERIES 3 repetitions, 6 scenario runs: 6 valid (6 passed, 0 failed), 0 lost to the environment; "
            "1 environment attempt was rerun", result.out)
        series = ws.manifest()["series"]
        self.assertEqual((series["scenarioRuns"], series["valid"], series["passed"], series["failed"], series["environment"],
            series["environmentAttempts"]), (6, 6, 6, 0, 0, 1))

    def test_a_survey_run_that_could_not_be_measured_is_environment_not_a_failure_of_the_product(self):
        ws = Workspace(self, [scenario("fix-a"), scenario("fix-b"), scenario("fix-c")],
            {"behaviours": {"fix-a": ["env"], "fix-b": ["fail:assertion"]}})
        result = ws.diagnose("ios", "--survey", "--repeat", "2")
        self.assertIn("SERIES 2 repetitions, 6 scenario runs: 4 valid (2 passed, 2 failed), 2 lost to the environment; "
            "2 environment attempts were not rerun", result.out)
        self.assertEqual(ws.manifest()["series"]["environment"], 2)

    def test_a_series_without_environment_failures_keeps_the_line_it_had(self):
        ws = Workspace(self, [scenario("fix-a")], {"behaviours": {"fix-a": ["fail:assertion", "pass"]}})
        result = ws.diagnose("ios", "--repeat", "2")
        self.assertIn("REPEAT 2 runs: 1 scenarios failed in at least one", result.out)
        self.assertNotIn("SERIES", result.out)
        self.assertEqual(ws.manifest()["series"]["environmentAttempts"], 0)


if __name__ == "__main__":
    unittest.main()
