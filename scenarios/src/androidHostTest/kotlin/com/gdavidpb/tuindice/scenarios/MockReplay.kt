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

	private class Request(
		val method: String,
		val path: String,
		val headers: Map<String, String>,
		val query: Map<String, String>,
		val password: String?
	)

	fun send(
		method: String,
		path: String,
		headers: Map<String, String> = emptyMap(),
		query: Map<String, String> = emptyMap(),
		password: String? = null
	): Reply {
		val request = Request(method, path, headers, query, password)
		val matching = mappings.filter { (_, mapping) -> matches(mapping, request) }
		val best = matching.minOfOrNull { priorityOf(it.second) } ?: return Reply(NOT_FOUND, "none")
		val candidates = matching.filter { priorityOf(it.second) == best }

		check(candidates.size == 1) {
			"$method $path is answered by ${candidates.map { it.first }} with the same priority"
		}

		val (name, mapping) = candidates.single()
		val scenario = mapping.string("scenarioName")
		val next = mapping.string("newScenarioState")

		if (scenario != null && next != null) states[scenario] = next

		val status = ((mapping["response"] as? JsonObject)?.get("status") as? JsonPrimitive)?.intOrNull ?: OK

		return Reply(status, name)
	}

	private fun priorityOf(mapping: JsonObject): Int =
		(mapping["priority"] as? JsonPrimitive)?.intOrNull ?: DEFAULT_PRIORITY

	private fun matches(mapping: JsonObject, sent: Request): Boolean {
		val request = mapping["request"] as? JsonObject ?: return false
		val scenario = mapping.string("scenarioName")
		val required = mapping.string("requiredScenarioState")
		val headers = request["headers"] as? JsonObject ?: JsonObject(emptyMap())
		val query = request["queryParameters"] as? JsonObject ?: JsonObject(emptyMap())

		return request.string("method").equals(sent.method, ignoreCase = true) &&
			request.string("urlPath") == sent.path &&
			(scenario == null || required == null || (states[scenario] ?: STARTED) == required) &&
			headers.all { (name, matcher) -> valueMatches(matcher as JsonObject, sent.headers[name]) } &&
			query.all { (name, matcher) -> valueMatches(matcher as JsonObject, sent.query[name]) } &&
			request.array("bodyPatterns").all { pattern -> bodyMatches(pattern as JsonObject, sent.password) }
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
		val path = pattern.string("matchesJsonPath").orEmpty()
		val expected = Regex("""@\.password == '([^']*)'""").find(path)?.groupValues?.get(1)

		return expected != null && expected == password
	}

	private companion object {
		const val OK = 200
		const val NOT_FOUND = 404
		const val DEFAULT_PRIORITY = 5
		const val STARTED = "Started"
	}
}
