package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.request.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.response.EvaluationResponse
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState

fun EvaluationResponse.toRemoteEvaluation() = RemoteEvaluation(
	id = id,
	subjectId = sid,
	subjectCode = subject.code,
	quarterId = qid,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = EvaluationType.entries[type],
	state = computeEvaluationState(grade = grade, date = date)
)

fun EvaluationAdd.toAddEvaluationRequest() = AddEvaluationRequest(
	reference = reference,
	subjectId = subjectId,
	type = type.ordinal,
	grade = grade,
	maxGrade = maxGrade,
	date = date
)

fun EvaluationUpdate.toUpdateEvaluationRequest() = UpdateEvaluationRequest(
	evaluationId = id,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type?.ordinal
)