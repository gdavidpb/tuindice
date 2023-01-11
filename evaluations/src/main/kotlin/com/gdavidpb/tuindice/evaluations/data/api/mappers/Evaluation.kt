package com.gdavidpb.tuindice.evaluations.data.api.mappers

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.data.api.bodies.EvaluationBody
import com.gdavidpb.tuindice.evaluations.data.api.responses.EvaluationResponse
import java.util.*

fun EvaluationResponse.toEvaluation() = Evaluation(
	id = id,
	sid = sid,
	qid = qid,
	subjectCode = subjectCode,
	notes = notes,
	grade = grade,
	maxGrade = maxGrade,
	date = Date(date),
	lastModified = Date(lastModified),
	type = EvaluationType.values()[type],
	isDone = isDone
)

fun Evaluation.toEvaluationBody() = EvaluationBody(
	sid = sid,
	qid = qid,
	notes = notes,
	grade = grade,
	maxGrade = maxGrade,
	date = date.time,
	type = type.ordinal,
	isDone = isDone
)