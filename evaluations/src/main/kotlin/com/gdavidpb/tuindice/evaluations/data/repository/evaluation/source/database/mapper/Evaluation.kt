package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.EvaluationWithSubject

fun RemoteEvaluation.toLocalEvaluation() = LocalEvaluation(
	evaluationId = evaluationId,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	state = state
)

fun LocalEvaluation.toEvaluation() = Evaluation(
	evaluationId = evaluationId,
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
	evaluationId = evaluationId,
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
	id = evaluationId,
	subjectId = subjectId,
	quarterId = quarterId,
	accountId = uid,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type
)

fun Evaluation.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = evaluationId,
	subjectId = subjectId,
	quarterId = quarterId,
	accountId = uid,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type
)

fun EvaluationWithSubject.toEvaluation() = LocalEvaluation(
	evaluationId = evaluation.id,
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