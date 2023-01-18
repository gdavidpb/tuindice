package com.gdavidpb.tuindice.evaluations.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.persistence.data.source.room.entities.EvaluationEntity

fun Evaluation.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = evaluationId,
	subjectId = subjectId,
	quarterId = quarterId,
	accountId = uid,
	subjectCode = subjectCode,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	lastModified = lastModified,
	type = type,
	isDone = isDone
)

fun EvaluationEntity.toEvaluation() = Evaluation(
	evaluationId = id,
	subjectId = subjectId,
	quarterId = quarterId,
	subjectCode = subjectCode,
	name = name,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	lastModified = lastModified,
	type = type,
	isDone = isDone
)