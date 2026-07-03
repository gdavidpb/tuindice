package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.data.model.AddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.model.AddEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.DeleteEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.EvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.GetEvaluationsResponse
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.UpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.model.UpdateEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState

fun EvaluationResponse.toRemoteEvaluation() = RemoteEvaluation(
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

fun GetEvaluationsResponse.toRemoteEvaluationsSnapshot() = RemoteEvaluationsSnapshot(
	evaluations = evaluations.map { evaluation -> evaluation.toRemoteEvaluation() }
)

fun EvaluationMutation.Add.toAddEvaluationRequest(
	mutationId: String
) = AddEvaluationRequest(
	referenceId = referenceId,
	attemptId = attemptId,
	termId = termId,
	scheduleMode = scheduleMode,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type,
	isDone = (grade != null),
	mutationId = mutationId
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
	evaluation = evaluationPatch.toRemoteEvaluation()
)

fun UpdateEvaluationResponse.toMutationAck() = EvaluationMutationAck.Update(
	mutationId = mutationId,
	evaluation = evaluationPatch.toRemoteEvaluation()
)

fun DeleteEvaluationResponse.toMutationAck() = EvaluationMutationAck.Remove(
	mutationId = mutationId,
	removedEvaluationId = removedEvaluationId
)

fun RemoteEvaluation.toLocalEvaluation() = LocalEvaluation(
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

fun LocalEvaluation.toEvaluation() = Evaluation(
	id = id,
	attemptId = attemptId,
	subjectCode = subjectCode,
	termId = termId,
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
