package com.gdavidpb.tuindice.base.domain.model.assistant

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantScreenContextSerializationTest {
	@Test
	fun roundTrip_keepsEveryField() {
		val context = AssistantScreenContext(
			screen = AssistantScreen.EVALUATION_DETAIL,
			entities = listOf(
				AssistantEntityRef(kind = AssistantEntityKind.EVALUATION, id = "evaluation-1"),
				AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "subject-9")
			),
			visibleFields = mapOf("grade" to "17", "date" to "2026-10-01"),
			filters = mapOf("term" to "2026-II")
		)

		val decoded = Json.decodeFromString<AssistantScreenContext>(Json.encodeToString(context))

		assertEquals(expected = context, actual = decoded)
	}

	@Test
	fun roundTrip_restoresTheDefaultsOfAMinimalContext() {
		val context = AssistantScreenContext(screen = AssistantScreen.SUMMARY)

		val decoded = Json.decodeFromString<AssistantScreenContext>(Json.encodeToString(context))

		assertEquals(expected = context, actual = decoded)
	}
}
