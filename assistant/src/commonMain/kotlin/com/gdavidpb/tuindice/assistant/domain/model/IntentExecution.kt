package com.gdavidpb.tuindice.assistant.domain.model

import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind

/**
 * How a port that runs an intent ended it: its outcome, its detail code and the revision it left, when known. A batch
 * of more than one mutation also carries how each of them ended, in [itemOutcomes]; a single mutation is its own
 * answer.
 */
data class IntentExecution(
	val outcome: OutcomeKind,
	val detailCode: String? = null,
	val newRevision: Long? = null,
	val itemOutcomes: List<MutationOutcome> = emptyList()
)
