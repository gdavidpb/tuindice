package com.gdavidpb.tuindice.evaluations.domain.usecase.param

data class RemoveEvaluationParams(
	val evaluationId: String,
	// The id the mutation is submitted with (the outbox's idempotency key). Generated when absent.
	val mutationId: String? = null
)
