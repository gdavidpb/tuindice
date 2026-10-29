package com.gdavidpb.tuindice.data.source.assistant

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

// The subject code as the record keeps it: trimmed and uppercase.
internal fun normalizeSubjectCode(code: String): String = code.trim().uppercase()

// The enum entry of a name, as the app spells it (EvaluationType: QUIZ).
internal inline fun <reified E : Enum<E>> enumByName(name: String): E? {
	return enumValues<E>().firstOrNull { entry -> entry.name == name }
}

// The enum entry of a serial name, as the wire spells it (EvaluationScheduleMode: "dated" for DATED).
internal inline fun <reified E : Enum<E>> enumBySerialName(serialName: String): E? {
	val elementSerializer = serializer<E>()

	return enumValues<E>().firstOrNull { entry ->
		Json.encodeToString(elementSerializer, entry).trim('"') == serialName
	}
}
