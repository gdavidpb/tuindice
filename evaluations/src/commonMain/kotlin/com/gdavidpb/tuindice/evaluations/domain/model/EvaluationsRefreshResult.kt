package com.gdavidpb.tuindice.evaluations.domain.model

data class EvaluationsRefreshResult(
	val hasEvaluations: Boolean,
	val hasAvailableAttempts: Boolean,
	// The current term has a class schedule (see EvaluationDisplayContext.hasSchedule).
	val hasSchedule: Boolean = false
)
