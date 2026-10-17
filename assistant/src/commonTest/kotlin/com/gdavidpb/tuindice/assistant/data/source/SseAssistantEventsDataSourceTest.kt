package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.data.exception.AssistantDisabledException
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
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

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
	fun exhaustedRetriesEndWithATransientRunError() = runTest {
		var attempts = 0
		val client = assistantHttpClient {
			attempts++
			sseAnswer("")
		}
		val source = SseAssistantEventsDataSource(client, RecordingServerDisabledRepository())

		val events = source.events("t1", afterSeq = 0).toList()

		// The first connection and five reconnections in a row.
		assertEquals(6, attempts)
		assertEquals(
			listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true)),
			events
		)
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
		assertEquals(listOf(1L, 2L), events.map { it.seq })
		assertEquals(RunError(seq = 2, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true), events.last())
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
		assertEquals(RunError(seq = 2, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true), events.last())
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
}
