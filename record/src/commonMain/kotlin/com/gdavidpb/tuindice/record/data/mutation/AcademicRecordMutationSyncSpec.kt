package com.gdavidpb.tuindice.record.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionFailed
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureKind
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureResolution
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationSyncSpec
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository

class AcademicRecordMutationSyncSpec(
	private val remoteDataSource: AcademicRecordRemoteDataRepository,
	private val persistConfirmedSnapshot: suspend (VersionedAcademicRecord) -> Unit,
	private val refreshRemoteSnapshot: suspend () -> VersionedAcademicRecord
) : MutationSyncSpec<String, AcademicRecordMutation, VersionedAcademicRecord> {
	override val maxRebaseAttempts: Int = 1

	override fun deletePendingBeforeConfirm(
		mutation: MutationEnvelope<String, AcademicRecordMutation>
	): Boolean = true

	override suspend fun send(
		mutation: MutationEnvelope<String, AcademicRecordMutation>
	): VersionedAcademicRecord {
		val expectedRevision = requireNotNull(mutation.expectedRevision) {
			"Record mutations require an expected revision."
		}

		return when (val command = mutation.command) {
			is AcademicRecordMutation.UpsertAttemptOverride ->
				remoteDataSource.upsertAttemptOverride(
					attemptId = command.attemptId,
					score = command.score,
					outcome = command.outcome,
					mutationId = mutation.mutationId,
					expectedRevision = expectedRevision
				)

			is AcademicRecordMutation.DeleteAttemptOverride ->
				remoteDataSource.deleteAttemptOverride(
					attemptId = command.attemptId,
					mutationId = mutation.mutationId,
					expectedRevision = expectedRevision
				)

			is AcademicRecordMutation.AddSyntheticTerm ->
				remoteDataSource.addSyntheticTerm(
					command = command,
					mutationId = mutation.mutationId,
					expectedRevision = expectedRevision
				)

			is AcademicRecordMutation.UpdateSyntheticTerm ->
				remoteDataSource.updateSyntheticTerm(
					command = command,
					mutationId = mutation.mutationId,
					expectedRevision = expectedRevision
				)

			is AcademicRecordMutation.DeleteSyntheticTerm ->
				remoteDataSource.deleteSyntheticTerm(
					termId = command.termId,
					mutationId = mutation.mutationId,
					expectedRevision = expectedRevision
				)
		}
	}

	override suspend fun confirm(
		mutation: MutationEnvelope<String, AcademicRecordMutation>,
		ack: VersionedAcademicRecord
	) {
		persistConfirmedSnapshot(ack)
	}

	override fun classifyError(
		mutation: MutationEnvelope<String, AcademicRecordMutation>,
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
		mutation: MutationEnvelope<String, AcademicRecordMutation>,
		throwable: Throwable
	): MutationFailureResolution<String, AcademicRecordMutation> {
		return when (val command = mutation.command) {
			is AcademicRecordMutation.UpsertAttemptOverride ->
				resolveUpsertAttemptOverrideFailure(
					mutation = mutation,
					command = command,
					throwable = throwable
				)

			is AcademicRecordMutation.DeleteAttemptOverride ->
				resolveDeleteAttemptOverrideFailure(
					mutation = mutation,
					command = command,
					throwable = throwable
				)

			is AcademicRecordMutation.AddSyntheticTerm,
			is AcademicRecordMutation.UpdateSyntheticTerm,
			is AcademicRecordMutation.DeleteSyntheticTerm ->
				resolveGenericRecordFailure(
					mutation = mutation,
					throwable = throwable
				)
		}
	}

	private suspend fun resolveUpsertAttemptOverrideFailure(
		mutation: MutationEnvelope<String, AcademicRecordMutation>,
		command: AcademicRecordMutation.UpsertAttemptOverride,
		throwable: Throwable
	): MutationFailureResolution<String, AcademicRecordMutation> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict,
			MutationFailureKind.PreconditionFailed -> {
				val refreshedSnapshot = refreshRemoteSnapshot()
				val remoteOverride = refreshedSnapshot.record.attemptOverrides.firstOrNull { override ->
					override.attemptId == command.attemptId
				}

				if (remoteOverride.matches(command)) {
					MutationFailureResolution.Drop()
				} else {
					MutationFailureResolution.Retry(
						mutation.copy(
							precondition = MutationPrecondition.Revision(refreshedSnapshot.revision)
						)
					)
				}
			}

			MutationFailureKind.NotFound -> {
				refreshRemoteSnapshot()
				MutationFailureResolution.Drop(propagate = true)
			}

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}

	private suspend fun resolveDeleteAttemptOverrideFailure(
		mutation: MutationEnvelope<String, AcademicRecordMutation>,
		command: AcademicRecordMutation.DeleteAttemptOverride,
		throwable: Throwable
	): MutationFailureResolution<String, AcademicRecordMutation> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict,
			MutationFailureKind.PreconditionFailed -> {
				val refreshedSnapshot = refreshRemoteSnapshot()
				val remoteOverride = refreshedSnapshot.record.attemptOverrides.firstOrNull { override ->
					override.attemptId == command.attemptId
				}

				if (remoteOverride == null) {
					MutationFailureResolution.Drop()
				} else {
					MutationFailureResolution.Retry(
						mutation.copy(
							precondition = MutationPrecondition.Revision(refreshedSnapshot.revision)
						)
					)
				}
			}

			MutationFailureKind.NotFound -> {
				refreshRemoteSnapshot()
				MutationFailureResolution.Drop()
			}

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}

	private suspend fun resolveGenericRecordFailure(
		mutation: MutationEnvelope<String, AcademicRecordMutation>,
		throwable: Throwable
	): MutationFailureResolution<String, AcademicRecordMutation> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict,
			MutationFailureKind.PreconditionFailed -> {
				val refreshedSnapshot = refreshRemoteSnapshot()
				MutationFailureResolution.Retry(
					mutation.copy(
						precondition = MutationPrecondition.Revision(refreshedSnapshot.revision)
					)
				)
			}

			MutationFailureKind.NotFound -> {
				refreshRemoteSnapshot()
				MutationFailureResolution.Drop(propagate = true)
			}

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}

	private fun AttemptOverride?.matches(
		command: AcademicRecordMutation.UpsertAttemptOverride
	): Boolean {
		return this?.score == command.score && this?.outcome == command.outcome
	}
}
