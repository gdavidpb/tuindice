package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.array
import com.gdavidpb.tuindice.scenarios.MockJson.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Answers requests from the mapping files the way WireMock does, enough for the stateful retry fixtures: the
 * method, the path, the query, the headers, the body (its password, or fields compared with `==` / `!=` and joined
 * with `&&` in a JSON path), and the state of the mapping's scenario, which [setState] can also set;
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
		val body: Map<String, String?>
	)

	fun send(
		method: String,
		path: String,
		headers: Map<String, String> = emptyMap(),
		query: Map<String, String> = emptyMap(),
		body: Map<String, String?> = emptyMap()
	): Reply {
		val request = Request(method, path, headers, query, body)
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

	/** What a scenario step that sets a mock state does (`PUT /__admin/scenarios/{scenario}/state`). */
	fun setState(scenario: String, state: String) {
		states[scenario] = state
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
			request.array("bodyPatterns").all { pattern -> bodyMatches(pattern as JsonObject, sent) }
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

	private fun bodyMatches(pattern: JsonObject, sent: Request): Boolean {
		val path = pattern.string("matchesJsonPath").orEmpty()
		val password = Regex("""@\.password == '([^']*)'""").find(path)?.groupValues?.get(1)

		return if (password != null) {
			password == sent.body["password"]
		} else {
			val conditions = Regex("""@\.(\w+) (==|!=) (null|'[^']*'|[\w.]+)""").findAll(path).toList()

			conditions.isNotEmpty() && conditions.all { condition -> conditionHolds(condition.groupValues, sent.body) }
		}
	}

	private fun conditionHolds(condition: List<String>, body: Map<String, String?>): Boolean {
		val literal = condition[LITERAL]
		val expected = if (literal == "null") null else literal.trim('\'')
		val equal = body[condition[FIELD]] == expected

		return if (condition[OPERATOR] == "==") equal else !equal
	}

	companion object {
		/** The mappings of the given WireMock scenarios plus the ones with no scenario, as the server holds them. */
		fun of(vararg scenarios: String, without: String? = null): MockReplay =
			MockReplay(
				RepoFiles.allMappings.walkTopDown().filter { it.isFile && it.extension == "json" }
					.filter { it.name != without }.sortedBy { it.path }.map { it.name to MockJson.obj(it) }.toList()
					.filter { (_, mapping) -> mapping.string("scenarioName").let { it == null || it in scenarios } }
			)

		private const val OK = 200
		private const val NOT_FOUND = 404
		private const val DEFAULT_PRIORITY = 5
		private const val STARTED = "Started"
		private const val FIELD = 1
		private const val OPERATOR = 2
		private const val LITERAL = 3
	}
}
