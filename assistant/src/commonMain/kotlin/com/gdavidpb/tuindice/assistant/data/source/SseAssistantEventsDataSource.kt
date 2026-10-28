package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.data.exception.AssistantDisabledException
import com.gdavidpb.tuindice.assistant.data.mapper.toAssistantException
import com.gdavidpb.tuindice.assistant.data.model.AssistantStreamEvent
import com.gdavidpb.tuindice.assistant.data.repository.AssistantEventsDataRepository
import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.chatcontract.ChatContract
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.IntentProposed
import com.gdavidpb.tuindice.chatcontract.event.IntentResolved
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.sse.ClientSSESession
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.sse.sseSession
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformWhile
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class SseAssistantEventsDataSource(
	private val httpClient: HttpClient,
	private val serverDisabled: AssistantServerDisabledDataRepository
) : AssistantEventsDataRepository {

	// Connections follow one another from the last seq delivered, and an event at or below it is dropped. A connection
	// that ends before a final event is followed by another one after the backoff. When the retries run out, the stream
	// ends without a final event: the result of the turn is unknown, and nothing is written in its place.
	override fun events(turnId: String, afterSeq: Long): Flow<AssistantStreamEvent> = flow {
		val progress = StreamProgress(afterSeq)

		while (true) {
			emitAll(connect(turnId, progress.lastSeq).filter(progress::accept))

			if (progress.finished) return@flow

			if (progress.retries == STREAM_RECONNECT_BACKOFF.size) return@flow

			delay(STREAM_RECONNECT_BACKOFF[progress.retries])
			progress.countRetry()
		}
	}

	// One connection, from [afterSeq] to its end: its decoded events, up to and including the first final one.
	private fun connect(turnId: String, afterSeq: Long): Flow<AssistantStreamEvent> = flow {
		val session = openSession(turnId, afterSeq) ?: return@flow

		try {
			emitAll(
				session.incoming
					// A broken connection ends the attempt like a closed one; the reconnection decides what follows.
					.catch { cause -> if (cause is CancellationException) throw cause }
					.mapNotNull { raw -> decodeEvent(raw.data) }
					.transformWhile { event ->
						emit(event)
						!event.isTerminal()
					}
			)
		} finally {
			session.cancel()
		}
	}

	// Opens the stream at [afterSeq]. Null means the connection failed in a way worth another attempt.
	private suspend fun openSession(turnId: String, afterSeq: Long): ClientSSESession? {
		// The session the connection belongs to, read before it opens (see send).
		val sessionMark = serverDisabled.sessionMark()
		val opened = runCatching {
			httpClient.sseSession(
				urlString = assistantPath("events", "turns", turnId),
				block = {
					if (afterSeq > 0) header(LAST_EVENT_ID_HEADER, afterSeq.toString())
				}
			)
		}
		val failure = opened.exceptionOrNull() ?: return opened.getOrNull()

		when (failure) {
			is CancellationException -> throw failure
			is SSEClientException -> failure.response?.let { response -> checkOpenFailure(response, sessionMark) }
			is ResponseException -> checkOpenFailure(failure.response, sessionMark)
		}

		return null
	}

	// Ends the stream for good on the answers that cannot succeed later. A server error is a broken attempt, like a cut.
	private suspend fun checkOpenFailure(response: HttpResponse, sessionMark: Int) {
		val failure = response.toAssistantException()

		if (failure is AssistantDisabledException) {
			serverDisabled.markServerDisabled(failure.retryAfterSeconds, sessionMark)
		}

		if (failure is AssistantApiException && failure.httpStatus >= SERVER_ERROR_STATUS) return

		throw failure
	}

	// Comments and heartbeats carry no data, and a payload this build cannot read cannot be applied: both are skipped,
	// except a payload that names what it changes, which the app's own events stand for.
	private fun decodeEvent(data: String?): AssistantStreamEvent? {
		if (data.isNullOrBlank()) return null

		return runCatching { AssistantStreamEvent.Contract(ChatContract.json.decodeFromString<AssistantEvent>(data)) }
			.getOrElse { ownEventOf(data) }
	}

	// A payload this build cannot read in full names what it changes when its envelope says so: an intent proposal names
	// its message, and a resolution whose outcome this build does not know, or lacks, names its intent. Any other payload
	// is skipped, as before.
	private fun ownEventOf(data: String): AssistantStreamEvent.Own? {
		val envelope = runCatching { ChatContract.json.decodeFromString(EventEnvelope.serializer(), data) }.getOrNull()
			?: return null

		return when (envelope.type) {
			INTENT_PROPOSED_TYPE -> envelope.messageId?.let { messageId ->
				AssistantStreamEvent.Own.UnknownIntentProposal(envelope.seq, envelope.turnId, messageId)
			}

			INTENT_RESOLVED_TYPE -> envelope.intentId?.takeIf { envelope.hasUnknownOutcome() }?.let { intentId ->
				AssistantStreamEvent.Own.ResolvedWithUnknownOutcome(envelope.seq, envelope.turnId, intentId)
			}

			else -> null
		}
	}

	// An outcome this build does not know, or none at all, cannot be read as a resolution: the envelope reads it instead.
	private fun EventEnvelope.hasUnknownOutcome(): Boolean = outcome == null || outcome !in KNOWN_OUTCOMES

	private companion object {
		const val LAST_EVENT_ID_HEADER = "Last-Event-ID"

		// The wire names read from the contract, so that the app and the contract cannot drift apart.
		val INTENT_PROPOSED_TYPE: String = IntentProposed.serializer().descriptor.serialName
		val INTENT_RESOLVED_TYPE: String = IntentResolved.serializer().descriptor.serialName

		// The outcomes this build knows, by their wire names. An outcome outside them is one this build does not know.
		val KNOWN_OUTCOMES: Set<String> = OutcomeKind.serializer().descriptor.let { descriptor ->
			(0 until descriptor.elementsCount).map { index -> descriptor.getElementName(index) }.toSet()
		}
	}
}

// The fields that name an event, read without the rest of its payload: the rest may be what this build cannot read.
@Serializable
private class EventEnvelope(
	val type: String,
	val seq: Long,
	val turnId: String,
	val messageId: String? = null,
	val intentId: String? = null,
	val outcome: String? = null
)

// What the stream has delivered so far, across its connections.
private class StreamProgress(afterSeq: Long) {
	var lastSeq: Long = afterSeq
		private set

	var retries: Int = 0
		private set

	var finished: Boolean = false
		private set

	// A fresh event moves the cursor and restarts the retry count; a final event ends the stream.
	fun accept(event: AssistantStreamEvent): Boolean {
		if (event.seq <= lastSeq) return false

		lastSeq = event.seq
		retries = 0
		finished = event.isTerminal()

		return true
	}

	fun countRetry() {
		retries++
	}
}

// Only a final event of the contract ends a stream: the app's own events never do.
private fun AssistantStreamEvent.isTerminal(): Boolean {
	return this is AssistantStreamEvent.Contract && (event is RunFinished || event is RunError)
}
