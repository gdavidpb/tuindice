package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.data.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.mapper.toEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.data.mapper.toEvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationSyncSpec
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataRepository
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsRefreshResult
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class EvaluationDataSource(
	private val databaseDataSource: DatabaseDataRepository,
	private val evaluationsApiDataSource: EvaluationsApiDataRepository,
	private val settingsDataSource: SettingsDataRepository,
	private val mutationEngine: StoreBackedMutationEngine<String, EvaluationMutation, LocalEvaluationsSnapshot, List<LocalEvaluation>, EvaluationMutationAck>,
	private val identifierRepository: IdentifierRepository
) : EvaluationRepository {
	private val mutationSyncSpec = EvaluationMutationSyncSpec(
		databaseDataSource = databaseDataSource,
		evaluationsApiDataSource = evaluationsApiDataSource,
		refreshRemoteSnapshot = ::refreshRemoteSnapshot
	)

	override suspend fun observeEvaluationsFlow(): Flow<List<Evaluation>> {
		return databaseDataSource.observeEvaluationsFlow()
			.map { evaluations -> evaluations.map { evaluation -> evaluation.toEvaluation() } }
	}

	override suspend fun observeHasSyncedEvaluationsFlow(): Flow<Boolean> {
		return databaseDataSource.observeHasSyncedEvaluationsFlow()
	}

	override suspend fun observeEvaluationsSnapshotFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> {
		return databaseDataSource.observeEvaluationsSnapshotFlow()
			.map { snapshot -> snapshot.toObservedSyncedSnapshot() }
	}

	override suspend fun getEvaluationsSnapshot(): ObservedSyncedSnapshot<List<Evaluation>> {
		return databaseDataSource.getEvaluationsSnapshot().toObservedSyncedSnapshot()
	}

	override suspend fun updateEvaluations(): EvaluationsRefreshResult {
		return updateEvaluations(forceRemote = false)
	}

	override suspend fun updateEvaluations(forceRemote: Boolean): EvaluationsRefreshResult {
		val isOnCooldown = settingsDataSource.isGetEvaluationsOnCooldown()
		val hasSyncedEvaluations = databaseDataSource.observeHasSyncedEvaluationsFlow().first()
		var fetchedSnapshot: RemoteEvaluationsSnapshot? = null

		if (forceRemote || !isOnCooldown || !hasSyncedEvaluations) {
			val snapshotVersion = mutationEngine.currentMutationVersion()
			val remoteSnapshot = evaluationsApiDataSource.getEvaluations()
			fetchedSnapshot = remoteSnapshot
			if (snapshotVersion == mutationEngine.currentMutationVersion()) {
				databaseDataSource.saveConfirmedSnapshot(remoteSnapshot.toLocalSnapshot())
				settingsDataSource.setGetEvaluationsOnCooldown()
			}
		}

		mutationEngine.drain(
			scopeKey = EVALUATIONS_MUTATION_SCOPE,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)

		val visibleSnapshot = databaseDataSource.getEvaluationsSnapshot()
		return EvaluationsRefreshResult(
			hasEvaluations = fetchedSnapshot?.evaluations?.isNotEmpty() == true ||
					visibleSnapshot.evaluations.isNotEmpty(),
			hasAvailableAttempts = databaseDataSource.getAvailableAttempts().isNotEmpty()
		)
	}

	override suspend fun drainPendingMutations() {
		mutationEngine.drain(
			scopeKey = EVALUATIONS_MUTATION_SCOPE,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun getEvaluation(eid: String): Evaluation? {
		return databaseDataSource.getEvaluation(eid)?.toEvaluation()
	}

	override suspend fun addEvaluation(add: EvaluationAdd) {
		val mutation = buildPendingAddMutation(
			command = EvaluationMutation.Add(
				referenceId = add.reference,
				attemptId = add.attemptId,
				subjectCode = add.subjectCode,
				termId = add.termId,
				scheduleMode = add.scheduleMode,
				grade = add.grade,
				maxGrade = add.maxGrade,
				date = add.date,
				type = add.type.ordinal
			)
		)
		val mutationVersion = mutationEngine.beginMutation(replaceKey = mutation.replaceKey)
		mutationEngine.rememberMutationVersion(mutation.mutationId, mutationVersion)
		mutationEngine.submitInBackground(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate) {
		val pendingAdd = pendingAddForReference(update.id)
		if (pendingAdd != null) {
			val rewrittenAdd = (pendingAdd.command as EvaluationMutation.Add).apply(update)
			val mutation = buildPendingAddMutation(
				command = rewrittenAdd
			)
			val mutationVersion = mutationEngine.beginMutation(replaceKey = mutation.replaceKey)
			mutationEngine.rememberMutationVersion(mutation.mutationId, mutationVersion)
			mutationEngine.submitInBackground(
				mutation = mutation,
				syncSpec = mutationSyncSpec,
				propagateTerminalErrors = false
			)
			return
		}

		val evaluation = databaseDataSource.getEvaluation(update.id)
			?: return
		val mutation = buildPendingUpdateMutation(
			evaluationId = evaluation.id,
			update = update,
			expectedRevision = evaluation.revision
		)
		val mutationVersion = mutationEngine.beginMutation(replaceKey = mutation.replaceKey)
		mutationEngine.rememberMutationVersion(mutation.mutationId, mutationVersion)
		mutationEngine.submitInBackground(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		val pendingAdd = pendingAddForReference(remove.id)
		if (pendingAdd != null) {
			mutationEngine.deletePendingMutation(
				scopeKey = EVALUATIONS_MUTATION_SCOPE,
				mutationId = pendingAdd.mutationId
			)
			return
		}

		val evaluation = databaseDataSource.getEvaluation(remove.id)
			?: return
		val mutation = buildPendingRemoveMutation(
			evaluationId = evaluation.id,
			expectedRevision = evaluation.revision
		)
		val mutationVersion = mutationEngine.beginMutation(replaceKey = mutation.replaceKey)
		mutationEngine.rememberMutationVersion(mutation.mutationId, mutationVersion)
		mutationEngine.submitInBackground(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun getAvailableAttempts(): List<EditableAttemptDescriptor> {
		return databaseDataSource.getAvailableAttempts()
			.map { attempt -> attempt.toEditableAttemptDescriptor() }
			.sortedBy(EditableAttemptDescriptor::code)
	}

	override suspend fun getCurrentTerm() =
		databaseDataSource.getCurrentTerm()?.toEvaluationTermDescriptor()

	private fun LocalEvaluationsSnapshot.toObservedSyncedSnapshot(): ObservedSyncedSnapshot<List<Evaluation>> {
		return ObservedSyncedSnapshot(
			value = evaluations.map { evaluation -> evaluation.toEvaluation() },
			hasSynced = hasSynced
		)
	}

	private suspend fun refreshRemoteSnapshot(): RemoteEvaluationsSnapshot {
		val snapshotVersion = mutationEngine.currentMutationVersion()
		val remoteSnapshot = evaluationsApiDataSource.getEvaluations()
		if (snapshotVersion == mutationEngine.currentMutationVersion()) {
			databaseDataSource.saveConfirmedSnapshot(remoteSnapshot.toLocalSnapshot())
		}
		return remoteSnapshot
	}

	private suspend fun pendingAddForReference(
		referenceId: String
	): MutationEnvelope<String, EvaluationMutation>? {
		return mutationEngine.getPendingMutations(EVALUATIONS_MUTATION_SCOPE)
			.firstOrNull { mutation ->
				val command = mutation.command
				command is EvaluationMutation.Add && command.referenceId == referenceId
			}
	}

	private fun buildPendingAddMutation(
		command: EvaluationMutation.Add
	): MutationEnvelope<String, EvaluationMutation> {
		val now = currentTimeMillis()
		return MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = EVALUATIONS_MUTATION_SCOPE,
			command = command,
			precondition = MutationPrecondition.None,
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}

	private fun buildPendingUpdateMutation(
		evaluationId: String,
		update: EvaluationUpdate,
		expectedRevision: Long
	): MutationEnvelope<String, EvaluationMutation> {
		val now = currentTimeMillis()
		return MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = EVALUATIONS_MUTATION_SCOPE,
			command = EvaluationMutation.Update(
				evaluationId = evaluationId,
				scheduleMode = update.scheduleMode,
				grade = update.grade,
				maxGrade = update.maxGrade,
				date = update.date,
				type = update.type?.ordinal
			),
			precondition = MutationPrecondition.Revision(expectedRevision),
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}

	private fun buildPendingRemoveMutation(
		evaluationId: String,
		expectedRevision: Long
	): MutationEnvelope<String, EvaluationMutation> {
		val now = currentTimeMillis()
		return MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = EVALUATIONS_MUTATION_SCOPE,
			command = EvaluationMutation.Remove(evaluationId = evaluationId),
			precondition = MutationPrecondition.Revision(expectedRevision),
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}

	private fun RemoteEvaluationsSnapshot.toLocalSnapshot() = LocalEvaluationsSnapshot(
		hasSynced = true,
		evaluations = evaluations.map { evaluation -> evaluation.toLocalEvaluation() }
	)

	private fun EvaluationMutation.Add.apply(update: EvaluationUpdate): EvaluationMutation.Add {
		val resolvedScheduleMode = update.scheduleMode ?: if (update.date != null) {
			EvaluationScheduleMode.DATED
		} else {
			scheduleMode
		}
		val resolvedDate = when (resolvedScheduleMode) {
			EvaluationScheduleMode.CONTINUOUS -> null
			EvaluationScheduleMode.DATED -> update.date ?: date
		}
		val resolvedGrade = update.grade

		return copy(
			scheduleMode = resolvedScheduleMode,
			grade = resolvedGrade,
			maxGrade = update.maxGrade ?: maxGrade,
			date = resolvedDate,
			type = update.type?.ordinal ?: type
		)
	}

}
