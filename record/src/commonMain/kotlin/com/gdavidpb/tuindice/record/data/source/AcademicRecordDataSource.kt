package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
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
import com.gdavidpb.tuindice.record.domain.model.RecordRejection
import com.gdavidpb.tuindice.record.domain.model.RecordRejectionKind
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

class AcademicRecordDataSource(
	private val localDataSource: AcademicRecordLocalDataRepository,
	private val remoteDataSource: AcademicRecordRemoteDataRepository,
	private val settingsDataSource: RecordSettingsDataRepository,
	private val mutationEngine: StoreBackedMutationEngine<String, AcademicRecordMutation, VersionedAcademicRecord>,
	private val identifierRepository: IdentifierRepository
) : AcademicRecordRepository, SessionMemory {
	// Held ONLY while a downloaded record is written locally, never during a request. The epoch is
	// checked and the record saved under it, and clearSessionMemory changes the epoch under it.
	private val writeGate = Mutex()

	// Bumped by clearSessionMemory. A download remembers the one it started in: if it differs when the
	// answer arrives, the account that asked is gone and the answer must leave no trace.
	@Volatile
	private var sessionEpoch: Int = 0

	private val mutationSyncSpec = AcademicRecordMutationSyncSpec(
		remoteDataSource = remoteDataSource,
		persistConfirmedSnapshot = ::persistRemoteSnapshot,
		refreshRemoteSnapshot = ::refreshRemoteSnapshot,
		currentLocalRevision = localDataSource::getRecordRevision
	)

	// Changes the epoch under the write gate, which only a local save holds (never a request): a save
	// ends before the session does or finds the new epoch and writes nothing. Not under a lock a request
	// may hold, so the wipe that a request causes (a 401) never waits for that request. The queue has its
	// own epoch (StoreBackedMutationEngine); this one fences what this data source saves itself.
	override suspend fun clearSessionMemory() {
		writeGate.withLock { sessionEpoch += 1 }
	}

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
		return observeTerminallyRejectedMutationsFlow()
			.map { rejections -> rejections.map { rejection -> rejection.mutationId } }
			.distinctUntilChanged()
	}

	override suspend fun observeTerminallyRejectedMutationsFlow(): Flow<List<RecordRejection>> {
		return mutationEngine.observeMutations(RECORD_MUTATION_SCOPE)
			.map { mutations ->
				mutations.filter { mutation ->
					mutation.status == PendingMutationStatus.FailedTerminal
				}.map { mutation ->
					RecordRejection(
						mutationId = mutation.mutationId,
						kind = when (mutation.command) {
							is AcademicRecordMutation.UpsertAttemptOverride,
							is AcademicRecordMutation.DeleteAttemptOverride -> RecordRejectionKind.Grade

							else -> RecordRejectionKind.Term
						}
					)
				}
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

	// The engine is this module's alone (its own outbox), so every outcome it publishes is one of these mutations.
	override fun observeMutationOutcomesFlow(): Flow<MutationOutcome> = mutationEngine.outcomes

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

	override suspend fun getRecordRevision(): Long? {
		return localDataSource.getRecordRevision()
	}

	override suspend fun updateAcademicRecord() {
		updateAcademicRecord(forceRemote = false)
	}

	override suspend fun updateAcademicRecord(forceRemote: Boolean) {
		val isOnCooldown = settingsDataSource.isGetAcademicRecordOnCooldown()
		val hasUsableLocalRecord = localDataSource.hasAcademicRecord() &&
				localDataSource.observeHasSyncedRecordFlow().first()

		if (forceRemote || !isOnCooldown || !hasUsableLocalRecord) {
			val epoch = sessionEpoch
			val snapshotVersion = mutationEngine.currentMutationVersion()
			val remoteRecord = remoteDataSource.getAcademicRecord()

			// An answer that outlived its session is not stored for the next account, and the session
			// cannot end between the check and the save.
			writeGate.withLock {
				if (epoch == sessionEpoch && snapshotVersion == mutationEngine.currentMutationVersion()) {
					persistRemoteSnapshot(remoteRecord)
					settingsDataSource.setGetAcademicRecordOnCooldown()
				}
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
		mutationId: String?
	) {
		val currentRevision = submissionRevision(mutationId) ?: return
		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = mutationId ?: identifierRepository.generateRandomIdentifier(),
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

	override suspend fun deleteAttemptOverride(attemptId: String, mutationId: String?) {
		val currentRevision = submissionRevision(mutationId) ?: return
		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = mutationId ?: identifierRepository.generateRandomIdentifier(),
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
		val currentRevision = submissionRevision(command.mutationId) ?: return
		val mutationCommand = command.toMutation()
		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = command.mutationId ?: identifierRepository.generateRandomIdentifier(),
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
		val currentRevision = submissionRevision(command.mutationId) ?: return
		val mutationCommand = command.toMutation()
		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = command.mutationId ?: identifierRepository.generateRandomIdentifier(),
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

	override suspend fun deleteSyntheticTerm(termId: String, mutationId: String?) {
		val visibleRecord = getAcademicRecord()
		val term = visibleRecord?.terms?.firstOrNull { term ->
			term.id == termId && term.kind.isSynthetic
		}
		val currentRevision = localDataSource.getRecordRevision()

		if (visibleRecord == null || term == null || currentRevision == null) {
			publishUnsubmittedRejection(
				mutationId,
				detailCode = when {
					visibleRecord == null -> "no_record"
					term == null -> "no_term"
					else -> "no_revision"
				}
			)
			return
		}

		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = mutationId ?: identifierRepository.generateRandomIdentifier(),
			scopeKey = RECORD_MUTATION_SCOPE,
			command = AcademicRecordMutation.DeleteSyntheticTerm(
				termId = termId,
				termKey = term.termKey
			),
			precondition = MutationPrecondition.Revision(currentRevision),
			status = PendingMutationStatus.Pending,
			createdAt = currentTimeMillis(),
			updatedAt = currentTimeMillis(),
			lastError = null
		)
		submitTrackedMutation(mutation = mutation)
	}

	// The download a 409 or a 404 asks for while the queue resolves a failure. It saves before the
	// engine looks at its own epoch (that happens when the resolution comes back), so the same fence as
	// the download of the screen is applied here. It does not take the gate of the engine: this runs
	// outside it, and confirm (persistRemoteSnapshot) runs under it.
	private suspend fun refreshRemoteSnapshot(): VersionedAcademicRecord {
		val epoch = sessionEpoch
		val snapshotVersion = mutationEngine.currentMutationVersion()
		val remoteRecord = remoteDataSource.getAcademicRecord()

		writeGate.withLock {
			if (epoch == sessionEpoch && snapshotVersion == mutationEngine.currentMutationVersion()) {
				persistRemoteSnapshot(remoteRecord)
			}
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

	// A write that cannot be submitted (no local record, no revision, no such synthetic term) never reaches the
	// outbox, so nothing else settles it. Whoever named the mutation is waiting for its result: it is rejected
	// under that id, with the reason as its code. Without a name nobody is waiting, so nothing is published.
	// The revision a mutation is made against. When the record or its revision is missing, the rejection is
	// published with the reason and null is returned, so every mutation stops at the same guard.
	private suspend fun submissionRevision(mutationId: String?): Long? {
		val record = localDataSource.getAcademicRecord()
		val currentRevision = localDataSource.getRecordRevision()

		if (record != null && currentRevision != null) return currentRevision

		publishUnsubmittedRejection(
			mutationId,
			detailCode = if (record == null) "no_record" else "no_revision"
		)
		return null
	}

	private suspend fun publishUnsubmittedRejection(mutationId: String?, detailCode: String) {
		if (mutationId == null) return

		mutationEngine.publishLocalOutcome(
			MutationOutcome(
				mutationId = mutationId,
				kind = MutationOutcomeKind.REJECTED,
				detailCode = detailCode
			)
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
