package com.gdavidpb.tuindice.scenariokit.codec

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Deterministic shapes of a JSON tree: sorted keys everywhere, and optionally without chosen keys. */
internal object CanonicalJson {
	fun sorted(element: JsonElement, without: Set<String> = emptySet()): JsonElement = when (element) {
		is JsonObject -> JsonObject(
			element.entries
				.filter { it.key !in without }
				.sortedBy { it.key }
				.associate { it.key to sorted(it.value, without) }
		)
		is JsonArray -> JsonArray(element.map { sorted(it, without) })
		else -> element
	}
}
