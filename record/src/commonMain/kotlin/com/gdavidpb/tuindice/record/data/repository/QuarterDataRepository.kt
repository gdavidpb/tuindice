package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.MutationOutboxRepository
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionFailed
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.repository.quarter.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.mapper.toQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.SetSubjectGradeResult
import com.gdavidpb.tuindice.record.domain.policy.QuarterMutationPolicy
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class QuarterDataRepository(
	private val localDataSource: QuarterLocalDataSource,
	private val remoteDataSource: QuarterRemoteDataSource,
	private val settingsDataSource: QuarterSettingsDataSource,
	private val mutationOutboxRepository: MutationOutboxRepository<RecordMutation>,
	private val identifierRepository: IdentifierRepository
) : QuarterRepository {
	private val drainMutex = Mutex()

	override suspend fun observeQuartersFlow(): Flow<List<Quarter>> {
		return localDataSource.getQuartersFlow()
			.map { localQuarters -> localQuarters.map { it.toQuarter() } }
	}

	override suspend fun updateQuarters() {
		val isOnCooldown = settingsDataSource.isGetQuartersOnCooldown()

		if (!isOnCooldown) {
			val remoteQuarters = remoteDataSource.getQuarters()
			localDataSource.saveQuarters(remoteQuarters.map { quarter -> quarter.toLocalQuarter() })
			settingsDataSource.setGetQuartersOnCooldown()
		}

		drainPendingMutations(propagateTerminalErrors = false)
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		val quarter = localDataSource.getQuarter(remove.id)
			?: return
		if (!QuarterMutationPolicy.canDelete(quarter)) return
		val mutation = buildPendingRemoveQuarterMutation(
			quarterId = remove.id,
			expectedRevision = quarter.revision
		)

		mutationOutboxRepository.replacePendingMutation(mutation)
		drainPendingMutations(
			propagateTerminalErrors = true,
			targetMutationId = mutation.mutationId
		)
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		val quarter = localDataSource.getQuarter(set.quarterId)
			?: return
		if (!QuarterMutationPolicy.canEditGrades(quarter)) return

		val localResult = localDataSource.setSubjectGradeAndRecompute(
			qid = set.quarterId,
			sid = set.id,
			grade = set.grade,
			commit = set.commit
		)

		if (!set.commit) return
		val appliedResult = localResult as? SetSubjectGradeResult.Applied ?: return
		val expectedRevision = appliedResult.expectedRevision
		val existing = mutationOutboxRepository.getPendingMutations()
			.firstOrNull { mutation -> mutation.mutation.replaceKey == "subject:${set.id}" }

		if (appliedResult.updatedQuarters.isEmpty() && existing == null) return

		val mutation = buildPendingSetSubjectGradeMutation(
			quarterId = set.quarterId,
			subjectId = set.id,
			grade = set.grade,
			expectedRevision = expectedRevision
		)

		mutationOutboxRepository.replacePendingMutation(mutation)
		drainPendingMutations(
			propagateTerminalErrors = true,
			targetMutationId = mutation.mutationId
		)
	}

	private suspend fun drainPendingMutations(
		propagateTerminalErrors: Boolean,
		targetMutationId: String? = null
	) {
		drainMutex.withLock {
			val pendingMutations = mutationOutboxRepository.getPendingMutations()

			pendingMutations.forEach { mutation ->
				if (targetMutationId != null && mutation.mutationId != targetMutationId) return@forEach

				when (mutation.mutation) {
					is RecordMutation.AddQuarter -> Unit

					is RecordMutation.SetSubjectGrade ->
						drainSetSubjectGradeMutation(
							mutation = mutation,
							propagateTerminalErrors = propagateTerminalErrors
						)

					is RecordMutation.RemoveQuarter ->
						drainRemoveQuarterMutation(
							mutation = mutation,
							propagateTerminalErrors = propagateTerminalErrors
						)
				}
			}
		}
	}

	private suspend fun drainSetSubjectGradeMutation(
		mutation: PendingMutation<RecordMutation>,
		propagateTerminalErrors: Boolean
	) {
		val payload = mutation.mutation as? RecordMutation.SetSubjectGrade
			?: return

		runCatching {
			remoteDataSource.setSubjectGrade(
				qid = payload.quarterId,
				sid = payload.subjectId,
				grade = payload.grade,
				mutationId = mutation.mutationId,
				expectedRevision = mutation.expectedRevision
			)
		}.onSuccess { ack ->
			if (ack.mutationId != mutation.mutationId) return

			mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
			localDataSource.saveQuarters(
				ack.affectedQuarters.map { quarter -> quarter.toLocalQuarter() }
			)
		}.onFailure { throwable ->
			when {
				throwable.isConflict() ->
					rebaseSetSubjectGradeMutation(
						mutation = mutation,
						payload = payload
					)

				throwable.isNotFound() || throwable.isPreconditionFailed() -> {
					mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
					refreshRemoteSnapshot()

					if (propagateTerminalErrors) throw throwable
				}

				else -> {
					mutationOutboxRepository.savePendingMutation(
						mutation.copy(
							status = PendingMutationStatus.Failed,
							updatedAt = currentTimeMillis(),
							lastError = throwable.message
						)
					)

					if (propagateTerminalErrors) throw throwable
				}
			}
		}
	}

	private suspend fun drainRemoveQuarterMutation(
		mutation: PendingMutation<RecordMutation>,
		propagateTerminalErrors: Boolean
	) {
		val payload = mutation.mutation as? RecordMutation.RemoveQuarter
			?: return

		runCatching {
			remoteDataSource.removeQuarter(
				qid = payload.quarterId,
				mutationId = mutation.mutationId,
				expectedRevision = mutation.expectedRevision
			)
		}.onSuccess { ack ->
			if (ack.mutationId != mutation.mutationId) return

			localDataSource.confirmQuarterRemoval(
				qid = ack.removedQuarterId,
				affectedQuarters = ack.affectedQuarters.map { quarter -> quarter.toLocalQuarter() }
			)
			mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
		}.onFailure { throwable ->
			when {
				throwable.isConflict() ->
					rebaseRemoveQuarterMutation(
						mutation = mutation,
						payload = payload
					)

				throwable.isNotFound() -> {
					localDataSource.removeQuarter(payload.quarterId)
					mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
					refreshRemoteSnapshot()
				}

				throwable.isPreconditionFailed() -> {
					mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
					refreshRemoteSnapshot()

					if (propagateTerminalErrors) throw throwable
				}

				else -> {
					mutationOutboxRepository.savePendingMutation(
						mutation.copy(
							status = PendingMutationStatus.Failed,
							updatedAt = currentTimeMillis(),
							lastError = throwable.message
						)
					)

					if (propagateTerminalErrors) throw throwable
				}
			}
		}
	}

	private suspend fun rebaseSetSubjectGradeMutation(
		mutation: PendingMutation<RecordMutation>,
		payload: RecordMutation.SetSubjectGrade
	) {
		val remoteQuarters = refreshRemoteSnapshot()
		val remoteSubject = remoteQuarters
			.firstOrNull { quarter -> quarter.id == payload.quarterId }
			?.subjects
			?.firstOrNull { subject -> subject.id == payload.subjectId }

		if (remoteSubject == null) {
			mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
			return
		}

		if (remoteSubject.grade == payload.grade) {
			mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
			return
		}

		mutationOutboxRepository.savePendingMutation(
			mutation.copy(
				expectedRevision = remoteSubject.revision,
				status = PendingMutationStatus.Pending,
				updatedAt = currentTimeMillis(),
				lastError = null
			)
		)
	}

	private suspend fun rebaseRemoveQuarterMutation(
		mutation: PendingMutation<RecordMutation>,
		payload: RecordMutation.RemoveQuarter
	) {
		val remoteQuarters = refreshRemoteSnapshot()
		val remoteQuarter = remoteQuarters.firstOrNull { quarter -> quarter.id == payload.quarterId }

		if (remoteQuarter == null) {
			localDataSource.removeQuarter(payload.quarterId)
			mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
			return
		}
		if (!QuarterMutationPolicy.canDelete(remoteQuarter)) {
			mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
			return
		}

		mutationOutboxRepository.savePendingMutation(
			mutation.copy(
				expectedRevision = remoteQuarter.revision,
				status = PendingMutationStatus.Pending,
				updatedAt = currentTimeMillis(),
				lastError = null
			)
		)
	}

	private suspend fun refreshRemoteSnapshot(): List<RemoteQuarter> {
		val remoteQuarters = remoteDataSource.getQuarters()
		localDataSource.saveQuarters(remoteQuarters.map { quarter -> quarter.toLocalQuarter() })
		return remoteQuarters
	}

	private fun buildPendingSetSubjectGradeMutation(
		quarterId: String,
		subjectId: String,
		grade: Int,
		expectedRevision: Long
	): PendingMutation<RecordMutation> {
		val now = currentTimeMillis()
		return PendingMutation(
			mutationId = identifierRepository.generateRandomIdentifier(),
			mutation = RecordMutation.SetSubjectGrade(
				quarterId = quarterId,
				subjectId = subjectId,
				grade = grade
			),
			expectedRevision = expectedRevision,
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}

	private fun buildPendingRemoveQuarterMutation(
		quarterId: String,
		expectedRevision: Long
	): PendingMutation<RecordMutation> {
		val now = currentTimeMillis()
		return PendingMutation(
			mutationId = identifierRepository.generateRandomIdentifier(),
			mutation = RecordMutation.RemoveQuarter(quarterId = quarterId),
			expectedRevision = expectedRevision,
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}
}
