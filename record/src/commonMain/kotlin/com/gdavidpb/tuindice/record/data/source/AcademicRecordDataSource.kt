package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.persistence.domain.record.reapplying
import com.gdavidpb.tuindice.persistence.domain.record.sortedForReplay
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutationSyncSpec
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.data.repository.RecordSettingsDataRepository
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AcademicRecordDataSource(
	private val localDataSource: AcademicRecordLocalDataRepository,
	private val remoteDataSource: AcademicRecordRemoteDataRepository,
	private val settingsDataSource: RecordSettingsDataRepository,
	private val mutationEngine: StoreBackedMutationEngine<String, AcademicRecordMutation, VersionedAcademicRecord>,
	private val identifierRepository: IdentifierRepository
) : AcademicRecordRepository {
	private val mutationSyncSpec = AcademicRecordMutationSyncSpec(
		remoteDataSource = remoteDataSource,
		persistConfirmedSnapshot = ::persistRemoteSnapshot,
		refreshRemoteSnapshot = ::refreshRemoteSnapshot
	)

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> {
		return observeVisibleRecordFlow().filterNotNull()
	}

	// El estado visible se deriva al leer: lo confirmado más los sobres del outbox.
	// La base local guarda solo lo confirmado, así que no hay dos representaciones del
	// mismo cambio que puedan divergir. Los sobres FailedTerminal quedan fuera: el
	// servidor rechazó ese cambio de forma definitiva, y mantenerlo aplicado haría que
	// la vista mintiera indefinidamente respecto al estado real.
	private fun observeVisibleRecordFlow(): Flow<AcademicRecord?> {
		return combine(
			localDataSource.observeAcademicRecordFlow(),
			mutationEngine.observeMutations(RECORD_MUTATION_SCOPE)
		) { confirmedRecord, mutations ->
			confirmedRecord?.reapplying(mutations.visibleForReplay().sortedForReplay())
		}
	}

	override suspend fun observeTerminallyRejectedMutationIdsFlow(): Flow<List<String>> {
		return mutationEngine.observeMutations(RECORD_MUTATION_SCOPE)
			.map { mutations ->
				mutations.filter { mutation ->
					mutation.status == PendingMutationStatus.FailedTerminal
				}.map { mutation -> mutation.mutationId }
			}
			.distinctUntilChanged()
	}

	override suspend fun acknowledgeTerminallyRejectedMutations(mutationIds: List<String>) {
		mutationIds.forEach { mutationId ->
			mutationEngine.discardMutation(
				scopeKey = RECORD_MUTATION_SCOPE,
				mutationId = mutationId
			)
		}
	}

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> {
		return localDataSource.observeHasSyncedRecordFlow()
	}

	override suspend fun observeAcademicRecordSnapshotFlow(): Flow<ObservedSyncedSnapshot<AcademicRecord>> {
		return combine(
			observeVisibleRecordFlow(),
			localDataSource.observeHasSyncedRecordFlow()
		) { record, hasSynced ->
			record?.let { value ->
				ObservedSyncedSnapshot(
					value = value,
					hasSynced = hasSynced
				)
			}
		}.filterNotNull()
	}

	override suspend fun getAcademicRecord(): AcademicRecord? {
		val pendingMutations = currentPendingMutations()

		return localDataSource.getAcademicRecord()?.reapplying(pendingMutations)
	}

	override suspend fun updateAcademicRecord() {
		updateAcademicRecord(forceRemote = false)
	}

	override suspend fun updateAcademicRecord(forceRemote: Boolean) {
		val isOnCooldown = settingsDataSource.isGetAcademicRecordOnCooldown()
		val hasUsableLocalRecord = localDataSource.hasAcademicRecord() &&
				localDataSource.observeHasSyncedRecordFlow().first()

		if (forceRemote || !isOnCooldown || !hasUsableLocalRecord) {
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
		outcome: AttemptOutcome?
	) {
		localDataSource.getAcademicRecord() ?: return
		val currentRevision = localDataSource.getRecordRevision() ?: return
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

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) {
		localDataSource.getAcademicRecord() ?: return
		val currentRevision = localDataSource.getRecordRevision() ?: return
		val mutationCommand = command.toMutation()
		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = RECORD_MUTATION_SCOPE,
			command = mutationCommand,
			precondition = MutationPrecondition.Revision(currentRevision),
			status = PendingMutationStatus.Pending,
			createdAt = currentTimeMillis(),
			updatedAt = currentTimeMillis(),
			lastError = null
		)
		submitTrackedMutation(mutation = mutation)
	}

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) {
		localDataSource.getAcademicRecord() ?: return
		val currentRevision = localDataSource.getRecordRevision() ?: return
		val mutationCommand = command.toMutation()
		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = RECORD_MUTATION_SCOPE,
			command = mutationCommand,
			precondition = MutationPrecondition.Revision(currentRevision),
			status = PendingMutationStatus.Pending,
			createdAt = currentTimeMillis(),
			updatedAt = currentTimeMillis(),
			lastError = null
		)
		submitTrackedMutation(mutation = mutation)
	}

	override suspend fun deleteSyntheticTerm(termId: String) {
		val visibleRecord = getAcademicRecord() ?: return
		visibleRecord.terms.firstOrNull { term ->
			term.id == termId && term.kind.isSynthetic
		} ?: return
		val currentRevision = localDataSource.getRecordRevision() ?: return
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
		localDataSource.saveAcademicRecord(remoteRecord)
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
		return mutationEngine.getMutations(RECORD_MUTATION_SCOPE).visibleForReplay().sortedForReplay()
	}

	private fun List<MutationEnvelope<String, AcademicRecordMutation>>.visibleForReplay() =
		filterNot { mutation -> mutation.status == PendingMutationStatus.FailedTerminal }

	private fun SyntheticTermCreationCommand.toMutation(): AcademicRecordMutation.AddSyntheticTerm {
		return AcademicRecordMutation.AddSyntheticTerm(
			termId = termId,
			periodYear = periodYear,
			periodCode = periodCode,
			attempts = attempts.map { attempt ->
				AcademicRecordMutation.AddSyntheticTerm.SyntheticAttemptSeed(
					attemptId = attempt.attemptId,
					subjectCode = attempt.subjectCode,
					subjectName = attempt.subjectName,
					credits = attempt.credits,
					gradingMode = attempt.gradingMode,
					score = attempt.score,
					outcome = attempt.outcome
				)
			}
		)
	}

	private fun SyntheticTermUpdateCommand.toMutation(): AcademicRecordMutation.UpdateSyntheticTerm {
		return AcademicRecordMutation.UpdateSyntheticTerm(
			targetTermId = targetTermId,
			targetTermKey = targetTermKey,
			termId = termId,
			periodYear = periodYear,
			periodCode = periodCode,
			attempts = attempts.map { attempt ->
				AcademicRecordMutation.UpdateSyntheticTerm.SyntheticAttemptSeed(
					attemptId = attempt.attemptId,
					subjectCode = attempt.subjectCode,
					subjectName = attempt.subjectName,
					credits = attempt.credits,
					gradingMode = attempt.gradingMode,
					score = attempt.score,
					outcome = attempt.outcome
				)
			}
		)
	}
}
