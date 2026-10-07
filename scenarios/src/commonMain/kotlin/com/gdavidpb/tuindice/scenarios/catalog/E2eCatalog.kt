package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.scenariokit.model.CatalogAccount
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.ScenarioCatalog
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts

object E2eCatalog {
	/**
	 * The scenarios of every module, one list per module file under `catalog/`. A module's list is the
	 * only thing a scenario of that module has to be added to; `ModuleCatalogTest` checks each scenario
	 * sits in the list of its own module.
	 */
	val byModule: Map<String, List<Scenario>> = mapOf(
		"auth" to authScenarios,
		"maincore" to maincoreScenarios,
		"coachmarks" to coachmarksScenarios,
		"summary" to summaryScenarios,
		"record" to recordScenarios,
		"enrollmentproof" to enrollmentproofScenarios,
		"pensum" to pensumScenarios,
		"subjects" to subjectsScenarios,
		"evaluations" to evaluationsScenarios,
		"about" to aboutScenarios,
		"poc" to pocScenarios
	)

	/** Every scenario; one left out of its module's list never runs. */
	val all: List<Scenario> = byModule.values.flatten()

	fun catalog(): ScenarioCatalog = ScenarioCatalog(
		accounts = E2eAccounts.all.map(::toCatalogAccount),
		scenarios = all,
		contractFixture = E2eContractFixture.fixture
	)

	private fun toCatalogAccount(account: E2eAccount) = CatalogAccount(
		id = account.id,
		usbId = account.usbIdFormatted,
		password = account.password,
		sessionId = account.session?.sessionId,
		accessToken = account.session?.accessToken,
		refreshToken = account.session?.refreshToken,
		mockScenario = account.mockScenario
	)
}
