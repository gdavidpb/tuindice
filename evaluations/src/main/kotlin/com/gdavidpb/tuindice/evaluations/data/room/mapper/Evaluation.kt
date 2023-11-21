package com.gdavidpb.tuindice.evaluations.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.mapper.toSubject
import com.gdavidpb.tuindice.persistence.data.room.otm.EvaluationWithSubject
import java.util.Date

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

fun EvaluationWithSubject.toEvaluation() = Evaluation(
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