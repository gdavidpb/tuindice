package com.gdavidpb.tuindice.evaluations.data.model

// The current term and the numeric attempts of THAT term, read together.
data class LocalEvaluationDisplayContext(
	val currentTerm: LocalCurrentTermDescriptor?,
	val attempts: List<LocalEditableAttemptDescriptor>,
	// Some attempt of the current term, of any grading mode, has a valid class meeting: the term has a
	// schedule to open, which the numeric attempts above cannot say on their own.
	val hasSchedule: Boolean = false
)
