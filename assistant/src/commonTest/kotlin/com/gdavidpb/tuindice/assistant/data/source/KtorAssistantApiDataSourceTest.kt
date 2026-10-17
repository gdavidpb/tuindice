package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.data.exception.AssistantDisabledException
import com.gdavidpb.tuindice.assistant.testing.RecordingServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.assistant.testing.jsonAnswer
import com.gdavidpb.tuindice.chatcontract.ChatContract
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.consent.ConsentDecision
import com.gdavidpb.tuindice.chatcontract.consent.ConsentStatus
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.event.UsageSummary
import com.gdavidpb.tuindice.chatcontract.insights.TermInsight
import com.gdavidpb.tuindice.chatcontract.insights.TermInsights
import com.gdavidpb.tuindice.chatcontract.memory.MemoryItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.chatcontract.memory.StillAppliesAnswer
import com.gdavidpb.tuindice.chatcontract.turn.AssistantStatus
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest
import com.gdavidpb.tuindice.chatcontract.turn.UserMessage
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KtorAssistantApiDataSourceTest {

	@Test
	fun postTurnSendsTheContractJsonAndReadsTheAcceptedTurn() = runTest {
		var sentPath = ""
		var sentBody: String? = null
		val client = assistantHttpClient { call ->
			sentPath = call.url.encodedPath
			sentBody = (call.body as? TextContent)?.text
			jsonAnswer(HttpStatusCode.Accepted, """{"turnId":"t1","conversationId":"c1","lastSeq":0}""")
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())
		val request = TurnRequest(
			clientTurnId = "c-1",
			message = UserMessage(text = "Hola"),
			supportedCatalogs = listOf("text")
		)

		val accepted = api.postTurn(request)

		assertEquals(TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0), accepted)
		assertEquals("/assistant/v1/turns", sentPath)
		assertEquals(ChatContract.json.encodeToString(TurnRequest.serializer(), request), sentBody)
		assertTrue(sentBody.orEmpty().contains("\"clientTurnId\":\"c-1\""))
	}

	@Test
	fun statusDecodesTheAssistantStatus() = runTest {
		val client = assistantHttpClient {
			jsonAnswer(
				HttpStatusCode.OK,
				"""{"mode":"on","consent":{"version":1,"accepted":true},"standing":{"level":"none"},""" +
						""""usage":{"remainingHour":4,"remainingDay":20,"remainingMonth":300}}"""
			)
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())

		val status = api.getStatus()

		assertEquals(
			AssistantStatus(
				mode = "on",
				consent = ConsentStatus(version = 1, accepted = true),
				standing = AbuseStanding(level = StandingLevel.NONE),
				usage = UsageSummary(remainingHour = 4, remainingDay = 20, remainingMonth = 300)
			),
			status
		)
	}

	@Test
	fun consentIsReadAndWrittenWithTheContractVersion() = runTest {
		var putBody: String? = null
		val client = assistantHttpClient { call ->
			if (call.method == HttpMethod.Get) {
				jsonAnswer(HttpStatusCode.OK, """{"version":1,"accepted":false}""")
			} else {
				putBody = (call.body as? TextContent)?.text
				jsonAnswer(HttpStatusCode.OK, """{"version":1,"accepted":true,"acceptedAtMillis":1700000000000}""")
			}
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())

		assertEquals(ConsentStatus(version = 1, accepted = false), api.getConsent())
		assertEquals(
			ConsentStatus(version = 1, accepted = true, acceptedAtMillis = 1_700_000_000_000L),
			api.putConsent(ConsentDecision(version = 1, accepted = true))
		)
		assertEquals("""{"version":1,"accepted":true}""", putBody)
	}

	@Test
	fun insightsDecodeTheTermInsights() = runTest {
		val client = assistantHttpClient {
			jsonAnswer(
				HttpStatusCode.OK,
				"""{"items":[{"id":"i1","termKey":"2026-1","kind":"risk","title":"Materia en riesgo",""" +
						""""body":"Revisa el parcial","entityRefs":[{"kind":"subject","id":"s1"}]}]}"""
			)
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())

		assertEquals(
			TermInsights(
				items = listOf(
					TermInsight(
						id = "i1",
						termKey = "2026-1",
						kind = "risk",
						title = "Materia en riesgo",
						body = "Revisa el parcial",
						entityRefs = listOf(EntityRef(kind = EntityKind.SUBJECT, id = "s1"))
					)
				)
			),
			api.getInsights()
		)
	}

	@Test
	fun memoriesDecodeTheMemoryList() = runTest {
		val client = assistantHttpClient {
			jsonAnswer(
				HttpStatusCode.OK,
				"""{"items":[{"id":"m1","category":"preference","text":"Prefiere las mananas",""" +
						""""state":"active","createdAtMillis":1700000000000}]}"""
			)
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())

		assertEquals(
			MemoryList(
				items = listOf(
					MemoryItem(
						id = "m1",
						category = "preference",
						text = "Prefiere las mananas",
						state = "active",
						createdAtMillis = 1_700_000_000_000L
					)
				)
			),
			api.getMemories()
		)
	}

	@Test
	fun standingDecodesTheAbuseStanding() = runTest {
		val client = assistantHttpClient {
			jsonAnswer(HttpStatusCode.OK, """{"level":"warning","appealable":true}""")
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())

		assertEquals(AbuseStanding(level = StandingLevel.WARNING, appealable = true), api.getStanding())
	}

	@Test
	fun noContentAnswersAreAcceptedWithoutABody() = runTest {
		var sentMethod = ""
		var sentPath = ""
		val client = assistantHttpClient { call ->
			sentMethod = call.method.value
			sentPath = call.url.encodedPath
			respond(content = "", status = HttpStatusCode.NoContent)
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())

		api.deleteAllMemories()

		assertEquals("DELETE", sentMethod)
		assertEquals("/assistant/v1/memories", sentPath)
	}

	@Test
	fun stillAppliesSendsTheAnswerToTheMemory() = runTest {
		var sentPath = ""
		var sentBody: String? = null
		val client = assistantHttpClient { call ->
			sentPath = call.url.encodedPath
			sentBody = (call.body as? TextContent)?.text
			respond(content = "", status = HttpStatusCode.NoContent)
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())

		api.postStillApplies("m1", StillAppliesAnswer(applies = false))

		assertEquals("/assistant/v1/memories/m1/still-applies", sentPath)
		assertEquals("""{"applies":false}""", sentBody)
	}

	@Test
	fun assistantDisabledAnswerMarksTheServerAndThrows() = runTest {
		val serverDisabled = RecordingServerDisabledRepository()
		val client = assistantHttpClient {
			jsonAnswer(HttpStatusCode.ServiceUnavailable, """{"code":"assistant_disabled","retryAfterSeconds":120}""")
		}
		val api = KtorAssistantApiDataSource(client, serverDisabled)

		val failure = assertFailsWith<AssistantDisabledException> { api.getStatus() }

		assertEquals(120, failure.retryAfterSeconds)
		assertEquals(listOf<Int?>(120), serverDisabled.marks)
	}

	@Test
	fun retryAfterHeaderIsUsedWhenTheBodyGivesNoTime() = runTest {
		val serverDisabled = RecordingServerDisabledRepository()
		val client = assistantHttpClient {
			jsonAnswer(HttpStatusCode.ServiceUnavailable, """{"code":"assistant_disabled"}""", retryAfter = "30")
		}
		val api = KtorAssistantApiDataSource(client, serverDisabled)

		val failure = assertFailsWith<AssistantDisabledException> { api.getStatus() }

		assertEquals(30, failure.retryAfterSeconds)
		assertEquals(listOf<Int?>(30), serverDisabled.marks)
	}

	@Test
	fun otherErrorsBecomeApiExceptionsWithTheirCode() = runTest {
		val serverDisabled = RecordingServerDisabledRepository()
		val client = assistantHttpClient {
			jsonAnswer(HttpStatusCode.Conflict, """{"code":"turn_in_progress","message":"Ya hay un turno"}""")
		}
		val api = KtorAssistantApiDataSource(client, serverDisabled)

		val failure = assertFailsWith<AssistantApiException> { api.postTurnCancel("t1") }

		assertEquals("turn_in_progress", failure.code)
		assertNull(failure.retryAfterSeconds)
		assertEquals(409, failure.httpStatus)
		assertTrue(serverDisabled.marks.isEmpty())
	}

	@Test
	fun unreadableErrorBodyBecomesAnUnknownApiException() = runTest {
		val client = assistantHttpClient {
			respond(content = "boom", status = HttpStatusCode.InternalServerError)
		}
		val api = KtorAssistantApiDataSource(client, RecordingServerDisabledRepository())

		val failure = assertFailsWith<AssistantApiException> { api.deleteThreads() }

		assertEquals("unknown", failure.code)
		assertEquals(500, failure.httpStatus)
	}
}
