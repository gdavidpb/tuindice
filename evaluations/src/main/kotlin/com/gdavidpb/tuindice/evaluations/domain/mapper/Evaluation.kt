package com.gdavidpb.tuindice.evaluations.domain.mapper

import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams

fun AddEvaluationParams.toEvaluationAdd() = EvaluationAdd(
	subjectId = subjectId,
	name = name!!,
	grade = grade!!,
	maxGrade = maxGrade!!,
	date = date!!,
	type = type!!,
	isDone = isDone!!
)

fun UpdateEvaluationParams.toEvaluationUpdate() = EvaluationUpdate(
	evaluationId = evaluationId,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = isDone
)