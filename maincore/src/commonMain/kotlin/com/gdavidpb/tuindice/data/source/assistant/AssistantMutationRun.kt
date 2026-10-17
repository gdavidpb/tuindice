package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Runs the mutations of an intent and answers with one execution. [outcomes] is subscribed before the first mutation
 * runs, because the outcomes are not replayed to a late subscriber. A mutation whose use call fails is FAILED at once,
 * with the name of the error, and nothing is waited for it. The others are waited for up to
 * [MUTATION_OUTCOME_TIMEOUT_MILLIS]; the ones still running then are QUEUED. The batch answers with its worst item.
 */
internal suspend fun settleMutations(
	mutations: List<AssistantMutation>,
	outcomes: Flow<MutationOutcome>
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

		refusals
			.map { (mutation, refusal) ->
				when {
					refusal != null -> IntentExecution(OutcomeKind.FAILED, refusal)
					else -> settled[mutation.mutationId]?.toIntentExecution() ?: IntentExecution(OutcomeKind.QUEUED)
				}
			}
			.worst()
	} finally {
		listener.cancel()
		received.close()
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

private fun MutationOutcome.toIntentExecution(): IntentExecution {
	return when (kind) {
		MutationOutcomeKind.CONFIRMED -> IntentExecution(OutcomeKind.CONFIRMED)
		MutationOutcomeKind.CONFLICT -> IntentExecution(OutcomeKind.CONFLICT, detailCode)
		MutationOutcomeKind.REJECTED -> IntentExecution(OutcomeKind.REJECTED, detailCode)
		// A superseded or cancelled mutation did not land: it fails with the name of its result.
		MutationOutcomeKind.SUPERSEDED, MutationOutcomeKind.CANCELLED -> IntentExecution(OutcomeKind.FAILED, kind.name)
	}
}

// The order a batch answers by, from the least severe outcome to the most severe one.
private val outcomeSeverity = listOf(
	OutcomeKind.CONFIRMED,
	OutcomeKind.QUEUED,
	OutcomeKind.FAILED,
	OutcomeKind.CONFLICT,
	OutcomeKind.REJECTED
)

// The batch answers with its worst item: REJECTED over CONFLICT over FAILED over QUEUED over CONFIRMED.
// Among items of the same weight, the first one answers. An empty batch has nothing that could have run.
internal fun List<IntentExecution>.worst(): IntentExecution {
	return maxByOrNull { execution -> outcomeSeverity.indexOf(execution.outcome) }
		?: invalidArgs()
}
