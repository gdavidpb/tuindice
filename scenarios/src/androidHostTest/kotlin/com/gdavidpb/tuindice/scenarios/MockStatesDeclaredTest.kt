package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A WireMock state the catalog names (at a scenario's start or in a `mockState` step) is a state some mapping of
 * that WireMock scenario declares. WireMock answers 422 to an unknown state and the interpreter reads it as a backend
 * outage, which the harness classifies as the environment: without this check a typo in the catalog would be read as
 * a flaky mock.
 */
class MockStatesDeclaredTest {
	private val mappings: List<Pair<String, JsonObject>> =
		RepoFiles.allMappings.walkTopDown().filter { it.isFile && it.extension == "json" }
			.sortedBy { it.path }.map { it.name to MockJson.obj(it) }.toList()

	@Test
	fun everyStateTheCatalogPutsAMockInIsDeclaredByAMapping() {
		val used = E2eCatalog.all.flatMap { scenario ->
			scenario.start.mockStates.map { it.scenario to it.state } +
				scenario.steps.flattened().filterIsInstance<Step.SetMockState>().map { it.scenario to it.state }
		}

		assertTrue(used.isNotEmpty(), "no mock state found in the catalog; the check would pass for nothing")
		assertEquals(emptyList(), MockRules.undeclaredStates(used, mappings))
	}

	@Test
	fun aStateOrAScenarioNoMappingDeclaresIsCaught() {
		fun mapping(scenario: String, required: String?, next: String?) =
			Json.parseToJsonElement(
				"""{"scenarioName": "$scenario"""" +
					(required?.let { """, "requiredScenarioState": "$it"""" }.orEmpty()) +
					(next?.let { """, "newScenarioState": "$it"""" }.orEmpty()) + "}"
			) as JsonObject

		val declared = listOf(
			"a.json" to mapping("flow", "Unavailable", null),
			"b.json" to mapping("flow", "Unavailable", "Recovered"),
			"c.json" to mapping("other", null, "Done")
		)
		val used = listOf(
			"flow" to "Unavailable",
			"flow" to "Recovered",
			"other" to "Done",
			"flow" to "Recoverd",
			"flow" to "Done",
			"missing" to "Unavailable",
			"flow" to "Recoverd"
		)

		assertEquals(
			listOf("flow" to "Recoverd", "flow" to "Done", "missing" to "Unavailable"),
			MockRules.undeclaredStates(used, declared)
		)
	}
}
