package com.gdavidpb.tuindice.evaluations.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.data.api.request.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.api.response.EvaluationResponse
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.record.data.api.mapper.toSubject
import java.util.Date

fun EvaluationResponse.toEvaluation() = Evaluation(
	evaluationId = id,
	subjectId = sid,
	quarterId = qid,
	grade = grade,
	maxGrade = maxGrade,
	date = Date(date),
	type = EvaluationType.values()[type],
	state = computeEvaluationState(grade = grade, date = date),
	subject = subject.toSubject(isEditable = true)
)

fun EvaluationResponse.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = id,
	subjectId = sid,
	quarterId = qid,
	accountId = uid,
	grade = grade,
	maxGrade = maxGrade,
	date = Date(date),
	type = EvaluationType.values()[type]
)

fun EvaluationAdd.toAddEvaluationRequest() = AddEvaluationRequest(
	reference = reference,
	subjectId = subjectId,
	maxGrade = maxGrade,
	date = date,
	type = type.ordinal
)

fun EvaluationUpdate.toAddEvaluationRequest() = UpdateEvaluationRequest(
	evaluationId = evaluationId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type?.ordinal
)