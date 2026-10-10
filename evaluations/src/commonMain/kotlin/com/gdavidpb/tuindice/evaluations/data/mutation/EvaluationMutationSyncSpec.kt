package com.gdavidpb.tuindice.evaluations.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionFailed
import com.gdavidpb.tuindice.base.utils.extension.isTransient
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
import kotlinx.coroutines.CancellationException

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
		// A lost connection, or a failure that says nothing about the change (see isTransient: a response
		// is judged by its code alone: 426, 429, 502, 503, 504; plus the retry window and a refused
		// attestation), leaves the row Pending so the next drain sends it again. 408, 500 and the 4xx
		// answers stay out: they are about the request itself, whatever their body says.
		if (throwable.isTransient()) {
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
				val snapshot = refreshRemoteSnapshotSafely()
					.getOrElse { failure -> return failure.afterFailedRefresh() }
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
				val snapshot = refreshRemoteSnapshotSafely()
					.getOrElse { failure -> return failure.afterFailedRefresh() }
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
				val snapshot = refreshRemoteSnapshotSafely()
					.getOrElse { failure -> return failure.afterFailedRefresh() }
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
				refreshRemoteSnapshotSafely().onFailure { failure -> return failure.afterFailedRefresh() }
				databaseDataSource.discardLocalEvaluationCopy(command.evaluationId)
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
				val snapshot = refreshRemoteSnapshotSafely()
					.getOrElse { failure -> return failure.afterFailedRefresh() }
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
				val snapshot = refreshRemoteSnapshotSafely()
					.getOrElse { failure -> return failure.afterFailedRefresh() }
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
				refreshRemoteSnapshotSafely().onFailure { failure -> return failure.afterFailedRefresh() }
				databaseDataSource.discardLocalEvaluationCopy(command.evaluationId)
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

	/**
	 * The refresh that resolves a 409, 412 or 404. Its failure is returned, not swallowed, because what
	 * the row becomes depends on why it failed (see afterFailedRefresh); only a cancelled scope is
	 * rethrown, since it is not a failed refresh.
	 */
	private suspend fun refreshRemoteSnapshotSafely(): Result<RemoteEvaluationsSnapshot> {
		return runCatching { refreshRemoteSnapshot() }
			.onFailure { throwable -> if (throwable is CancellationException) throw throwable }
	}
}

// A refresh that fails because the service is away leaves the row Pending for the next drain, like the
// send itself would; any other failure is a verdict and parks the row.
private fun Throwable.afterFailedRefresh(): MutationFailureResolution<String, EvaluationMutation> {
	return if (isTransient()) MutationFailureResolution.Defer() else MutationFailureResolution.Fail()
}
