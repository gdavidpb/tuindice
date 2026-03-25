package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.data.model.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.model.AddEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.DeleteEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.EvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.GetEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.GetEvaluationsResponse
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.model.UpdateEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState

fun EvaluationResponse.toRemoteEvaluation() = RemoteEvaluation(
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

fun GetEvaluationsResponse.toRemoteEvaluationsSnapshot() = RemoteEvaluationsSnapshot(
	anchorRevision = anchorRevision,
	evaluations = evaluations.map { evaluation -> evaluation.toRemoteEvaluation() }
)

fun GetEvaluationResponse.toRemoteEvaluationsSnapshot() = RemoteEvaluationsSnapshot(
	anchorRevision = anchorRevision,
	evaluations = listOf(evaluation.toRemoteEvaluation())
)

fun EvaluationMutation.Add.toAddEvaluationRequest(
	mutationId: String,
	expectedRevision: Long
) = AddEvaluationRequest(
	referenceId = referenceId,
	subjectId = subjectId,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = (grade != null),
	mutationId = mutationId,
	expectedRevision = expectedRevision
)

fun EvaluationMutation.Update.toUpdateEvaluationRequest(
	mutationId: String,
	expectedRevision: Long
) = UpdateEvaluationRequest(
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = grade != null,
	mutationId = mutationId,
	expectedRevision = expectedRevision
)

fun AddEvaluationResponse.toMutationAck() = EvaluationMutationAck.Add(
	mutationId = mutationId,
	anchorRevision = anchorRevision,
	evaluation = evaluationPatch.toRemoteEvaluation()
)

fun UpdateEvaluationResponse.toMutationAck() = EvaluationMutationAck.Update(
	mutationId = mutationId,
	anchorRevision = anchorRevision,
	evaluation = evaluationPatch.toRemoteEvaluation()
)

fun DeleteEvaluationResponse.toMutationAck() = EvaluationMutationAck.Remove(
	mutationId = mutationId,
	anchorRevision = anchorRevision,
	removedEvaluationId = removedEvaluationId
)

fun Evaluation.toRemoteEvaluation() = RemoteEvaluation(
	id = id,
	referenceId = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	revision = 0L,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type.ordinal,
	isDone = (state == EvaluationState.COMPLETED)
)

fun RemoteEvaluation.toLocalEvaluation() = LocalEvaluation(
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
	referenceId = id,
	subjectId = subjectId,
	subjectCode = subjectCode,
	quarterId = quarterId,
	revision = 0L,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type.ordinal,
	isDone = (state == EvaluationState.COMPLETED)
)
