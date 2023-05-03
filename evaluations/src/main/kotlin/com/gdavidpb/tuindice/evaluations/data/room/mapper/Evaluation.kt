package com.gdavidpb.tuindice.evaluations.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import java.util.Date

fun Evaluation.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = evaluationId,
	subjectId = subjectId,
	quarterId = quarterId,
	accountId = uid,
	subjectCode = subjectCode,
	subjectName = subjectName,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	lastModified = lastModified,
	type = type,
	isDone = isDone
)

fun EvaluationAdd.toEvaluationEntity(
	uid: String,
	subjectCode: String,
	subjectName: String
) = EvaluationEntity(
	id = reference,
	subjectId = subjectId,
	quarterId = quarterId,
	accountId = uid,
	subjectCode = subjectCode,
	subjectName = subjectName,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = Date(date),
	lastModified = Date(date),
	type = type,
	isDone = isDone
)

fun EvaluationEntity.toEvaluation() = Evaluation(
	evaluationId = id,
	subjectId = subjectId,
	quarterId = quarterId,
	subjectCode = subjectCode,
	subjectName = subjectName,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	lastModified = lastModified,
	type = type,
	isDone = isDone
)