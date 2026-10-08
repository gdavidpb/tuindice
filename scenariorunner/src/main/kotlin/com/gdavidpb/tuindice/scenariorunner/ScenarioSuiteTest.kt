package com.gdavidpb.tuindice.scenariorunner

import com.gdavidpb.tuindice.scenariokit.engine.ScenarioRunner
import com.gdavidpb.tuindice.scenariorunner.driver.FailureArtifacts
import com.gdavidpb.tuindice.scenariorunner.driver.UiAutomatorScenarioDriver
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** One test per catalog scenario, named `run[<id>]`; a failed scenario fails the test with its report. */
@RunWith(Parameterized::class)
class ScenarioSuiteTest(private val scenarioId: String) {
	@Test
	fun run() {
		val driver = UiAutomatorScenarioDriver()
		driver.beginScenario(scenarioId)

		val outcome = ScenarioRunner.run(CatalogAsset.json, scenarioId, driver)
		FailureArtifacts.writeResult(scenarioId, outcome.resultJson)

		if (!outcome.passed) throw AssertionError(outcome.report)
	}

	companion object {
		@JvmStatic
		@Parameterized.Parameters(name = "{0}")
		fun ids(): List<String> = CatalogAsset.selectedIds()
	}
}
