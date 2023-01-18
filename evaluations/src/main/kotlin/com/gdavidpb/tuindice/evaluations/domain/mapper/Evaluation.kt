package com.gdavidpb.tuindice.evaluations.domain.mapper

import com.gdavidpb.tuindice.evaluations.domain.model.NewEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.UpdateEvaluation
import com.gdavidpb.tuindice.evaluations.domain.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams

fun AddEvaluationParams.toNewEvaluation() = NewEvaluation(
	subjectId = subjectId,
	name = name!!,
	grade = grade!!,
	maxGrade = maxGrade!!,
	date = date!!,
	type = type!!,
	isDone = isDone!!
)

fun UpdateEvaluationParams.toUpdateEvaluation() = UpdateEvaluation(
	evaluationId = evaluationId,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = isDone
)