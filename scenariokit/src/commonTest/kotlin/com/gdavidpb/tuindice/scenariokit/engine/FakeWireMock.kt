package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.HttpReply
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** The slice of the WireMock admin API the interpreter talks to, over a scripted request journal. */
class FakeWireMock {
	val calls = mutableListOf<String>()
	val authorizations = mutableMapOf<String, String?>()
	val journal = mutableListOf<AppRequest>()
	val states = mutableMapOf("login-token-lifecycle" to "Started", "other" to "Started")
	var down = false
	var failingPath: String? = null

	fun appRequest(method: String, url: String, status: Int, authorization: String? = null) {
		journal += AppRequest(method, url, status, authorization)
	}

	fun http(method: String, path: String, body: String?, authorization: String?): HttpReply {
		calls += "$method $path"
		authorizations["$method $path"] = authorization
		return when {
			down -> HttpReply(-1, "connection refused")
			path == failingPath -> HttpReply(SERVICE_UNAVAILABLE, "scripted failure")
			else -> route(method, path, body)
		}
	}

	private fun route(method: String, path: String, body: String?): HttpReply = when {
		path.startsWith("/__admin/requests/") -> query(path, body)
		method == "POST" && path == "/__admin/scenarios/reset" -> ok { states.keys.forEach { states[it] = "Started" } }
		method == "DELETE" && path == "/__admin/requests" -> ok { journal.clear() }
		method == "PUT" && path.startsWith("/__admin/scenarios/") -> ok { setState(path, body) }
		method == "GET" && path.startsWith("/__admin/requests") -> recent()
		method == "GET" && path == "/__admin/scenarios" -> scenarios()
		else -> HttpReply(OK, "{}")
	}

	private fun query(path: String, body: String?): HttpReply = when (path) {
		"/__admin/requests/count" -> count(parse(body))
		"/__admin/requests/find" -> find(parse(body))
		else -> HttpReply(OK, "{}")
	}

	private fun ok(action: () -> Unit = {}): HttpReply {
		action()
		return HttpReply(OK, "{}")
	}

	private fun parse(body: String?): JsonObject = Json.parseToJsonElement(body ?: "{}").jsonObject

	private fun setState(path: String, body: String?) {
		val name = path.removePrefix("/__admin/scenarios/").removeSuffix("/state")
		states[name] = parse(body).getValue("state").let { (it as JsonPrimitive).content }
	}

	private fun matching(pattern: JsonObject): List<AppRequest> {
		val wanted = pattern["headers"]?.jsonObject?.get("Authorization")?.jsonObject?.get("equalTo")
			?.let { (it as JsonPrimitive).contentOrNull }
		return journal.filter {
			it.method == pattern.getValue("method").let { m -> (m as JsonPrimitive).content } &&
				it.url.substringBefore('?') == pattern.getValue("urlPath").let { p -> (p as JsonPrimitive).content } &&
				(wanted == null || it.authorization == wanted)
		}.asReversed()
	}

	private fun count(pattern: JsonObject) = HttpReply(
		OK,
		buildJsonObject { put("count", matching(pattern).size) }.toString()
	)

	private fun find(pattern: JsonObject) = HttpReply(
		OK,
		buildJsonObject {
			put(
				"requests",
				buildJsonArray {
				matching(pattern).forEach { add(entry(it).getValue("request")) }
			}
			)
		}.toString()
	)

	private fun recent() = HttpReply(
		OK,
		buildJsonObject {
			put("requests", buildJsonArray { journal.asReversed().take(RECENT_LIMIT).forEach { add(entry(it)) } })
		}.toString()
	)

	private fun entry(request: AppRequest) = buildJsonObject {
		putJsonObject("request") {
			put("method", request.method)
			put("url", request.url)
			putJsonObject("headers") { request.authorization?.let { put("Authorization", it) } }
		}
		putJsonObject("response") { put("status", request.status) }
	}

	private fun scenarios(): HttpReply {
		val entries = states.map { (name, state) ->
			buildJsonObject {
				put("name", name)
				put("state", state)
			}
		}
		return HttpReply(OK, buildJsonObject { put("scenarios", JsonArray(entries)) }.toString())
	}

	private companion object {
		const val OK = 200
		const val SERVICE_UNAVAILABLE = 503
		const val RECENT_LIMIT = 5
	}
}
