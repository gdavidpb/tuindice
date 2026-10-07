package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.array
import com.gdavidpb.tuindice.scenarios.MockJson.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Answers requests from the mapping files the way WireMock does, enough for the stateful retry fixtures: the
 * method, the path, the query, the headers, the password in the body, and the state of the mapping's scenario;
 * the lowest priority wins (no priority means 5) and a tie between the best two is an error, because WireMock
 * would pick one of them by insertion order. A request no mapping answers gets 404, like the real server.
 */
internal class MockReplay(private val mappings: List<Pair<String, JsonObject>>) {
	private val states = mutableMapOf<String, String>()

	/** The status sent, and the file that sent it ("none" for a 404). */
	data class Reply(val status: Int, val mapping: String)

	fun send(
		method: String,
		path: String,
		headers: Map<String, String> = emptyMap(),
		query: Map<String, String> = emptyMap(),
		password: String? = null
	): Reply {
		val matching = mappings.filter { (_, mapping) -> matches(mapping, method, path, headers, query, password) }
		val best = matching.minOfOrNull { priorityOf(it.second) } ?: return Reply(NOT_FOUND, "none")
		val candidates = matching.filter { priorityOf(it.second) == best }

		check(candidates.size == 1) { "$method $path is answered by ${candidates.map { it.first }} with the same priority" }

		val (name, mapping) = candidates.single()
		val scenario = mapping.string("scenarioName")
		val next = mapping.string("newScenarioState")

		if (scenario != null && next != null) states[scenario] = next

		val status = ((mapping["response"] as? JsonObject)?.get("status") as? JsonPrimitive)?.intOrNull ?: OK

		return Reply(status, name)
	}

	private fun priorityOf(mapping: JsonObject): Int = (mapping["priority"] as? JsonPrimitive)?.intOrNull ?: DEFAULT_PRIORITY

	private fun matches(
		mapping: JsonObject,
		method: String,
		path: String,
		headers: Map<String, String>,
		query: Map<String, String>,
		password: String?
	): Boolean {
		val request = mapping["request"] as? JsonObject ?: return false
		val scenario = mapping.string("scenarioName")
		val required = mapping.string("requiredScenarioState")

		return request.string("method").equals(method, ignoreCase = true) &&
			request.string("urlPath") == path &&
			(scenario == null || required == null || (states[scenario] ?: STARTED) == required) &&
			(request["headers"] as? JsonObject).orEmpty().all { (name, matcher) -> valueMatches(matcher as JsonObject, headers[name]) } &&
			(request["queryParameters"] as? JsonObject).orEmpty().all { (name, matcher) -> valueMatches(matcher as JsonObject, query[name]) } &&
			request.array("bodyPatterns").all { pattern -> bodyMatches(pattern as JsonObject, password) }
	}

	private fun valueMatches(matcher: JsonObject, value: String?): Boolean {
		val absent = (matcher["absent"] as? JsonPrimitive)?.contentOrNull == "true"

		return when {
			absent -> value == null
			value == null -> false
			else -> matcher.string("equalTo")?.let { it == value }
				?: matcher.string("matches")?.let { Regex(it).matches(value) }
				?: matcher.string("contains")?.let { it in value }
				?: error("unsupported matcher $matcher")
		}
	}

	private fun bodyMatches(pattern: JsonObject, password: String?): Boolean {
		val expected = Regex("""@\.password == '([^']*)'""").find(pattern.string("matchesJsonPath").orEmpty())?.groupValues?.get(1)

		return expected != null && expected == password
	}

	private fun JsonObject?.orEmpty(): JsonObject = this ?: JsonObject(emptyMap())

	private companion object {
		const val OK = 200
		const val NOT_FOUND = 404
		const val DEFAULT_PRIORITY = 5
		const val STARTED = "Started"
	}
}
