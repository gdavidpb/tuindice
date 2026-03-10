package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.data.model.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.model.EvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState

fun EvaluationResponse.toRemoteEvaluation() = RemoteEvaluation(
	id = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = isDone
)

fun RemoteEvaluation.toAddEvaluationRequest() = AddEvaluationRequest(
	subjectId = subjectId,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = isDone
)

fun RemoteEvaluation.toUpdateEvaluationRequest() = UpdateEvaluationRequest(
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = isDone
)

fun Evaluation.toRemoteEvaluation() = RemoteEvaluation(
	id = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type.ordinal,
	isDone = (state == EvaluationState.COMPLETED)
)

fun RemoteEvaluation.toLocalEvaluation() = LocalEvaluation(
	id = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = isDone
)

fun LocalEvaluation.toEvaluation() = Evaluation(
	id = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = EvaluationType.entries[type],
	state = computeEvaluationState(
		scheduleMode = scheduleMode,
		grade = grade,
		date = date
	)
)

fun Evaluation.toLocalEvaluation() = LocalEvaluation(
	id = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type.ordinal,
	isDone = (state == EvaluationState.COMPLETED)
)
