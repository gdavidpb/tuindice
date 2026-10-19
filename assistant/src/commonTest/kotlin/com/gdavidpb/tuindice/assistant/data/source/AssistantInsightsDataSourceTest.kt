package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.testing.RecordingServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.assistant.testing.jsonAnswer
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantInsightAction
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantInsightSection
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

	@Test
	fun refreshMapsTheSectionsTheActionsAndTheInstantOfAReport() = runTest {
		val client = assistantHttpClient {
			jsonAnswer(
				HttpStatusCode.OK,
				"""{"items":[{"id":"term-1:2","termKey":"2026-APR_JUL","kind":"term_end","title":"Cerraste con 4,18",""" +
						""""body":"Aprobaste las 3 materias.","entityRefs":[{"kind":"term","id":"term-1"}],""" +
						""""sections":[{"title":"Cerraste con 4,18","text":"Aprobaste las 3 materias."}],""" +
						""""nextActions":[{"target":"record.projection","label":"Proyectar tu índice",""" +
						""""entityRefs":[]},{"target":"share_term","label":"Compartir",""" +
						""""entityRefs":[{"kind":"term","id":"term-1"}]}],"createdAtMillis":1761000000000}]}"""
			)
		}
		val source = AssistantInsightsDataSource(KtorAssistantApiDataSource(client, RecordingServerDisabledRepository()))

		source.refreshInsights()

		val insight = source.observeInsightsFlow().first().single()

		assertEquals(
			listOf(AssistantInsightSection(title = "Cerraste con 4,18", text = "Aprobaste las 3 materias.")),
			insight.sections
		)
		assertEquals(
			listOf(
				AssistantInsightAction(target = AssistantNavTarget.RECORD_PROJECTION, label = "Proyectar tu índice"),
				AssistantInsightAction(
					target = AssistantNavTarget.SHARE_TERM,
					label = "Compartir",
					entityRefs = listOf(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-1"))
				)
			),
			insight.nextActions
		)
		assertEquals(1_761_000_000_000L, insight.createdAtMillis)
	}

	@Test
	fun rateSendsTheThumbToTheFeedbackPathOfTheInsight() = runTest {
		var path: String? = null
		var body: String? = null
		val client = assistantHttpClient { request ->
			path = request.url.encodedPath
			body = (request.body as TextContent).text

			respond(content = "", status = HttpStatusCode.NoContent)
		}
		val source = AssistantInsightsDataSource(KtorAssistantApiDataSource(client, RecordingServerDisabledRepository()))

		source.rateInsight(insightId = "term-1:2", useful = true)

		assertEquals("/assistant/v1/insights/term-1:2/feedback", path)
		assertEquals("""{"useful":true}""", body)
	}

	@Test
	fun rateRaisesTheRefusalOfAReportThatIsNotTheStudentsOwn() = runTest {
		val client = assistantHttpClient {
			jsonAnswer(HttpStatusCode.NotFound, """{"code":"not_found"}""")
		}
		val source = AssistantInsightsDataSource(KtorAssistantApiDataSource(client, RecordingServerDisabledRepository()))

		val refusal = assertFailsWith<AssistantApiException> {
			source.rateInsight(insightId = "term-1:2", useful = false)
		}

		assertEquals(HttpStatusCode.NotFound.value, refusal.httpStatus)
	}

	// The insights a refresh asked for, answered after the session ended, are not kept for the one that follows.
	@Test
	fun aRefreshAnsweredAfterTheSessionEndedKeepsNoInsights() = runTest {
		val getStarted = CompletableDeferred<Unit>()
		val getAnswer = CompletableDeferred<Unit>()
		val client = assistantHttpClient {
			getStarted.complete(Unit)
			getAnswer.await()
			jsonAnswer(
				HttpStatusCode.OK,
				"""{"items":[{"id":"i1","termKey":"2026-1","kind":"risk","title":"Materia en riesgo",""" +
						""""body":"Revisa el parcial","entityRefs":[{"kind":"subject","id":"s1"}]}]}"""
			)
		}
		val source = AssistantInsightsDataSource(KtorAssistantApiDataSource(client, RecordingServerDisabledRepository()))

		val refresh = launch { source.refreshInsights() }
		getStarted.await()

		source.clearSessionMemory()
		getAnswer.complete(Unit)
		refresh.join()

		assertEquals(emptyList(), source.observeInsightsFlow().first())
	}
}
