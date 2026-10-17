package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.data.mapper.toAssistantException
import com.gdavidpb.tuindice.assistant.data.repository.AssistantEventsDataRepository
import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.assistant.utils.STREAM_RECONNECT_BACKOFF
import com.gdavidpb.tuindice.chatcontract.ChatContract
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.sse.ClientSSESession
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.sse.sseSession
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.encodeURLPathPart
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
import kotlinx.serialization.decodeFromString

class SseAssistantEventsDataSource(
	private val httpClient: HttpClient,
	private val serverDisabled: AssistantServerDisabledDataRepository
) : AssistantEventsDataRepository {

	// Connections follow one another from the last seq delivered, and an event at or below it is dropped. A connection
	// that ends before a final event is followed by another one after the backoff. When the retries run out, the stream
	// ends with a transient run_error that carries the next seq.
	override fun events(turnId: String, afterSeq: Long): Flow<AssistantEvent> = flow {
		val progress = StreamProgress(afterSeq)

		while (true) {
			emitAll(connect(turnId, progress.lastSeq).filter(progress::accept))

			if (progress.finished) return@flow

			if (progress.retries == STREAM_RECONNECT_BACKOFF.size) {
				emit(RunError(seq = progress.lastSeq + 1, turnId = turnId, code = RunErrorCode.TRANSIENT, retryable = true))
				return@flow
			}

			delay(STREAM_RECONNECT_BACKOFF[progress.retries])
			progress.countRetry()
		}
	}

	// One connection, from [afterSeq] to its end: its decoded events, up to and including the first final one.
	private fun connect(turnId: String, afterSeq: Long): Flow<AssistantEvent> = flow {
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
		val opened = runCatching {
			httpClient.sseSession(
				urlString = "assistant/v1/events/turns/${turnId.encodeURLPathPart()}",
				block = {
					if (afterSeq > 0) header(LAST_EVENT_ID_HEADER, afterSeq.toString())
				}
			)
		}
		val failure = opened.exceptionOrNull() ?: return opened.getOrNull()

		when (failure) {
			is CancellationException -> throw failure
			is SSEClientException -> failure.response?.let { response -> checkOpenFailure(response) }
			is ResponseException -> checkOpenFailure(failure.response)
		}

		return null
	}

	// Ends the stream for good on the answers that cannot succeed later. A server error is a broken attempt, like a cut.
	private suspend fun checkOpenFailure(response: HttpResponse) {
		val failure = response.toAssistantException(serverDisabled)

		if (failure is AssistantApiException && failure.httpStatus >= SERVER_ERROR_STATUS) return

		throw failure
	}

	// Comments and heartbeats carry no data, and a payload this build cannot read cannot be applied: both are skipped.
	private fun decodeEvent(data: String?): AssistantEvent? {
		if (data.isNullOrBlank()) return null

		return runCatching { ChatContract.json.decodeFromString<AssistantEvent>(data) }.getOrNull()
	}

	private companion object {
		const val LAST_EVENT_ID_HEADER = "Last-Event-ID"
		const val SERVER_ERROR_STATUS = 500
	}
}

// What the stream has delivered so far, across its connections.
private class StreamProgress(afterSeq: Long) {
	var lastSeq: Long = afterSeq
		private set

	var retries: Int = 0
		private set

	var finished: Boolean = false
		private set

	// A fresh event moves the cursor and restarts the retry count; a final event ends the stream.
	fun accept(event: AssistantEvent): Boolean {
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

private fun AssistantEvent.isTerminal(): Boolean = this is RunFinished || this is RunError
