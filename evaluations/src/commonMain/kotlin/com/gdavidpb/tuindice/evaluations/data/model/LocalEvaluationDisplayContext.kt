package com.gdavidpb.tuindice.evaluations.data.model

// The current term and the numeric attempts of THAT term, read together.
data class LocalEvaluationDisplayContext(
	val currentTerm: LocalCurrentTermDescriptor?,
	val attempts: List<LocalEditableAttemptDescriptor>
)
