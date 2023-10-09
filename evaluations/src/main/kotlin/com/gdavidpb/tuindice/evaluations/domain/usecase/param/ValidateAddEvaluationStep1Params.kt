package com.gdavidpb.tuindice.evaluations.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject

data class ValidateAddEvaluationStep1Params(
	val subject: Subject?,
	val type: EvaluationType?
)