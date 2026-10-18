package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome as ItemOutcome

// How long the session follows a queued mutation that no intent asks about: the life of a proposal.
internal const val QUEUED_FOLLOW_LIMIT_MILLIS = 30 * 60 * 1_000L

/**
 * The mutations of the assistant's intents that were still running when their intent answered QUEUED. Each one has a
 * subscription to the outcomes of its domain in the session scope, which outlives the chat sheet: it is born when the
 * mutation is queued, ends as soon as the mutation settles, and is given up at the intent's deadline or after
 * [QUEUED_FOLLOW_LIMIT_MILLIS]. The end of the session cancels them all and forgets them.
 */
class AssistantQueuedMutationsDataSource(
	private val sessionCoroutineScope: SessionCoroutineScope,
	private val clock: Clock
) : AssistantQueuedMutationsRepository, SessionMemory {
	private val lock = Mutex()
	private val followers = mutableMapOf<String, CompletableDeferred<ItemOutcome>>()

	/**
	 * Follows [mutationIds] on [outcomes] in the session until each one settles. The subscription is made before this
	 * returns, so an outcome that comes after the call is never missed.
	 */
	internal suspend fun adopt(mutationIds: Set<String>, outcomes: Flow<MutationOutcome>) {
		val pending = lock.withLock {
			mutationIds.associateWith { mutationId -> followers.getOrPut(mutationId) { CompletableDeferred() } }
		}

		sessionCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
			withTimeoutOrNull(QUEUED_FOLLOW_LIMIT_MILLIS) {
				val listening = launch(start = CoroutineStart.UNDISPATCHED) {
					outcomes.collect { outcome -> pending[outcome.mutationId]?.complete(outcome.toItemOutcome()) }
				}

				// A follower ends when its mutation settles or when it is given up: then nothing is listened to anymore.
				pending.values.forEach { follower -> follower.join() }
				listening.cancel()
			}

			forget(pending)
		}
	}

	/** An outcome of a followed mutation that came before its subscription took over. */
	internal suspend fun settle(outcome: MutationOutcome) {
		lock.withLock { followers[outcome.mutationId]?.complete(outcome.toItemOutcome()) }
	}

	override fun followQueued(
		mutationIds: List<String>,
		untilMillis: Long,
		onSettled: suspend (List<ItemOutcome>) -> Unit
	) {
		sessionCoroutineScope.launch {
			val waiting = lock.withLock { mutationIds.mapNotNull { mutationId -> followers[mutationId] } }
			val left = (untilMillis - clock.now().toEpochMilliseconds()).coerceAtLeast(0L)

			withTimeoutOrNull(left) { waiting.forEach { follower -> follower.join() } }

			val settled = waiting
				.filter { follower -> follower.isCompleted && !follower.isCancelled }
				.map { follower -> follower.await() }
			lock.withLock { mutationIds.forEach { mutationId -> followers.remove(mutationId)?.cancel() } }

			if (settled.isNotEmpty()) onSettled(settled)
		}
	}

	override suspend fun clearSessionMemory() {
		lock.withLock {
			followers.values.forEach { follower -> follower.cancel() }
			followers.clear()
		}
	}

	// A follower given up past the limit is cancelled and forgotten; one that settled stays until its intent reads it.
	private suspend fun forget(pending: Map<String, CompletableDeferred<ItemOutcome>>) {
		lock.withLock {
			pending.forEach { (mutationId, follower) ->
				if (!follower.isCompleted) {
					follower.cancel()
					if (followers[mutationId] === follower) followers.remove(mutationId)
				}
			}
		}
	}
}
