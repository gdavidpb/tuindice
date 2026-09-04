package com.gdavidpb.tuindice.record.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionFailed
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureKind
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureResolution
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationSyncSpec
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import kotlinx.coroutines.CancellationException

class AcademicRecordMutationSyncSpec(
	private val remoteDataSource: AcademicRecordRemoteDataRepository,
	private val persistConfirmedSnapshot: suspend (VersionedAcademicRecord) -> Unit,
	private val refreshRemoteSnapshot: suspend () -> VersionedAcademicRecord
) : MutationSyncSpec<String, AcademicRecordMutation, VersionedAcademicRecord> {
	override val maxRebaseAttempts: Int = 3

	// false: confirm() (which persists the confirmed snapshot to localDataSource) must
	// land BEFORE the outbox row is deleted. With the outbox emptied first, there is a
	// real window where neither source shows the mutation — confirmed data hasn't
	// landed yet and the overlay is already gone — and a reactive observer that reads
	// mid-window (e.g. ObserveRecordUseCase's own "persist resolved selection" side
	// effect racing Room's async invalidation dispatch) can act on a snapshot that
	// looks like the mutation never happened. evaluations already made this choice for
	// the same reason (EvaluationMutationSyncSpec.deletePendingBeforeConfirm).
	override fun deletePendingBeforeConfirm(
		mutation: MutationEnvelope<String, AcademicRecordMutation>
	): Boolean = false

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
		if (throwable.isConnection()) {
			return MutationFailureResolution.Defer()
		}

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
			is AcademicRecordMutation.UpdateSyntheticTerm ->
				resolveGenericRecordFailure(
					mutation = mutation,
					throwable = throwable
				)

			is AcademicRecordMutation.DeleteSyntheticTerm ->
				resolveGenericRecordFailure(
					mutation = mutation,
					throwable = throwable,
					// A term that is already gone is the end state this mutation wanted. Reporting it
					// as a failure shows the user an error for something that did happen, which is why
					// the attempt-override delete has always dropped quietly here.
					propagateNotFound = false
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
				val refreshedSnapshot = refreshRemoteSnapshotSafely()
					?: return MutationFailureResolution.Defer()
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
				refreshRemoteSnapshotSafely()
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
				val refreshedSnapshot = refreshRemoteSnapshotSafely()
					?: return MutationFailureResolution.Defer()
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
				refreshRemoteSnapshotSafely()
				MutationFailureResolution.Drop()
			}

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}

	private suspend fun resolveGenericRecordFailure(
		mutation: MutationEnvelope<String, AcademicRecordMutation>,
		throwable: Throwable,
		propagateNotFound: Boolean = true
	): MutationFailureResolution<String, AcademicRecordMutation> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict,
			MutationFailureKind.PreconditionFailed -> {
				val refreshedSnapshot = refreshRemoteSnapshotSafely()
					?: return MutationFailureResolution.Defer()
				MutationFailureResolution.Retry(
					mutation.copy(
						precondition = MutationPrecondition.Revision(refreshedSnapshot.revision)
					)
				)
			}

			MutationFailureKind.NotFound -> {
				refreshRemoteSnapshotSafely()
				MutationFailureResolution.Drop(propagate = propagateNotFound)
			}

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}

	// runCatching: the refresh is a network read that fails during the same degraded windows that
	// break sends. Decisional callers Defer on null — evaluations Fails there, but in record Fail
	// parks the row as silent FailedTerminal, and after the isConnection preamble the remaining
	// refresh failures are dominated by transient outages that self-heal on a later drain.
	// Reconciliation callers (NotFound) treat it as best-effort: the 404 already decided the
	// resolution, and a dropped envelope can transiently resurrect the stale local base state
	// until the next successful GET converges the snapshot.
	private suspend fun refreshRemoteSnapshotSafely(): VersionedAcademicRecord? {
		return runCatching { refreshRemoteSnapshot() }
			.onFailure { throwable -> if (throwable is CancellationException) throw throwable }
			.getOrNull()
	}

	private fun AttemptOverride?.matches(
		command: AcademicRecordMutation.UpsertAttemptOverride
	): Boolean {
		return this?.score == command.score && this?.outcome == command.outcome
	}
}
