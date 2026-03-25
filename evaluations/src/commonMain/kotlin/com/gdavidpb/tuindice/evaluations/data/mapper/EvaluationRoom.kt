package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.otm.EvaluationWithSubject

fun LocalEvaluation.toEvaluationEntity() = EvaluationEntity(
	id = id,
	referenceId = referenceId,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	revision = revision,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = isDone
)

fun EvaluationWithSubject.toLocalEvaluation() = LocalEvaluation(
	id = evaluation.id,
	referenceId = evaluation.referenceId,
	subjectId = evaluation.subjectId,
	subjectCode = subject.code,
	quarterId = evaluation.quarterId,
	revision = evaluation.revision,
	scheduleMode = evaluation.scheduleMode,
	grade = evaluation.grade,
	maxGrade = evaluation.maxGrade,
	date = evaluation.date,
	type = evaluation.type,
	isDone = evaluation.isDone
)
