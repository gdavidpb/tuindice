package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.testing.RecordingServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.assistant.testing.jsonAnswer
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantInsightsDataSourceTest {

	@Test
	fun refreshMapsTheInsightsAndClearingEmptiesThem() = runTest {
		val client = assistantHttpClient {
			jsonAnswer(
				HttpStatusCode.OK,
				"""{"items":[{"id":"i1","termKey":"2026-1","kind":"risk","title":"Materia en riesgo",""" +
						""""body":"Revisa el parcial","entityRefs":[{"kind":"subject","id":"s1"}]}]}"""
			)
		}
		val source = AssistantInsightsDataSource(KtorAssistantApiDataSource(client, RecordingServerDisabledRepository()))

		source.refreshInsights()

		assertEquals(
			listOf(
				AssistantTermInsight(
					id = "i1",
					termKey = "2026-1",
					kind = "risk",
					title = "Materia en riesgo",
					body = "Revisa el parcial",
					entityRefs = listOf(AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "s1"))
				)
			),
			source.observeInsightsFlow().first()
		)

		source.clearSessionMemory()

		assertEquals(emptyList(), source.observeInsightsFlow().first())
	}
}
