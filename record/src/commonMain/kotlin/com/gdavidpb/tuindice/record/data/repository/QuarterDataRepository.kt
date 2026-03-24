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
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.policy.QuarterMutationPolicy
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
	private companion object {
		const val MAX_SUBJECT_MUTATION_REBASE_ATTEMPTS = 3
	}

	private val drainMutex = Mutex()
	private val snapshotVersionMutex = Mutex()
	private var latestMutationVersion = 0L
	private val latestMutationVersionByReplaceKey = mutableMapOf<String, Long>()
	private val mutationVersionById = mutableMapOf<String, Long>()

	override suspend fun observeQuartersFlow(): Flow<List<Quarter>> {
		return localDataSource.getQuartersFlow()
			.map { localQuarters -> localQuarters.map { it.toQuarter() } }
	}

	override suspend fun updateQuarters() {
		val isOnCooldown = settingsDataSource.isGetQuartersOnCooldown()

		if (!isOnCooldown) {
			val snapshotVersion = currentMutationVersion()
			val remoteQuarters = remoteDataSource.getQuarters()
			if (snapshotVersion == currentMutationVersion()) {
				localDataSource.saveQuarters(remoteQuarters.map { quarter -> quarter.toLocalQuarter() })
				settingsDataSource.setGetQuartersOnCooldown()
			}
		}

		drainPendingMutations(propagateTerminalErrors = false)
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		val quarter = localDataSource.getQuarter(remove.id)
			?: return
		if (!QuarterMutationPolicy.canDelete(isCurrent = quarter.isCurrent, isReadOnly = quarter.isReadOnly)) return
		markMutationVersion()
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
		if (!QuarterMutationPolicy.canEditGrades(isReadOnly = quarter.isReadOnly)) return
		val mutationVersion = if (set.commit) {
			markMutationVersion(replaceKey = "subject:${set.id}")
		} else {
			null
		}

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

		rememberMutationVersion(
			mutationId = mutation.mutationId,
			version = mutationVersion ?: return
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
		var currentMutation = mutation
		var rebaseAttempts = 0

		while (true) {
			try {
				val ack = remoteDataSource.setSubjectGrade(
					qid = payload.quarterId,
					sid = payload.subjectId,
					grade = payload.grade,
					mutationId = currentMutation.mutationId,
					expectedRevision = currentMutation.expectedRevision
				)

				if (ack.mutationId != currentMutation.mutationId) return
				if (!shouldApplySetSubjectGradeResult(mutation = currentMutation, payload = payload)) {
					forgetMutationVersion(currentMutation.mutationId)
					return
				}

				mutationOutboxRepository.deletePendingMutation(currentMutation.mutationId)
				forgetMutationVersion(currentMutation.mutationId)
				localDataSource.confirmSubjectGradeMutation(
					ack.affectedQuarters.map { quarter -> quarter.toLocalQuarter() }
				)
				return
			} catch (throwable: Throwable) {
				if (!shouldApplySetSubjectGradeResult(mutation = currentMutation, payload = payload)) {
					forgetMutationVersion(currentMutation.mutationId)
					return
				}

				when {
					throwable.isConflict() || throwable.isPreconditionFailed() -> {
						val rebasedMutation = rebaseSetSubjectGradeMutation(
							mutation = currentMutation,
							payload = payload
						)

						if (rebasedMutation == null) {
							forgetMutationVersion(currentMutation.mutationId)
							return
						}
						if (rebasedMutation.expectedRevision == currentMutation.expectedRevision) {
							mutationOutboxRepository.savePendingMutation(
								rebasedMutation.copy(
									status = PendingMutationStatus.Failed,
									updatedAt = currentTimeMillis(),
									lastError = throwable.message
								)
							)

							if (propagateTerminalErrors) throw throwable
							return
						}
						if (rebaseAttempts >= MAX_SUBJECT_MUTATION_REBASE_ATTEMPTS) {
							mutationOutboxRepository.savePendingMutation(
								rebasedMutation.copy(
									status = PendingMutationStatus.Failed,
									updatedAt = currentTimeMillis(),
									lastError = throwable.message
								)
							)

							if (propagateTerminalErrors) throw throwable
							return
						}

						rebaseAttempts += 1
						currentMutation = rebasedMutation
					}

					throwable.isNotFound() -> {
						mutationOutboxRepository.deletePendingMutation(currentMutation.mutationId)
						forgetMutationVersion(currentMutation.mutationId)
						refreshRemoteSnapshot()

						if (propagateTerminalErrors) throw throwable
						return
					}

					else -> {
						mutationOutboxRepository.savePendingMutation(
							currentMutation.copy(
								status = PendingMutationStatus.Failed,
								updatedAt = currentTimeMillis(),
								lastError = throwable.message
							)
						)

						if (propagateTerminalErrors) throw throwable
						return
					}
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
	): PendingMutation<RecordMutation>? {
		val remoteQuarters = refreshRemoteSnapshot()
		val remoteSubject = remoteQuarters
			.firstOrNull { quarter -> quarter.id == payload.quarterId }
			?.subjects
			?.firstOrNull { subject -> subject.id == payload.subjectId }

		if (remoteSubject == null) {
			mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
			return null
		}

		if (remoteSubject.grade == payload.grade) {
			mutationOutboxRepository.deletePendingMutation(mutation.mutationId)
			return null
		}

		val rebasedMutation = mutation.copy(
			expectedRevision = remoteSubject.revision,
			status = PendingMutationStatus.Pending,
			updatedAt = currentTimeMillis(),
			lastError = null
		)
		mutationOutboxRepository.savePendingMutation(rebasedMutation)

		return rebasedMutation
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
		if (!QuarterMutationPolicy.canDelete(
				isCurrent = remoteQuarter.isCurrent,
				isReadOnly = remoteQuarter.isReadOnly
			)
		) {
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
		val snapshotVersion = currentMutationVersion()
		val remoteQuarters = remoteDataSource.getQuarters()
		if (snapshotVersion == currentMutationVersion()) {
			localDataSource.saveQuarters(remoteQuarters.map { quarter -> quarter.toLocalQuarter() })
		}
		return remoteQuarters
	}

	private suspend fun isMutationStillPending(mutationId: String): Boolean {
		return mutationOutboxRepository.getPendingMutation(mutationId) != null
	}

	private suspend fun shouldApplySetSubjectGradeResult(
		mutation: PendingMutation<RecordMutation>,
		payload: RecordMutation.SetSubjectGrade
	): Boolean {
		if (!isMutationStillPending(mutation.mutationId)) return false

		return snapshotVersionMutex.withLock {
			val mutationVersion = mutationVersionById[mutation.mutationId]
				?: return@withLock false
			latestMutationVersionByReplaceKey[payload.replaceKey] == mutationVersion
		}
	}

	private suspend fun markMutationVersion(replaceKey: String? = null): Long {
		return snapshotVersionMutex.withLock {
			latestMutationVersion += 1
			replaceKey?.let { key ->
				latestMutationVersionByReplaceKey[key] = latestMutationVersion
			}
			latestMutationVersion
		}
	}

	private suspend fun currentMutationVersion(): Long {
		return snapshotVersionMutex.withLock {
			latestMutationVersion
		}
	}

	private suspend fun rememberMutationVersion(
		mutationId: String,
		version: Long
	) {
		snapshotVersionMutex.withLock {
			mutationVersionById[mutationId] = version
		}
	}

	private suspend fun forgetMutationVersion(mutationId: String) {
		snapshotVersionMutex.withLock {
			mutationVersionById.remove(mutationId)
		}
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
