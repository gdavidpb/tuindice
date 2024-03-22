package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.request.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.response.EvaluationResponse
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState

fun EvaluationResponse.toRemoteEvaluation() = RemoteEvaluation(
	evaluationId = id,
	subjectId = sid,
	quarterId = qid,
	subjectCode = subject.code,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = EvaluationType.entries[type],
	state = computeEvaluationState(grade = grade, date = date)
)

fun Evaluation.toAddEvaluationRequest() = AddEvaluationRequest(
	reference = evaluationId,
	subjectId = subjectId,
	type = type.ordinal,
	grade = grade,
	maxGrade = maxGrade,
	date = date
)

fun EvaluationUpdate.toUpdateEvaluationRequest() = UpdateEvaluationRequest(
	evaluationId = evaluationId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type?.ordinal
)