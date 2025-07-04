package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.model.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.model.EvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.EvaluationWithSubject

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

fun RemoteEvaluation.toAddEvaluationRequest() = AddEvaluationRequest(
	reference = id,
	subjectId = subjectId,
	type = type.ordinal,
	grade = grade,
	maxGrade = maxGrade,
	date = date
)

fun RemoteEvaluation.toUpdateEvaluationRequest() = UpdateEvaluationRequest(
	evaluationId = id,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type.ordinal
)

fun Evaluation.toRemoteEvaluation() = RemoteEvaluation(
	id = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	state = computeEvaluationState(grade = grade, date = date)
)

fun RemoteEvaluation.toLocalEvaluation() = LocalEvaluation(
	id = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	state = state
)

fun LocalEvaluation.toLocalEvaluation() = Evaluation(
	id = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	state = state
)

fun Evaluation.toLocalEvaluation() = LocalEvaluation(
	id = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	state = state
)

fun LocalEvaluation.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = id,
	subjectId = subjectId,
	quarterId = quarterId,
	accountId = uid,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type
)

fun EvaluationWithSubject.toLocalEvaluation() = LocalEvaluation(
	id = evaluation.id,
	subjectId = evaluation.subjectId,
	subjectCode = subject.code,
	quarterId = evaluation.quarterId,
	grade = evaluation.grade,
	maxGrade = evaluation.maxGrade,
	date = evaluation.date,
	type = evaluation.type,
	state = computeEvaluationState(
		grade = evaluation.grade,
		date = evaluation.date
	)
)