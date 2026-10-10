package com.gdavidpb.tuindice.scenariokit.engine

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Tolerant readers for the WireMock admin replies; anything unexpected reads as absent. */
internal object WireMockJson {
	fun obj(body: String): JsonObject? =
		runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull()

	fun array(source: JsonObject?, key: String): List<JsonObject> =
		(source?.get(key) as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty()

	fun child(source: JsonObject?, key: String): JsonObject? = source?.get(key) as? JsonObject

	fun text(source: JsonObject?, key: String): String? =
		(source?.get(key) as? JsonPrimitive)?.contentOrNull

	fun number(source: JsonObject?, key: String): Int? =
		(source?.get(key) as? JsonPrimitive)?.intOrNull

	/** Header lookup ignoring case, since journals keep the casing the client sent. */
	fun header(headers: JsonObject?, name: String): String? =
		headers?.entries?.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value?.let { value ->
			when (value) {
				is JsonArray -> value.firstOrNull()?.jsonPrimitive?.contentOrNull
				is JsonPrimitive -> value.contentOrNull
				else -> null
			}
		}
}
