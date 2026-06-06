package com.gdavidpb.tuindice.evaluations.domain.model

data class EvaluationDisplayContext(
	val attempts: List<EditableAttemptDescriptor>,
	val currentTerm: EvaluationTermDescriptor?
)
