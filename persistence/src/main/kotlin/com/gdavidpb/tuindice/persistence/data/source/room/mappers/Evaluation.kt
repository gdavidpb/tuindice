package com.gdavidpb.tuindice.persistence.data.source.room.mappers

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.persistence.data.source.room.entities.EvaluationEntity

fun Evaluation.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = id,
	subjectId = sid,
	quarterId = qid,
	accountId = uid,
	subjectCode = subjectCode,
	notes = notes,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	lastModified = lastModified,
	type = type,
	isDone = isDone
)

fun EvaluationEntity.toEvaluation() = Evaluation(
	id = id,
	sid = subjectId,
	qid = quarterId,
	subjectCode = subjectCode,
	notes = notes,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	lastModified = lastModified,
	type = type,
	isDone = isDone
)