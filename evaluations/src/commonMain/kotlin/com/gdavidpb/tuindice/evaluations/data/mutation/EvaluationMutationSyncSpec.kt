package com.gdavidpb.tuindice.evaluations.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionFailed
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataRepository
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureKind
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureResolution
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationSyncSpec

class EvaluationMutationSyncSpec(
	private val databaseDataSource: DatabaseDataRepository,
	private val evaluationsApiDataSource: EvaluationsApiDataRepository,
	private val refreshRemoteSnapshot: suspend () -> RemoteEvaluationsSnapshot
) : MutationSyncSpec<String, EvaluationMutation, EvaluationMutationAck> {
	override val maxRebaseAttempts: Int = 3

	override fun deletePendingBeforeConfirm(
		mutation: MutationEnvelope<String, EvaluationMutation>
	): Boolean = false

	override suspend fun send(
		mutation: MutationEnvelope<String, EvaluationMutation>
	): EvaluationMutationAck {
		return when (val command = mutation.command) {
			is EvaluationMutation.Add ->
				evaluationsApiDataSource.addEvaluation(
					add = command,
					mutationId = mutation.mutationId
				)

			is EvaluationMutation.Update ->
				evaluationsApiDataSource.updateEvaluation(
					update = command,
					mutationId = mutation.mutationId,
					expectedRevision = mutation.expectedRevision
						?: error("Update evaluation requires revision.")
				)

			is EvaluationMutation.Remove ->
				evaluationsApiDataSource.removeEvaluation(
					eid = command.evaluationId,
					mutationId = mutation.mutationId,
					expectedRevision = mutation.expectedRevision
						?: error("Remove evaluation requires revision.")
				)
		}
	}

	override suspend fun confirm(
		mutation: MutationEnvelope<String, EvaluationMutation>,
		ack: EvaluationMutationAck
	) {
		when (ack) {
			is EvaluationMutationAck.Add -> {
				if (ack.mutationId != mutation.mutationId) return
				databaseDataSource.confirmAddedEvaluation(
					evaluation = ack.evaluation.toLocalEvaluation()
				)
			}

			is EvaluationMutationAck.Update -> {
				if (ack.mutationId != mutation.mutationId) return
				databaseDataSource.confirmUpdatedEvaluation(
					evaluation = ack.evaluation.toLocalEvaluation()
				)
			}

			is EvaluationMutationAck.Remove -> {
				if (ack.mutationId != mutation.mutationId) return
				databaseDataSource.confirmEvaluationRemoval(
					eid = ack.removedEvaluationId
				)
			}
		}
	}

	override fun classifyError(
		mutation: MutationEnvelope<String, EvaluationMutation>,
		throwable: Throwable
	): MutationFailureKind {
		return when {
			throwable.isConflict() -> MutationFailureKind.Conflict
			throwable.isPreconditionFailed() -> MutationFailureKind.PreconditionFailed
			throwable.isNotFound() -> MutationFailureKind.NotFound
			else -> MutationFailureKind.Terminal
		}
	}

	override suspend fun resolveFailure(
		mutation: MutationEnvelope<String, EvaluationMutation>,
		throwable: Throwable
	): MutationFailureResolution<String, EvaluationMutation> {
		if (throwable.isConnection()) {
			return MutationFailureResolution.Defer()
		}

		return when (val command = mutation.command) {
			is EvaluationMutation.Add ->
				resolveAddFailure(mutation, command, throwable)

			is EvaluationMutation.Update ->
				resolveUpdateFailure(mutation, command, throwable)

			is EvaluationMutation.Remove ->
				resolveRemoveFailure(mutation, command, throwable)
		}
	}

	private suspend fun resolveAddFailure(
		mutation: MutationEnvelope<String, EvaluationMutation>,
		command: EvaluationMutation.Add,
		throwable: Throwable
	): MutationFailureResolution<String, EvaluationMutation> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict -> {
				val snapshot = refreshRemoteSnapshotSafely() ?: return MutationFailureResolution.Fail()
				val remoteMatch = snapshot.evaluations.firstOrNull { evaluation ->
					evaluation.referenceId == command.referenceId
				}

				if (remoteMatch != null) {
					MutationFailureResolution.Drop()
				} else {
					MutationFailureResolution.Retry(
						mutation.copy(
							precondition = MutationPrecondition.None
						)
					)
				}
			}

			MutationFailureKind.PreconditionFailed,
			MutationFailureKind.NotFound -> {
				val snapshot = refreshRemoteSnapshotSafely() ?: return MutationFailureResolution.Fail()
				val remoteMatch = snapshot.evaluations.firstOrNull { evaluation ->
					evaluation.referenceId == command.referenceId
				}

				if (remoteMatch != null) {
					MutationFailureResolution.Drop()
				} else {
					MutationFailureResolution.Drop(propagate = true)
				}
			}

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}

	private suspend fun resolveUpdateFailure(
		mutation: MutationEnvelope<String, EvaluationMutation>,
		command: EvaluationMutation.Update,
		throwable: Throwable
	): MutationFailureResolution<String, EvaluationMutation> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict,
			MutationFailureKind.PreconditionFailed -> {
				val snapshot = refreshRemoteSnapshotSafely() ?: return MutationFailureResolution.Fail()
				val remoteEvaluation = snapshot.evaluations
					.firstOrNull { evaluation -> evaluation.id == command.evaluationId }

				when {
					remoteEvaluation == null ->
						MutationFailureResolution.Drop()

					remoteEvaluation.matches(command) ->
						MutationFailureResolution.Drop()

					else ->
						MutationFailureResolution.Retry(
							mutation.copy(
								precondition = MutationPrecondition.Revision(remoteEvaluation.revision)
							)
						)
				}
			}

			MutationFailureKind.NotFound -> {
				databaseDataSource.discardLocalEvaluationCopy(command.evaluationId)
				refreshRemoteSnapshotSafely() ?: return MutationFailureResolution.Fail()
				MutationFailureResolution.Drop(propagate = true)
			}

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}

	private suspend fun resolveRemoveFailure(
		mutation: MutationEnvelope<String, EvaluationMutation>,
		command: EvaluationMutation.Remove,
		throwable: Throwable
	): MutationFailureResolution<String, EvaluationMutation> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict -> {
				val snapshot = refreshRemoteSnapshotSafely() ?: return MutationFailureResolution.Fail()
				val remoteEvaluation = snapshot.evaluations
					.firstOrNull { evaluation -> evaluation.id == command.evaluationId }

				if (remoteEvaluation == null) {
					databaseDataSource.discardLocalEvaluationCopy(command.evaluationId)
					MutationFailureResolution.Drop()
				} else {
					MutationFailureResolution.Retry(
						mutation.copy(
							precondition = MutationPrecondition.Revision(remoteEvaluation.revision)
						)
					)
				}
			}

			MutationFailureKind.PreconditionFailed -> {
				val snapshot = refreshRemoteSnapshotSafely() ?: return MutationFailureResolution.Fail()
				val remoteEvaluation = snapshot.evaluations
					.firstOrNull { evaluation -> evaluation.id == command.evaluationId }

				if (remoteEvaluation == null) {
					databaseDataSource.discardLocalEvaluationCopy(command.evaluationId)
					MutationFailureResolution.Drop()
				} else {
					MutationFailureResolution.Drop(propagate = true)
				}
			}

			MutationFailureKind.NotFound -> {
				databaseDataSource.discardLocalEvaluationCopy(command.evaluationId)
				refreshRemoteSnapshotSafely() ?: return MutationFailureResolution.Fail()
				MutationFailureResolution.Drop(propagate = true)
			}

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}

	private fun RemoteEvaluation.matches(
		command: EvaluationMutation.Update
	): Boolean {
		val resolvedScheduleMode = command.scheduleMode ?: if (command.date != null) {
			EvaluationScheduleMode.DATED
		} else {
			scheduleMode
		}
		val resolvedDate = when (resolvedScheduleMode) {
			EvaluationScheduleMode.CONTINUOUS -> null
			EvaluationScheduleMode.DATED -> command.date ?: date
		}

		return scheduleMode == resolvedScheduleMode &&
			grade == command.grade &&
			maxGrade == (command.maxGrade ?: maxGrade) &&
			date == resolvedDate &&
			type == (command.type ?: type)
	}

	private suspend fun refreshRemoteSnapshotSafely(): RemoteEvaluationsSnapshot? {
		return runCatching { refreshRemoteSnapshot() }.getOrNull()
	}
}
