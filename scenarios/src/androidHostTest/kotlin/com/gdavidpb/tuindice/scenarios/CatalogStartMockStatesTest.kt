package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The WireMock states a scenario starts with besides its account's are the listed ones and only theirs
 * (`CatalogStartTest` compares every seeded start with its account's state plus this list); here, that each listed
 * scenario exists, starts seeded and still holds them.
 */
class CatalogStartMockStatesTest {
	private val scenarios = E2eCatalog.all.associateBy { it.id }

	@Test
	fun everyListedScenarioExistsStartsSeededAndHoldsItsStates() {
		ExtraMockStatesAtStart.scenarioIds.forEach { id ->
			val scenario = scenarios[id]

			assertTrue(scenario != null, "the list names '$id', which is not in the catalog")
			assertTrue(DebugLaunchArguments.SEED_SESSION_ID in scenario.start.arguments, "$id does not start seeded")

			val held = scenario.start.mockStates.map { it.scenario to it.state }

			assertTrue(ExtraMockStatesAtStart.of(id).all { it in held }, "$id lost its states: $held")
		}
	}
}
