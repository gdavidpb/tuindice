package com.gdavidpb.tuindice.evaluations.domain.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState

fun AddEvaluationParams.toEvaluationAdd(reference: String) = EvaluationAdd(
	reference = reference,
	subjectId = subjectId!!,
	subjectCode = subjectCode!!,
	quarterId = quarterId!!,
	grade = grade,
	maxGrade = maxGrade!!,
	date = date,
	type = type!!
)

fun UpdateEvaluationParams.toEvaluationUpdate() = EvaluationUpdate(
	id = evaluationId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type
)

fun EvaluationAdd.toEvaluation() = Evaluation(
	id = reference,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	type = type,
	date = date,
	grade = grade,
	maxGrade = maxGrade,
	state = computeEvaluationState(grade, date)
)