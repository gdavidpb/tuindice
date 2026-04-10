package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

data class EvaluationAndAvailableAttempts(
	val evaluation: Evaluation?,
	val availableAttempts: List<EditableAttemptDescriptor>
)
