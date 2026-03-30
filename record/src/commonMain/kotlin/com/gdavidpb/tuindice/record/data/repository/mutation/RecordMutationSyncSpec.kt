package com.gdavidpb.tuindice.record.data.repository.mutation

import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionFailed
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureKind
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureResolution
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationSyncSpec
import com.gdavidpb.tuindice.record.data.source.QuarterLocalDataSource
import com.gdavidpb.tuindice.record.data.source.QuarterRemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteAddQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSetSubjectGradeAck
import com.gdavidpb.tuindice.record.data.source.database.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.domain.policy.QuarterMutationPolicy

class RecordMutationSyncSpec(
	private val localDataSource: QuarterLocalDataSource,
	private val remoteDataSource: QuarterRemoteDataSource,
	private val refreshRemoteSnapshot: suspend () -> List<RemoteQuarter>
) : MutationSyncSpec<String, RecordMutation, List<LocalQuarter>, List<LocalQuarter>, RecordMutationAck> {
	override val maxRebaseAttempts: Int = 3

	override fun deletePendingBeforeConfirm(
		mutation: MutationEnvelope<String, RecordMutation>
	): Boolean {
		return mutation.command !is RecordMutation.RemoveQuarter
	}

	override suspend fun send(
		mutation: MutationEnvelope<String, RecordMutation>
	): RecordMutationAck {
		return when (val command = mutation.command) {
			is RecordMutation.AddQuarter ->
				remoteDataSource.addQuarter(
					add = command,
					mutationId = mutation.mutationId,
					expectedRevision = mutation.expectedRevision
						?: error("AddQuarter requires a revision precondition.")
				)

			is RecordMutation.SetSubjectGrade ->
				remoteDataSource.setSubjectGrade(
					qid = command.quarterId,
					sid = command.subjectId,
					grade = command.grade,
					mutationId = mutation.mutationId,
					expectedRevision = mutation.expectedRevision
						?: error("SetSubjectGrade requires a revision precondition.")
				)

			is RecordMutation.RemoveQuarter ->
				remoteDataSource.removeQuarter(
					qid = command.quarterId,
					mutationId = mutation.mutationId,
					expectedRevision = mutation.expectedRevision
						?: error("RemoveQuarter requires a revision precondition.")
				)
		}
	}

	override suspend fun confirm(
		mutation: MutationEnvelope<String, RecordMutation>,
		ack: RecordMutationAck
	) {
		when (val command = mutation.command) {
			is RecordMutation.AddQuarter -> {
				val addAck = ack as? RemoteAddQuarterAck
					?: return
				if (addAck.mutationId != mutation.mutationId) return

				localDataSource.confirmQuarterAddition(
					addedQuarter = addAck.quarter.toLocalQuarter(),
					affectedQuarters = addAck.affectedQuarters.map { quarter -> quarter.toLocalQuarter() }
				)
			}

			is RecordMutation.SetSubjectGrade -> {
				val subjectAck = ack as? RemoteSetSubjectGradeAck
					?: return
				if (subjectAck.mutationId != mutation.mutationId) return

				localDataSource.confirmSubjectGradeMutation(
					subjectAck.affectedQuarters.map { quarter -> quarter.toLocalQuarter() }
				)
			}

			is RecordMutation.RemoveQuarter -> {
				val deleteAck = ack as? RemoteDeleteQuarterAck
					?: return
				if (deleteAck.mutationId != mutation.mutationId) return

				localDataSource.confirmQuarterRemoval(
					qid = command.quarterId,
					affectedQuarters = deleteAck.affectedQuarters.map { quarter -> quarter.toLocalQuarter() }
				)
			}
		}
	}

	override fun classifyError(
		mutation: MutationEnvelope<String, RecordMutation>,
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
		mutation: MutationEnvelope<String, RecordMutation>,
		throwable: Throwable
	): MutationFailureResolution<String, RecordMutation> {
		return when (val command = mutation.command) {
			is RecordMutation.AddQuarter ->
				resolveAddQuarterFailure(
					mutation = mutation,
					throwable = throwable
				)

			is RecordMutation.SetSubjectGrade ->
				resolveSetSubjectGradeFailure(
					mutation = mutation,
					command = command,
					throwable = throwable
				)

			is RecordMutation.RemoveQuarter ->
				resolveRemoveQuarterFailure(
					mutation = mutation,
					command = command,
					throwable = throwable
				)
		}
	}

	private suspend fun resolveAddQuarterFailure(
		mutation: MutationEnvelope<String, RecordMutation>,
		throwable: Throwable
	): MutationFailureResolution<String, RecordMutation> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict,
			MutationFailureKind.PreconditionFailed,
			MutationFailureKind.NotFound -> {
				refreshRemoteSnapshot()
				MutationFailureResolution.Drop(propagate = true)
			}

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}

	private suspend fun resolveSetSubjectGradeFailure(
		mutation: MutationEnvelope<String, RecordMutation>,
		command: RecordMutation.SetSubjectGrade,
		throwable: Throwable
	): MutationFailureResolution<String, RecordMutation> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict,
			MutationFailureKind.PreconditionFailed -> {
				val remoteSubject = refreshRemoteSnapshot()
					.firstOrNull { quarter -> quarter.id == command.quarterId }
					?.subjects
					?.firstOrNull { subject -> subject.id == command.subjectId }

				when {
					remoteSubject == null ->
						MutationFailureResolution.Drop()

					remoteSubject.grade == command.grade ->
						MutationFailureResolution.Drop()

					else ->
						MutationFailureResolution.Retry(
							mutation.copy(
								precondition = MutationPrecondition.Revision(remoteSubject.revision)
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

	private suspend fun resolveRemoveQuarterFailure(
		mutation: MutationEnvelope<String, RecordMutation>,
		command: RecordMutation.RemoveQuarter,
		throwable: Throwable
	): MutationFailureResolution<String, RecordMutation> {
		return when (classifyError(mutation, throwable)) {
			MutationFailureKind.Conflict -> {
				val remoteQuarter = refreshRemoteSnapshot()
					.firstOrNull { quarter -> quarter.id == command.quarterId }

				when {
					remoteQuarter == null -> {
						localDataSource.removeQuarter(command.quarterId)
						MutationFailureResolution.Drop()
					}

					!QuarterMutationPolicy.canDelete(
						isCurrent = remoteQuarter.isCurrent,
						isReadOnly = remoteQuarter.isReadOnly
					) -> MutationFailureResolution.Drop()

					else -> MutationFailureResolution.Retry(
						mutation.copy(
							precondition = MutationPrecondition.Revision(remoteQuarter.revision)
						)
					)
				}
			}

			MutationFailureKind.NotFound -> {
				localDataSource.removeQuarter(command.quarterId)
				refreshRemoteSnapshot()
				MutationFailureResolution.Drop(propagate = true)
			}

			MutationFailureKind.PreconditionFailed -> {
				refreshRemoteSnapshot()
				MutationFailureResolution.Drop(propagate = true)
			}

			MutationFailureKind.Terminal ->
				MutationFailureResolution.Fail()
		}
	}
}
