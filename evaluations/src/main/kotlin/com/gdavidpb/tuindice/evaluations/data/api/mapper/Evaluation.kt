package com.gdavidpb.tuindice.evaluations.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.data.api.request.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.response.EvaluationResponse
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import java.util.*

fun EvaluationResponse.toEvaluation() = Evaluation(
	evaluationId = id,
	subjectId = sid,
	quarterId = qid,
	subjectCode = subjectCode,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = Date(date),
	lastModified = Date(lastModified),
	type = EvaluationType.values()[type],
	isDone = isDone
)

fun EvaluationAdd.toAddEvaluationRequest() = AddEvaluationRequest(
	subjectId = subjectId,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type.ordinal,
	isDone = isDone
)

fun EvaluationUpdate.toAddEvaluationRequest() = UpdateEvaluationRequest(
	evaluationId = evaluationId,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type?.ordinal,
	isDone = isDone
)