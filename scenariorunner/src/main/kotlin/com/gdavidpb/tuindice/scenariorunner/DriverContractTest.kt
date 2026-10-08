package com.gdavidpb.tuindice.scenariorunner

import com.gdavidpb.tuindice.scenariokit.engine.ScenarioRunner
import com.gdavidpb.tuindice.scenariorunner.driver.FailureArtifacts
import com.gdavidpb.tuindice.scenariorunner.driver.UiAutomatorScenarioDriver
import org.junit.Assume.assumeFalse
import org.junit.Test

/** Probes the driver with the catalog's contract fixture; runs only when no scenario filter is given. */
class DriverContractTest {
	@Test
	fun driverHonoursTheContract() {
		assumeFalse("a scenario filter is set", RunConfig.isFiltered)
		val driver = UiAutomatorScenarioDriver()
		driver.beginScenario("driver-contract")

		val outcome = ScenarioRunner.driverContract(CatalogAsset.json, driver)

		FailureArtifacts.writeResult("driver-contract", outcome.resultJson)

		if (!outcome.passed) throw AssertionError(outcome.report)
	}
}
