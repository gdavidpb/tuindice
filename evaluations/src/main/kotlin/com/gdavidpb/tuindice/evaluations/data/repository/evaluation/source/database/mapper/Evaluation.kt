package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.mapper.toSubject
import com.gdavidpb.tuindice.persistence.data.room.otm.EvaluationWithSubject

fun RemoteEvaluation.toEvaluation() = Evaluation(
	evaluationId = evaluationId,
	subjectId = subjectId,
	quarterId = quarterId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	state = state,
	subject = subject
)

fun RemoteEvaluation.toLocalEvaluation() = LocalEvaluation(
	evaluationId = evaluationId,
	subjectId = subjectId,
	quarterId = quarterId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	state = state,
	subject = subject
)

fun LocalEvaluation.toEvaluation() = Evaluation(
	evaluationId = evaluationId,
	subjectId = subjectId,
	quarterId = quarterId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	state = state,
	subject = subject
)

fun Evaluation.toLocalEvaluation() = LocalEvaluation(
	evaluationId = evaluationId,
	subjectId = subjectId,
	quarterId = quarterId,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	state = state,
	subject = subject
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

fun EvaluationAdd.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = reference,
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
	quarterId = evaluation.quarterId,
	grade = evaluation.grade,
	maxGrade = evaluation.maxGrade,
	date = evaluation.date,
	type = evaluation.type,
	state = computeEvaluationState(
		grade = evaluation.grade,
		date = evaluation.date
	),
	subject = subject.toSubject(isEditable = true)
)