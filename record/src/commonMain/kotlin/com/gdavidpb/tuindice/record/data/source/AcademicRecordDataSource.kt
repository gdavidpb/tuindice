package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
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
import kotlinx.coroutines.flow.first

class AcademicRecordDataSource(
	private val localDataSource: AcademicRecordLocalDataRepository,
	private val remoteDataSource: AcademicRecordRemoteDataRepository,
	private val settingsDataSource: RecordSettingsDataRepository,
	private val mutationEngine: StoreBackedMutationEngine<String, AcademicRecordMutation, AcademicRecord, AcademicRecord, VersionedAcademicRecord>,
	private val identifierRepository: IdentifierRepository
) : AcademicRecordRepository {
	private val mutationSyncSpec = AcademicRecordMutationSyncSpec(
		remoteDataSource = remoteDataSource,
		persistConfirmedSnapshot = ::persistRemoteSnapshot,
		refreshRemoteSnapshot = ::refreshRemoteSnapshot
	)

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> {
		return localDataSource.observeAcademicRecordFlow().filterNotNull()
	}

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> {
		return localDataSource.observeHasSyncedRecordFlow()
	}

	override suspend fun getAcademicRecord(): AcademicRecord? {
		return localDataSource.getAcademicRecord()
	}

	override suspend fun updateAcademicRecord() {
		val isOnCooldown = settingsDataSource.isGetAcademicRecordOnCooldown()
		val hasUsableLocalRecord = localDataSource.hasAcademicRecord() &&
				localDataSource.observeHasSyncedRecordFlow().first()

		if (!isOnCooldown || !hasUsableLocalRecord) {
			val snapshotVersion = mutationEngine.currentMutationVersion()
			val remoteRecord = remoteDataSource.getAcademicRecord()
			if (snapshotVersion == mutationEngine.currentMutationVersion()) {
				persistRemoteSnapshot(remoteRecord)
				settingsDataSource.setGetAcademicRecordOnCooldown()
			}
		}

		mutationEngine.drain(
			scopeKey = RECORD_MUTATION_SCOPE,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun drainPendingMutations() {
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
		submitTrackedMutation(mutation = mutation)
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
		submitTrackedMutation(mutation = mutation)
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
		submitTrackedMutation(mutation = mutation)
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
		submitTrackedMutation(mutation = mutation)
	}

	private suspend fun refreshRemoteSnapshot(): VersionedAcademicRecord {
		val snapshotVersion = mutationEngine.currentMutationVersion()
		val remoteRecord = remoteDataSource.getAcademicRecord()
		if (snapshotVersion == mutationEngine.currentMutationVersion()) {
			persistRemoteSnapshot(remoteRecord)
		}
		return remoteRecord
	}

	private suspend fun persistRemoteSnapshot(
		remoteRecord: VersionedAcademicRecord
	) {
		localDataSource.saveAcademicRecord(
			remoteRecord.reapplyingPendingMutations(currentPendingMutations())
		)
	}

	private suspend fun submitTrackedMutation(
		mutation: MutationEnvelope<String, AcademicRecordMutation>
	) {
		val mutationVersion = mutationEngine.beginMutation(replaceKey = mutation.command.replaceKey)
		mutationEngine.rememberMutationVersion(
			mutationId = mutation.mutationId,
			version = mutationVersion
		)
		mutationEngine.submit(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	private suspend fun currentPendingMutations(): List<MutationEnvelope<String, AcademicRecordMutation>> {
		return mutationEngine.getPendingMutations(RECORD_MUTATION_SCOPE)
			.sortedWith(compareBy(MutationEnvelope<String, AcademicRecordMutation>::createdAt, MutationEnvelope<String, AcademicRecordMutation>::mutationId))
	}

	private fun VersionedAcademicRecord.reapplyingPendingMutations(
		pendingMutations: List<MutationEnvelope<String, AcademicRecordMutation>>
	): VersionedAcademicRecord {
		return pendingMutations.fold(this) { currentRecord, pendingMutation ->
			currentRecord.copy(record = currentRecord.record.reapplying(pendingMutation.command))
		}
	}

	private fun AcademicRecord.reapplying(
		mutation: AcademicRecordMutation
	): AcademicRecord {
		return when (mutation) {
			is AcademicRecordMutation.UpsertAttemptOverride ->
				copy(
					attemptOverrides = attemptOverrides
						.filterNot { override -> override.attemptId == mutation.attemptId } +
						AttemptOverride(
							attemptId = mutation.attemptId,
							score = mutation.score,
							outcome = mutation.outcome,
							updatedAtMillis = currentTimeMillis()
						)
				)

			is AcademicRecordMutation.DeleteAttemptOverride ->
				copy(
					attemptOverrides = attemptOverrides.filterNot { override ->
						override.attemptId == mutation.attemptId
					}
				)

			is AcademicRecordMutation.AddSyntheticTerm ->
				copy(
					terms = normalizeTerms(
						terms.filterNot { term -> term.id == mutation.termId } + mutation.toAcademicTerm()
					)
				)

			is AcademicRecordMutation.DeleteSyntheticTerm -> {
				val removedAttemptIds = terms.firstOrNull { term -> term.id == mutation.termId }
					?.attempts
					?.map(AcademicAttempt::id)
					?.toSet()
					.orEmpty()
				copy(
					terms = terms.filterNot { term -> term.id == mutation.termId },
					attemptOverrides = attemptOverrides.filterNot { override ->
						override.attemptId in removedAttemptIds
					}
				)
			}
		}
	}

	private fun normalizeTerms(terms: List<AcademicTerm>): List<AcademicTerm> {
		return terms.sortedWith(
			compareBy(AcademicTerm::startAtMillis, AcademicTerm::endAtMillis, AcademicTerm::id)
		)
	}
}
