package com.gdavidpb.tuindice.evaluations.data.mutation

import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionFailed
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.contract.DatabaseDataSource
import com.gdavidpb.tuindice.evaluations.data.contract.EvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureKind
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureResolution
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationSyncSpec

class EvaluationMutationSyncSpec(
	private val databaseDataSource: DatabaseDataSource,
	private val evaluationsApiDataSource: EvaluationsApiDataSource,
	private val refreshRemoteSnapshot: suspend () -> RemoteEvaluationsSnapshot
) : MutationSyncSpec<String, EvaluationMutation, LocalEvaluationsSnapshot, List<com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation>, EvaluationMutationAck> {
	override val maxRebaseAttempts: Int = 3

	override suspend fun send(
		mutation: MutationEnvelope<String, EvaluationMutation>
	): EvaluationMutationAck {
		return when (val command = mutation.command) {
			is EvaluationMutation.Add ->
				evaluationsApiDataSource.addEvaluation(
					add = command,
					mutationId = mutation.mutationId,
					expectedRevision = mutation.expectedRevision
						?: error("Add evaluation requires anchor revision.")
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
						?: error("Remove evaluation requires anchor revision.")
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
					evaluation = ack.evaluation.toLocalEvaluation(),
					anchorRevision = ack.anchorRevision
				)
			}

			is EvaluationMutationAck.Update -> {
				if (ack.mutationId != mutation.mutationId) return
				databaseDataSource.confirmUpdatedEvaluation(
					evaluation = ack.evaluation.toLocalEvaluation(),
					anchorRevision = ack.anchorRevision
				)
			}

			is EvaluationMutationAck.Remove -> {
				if (ack.mutationId != mutation.mutationId) return
				databaseDataSource.confirmRemovedEvaluation(
					eid = ack.removedEvaluationId,
					anchorRevision = ack.anchorRevision
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
		val snapshot = refreshRemoteSnapshot()
		val remoteMatch = snapshot.evaluations.firstOrNull { evaluation ->
			evaluation.referenceId == command.referenceId
		}

		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict -> {
				if (remoteMatch != null) {
					MutationFailureResolution.Drop()
				} else {
					MutationFailureResolution.Retry(
						mutation.copy(
							precondition = MutationPrecondition.Revision(snapshot.anchorRevision)
						)
					)
				}
			}

			MutationFailureKind.PreconditionFailed,
			MutationFailureKind.NotFound -> {
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
				val remoteEvaluation = refreshRemoteSnapshot().evaluations
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
				databaseDataSource.removeConfirmedEvaluation(command.evaluationId)
				refreshRemoteSnapshot()
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
				val remoteEvaluation = refreshRemoteSnapshot().evaluations
					.firstOrNull { evaluation -> evaluation.id == command.evaluationId }

				if (remoteEvaluation == null) {
					databaseDataSource.removeConfirmedEvaluation(command.evaluationId)
					MutationFailureResolution.Drop()
				} else {
					MutationFailureResolution.Retry(
						mutation.copy(
							precondition = MutationPrecondition.Revision(
								refreshRemoteSnapshot().anchorRevision
							)
						)
					)
				}
			}

			MutationFailureKind.PreconditionFailed -> {
				val remoteEvaluation = refreshRemoteSnapshot().evaluations
					.firstOrNull { evaluation -> evaluation.id == command.evaluationId }

				if (remoteEvaluation == null) {
					databaseDataSource.removeConfirmedEvaluation(command.evaluationId)
					MutationFailureResolution.Drop()
				} else {
					MutationFailureResolution.Drop(propagate = true)
				}
			}

			MutationFailureKind.NotFound -> {
				databaseDataSource.removeConfirmedEvaluation(command.evaluationId)
				refreshRemoteSnapshot()
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
			com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode.DATED
		} else {
			scheduleMode
		}
		val resolvedDate = when (resolvedScheduleMode) {
			com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode.CONTINUOUS -> null
			com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode.DATED -> command.date ?: date
		}

		return scheduleMode == resolvedScheduleMode &&
			grade == command.grade &&
			maxGrade == (command.maxGrade ?: maxGrade) &&
			date == resolvedDate &&
			type == (command.type ?: type)
	}
}
