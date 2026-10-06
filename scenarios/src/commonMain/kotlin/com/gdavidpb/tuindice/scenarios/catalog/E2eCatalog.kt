package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.scenariokit.model.CatalogAccount
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.ScenarioCatalog
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts

object E2eCatalog {
	/** Every scenario, listed explicitly; a scenario left out of this list never runs. */
	val all: List<Scenario> = listOf(
		authLoginCancel,
		evaluationsSwipeDelete,
		summaryProfilePicture,
		pocExpectedFailure
	)

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
