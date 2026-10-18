package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.assistant.domain.mapper.toIntentExecution
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureKind
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome as ItemOutcome

/**
 * Runs the mutations of an intent and answers with one execution. [outcomes] is subscribed before the first mutation
 * runs, because the outcomes are not replayed to a late subscriber. A mutation whose use call fails is FAILED at once,
 * with the name of the error, and nothing is waited for it. The others are waited for up to
 * [MUTATION_OUTCOME_TIMEOUT_MILLIS]; the ones still running then are QUEUED, and [queued] takes them over before this
 * run stops listening, so their outcomes are followed in the session and none is lost in between. The execution follows
 * from how each mutation ended (toIntentExecution), and a batch carries the outcome of each.
 */
internal suspend fun settleMutations(
	mutations: List<AssistantMutation>,
	outcomes: Flow<MutationOutcome>,
	queued: AssistantQueuedMutationsDataSource
): IntentExecution = coroutineScope {
	val received = Channel<MutationOutcome>(Channel.UNLIMITED)
	val listener = launch(start = CoroutineStart.UNDISPATCHED) {
		outcomes.collect { outcome -> received.trySend(outcome) }
	}

	try {
		val refusals = mutations.map { mutation -> mutation to mutation.refusalOrNull() }
		val awaited = refusals
			.filter { (_, refusal) -> refusal == null }
			.mapTo(mutableSetOf()) { (mutation, _) -> mutation.mutationId }
		val settled = awaitOutcomes(awaited, received)

		val stillRunning = awaited - settled.keys
		if (stillRunning.isNotEmpty()) handOver(stillRunning, outcomes, queued, listener, received)

		refusals
			.map { (mutation, refusal) ->
				when {
					refusal != null -> ItemOutcome(mutation.mutationId, OutcomeKind.FAILED, refusal)
					else -> settled[mutation.mutationId]?.toItemOutcome()
						?: ItemOutcome(mutation.mutationId, OutcomeKind.QUEUED)
				}
			}
			.toIntentExecution()
			?: invalidArgs()
	} finally {
		listener.cancel()
		received.close()
	}
}

// The session follows [mutationIds] from here: it subscribes before this run stops listening, and then takes the
// outcomes this run received in between, so every outcome reaches one of the two.
private suspend fun handOver(
	mutationIds: Set<String>,
	outcomes: Flow<MutationOutcome>,
	queued: AssistantQueuedMutationsDataSource,
	listener: Job,
	received: ReceiveChannel<MutationOutcome>
) {
	queued.adopt(mutationIds, outcomes)
	listener.cancelAndJoin()

	while (true) {
		val outcome = received.tryReceive().getOrNull() ?: break
		if (outcome.mutationId in mutationIds) queued.settle(outcome)
	}
}

// The outcomes of the awaited mutations as they settle, up to the timeout. The outcomes of other mutations are dropped.
private suspend fun awaitOutcomes(
	awaited: Set<String>,
	received: ReceiveChannel<MutationOutcome>
): Map<String, MutationOutcome> {
	val settled = mutableMapOf<String, MutationOutcome>()
	if (awaited.isEmpty()) return settled

	withTimeoutOrNull(MUTATION_OUTCOME_TIMEOUT_MILLIS) {
		while (settled.size < awaited.size) {
			val outcome = received.receive()

			if (outcome.mutationId in awaited && outcome.mutationId !in settled) {
				settled[outcome.mutationId] = outcome
			}
		}
	}

	return settled
}

// The detail code the call was refused with, or null when the call ran: its outcome comes on the stream then.
private suspend fun AssistantMutation.refusalOrNull(): String? {
	return runCatching { call() }
		.map { state -> if (state is UseCaseState.Error) errorDetailCode(state.error) else null }
		.getOrElse { failure -> failureDetailCode(failure) }
}

// How one mutation ended, as the intent reports it.
internal fun MutationOutcome.toItemOutcome(): ItemOutcome {
	return when (kind) {
		MutationOutcomeKind.CONFIRMED -> ItemOutcome(mutationId, OutcomeKind.CONFIRMED)
		MutationOutcomeKind.CONFLICT -> ItemOutcome(mutationId, OutcomeKind.CONFLICT, detailCode)
		// The server no longer has what the mutation changes: it was changed elsewhere, which is a conflict.
		MutationOutcomeKind.REJECTED -> if (detailCode == NOT_FOUND_DETAIL) {
			ItemOutcome(mutationId, OutcomeKind.CONFLICT, detailCode)
		} else {
			ItemOutcome(mutationId, OutcomeKind.REJECTED, detailCode)
		}
		// A superseded or cancelled mutation did not land: it fails with the name of its result.
		MutationOutcomeKind.SUPERSEDED, MutationOutcomeKind.CANCELLED -> {
			ItemOutcome(mutationId, OutcomeKind.FAILED, kind.name)
		}
	}
}

// The detail the outbox ends a mutation with when the server no longer has its target.
private val NOT_FOUND_DETAIL = MutationFailureKind.NotFound.name
