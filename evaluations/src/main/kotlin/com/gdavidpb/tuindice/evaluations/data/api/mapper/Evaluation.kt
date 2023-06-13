package com.gdavidpb.tuindice.evaluations.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.data.api.request.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.request.RemoveEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.response.EvaluationResponse
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.utils.extension.isOverdue
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import java.util.Date

fun EvaluationResponse.toEvaluation() = Evaluation(
	evaluationId = id,
	subjectId = sid,
	quarterId = qid,
	subjectCode = subjectCode,
	subjectName = subjectName,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = Date(date),
	lastModified = Date(lastModified),
	type = EvaluationType.values()[type],
	isCompleted = isCompleted,
	isOverdue = Date(date).isOverdue()
)

fun EvaluationResponse.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = id,
	subjectId = sid,
	quarterId = qid,
	accountId = uid,
	subjectCode = subjectCode,
	subjectName = subjectName,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = Date(date),
	lastModified = Date(lastModified),
	isCompleted = isCompleted,
	type = EvaluationType.values()[type]
)

fun EvaluationAdd.toAddEvaluationRequest() = AddEvaluationRequest(
	reference = reference,
	subjectId = subjectId,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type.ordinal,
	isCompleted = isCompleted
)

fun EvaluationUpdate.toAddEvaluationRequest() = UpdateEvaluationRequest(
	evaluationId = evaluationId,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type?.ordinal,
	isCompleted = isCompleted
)

fun EvaluationRemove.toRemoveEvaluationRequest() = RemoveEvaluationRequest(
	evaluationId = evaluationId
)