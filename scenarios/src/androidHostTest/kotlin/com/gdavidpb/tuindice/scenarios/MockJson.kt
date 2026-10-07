package com.gdavidpb.tuindice.scenarios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.io.File

/** Reads the mock files the host tests check. */
internal object MockJson {
	fun obj(file: File): JsonObject = Json.parseToJsonElement(file.readText()) as JsonObject

	/** Every `*.json` under [directory] that is an object, in path order. */
	fun objects(directory: File): List<JsonObject> =
		directory.walkTopDown().filter { it.isFile && it.extension == "json" }.sortedBy { it.path }
			.map { Json.parseToJsonElement(it.readText()) }.filterIsInstance<JsonObject>().toList()

	fun JsonObject.array(key: String): JsonArray = this[key] as? JsonArray ?: JsonArray(emptyList())

	/** The string at [path], descending through objects; null when any step is missing. */
	fun JsonObject.string(vararg path: String): String? {
		var node: JsonObject = this

		path.dropLast(1).forEach { key -> node = node[key] as? JsonObject ?: return null }

		return (node[path.last()] as? JsonPrimitive)?.contentOrNull
	}
}
