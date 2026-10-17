package com.gdavidpb.tuindice.evaluations.domain.model

data class EvaluationRemove(
	val id: String,
	// The id the mutation is submitted with (the outbox's idempotency key). Generated when absent.
	val mutationId: String? = null
)