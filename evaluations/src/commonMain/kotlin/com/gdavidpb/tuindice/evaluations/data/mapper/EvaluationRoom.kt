package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity

fun LocalEvaluation.toEvaluationEntity() = EvaluationEntity(
	id = id,
	referenceId = referenceId,
	attemptId = attemptId,
	subjectCode = subjectCode,
	termId = termId,
	revision = revision,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = isDone
)

fun EvaluationEntity.toLocalEvaluation() = LocalEvaluation(
	id = id,
	referenceId = referenceId,
	attemptId = attemptId,
	subjectCode = subjectCode,
	termId = termId,
	revision = revision,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = isDone
)
