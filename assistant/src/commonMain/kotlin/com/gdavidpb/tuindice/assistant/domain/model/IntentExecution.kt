package com.gdavidpb.tuindice.assistant.domain.model

import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind

/**
 * How a port that runs an intent ended it: its outcome, its detail code and the revision it left, when known.
 */
data class IntentExecution(
	val outcome: OutcomeKind,
	val detailCode: String? = null,
	val newRevision: Long? = null
)
