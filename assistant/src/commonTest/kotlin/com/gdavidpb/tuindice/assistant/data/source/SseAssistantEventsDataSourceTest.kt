package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.data.exception.AssistantDisabledException
import com.gdavidpb.tuindice.assistant.data.model.AssistantStreamEvent
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantConfigRepository
import com.gdavidpb.tuindice.assistant.testing.HEARTBEAT
import com.gdavidpb.tuindice.assistant.testing.RecordingServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.VirtualClock
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.assistant.testing.frame
import com.gdavidpb.tuindice.assistant.testing.jsonAnswer
import com.gdavidpb.tuindice.assistant.testing.progressJson
import com.gdavidpb.tuindice.assistant.testing.runErrorJson
import com.gdavidpb.tuindice.assistant.testing.runFinishedJson
import com.gdavidpb.tuindice.assistant.testing.runStartedJson
import com.gdavidpb.tuindice.assistant.testing.sseAnswer
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.event.ComponentEmitted
import com.gdavidpb.tuindice.chatcontract.event.IntentResolved
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SseAssistantEventsDataSourceTest {

	@Test
	fun eventsArriveInOrderUntilTheRunFinishes() = runTest {
		val client = assistantHttpClient {
			sseAnswer(frame(1, runStartedJson(1)) + frame(2, progressJson(2)) + frame(3, runFinishedJson(3)))
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val seqs = source.events("t1", afterSeq = 0).toList().map { it.seq }

		assertEquals(listOf(1L, 2L, 3L), seqs)
	}

	@Test
	fun duplicatedSeqIsDropped() = runTest {
		val client = assistantHttpClient {
			sseAnswer(
				frame(1, runStartedJson(1)) +
						frame(2, progressJson(2)) + frame(2, progressJson(2)) +
						frame(3, runStartedJson(3)) + frame(3, runStartedJson(3)) +
						frame(4, runFinishedJson(4))
			)
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val seqs = source.events("t1", afterSeq = 0).toList().map { it.seq }

		assertEquals(listOf(1L, 2L, 3L, 4L), seqs)
	}

	@Test
	fun eventsAtOrBelowTheStartingSeqAreNotEmitted() = runTest {
		val client = assistantHttpClient {
			sseAnswer(frame(2, progressJson(2)) + frame(3, runFinishedJson(3)))
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val seqs = source.events("t1", afterSeq = 2).toList().map { it.seq }

		assertEquals(listOf(3L), seqs)
	}

	@Test
	fun aCutResumesAfterTheLastSeqWithLastEventId() = runTest {
		val lastEventIds = mutableListOf<String?>()
		val client = assistantHttpClient { request ->
			lastEventIds += request.headers["Last-Event-ID"]

			if (lastEventIds.size == 1) {
				// The connection closes before the run ends.
				sseAnswer(frame(1, runStartedJson(1)) + frame(2, progressJson(2)))
			} else {
				sseAnswer(frame(3, progressJson(3)) + frame(4, runFinishedJson(4)))
			}
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val seqs = source.events("t1", afterSeq = 0).toList().map { it.seq }

		assertEquals(listOf(1L, 2L, 3L, 4L), seqs)
		assertEquals(listOf(null, "2"), lastEventIds)
	}

	@Test
	fun heartbeatsAreIgnored() = runTest {
		val client = assistantHttpClient {
			sseAnswer(HEARTBEAT + frame(1, runStartedJson(1)) + HEARTBEAT + frame(2, runFinishedJson(2)) + HEARTBEAT)
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val seqs = source.events("t1", afterSeq = 0).toList().map { it.seq }

		assertEquals(listOf(1L, 2L), seqs)
	}

	@Test
	fun exhaustedRetriesEndTheStreamWithoutAFinalEvent() = runTest {
		var attempts = 0
		val client = assistantHttpClient {
			attempts++
			sseAnswer("")
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val events = source.events("t1", afterSeq = 0).toList()

		// The first connection and five reconnections in a row. Nothing stands in for the final event the turn never sent.
		assertEquals(6, attempts)
		assertTrue(events.isEmpty())
	}

	@Test
	fun anEventResetsTheRetryCount() = runTest {
		var attempts = 0
		val client = assistantHttpClient {
			attempts++

			// The fifth connection delivers one event before it closes, so the count starts over after it.
			if (attempts == 5) sseAnswer(frame(1, runStartedJson(1))) else sseAnswer("")
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val events = source.events("t1", afterSeq = 0).toList()

		assertEquals(10, attempts)
		assertEquals(listOf(1L), events.map { it.seq })
	}

	@Test
	fun aServerErrorIsRetried() = runTest {
		var attempts = 0
		val client = assistantHttpClient {
			attempts++

			if (attempts == 1) {
				jsonAnswer(HttpStatusCode.InternalServerError, """{"code":"internal"}""")
			} else {
				sseAnswer(frame(1, runStartedJson(1)) + frame(2, runFinishedJson(2)))
			}
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val seqs = source.events("t1", afterSeq = 0).toList().map { it.seq }

		assertEquals(listOf(1L, 2L), seqs)
		assertEquals(2, attempts)
	}

	@Test
	fun aRunErrorEndsTheStreamWithoutRetrying() = runTest {
		var attempts = 0
		val client = assistantHttpClient {
			attempts++
			sseAnswer(frame(1, runStartedJson(1)) + frame(2, runErrorJson(2)))
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val events = source.events("t1", afterSeq = 0).toList()

		assertEquals(1, attempts)

		val expected = RunError(seq = 2, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true)

		assertEquals(AssistantStreamEvent.Contract(expected), events.last())
	}

	@Test
	fun assistantDisabledAnswerHoldsTheAssistantOffUntilTheRetryAfter() = runTest {
		val availability = AssistantAvailabilityDataSource(FakeAssistantConfigRepository(), VirtualClock(testScheduler))
		val client = assistantHttpClient {
			jsonAnswer(HttpStatusCode.ServiceUnavailable, """{"code":"assistant_disabled","retryAfterSeconds":60}""")
		}
		val source = SseAssistantEventsDataSource(client, availability)

		val failure = assertFailsWith<AssistantDisabledException> { source.events("t1", afterSeq = 0).toList() }

		assertEquals(60, failure.retryAfterSeconds)
		assertTrue(availability.currentAvailability().serverDisabled)

		advanceTimeBy(60.seconds)

		assertFalse(availability.currentAvailability().serverDisabled)
	}

	@Test
	fun aClientErrorEndsTheStreamWithoutRetrying() = runTest {
		var attempts = 0
		val client = assistantHttpClient {
			attempts++
			jsonAnswer(HttpStatusCode.NotFound, """{"code":"turn_not_found"}""")
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val failure = assertFailsWith<AssistantApiException> { source.events("t1", afterSeq = 0).toList() }

		assertEquals("turn_not_found", failure.code)
		assertEquals(404, failure.httpStatus)
		assertEquals(1, attempts)
	}

	// A resolution whose outcome this build does not know is not dropped: it reaches the app as an event of its own,
	// and that event closes the card.
	@Test
	fun anIntentResolvedWithAnOutcomeThisBuildDoesNotKnowBecomesAnUnresolvedCard() = runTest {
		val resolved = """{"type":"intent_resolved","seq":2,"turnId":"t1","intentId":"i1","outcome":"resultado_nuevo"}"""
		val client = assistantHttpClient {
			sseAnswer(frame(1, runStartedJson(1)) + frame(2, resolved) + frame(3, runFinishedJson(3)))
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val events = source.events("t1", afterSeq = 0).toList()

		assertEquals(listOf(1L, 2L, 3L), events.map { it.seq })
		assertEquals(AssistantStreamEvent.Own.ResolvedWithUnknownOutcome(2, "t1", "i1"), events[1])
	}

	// An outcome that is missing is no outcome this build knows: the resolution is read the same way.
	@Test
	fun anIntentResolvedWithoutAnOutcomeBecomesAnUnresolvedCard() = runTest {
		val resolved = """{"type":"intent_resolved","seq":2,"turnId":"t1","intentId":"i1"}"""
		val client = assistantHttpClient {
			sseAnswer(frame(1, runStartedJson(1)) + frame(2, resolved) + frame(3, runFinishedJson(3)))
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val events = source.events("t1", afterSeq = 0).toList()

		assertEquals(AssistantStreamEvent.Own.ResolvedWithUnknownOutcome(2, "t1", "i1"), events[1])
	}

	// A status this build does not know leaves a known outcome as it is: the resolution stays an event of the contract.
	@Test
	fun anIntentResolvedWithAKnownOutcomeAndAnUnknownStatusIsAContractEvent() = runTest {
		val resolved = """
			{"type":"intent_resolved","seq":2,"turnId":"t1","intentId":"i1",
			"outcome":"confirmed","status":"nuevo"}
		""".trimIndent().replace("\n", "")
		val client = assistantHttpClient {
			sseAnswer(frame(1, runStartedJson(1)) + frame(2, resolved) + frame(3, runFinishedJson(3)))
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val events = source.events("t1", afterSeq = 0).toList()

		assertEquals(AssistantStreamEvent.Contract(IntentResolved(2, "t1", "i1", OutcomeKind.CONFIRMED)), events[1])
	}

	// An unknown outcome whose envelope names no intent names nothing to close: the payload is skipped, as before.
	@Test
	fun anUnknownOutcomeWithoutAnIntentIdIsSkipped() = runTest {
		val resolved = """{"type":"intent_resolved","seq":2,"turnId":"t1","outcome":"resultado_nuevo"}"""
		val client = assistantHttpClient {
			sseAnswer(frame(1, runStartedJson(1)) + frame(2, resolved) + frame(3, runFinishedJson(3)))
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val seqs = source.events("t1", afterSeq = 0).toList().map { it.seq }

		assertEquals(listOf(1L, 3L), seqs)
	}

	@Test
	fun aComponentWithAValueThisBuildDoesNotKnowArrivesWithItsFallbackText() = runTest {
		// Written over lines for reading; the data field of a frame must be one line, so the breaks go.
		val emitted = """
			{"type":"component_emitted","seq":2,"turnId":"t1","messageId":"m1","component":
			{"type":"nav.abrir/1","id":"c1","fallbackText":"Abre el resumen","target":"destino_nuevo","label":"Abrir"}}
		""".trimIndent().replace("\n", "")
		val client = assistantHttpClient {
			sseAnswer(frame(1, runStartedJson(1)) + frame(2, emitted) + frame(3, runFinishedJson(3)))
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val events = source.events("t1", afterSeq = 0).toList()

		assertEquals(listOf(1L, 2L, 3L), events.map { it.seq })
		val component = UnknownChatComponent("c1", "Abre el resumen")

		assertEquals(AssistantStreamEvent.Contract(ComponentEmitted(2, "t1", "m1", component)), events[1])
	}

	// A proposal whose intent this build cannot read still names its message, so that the message can show the notice.
	@Test
	fun anIntentProposalWithAnIntentThisBuildCannotReadStillNamesItsMessage() = runTest {
		// Written over lines for reading; the data field of a frame must be one line, so the breaks go.
		val proposal = """
			{"type":"intent_proposed","seq":2,"turnId":"t1","messageId":"m1","intent":
			{"id":"intent-1","args":{"type":"evaluaciones.agregar/1","items":[],"source":"manual"},"title":"Agregar",
			"effect":[],"risk":"riesgo_nuevo","editable":[],"binding":{"mutationIds":[],"expectedRevision":12,"argsHash":"h1"},
			"expiresAtMillis":1760000600000,"status":"proposed","confirmLabel":"Agregar","cancelLabel":"Cancelar"}}
		""".trimIndent().replace("\n", "")
		val client = assistantHttpClient {
			sseAnswer(frame(1, runStartedJson(1)) + frame(2, proposal) + frame(3, runFinishedJson(3)))
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val events = source.events("t1", afterSeq = 0).toList()

		assertEquals(listOf(1L, 2L, 3L), events.map { it.seq })
		assertEquals(AssistantStreamEvent.Own.UnknownIntentProposal(2, "t1", "m1"), events[1])
	}

	// A payload this build cannot read is still skipped when it does not name an unreadable proposal: a message that does
	// not decode, and an event without its turn, both leave the stream as it was.
	@Test
	fun aPayloadThatNamesNoUnreadableProposalIsSkipped() = runTest {
		val messageWithoutMessage = """{"type":"message_completed","seq":2,"turnId":"t1","messageId":"m1"}"""
		val progressWithoutTurn = """{"type":"progress","seq":3}"""
		val client = assistantHttpClient {
			sseAnswer(
				frame(1, runStartedJson(1)) + frame(2, messageWithoutMessage) +
						frame(3, progressWithoutTurn) + frame(4, runFinishedJson(4))
			)
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val seqs = source.events("t1", afterSeq = 0).toList().map { it.seq }

		assertEquals(listOf(1L, 4L), seqs)
	}
}
