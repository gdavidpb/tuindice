package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.subject.Subject

data class EvaluationAndAvailableSubjects(
	val evaluation: Evaluation,
	val availableSubjects: List<Subject>
)