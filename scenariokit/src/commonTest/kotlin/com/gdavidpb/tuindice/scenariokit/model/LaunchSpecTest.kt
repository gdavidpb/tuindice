package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LaunchSpecTest {
	private val json = Json { encodeDefaults = true }

	@Test
	fun launchSpec_hasOnlyArgumentsAndMockStates() {
		val spec = LaunchSpec(mapOf("KEY" to "value"), listOf(MockState("scenario", "State")))

		val (arguments, mockStates) = spec

		assertEquals(mapOf("KEY" to "value"), arguments)
		assertEquals(listOf(MockState("scenario", "State")), mockStates)
	}

	@Test
	fun launchSpec_serialisesWithoutAnyCleanupFlag() {
		val keys = json
			.encodeToJsonElement(LaunchSpec.serializer(), LaunchSpec(emptyMap()))
			.jsonObject.keys

		assertEquals(setOf("arguments", "mockStates"), keys)
	}

	@Test
	fun scenario_carriesAccountSignsInAndTimeoutWithDefaults() {
		val scenario = Scenario("auth-x", "auth", LaunchSpec(emptyMap()), emptyList())

		assertNull(scenario.account)
		assertEquals(false, scenario.signsIn)
		assertEquals(180, scenario.timeoutSeconds)
	}
}
