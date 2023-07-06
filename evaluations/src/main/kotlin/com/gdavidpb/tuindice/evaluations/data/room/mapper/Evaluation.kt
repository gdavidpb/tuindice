package com.gdavidpb.tuindice.evaluations.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.utils.extension.isOverdue
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.mapper.toSubject
import com.gdavidpb.tuindice.persistence.data.room.otm.EvaluationWithSubject
import java.util.Date

fun Evaluation.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = evaluationId,
	subjectId = subjectId,
	quarterId = quarterId,
	accountId = uid,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	lastModified = lastModified,
	type = type,
	isCompleted = isCompleted
)

fun EvaluationAdd.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = reference,
	subjectId = subjectId,
	quarterId = quarterId,
	accountId = uid,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = Date(date),
	lastModified = Date(date),
	type = type,
	isCompleted = isCompleted
)

fun EvaluationWithSubject.toEvaluation() = Evaluation(
	evaluationId = evaluation.id,
	subjectId = evaluation.subjectId,
	quarterId = evaluation.quarterId,
	name = evaluation.name,
	grade = evaluation.grade,
	maxGrade = evaluation.maxGrade,
	date = evaluation.date,
	lastModified = evaluation.lastModified,
	type = evaluation.type,
	subject = subject.toSubject(isEditable = true),
	isCompleted = evaluation.isCompleted,
	isOverdue = evaluation.date.isOverdue()
)