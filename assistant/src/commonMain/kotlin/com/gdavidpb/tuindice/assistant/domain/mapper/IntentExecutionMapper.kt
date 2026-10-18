package com.gdavidpb.tuindice.assistant.domain.mapper

import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind

// The order a batch with nothing confirmed answers by, from the least severe outcome to the most severe one.
private val outcomeSeverity = listOf(
	OutcomeKind.CONFIRMED,
	OutcomeKind.QUEUED,
	OutcomeKind.FAILED,
	OutcomeKind.CONFLICT,
	OutcomeKind.REJECTED
)

/**
 * How an intent ends from how each of its mutations ended. It is queued while any mutation is still queued, since
 * its final outcome comes later. Otherwise it is confirmed when any mutation landed: all of them, or some of them (a
 * partial batch, which the server reads from the outcome of each mutation). With nothing landed, it answers with its
 * worst mutation and that mutation's detail: REJECTED over CONFLICT over FAILED; among mutations of the same weight,
 * the first one answers. A batch of more than one mutation carries the outcome of each. Null when no mutation ran.
 */
fun List<MutationOutcome>.toIntentExecution(): IntentExecution? {
	if (isEmpty()) return null

	val items = if (size > 1) this else emptyList()
	val kinds = map { item -> item.outcome }

	return when {
		OutcomeKind.QUEUED in kinds -> IntentExecution(OutcomeKind.QUEUED, itemOutcomes = items)
		OutcomeKind.CONFIRMED in kinds -> IntentExecution(OutcomeKind.CONFIRMED, itemOutcomes = items)
		else -> maxBy { item -> outcomeSeverity.indexOf(item.outcome) }.let { worst ->
			IntentExecution(worst.outcome, worst.detailCode, itemOutcomes = items)
		}
	}
}
