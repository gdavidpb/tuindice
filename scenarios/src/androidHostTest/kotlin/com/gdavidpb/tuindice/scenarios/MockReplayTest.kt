package com.gdavidpb.tuindice.scenarios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * The replay answers what it understands and stops at what it does not: a matcher it silently ignored would make
 * a mapping look unreachable (or always reachable) and the test that replays it pass for nothing.
 */
class MockReplayTest {
	private fun mapping(request: String): JsonObject =
		Json.parseToJsonElement(
			"""{"request": {"method": "POST", $request}, "response": {"status": 503}}"""
		) as JsonObject

	private fun replayOf(request: String) = MockReplay(listOf("m.json" to mapping(request)))

	@Test
	fun aMappingItUnderstandsAnswersAndOneThatDoesNotMatchIsNotFound() {
		val replay = replayOf(
			""""urlPath": "/a", "bodyPatterns": [{"matchesJsonPath": "$[?(@.type == 0 && @.date != null)]"}]"""
		)
		val dated = mapOf("type" to "0", "date" to "d")

		assertEquals(MockReplay.Reply(SERVICE_UNAVAILABLE, "m.json"), replay.send("POST", "/a", body = dated))
		assertEquals(NOT_FOUND, replay.send("POST", "/a", body = mapOf("type" to "0", "date" to null)).status)
		assertEquals(NOT_FOUND, replay.send("POST", "/b", body = dated).status)
	}

	@Test
	fun aUrlPathPatternThatCouldAnswerStopsTheReplay() {
		val replay = replayOf(""""urlPathPattern": "/a/[a-z]+"""")

		assertFailsWith<IllegalStateException> { replay.send("POST", "/a/xyz") }
	}

	@Test
	fun aUrlPathPatternThatCouldNotAnswerLeavesTheRequestUnanswered() {
		val replay = replayOf(""""urlPathPattern": "/a/[a-z]+"""")

		assertEquals(NOT_FOUND, replay.send("POST", "/b/xyz").status)
		assertEquals(NOT_FOUND, replay.send("GET", "/a/xyz").status)
	}

	@Test
	fun anotherUrlMatcherStopsTheReplay() {
		assertFailsWith<IllegalStateException> { replayOf(""""url": "/a?x=1"""").send("POST", "/a") }
		assertFailsWith<IllegalStateException> { replayOf(""""urlPattern": "/a.*"""").send("POST", "/a") }
	}

	@Test
	fun aBodyPatternItDoesNotUnderstandStopsTheReplayWhenTheRestOfTheMappingMatches() {
		val equalToJson = replayOf(""""urlPath": "/a", "bodyPatterns": [{"equalToJson": "{}"}]""")
		val existence = replayOf(""""urlPath": "/a", "bodyPatterns": [{"matchesJsonPath": "$.key_id"}]""")
		val regex = replayOf(""""urlPath": "/a", "bodyPatterns": [{"matchesJsonPath": "$[?(@.token =~ /x:.+/)]"}]""")

		assertFailsWith<IllegalStateException> { equalToJson.send("POST", "/a") }
		assertFailsWith<IllegalStateException> { existence.send("POST", "/a") }
		assertFailsWith<IllegalStateException> { regex.send("POST", "/a") }
		assertEquals(NOT_FOUND, equalToJson.send("POST", "/other").status, "a mapping for another path is not evaluated")
	}

	private companion object {
		const val SERVICE_UNAVAILABLE = 503
		const val NOT_FOUND = 404
	}
}
