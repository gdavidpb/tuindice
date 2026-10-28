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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome as ItemOutcome

/**
 * Runs the mutations of an intent and answers with one execution. [outcomes] is subscribed before the first mutation
 * runs, because the outcomes are not replayed to a late subscriber. A mutation whose use call fails is FAILED at once,
 * with the name of the error, and nothing is waited for it. The others are waited for up to
 * [MUTATION_OUTCOME_TIMEOUT_MILLIS], counted from the start of the run, before the first call: the time the calls take
 * comes out of that wait, and no call is cut short by it. The ones still running when it ends are QUEUED, and [queued]
 * takes them over before this run stops listening, so their outcomes are followed in the session and none is lost in
 * between. The execution follows from how each mutation ended (toIntentExecution), and a batch carries the outcome of
 * each.
 */
internal suspend fun settleMutations(
	mutations: List<AssistantMutation>,
	outcomes: Flow<MutationOutcome>,
	queued: AssistantQueuedMutationsDataSource
): IntentExecution = coroutineScope {
	val received = Channel<MutationOutcome>(Channel.UNLIMITED)
	// Subscribed in the session before the first call, so no outcome that comes after is missed. A run that hands its
	// mutations over gives this subscription and the channel to the session; a run that does not stops them itself.
	val listener = queued.listen(outcomes, received)
	// The budget starts before the first call, so the calls take their time out of it.
	val budget = launch(start = CoroutineStart.UNDISPATCHED) {
		delay(MUTATION_OUTCOME_TIMEOUT_MILLIS)
	}

	var handedOver = false

	try {
		val refusals = mutations.map { mutation -> mutation to mutation.refusalOrNull() }
		val awaited = refusals
			.filter { (_, refusal) -> refusal == null }
			.mapTo(mutableSetOf()) { (mutation, _) -> mutation.mutationId }
		val settled = awaitOutcomes(awaited, received, budget)

		val stillRunning = awaited - settled.keys
		if (stillRunning.isNotEmpty()) {
			handOver(stillRunning, queued, listener, received)
			handedOver = true
		}

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
		budget.cancel()
		// Not handed over: nothing else follows the listener, so it stops here. Handed over: adopt stops it when the
		// followed mutations end.
		if (!handedOver) {
			listener.cancel()
			received.close()
		}
	}
}

// The session takes over the channel that the listener feeds, and the listener with it. Nothing is cancelled and
// nothing is subscribed again, so no outcome falls between two subscriptions. adopt stops the listener at the end.
private suspend fun handOver(
	mutationIds: Set<String>,
	queued: AssistantQueuedMutationsDataSource,
	listener: Job,
	received: ReceiveChannel<MutationOutcome>
) {
	queued.adopt(mutationIds, received.receiveAsFlow().onCompletion { listener.cancel() })
}

// The outcomes of the awaited mutations as they settle, until the budget ends. Other mutations' outcomes are dropped.
private suspend fun awaitOutcomes(
	awaited: Set<String>,
	received: ReceiveChannel<MutationOutcome>,
	budget: Job
): Map<String, MutationOutcome> {
	val settled = mutableMapOf<String, MutationOutcome>()

	while (settled.size < awaited.size) {
		// An outcome that came in while the calls ran is taken first, even when the budget is spent already.
		val outcome = received.tryReceive().getOrNull() ?: nextOutcomeWithin(received, budget) ?: break

		if (outcome.mutationId in awaited && outcome.mutationId !in settled) {
			settled[outcome.mutationId] = outcome
		}
	}

	return settled
}

// The next outcome that comes in, or null when the budget ends first.
private suspend fun nextOutcomeWithin(received: ReceiveChannel<MutationOutcome>, budget: Job): MutationOutcome? {
	return select<MutationOutcome?> {
		received.onReceive { outcome -> outcome }
		budget.onJoin { null }
	}
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
