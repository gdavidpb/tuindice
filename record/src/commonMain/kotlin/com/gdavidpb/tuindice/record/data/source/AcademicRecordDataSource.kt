package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutationSyncSpec
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.data.repository.RecordSettingsDataRepository
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class AcademicRecordDataSource(
	private val localDataSource: AcademicRecordLocalDataRepository,
	private val remoteDataSource: AcademicRecordRemoteDataRepository,
	private val settingsDataSource: RecordSettingsDataRepository,
	private val mutationEngine: StoreBackedMutationEngine<String, AcademicRecordMutation, AcademicRecord, AcademicRecord, VersionedAcademicRecord>,
	private val identifierRepository: IdentifierRepository
) : AcademicRecordRepository {
	private val mutationSyncSpec = AcademicRecordMutationSyncSpec(
		localDataSource = localDataSource,
		remoteDataSource = remoteDataSource,
		refreshRemoteSnapshot = ::refreshRemoteSnapshot
	)

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> {
		return localDataSource.observeAcademicRecordFlow().filterNotNull()
	}

	override suspend fun refreshAcademicRecord() {
		val isOnCooldown = settingsDataSource.isGetAcademicRecordOnCooldown()

		if (!isOnCooldown) {
			val snapshotVersion = mutationEngine.currentMutationVersion()
			val remoteRecord = remoteDataSource.getAcademicRecord()
			if (snapshotVersion == mutationEngine.currentMutationVersion()) {
				localDataSource.saveAcademicRecord(remoteRecord)
				settingsDataSource.setGetAcademicRecordOnCooldown()
			}
		}

		mutationEngine.drain(
			scopeKey = RECORD_MUTATION_SCOPE,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		commit: Boolean
	) {
		localDataSource.getAcademicRecord() ?: return
		val currentRevision = localDataSource.getRecordRevision() ?: return
		localDataSource.upsertAttemptOverride(
			attemptId = attemptId,
			score = score,
			outcome = outcome,
			committed = commit
		)

		if (!commit) return

		val mutationVersion = mutationEngine.beginMutation(replaceKey = "attempt:$attemptId")
		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = RECORD_MUTATION_SCOPE,
			command = AcademicRecordMutation.UpsertAttemptOverride(
				attemptId = attemptId,
				score = score,
				outcome = outcome
			),
			precondition = MutationPrecondition.Revision(currentRevision),
			status = PendingMutationStatus.Pending,
			createdAt = currentTimeMillis(),
			updatedAt = currentTimeMillis(),
			lastError = null
		)
		mutationEngine.rememberMutationVersion(
			mutationId = mutation.mutationId,
			version = mutationVersion
		)
		mutationEngine.submit(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = true
		)
	}

	override suspend fun deleteAttemptOverride(attemptId: String) {
		localDataSource.getAcademicRecord() ?: return
		val currentRevision = localDataSource.getRecordRevision() ?: return
		localDataSource.deleteAttemptOverride(attemptId)
		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = RECORD_MUTATION_SCOPE,
			command = AcademicRecordMutation.DeleteAttemptOverride(attemptId),
			precondition = MutationPrecondition.Revision(currentRevision),
			status = PendingMutationStatus.Pending,
			createdAt = currentTimeMillis(),
			updatedAt = currentTimeMillis(),
			lastError = null
		)
		mutationEngine.beginMutation(replaceKey = mutation.command.replaceKey)
		mutationEngine.submit(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = true
		)
	}

	override suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm) {
		localDataSource.getAcademicRecord() ?: return
		val currentRevision = localDataSource.getRecordRevision() ?: return
		localDataSource.addSyntheticTerm(command)
		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = RECORD_MUTATION_SCOPE,
			command = command,
			precondition = MutationPrecondition.Revision(currentRevision),
			status = PendingMutationStatus.Pending,
			createdAt = currentTimeMillis(),
			updatedAt = currentTimeMillis(),
			lastError = null
		)
		mutationEngine.beginMutation(replaceKey = command.replaceKey)
		mutationEngine.submit(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = true
		)
	}

	override suspend fun deleteSyntheticTerm(termId: String) {
		localDataSource.getAcademicRecord() ?: return
		val currentRevision = localDataSource.getRecordRevision() ?: return
		localDataSource.deleteSyntheticTerm(termId)
		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = RECORD_MUTATION_SCOPE,
			command = AcademicRecordMutation.DeleteSyntheticTerm(termId),
			precondition = MutationPrecondition.Revision(currentRevision),
			status = PendingMutationStatus.Pending,
			createdAt = currentTimeMillis(),
			updatedAt = currentTimeMillis(),
			lastError = null
		)
		mutationEngine.beginMutation(replaceKey = mutation.command.replaceKey)
		mutationEngine.submit(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = true
		)
	}

	private suspend fun refreshRemoteSnapshot(): VersionedAcademicRecord {
		val snapshotVersion = mutationEngine.currentMutationVersion()
		val remoteRecord = remoteDataSource.getAcademicRecord()
		if (snapshotVersion == mutationEngine.currentMutationVersion()) {
			localDataSource.saveAcademicRecord(remoteRecord)
		}
		return remoteRecord
	}
}
