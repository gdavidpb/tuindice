package com.gdavidpb.tuindice.evaluations.domain.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState

fun AddEvaluationParams.toEvaluationAdd(reference: String) = EvaluationAdd(
	reference = reference,
	attemptId = attemptId!!,
	subjectCode = subjectCode!!,
	termId = termId!!,
	grade = grade,
	maxGrade = maxGrade!!,
	scheduleMode = scheduleMode,
	date = date,
	type = type!!
)

fun UpdateEvaluationParams.toEvaluationUpdate() = EvaluationUpdate(
	id = evaluationId,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type
)

fun EvaluationAdd.toEvaluation() = Evaluation(
	id = reference,
	attemptId = attemptId,
	subjectCode = subjectCode,
	termId = termId,
	scheduleMode = scheduleMode,
	type = type,
	date = date,
	grade = grade,
	maxGrade = maxGrade,
	state = computeEvaluationState(
		scheduleMode = scheduleMode,
		grade = grade,
		date = date
	)
)
