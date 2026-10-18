package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerDisabledRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

/**
 * Follows the events of a turn into the conversation, and holds the assistant off when the server disables it.
 *
 * A stream that ends before the final event of the turn is followed once more from the last seq it reached: that
 * check tells where the turn stands. When the check ends the same way, the turn is left unconfirmed, because its
 * result is not known, and no event is made up for it. A failure the stream broke with is rethrown after that, so it
 * is reported.
 */
class AssistantTurnStream(
	private val turnRepository: AssistantTurnRepository,
	private val conversationRepository: AssistantConversationRepository,
	private val serverDisabledRepository: AssistantServerDisabledRepository
) {
	fun follow(turnId: String, afterSeq: Long): Flow<AssistantEvent> = flow {
		val progress = TurnProgress(afterSeq)

		var failure = followFrom(turnId, progress)

		if (!progress.finished) {
			failure = followFrom(turnId, progress) ?: failure
		}

		if (!progress.finished) {
			conversationRepository.markTurnUnconfirmed(turnId, progress.lastSeq)

			failure?.let { cause -> throw cause }
		}
	}

	// One follow from the last seq reached. Each event is applied to the conversation before it is emitted. Returns the
	// failure the stream broke with, when it did.
	private suspend fun FlowCollector<AssistantEvent>.followFrom(turnId: String, progress: TurnProgress): Throwable? {
		var failure: Throwable? = null

		turnRepository.turnEvents(turnId = turnId, afterSeq = progress.lastSeq)
			.catch { cause ->
				if (cause is CancellationException) throw cause

				failure = cause
			}
			.collect { event ->
				if (progress.finished || event.seq <= progress.lastSeq) return@collect

				conversationRepository.applyEvent(event)

				if (event is RunError && event.code == RunErrorCode.ASSISTANT_DISABLED) {
					serverDisabledRepository.markServerDisabled(event.retryAfterSeconds)
				}

				progress.advance(event)

				emit(event)
			}

		return failure
	}
}

// What the follows of a turn have delivered: the last seq reached, and whether its final event came.
private class TurnProgress(afterSeq: Long) {
	var lastSeq: Long = afterSeq
		private set

	var finished: Boolean = false
		private set

	fun advance(event: AssistantEvent) {
		lastSeq = event.seq
		finished = finished || event is RunFinished || event is RunError
	}
}
