package com.gdavidpb.tuindice.evaluations.domain.mapper

import com.gdavidpb.tuindice.base.utils.extension.generateReference
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams

fun AddEvaluationParams.toEvaluationAdd(name: String) = EvaluationAdd(
	reference = generateReference(),
	quarterId = quarterId!!,
	subjectId = subjectId!!,
	name = name,
	grade = grade!!,
	maxGrade = maxGrade!!,
	date = date!!,
	type = type!!,
	isCompleted = isCompleted!!
)

fun UpdateEvaluationParams.toEvaluationUpdate() = EvaluationUpdate(
	evaluationId = evaluationId!!,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isCompleted = isCompleted
)