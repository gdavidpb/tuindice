package com.gdavidpb.tuindice.evaluations.domain.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState

fun AddEvaluationParams.toEvaluation(evaluationId: String) = Evaluation(
	evaluationId = evaluationId,
	subjectId = subjectId!!,
	subjectCode = subjectCode!!,
	quarterId = quarterId!!,
	grade = grade,
	maxGrade = maxGrade!!,
	date = date,
	type = type!!,
	state = computeEvaluationState(grade, date)
)

fun UpdateEvaluationParams.toEvaluationUpdate() = EvaluationUpdate(
	evaluationId = evaluationId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type
)