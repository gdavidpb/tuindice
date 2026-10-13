package com.gdavidpb.tuindice.evaluations.domain.model

data class EvaluationDisplayContext(
	val attempts: List<EditableAttemptDescriptor>,
	val currentTerm: EvaluationTermDescriptor?,
	// The current term has a class schedule: some attempt of it, of any grading mode, meets.
	val hasSchedule: Boolean = false
)
