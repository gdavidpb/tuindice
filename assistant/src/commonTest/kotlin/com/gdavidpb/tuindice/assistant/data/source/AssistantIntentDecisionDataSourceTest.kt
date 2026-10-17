package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.testing.RecordingServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.assistant.testing.jsonAnswer
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AssistantIntentDecisionDataSourceTest {
	private val decision = IntentDecision(intentId = "i1", accepted = true)

	@Test
	fun anAcceptedDecisionIsAccepted() = runTest {
		val api = KtorAssistantApiDataSource(
			httpClient = assistantHttpClient { respond(content = "", status = HttpStatusCode.NoContent) },
			serverDisabled = RecordingServerDisabledRepository()
		)

		assertEquals(IntentDecisionResult.Accepted, api.decide(decision))
	}

	@Test
	fun anIntentThatExpiredOrConflictedIsAResultNotAFailure() = runTest {
		val expired = KtorAssistantApiDataSource(
			httpClient = assistantHttpClient { jsonAnswer(HttpStatusCode.Conflict, """{"code":"intent_expired"}""") },
			serverDisabled = RecordingServerDisabledRepository()
		)
		val conflict = KtorAssistantApiDataSource(
			httpClient = assistantHttpClient { jsonAnswer(HttpStatusCode.Conflict, """{"code":"intent_conflict"}""") },
			serverDisabled = RecordingServerDisabledRepository()
		)

		assertEquals(IntentDecisionResult.Expired, expired.decide(decision))
		assertEquals(IntentDecisionResult.Conflict, conflict.decide(decision))
	}

	@Test
	fun anyOtherRefusalIsStillThrown() = runTest {
		val api = KtorAssistantApiDataSource(
			httpClient = assistantHttpClient {
				jsonAnswer(HttpStatusCode.Forbidden, """{"code":"consent_required"}""")
			},
			serverDisabled = RecordingServerDisabledRepository()
		)

		val failure = assertFailsWith<AssistantApiException> { api.decide(decision) }

		assertEquals("consent_required", failure.code)
	}
}
