package com.gdavidpb.tuindice.evaluations.testing

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalCurrentTermDescriptor
import com.gdavidpb.tuindice.evaluations.data.model.LocalEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataRepository
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_STORE_ID
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsRefreshResult
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private const val PAST_EVALUATION_DATE = 1_780_545_600_000L
private const val FUTURE_EVALUATION_DATE = 1_780_632_000_000L
const val DEFAULT_HAS_SYNCED_EVALUATIONS = true

class ReadyRecordDataPrerequisiteRepository(
	private val states: Flow<RecordDataPrerequisiteState> = flowOf(
		RecordDataPrerequisiteState(
			isReady = true,
			hasFailed = false
		)
	)
) : RecordDataPrerequisiteRepository {
	override fun observeRecordDataPrerequisiteFlow(): Flow<RecordDataPrerequisiteState> = states

	override suspend fun isRecordDataReady(): Boolean = states.first().isReady
}

class RecordingSyncStatusRepository(
	initialReport: SyncReport = SyncReport.success()
) : SyncStatusRepository {
	private val syncStatus = MutableStateFlow(SyncStatus.Healthy)
	private val syncReport = MutableStateFlow(initialReport)
	private val lastSuccessfulSyncAt = MutableStateFlow<Long?>(null)

	override fun observeSyncStatus(): Flow<SyncStatus> = syncStatus

	override fun observeSyncReport(): Flow<SyncReport> = syncReport

	override fun observeLastSuccessfulSyncAt(): Flow<Long?> = lastSuccessfulSyncAt

	override suspend fun getSyncStatus(): SyncStatus = syncStatus.value

	override suspend fun getSyncReport(): SyncReport = syncReport.value

	override suspend fun getLastSuccessfulSyncAt(): Long? = lastSuccessfulSyncAt.value

	override suspend fun setSyncStatus(status: SyncStatus) {
		syncStatus.value = status
	}

	override suspend fun setSyncReport(report: SyncReport) {
		syncReport.value = report
	}

	override suspend fun setLastSuccessfulSyncAt(timestamp: Long) {
		lastSuccessfulSyncAt.value = timestamp
	}

	override suspend fun reset() {
		syncStatus.value = SyncStatus.Healthy
		syncReport.value = SyncReport.success()
		lastSuccessfulSyncAt.value = null
	}
}

val DEFAULT_EVALUATION_SUBJECT = EditableAttemptDescriptor(
	id = "subject-1",
	termId = "quarter-1",
	code = "INF-101",
	name = "Programacion",
	credits = 6,
	grade = 70
)

val SECOND_EVALUATION_SUBJECT = EditableAttemptDescriptor(
	id = "subject-2",
	termId = "quarter-1",
	code = "MAT-101",
	name = "Calculo",
	credits = 6,
	grade = 65
)

val DEFAULT_EVALUATION_TERM = EvaluationTermDescriptor(
	id = DEFAULT_EVALUATION_SUBJECT.termId,
	periodYear = 2026,
	periodCode = AcademicTermPeriod.APR_JUL,
	periodLabel = "Abril - Julio 2026"
)

val DEFAULT_LOCAL_EVALUATION_TERM = LocalCurrentTermDescriptor(
	id = DEFAULT_EVALUATION_TERM.id,
	periodYear = DEFAULT_EVALUATION_TERM.periodYear,
	periodCode = DEFAULT_EVALUATION_TERM.periodCode,
	periodLabel = DEFAULT_EVALUATION_TERM.periodLabel
)

val DEFAULT_LOCAL_EVALUATION_SUBJECT = LocalEditableAttemptDescriptor(
	id = DEFAULT_EVALUATION_SUBJECT.id,
	termId = DEFAULT_EVALUATION_SUBJECT.termId,
	code = DEFAULT_EVALUATION_SUBJECT.code,
	name = DEFAULT_EVALUATION_SUBJECT.name,
	credits = DEFAULT_EVALUATION_SUBJECT.credits,
	grade = DEFAULT_EVALUATION_SUBJECT.grade
)

val SECOND_LOCAL_EVALUATION_SUBJECT = LocalEditableAttemptDescriptor(
	id = SECOND_EVALUATION_SUBJECT.id,
	termId = SECOND_EVALUATION_SUBJECT.termId,
	code = SECOND_EVALUATION_SUBJECT.code,
	name = SECOND_EVALUATION_SUBJECT.name,
	credits = SECOND_EVALUATION_SUBJECT.credits,
	grade = SECOND_EVALUATION_SUBJECT.grade
)

val DEFAULT_PENDING_EVALUATION = Evaluation(
	id = "evaluation-1",
	attemptId = DEFAULT_EVALUATION_SUBJECT.id,
	subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
	termId = DEFAULT_EVALUATION_SUBJECT.termId,
	scheduleMode = EvaluationScheduleMode.DATED,
	grade = null,
	maxGrade = 35.0,
	date = FUTURE_EVALUATION_DATE,
	type = EvaluationType.QUIZ,
	state = EvaluationState.PENDING
)

val DEFAULT_COMPLETED_EVALUATION = Evaluation(
	id = "evaluation-2",
	attemptId = SECOND_EVALUATION_SUBJECT.id,
	subjectCode = SECOND_EVALUATION_SUBJECT.code,
	termId = SECOND_EVALUATION_SUBJECT.termId,
	scheduleMode = EvaluationScheduleMode.DATED,
	grade = 18.0,
	maxGrade = 25.0,
	date = PAST_EVALUATION_DATE,
	type = EvaluationType.TEST,
	state = EvaluationState.COMPLETED
)

val DEFAULT_LOCAL_PENDING_EVALUATION = LocalEvaluation(
	id = DEFAULT_PENDING_EVALUATION.id,
	referenceId = DEFAULT_PENDING_EVALUATION.id,
	attemptId = DEFAULT_PENDING_EVALUATION.attemptId,
	subjectCode = DEFAULT_PENDING_EVALUATION.subjectCode,
	termId = DEFAULT_PENDING_EVALUATION.termId,
	revision = 3L,
	scheduleMode = DEFAULT_PENDING_EVALUATION.scheduleMode,
	grade = DEFAULT_PENDING_EVALUATION.grade,
	maxGrade = DEFAULT_PENDING_EVALUATION.maxGrade,
	date = DEFAULT_PENDING_EVALUATION.date,
	type = DEFAULT_PENDING_EVALUATION.type.ordinal,
	isDone = false
)

val DEFAULT_LOCAL_COMPLETED_EVALUATION = LocalEvaluation(
	id = DEFAULT_COMPLETED_EVALUATION.id,
	referenceId = DEFAULT_COMPLETED_EVALUATION.id,
	attemptId = DEFAULT_COMPLETED_EVALUATION.attemptId,
	subjectCode = DEFAULT_COMPLETED_EVALUATION.subjectCode,
	termId = DEFAULT_COMPLETED_EVALUATION.termId,
	revision = 4L,
	scheduleMode = DEFAULT_COMPLETED_EVALUATION.scheduleMode,
	grade = DEFAULT_COMPLETED_EVALUATION.grade,
	maxGrade = DEFAULT_COMPLETED_EVALUATION.maxGrade,
	date = DEFAULT_COMPLETED_EVALUATION.date,
	type = DEFAULT_COMPLETED_EVALUATION.type.ordinal,
	isDone = true
)

val DEFAULT_REMOTE_PENDING_EVALUATION = RemoteEvaluation(
	id = DEFAULT_PENDING_EVALUATION.id,
	referenceId = DEFAULT_PENDING_EVALUATION.id,
	attemptId = DEFAULT_PENDING_EVALUATION.attemptId,
	subjectCode = DEFAULT_PENDING_EVALUATION.subjectCode,
	termId = DEFAULT_PENDING_EVALUATION.termId,
	revision = DEFAULT_LOCAL_PENDING_EVALUATION.revision,
	scheduleMode = DEFAULT_PENDING_EVALUATION.scheduleMode,
	grade = DEFAULT_PENDING_EVALUATION.grade,
	maxGrade = DEFAULT_PENDING_EVALUATION.maxGrade,
	date = DEFAULT_PENDING_EVALUATION.date,
	type = DEFAULT_PENDING_EVALUATION.type.ordinal,
	isDone = false
)

val DEFAULT_REMOTE_COMPLETED_EVALUATION = RemoteEvaluation(
	id = DEFAULT_COMPLETED_EVALUATION.id,
	referenceId = DEFAULT_COMPLETED_EVALUATION.id,
	attemptId = DEFAULT_COMPLETED_EVALUATION.attemptId,
	subjectCode = DEFAULT_COMPLETED_EVALUATION.subjectCode,
	termId = DEFAULT_COMPLETED_EVALUATION.termId,
	revision = DEFAULT_LOCAL_COMPLETED_EVALUATION.revision,
	scheduleMode = DEFAULT_COMPLETED_EVALUATION.scheduleMode,
	grade = DEFAULT_COMPLETED_EVALUATION.grade,
	maxGrade = DEFAULT_COMPLETED_EVALUATION.maxGrade,
	date = DEFAULT_COMPLETED_EVALUATION.date,
	type = DEFAULT_COMPLETED_EVALUATION.type.ordinal,
	isDone = true
)

class RecordingEvaluationRepository(
	initialEvaluations: List<Evaluation> = listOf(
		DEFAULT_PENDING_EVALUATION,
		DEFAULT_COMPLETED_EVALUATION
	),
	private val evaluationsFlow: Flow<List<Evaluation>>? = null,
	private val addThrowable: Throwable? = null,
	private val updateThrowable: Throwable? = null,
	private val removeThrowable: Throwable? = null,
	private val refreshThrowable: Throwable? = null,
	private val refreshedEvaluations: List<Evaluation>? = null,
	private val getEvaluationThrowable: Throwable? = null,
	private val availableAttemptsThrowable: Throwable? = null,
	private val hasSyncedEvaluationsFlow: Flow<Boolean> = flowOf(true),
	private val evaluationsSnapshotFlow: Flow<ObservedSyncedSnapshot<List<Evaluation>>>? = null,
	private val refreshResult: EvaluationsRefreshResult? = null,
	private val availableSubjects: List<EditableAttemptDescriptor> = listOf(
		DEFAULT_EVALUATION_SUBJECT,
		SECOND_EVALUATION_SUBJECT
	),
	private val currentTerm: EvaluationTermDescriptor? = DEFAULT_EVALUATION_TERM
) : EvaluationRepository {
	private val evaluationsState = MutableStateFlow(initialEvaluations)

	val addCalls = mutableListOf<EvaluationAdd>()
	val updateCalls = mutableListOf<EvaluationUpdate>()
	val removeCalls = mutableListOf<EvaluationRemove>()
	var updateEvaluationsCalls = 0
	val updateEvaluationsForceRemoteCalls = mutableListOf<Boolean>()

	override suspend fun observeEvaluationsFlow(): Flow<List<Evaluation>> = evaluationsFlow ?: evaluationsState

	override suspend fun observeHasSyncedEvaluationsFlow(): Flow<Boolean> = hasSyncedEvaluationsFlow

	override suspend fun observeEvaluationsSnapshotFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> {
		return evaluationsSnapshotFlow ?: kotlinx.coroutines.flow.combine(
			evaluationsFlow ?: evaluationsState,
			hasSyncedEvaluationsFlow
		) { evaluations, hasSyncedEvaluations ->
			ObservedSyncedSnapshot(
				value = evaluations,
				hasSynced = hasSyncedEvaluations
			)
		}
	}

	override suspend fun getEvaluationsSnapshot(): ObservedSyncedSnapshot<List<Evaluation>> {
		return observeEvaluationsSnapshotFlow().first()
	}

	override suspend fun updateEvaluations(): EvaluationsRefreshResult {
		return updateEvaluations(forceRemote = false)
	}

	override suspend fun updateEvaluations(forceRemote: Boolean): EvaluationsRefreshResult {
		updateEvaluationsCalls++
		updateEvaluationsForceRemoteCalls += forceRemote
		refreshThrowable?.let { throw it }
		refreshedEvaluations?.let { evaluations -> evaluationsState.value = evaluations }
		return refreshResult ?: EvaluationsRefreshResult(
			hasEvaluations = evaluationsState.value.isNotEmpty(),
			hasAvailableAttempts = availableSubjects.isNotEmpty()
		)
	}

	override suspend fun drainPendingMutations() = Unit

	override suspend fun getEvaluation(eid: String): Evaluation? {
		getEvaluationThrowable?.let { throw it }
		return evaluationsState.value.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun addEvaluation(add: EvaluationAdd) {
		addCalls += add
		addThrowable?.let { throw it }
		evaluationsState.value += add.toEvaluation()
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate) {
		updateCalls += update
		updateThrowable?.let { throw it }
		evaluationsState.value = evaluationsState.value.map { evaluation ->
			if (evaluation.id == update.id) {
				val resolvedScheduleMode = update.scheduleMode ?: if (update.date != null) {
					EvaluationScheduleMode.DATED
				} else {
					evaluation.scheduleMode
				}
				val resolvedDate = when (resolvedScheduleMode) {
					EvaluationScheduleMode.CONTINUOUS -> null
					EvaluationScheduleMode.DATED -> update.date ?: evaluation.date
				}

				evaluation.copy(
					scheduleMode = resolvedScheduleMode,
					grade = update.grade,
					maxGrade = update.maxGrade ?: evaluation.maxGrade,
					date = resolvedDate,
					type = update.type ?: evaluation.type,
					state = computeEvaluationState(
						scheduleMode = resolvedScheduleMode,
						grade = update.grade,
						date = resolvedDate
					)
				)
			} else {
				evaluation
			}
		}
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		removeCalls += remove
		removeThrowable?.let { throw it }
		evaluationsState.value = evaluationsState.value.filterNot { evaluation ->
			evaluation.id == remove.id
		}
	}

	override suspend fun getAvailableAttempts(): List<EditableAttemptDescriptor> {
		availableAttemptsThrowable?.let { throw it }
		return availableSubjects
	}

	override suspend fun getCurrentTerm(): EvaluationTermDescriptor? = currentTerm
}

class FakeDatabaseDataSource(
	initialSnapshot: LocalEvaluationsSnapshot = LocalEvaluationsSnapshot(
		hasSynced = DEFAULT_HAS_SYNCED_EVALUATIONS,
		evaluations = listOf(
			DEFAULT_LOCAL_PENDING_EVALUATION,
			DEFAULT_LOCAL_COMPLETED_EVALUATION
		)
	),
	private val availableSubjects: List<LocalEditableAttemptDescriptor> = listOf(
		DEFAULT_LOCAL_EVALUATION_SUBJECT,
		SECOND_LOCAL_EVALUATION_SUBJECT
	),
	private val currentTerm: LocalCurrentTermDescriptor? = DEFAULT_LOCAL_EVALUATION_TERM
) : DatabaseDataRepository {
	private val snapshotState = MutableStateFlow(initialSnapshot)
	private val hasSyncedEvaluationsState = MutableStateFlow(initialSnapshot.hasSynced)

	val savedSnapshots = mutableListOf<LocalEvaluationsSnapshot>()
	val addedEvaluations = mutableListOf<LocalEvaluation>()
	val updatedEvaluations = mutableListOf<LocalEvaluation>()
	val removedEvaluations = mutableListOf<String>()

	override fun observeEvaluationsFlow(): Flow<List<LocalEvaluation>> {
		return snapshotState.map { snapshot -> snapshot.evaluations }
	}

	override fun observeHasSyncedEvaluationsFlow(): Flow<Boolean> = hasSyncedEvaluationsState

	override fun observeEvaluationsSnapshotFlow(): Flow<LocalEvaluationsSnapshot> = snapshotState

	override suspend fun getEvaluationsSnapshot(): LocalEvaluationsSnapshot = snapshotState.value

	override suspend fun getEvaluation(eid: String): LocalEvaluation? {
		return snapshotState.value.evaluations.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun getConfirmedSnapshot(): LocalEvaluationsSnapshot = snapshotState.value

	override suspend fun getAvailableAttempts(): List<LocalEditableAttemptDescriptor> = availableSubjects

	override suspend fun getCurrentTerm(): LocalCurrentTermDescriptor? = currentTerm

	override suspend fun confirmAddedEvaluation(evaluation: LocalEvaluation): LocalEvaluation {
		addedEvaluations += evaluation
		hasSyncedEvaluationsState.value = true
		snapshotState.value = snapshotState.value.copy(
			hasSynced = true,
			evaluations = snapshotState.value.evaluations
				.filterNot { current ->
					current.id == evaluation.id || current.referenceId == evaluation.referenceId
				} + evaluation
		)
		return evaluation
	}

	override suspend fun confirmUpdatedEvaluation(evaluation: LocalEvaluation): LocalEvaluation {
		updatedEvaluations += evaluation
		hasSyncedEvaluationsState.value = true
		snapshotState.value = snapshotState.value.copy(
			hasSynced = true,
			evaluations = snapshotState.value.evaluations.map { current ->
				if (current.id == evaluation.id) evaluation else current
			}
		)
		return evaluation
	}

	override suspend fun confirmRemovedEvaluation(eid: String) {
		removedEvaluations += eid
		hasSyncedEvaluationsState.value = true
		snapshotState.value = snapshotState.value.copy(
			hasSynced = true,
			evaluations = snapshotState.value.evaluations.filterNot { evaluation -> evaluation.id == eid }
		)
	}

	override suspend fun removeConfirmedEvaluation(eid: String) {
		removedEvaluations += eid
		snapshotState.value = snapshotState.value.copy(
			evaluations = snapshotState.value.evaluations.filterNot { evaluation -> evaluation.id == eid }
		)
	}

	override suspend fun saveConfirmedSnapshot(snapshot: LocalEvaluationsSnapshot) {
		savedSnapshots += snapshot
		hasSyncedEvaluationsState.value = true
		snapshotState.value = snapshot
	}
}

data class AddEvaluationRemoteCall(
	val add: EvaluationMutation.Add,
	val mutationId: String
)

data class UpdateEvaluationRemoteCall(
	val update: EvaluationMutation.Update,
	val mutationId: String,
	val expectedRevision: Long
)

data class RemoveEvaluationRemoteCall(
	val evaluationId: String,
	val mutationId: String,
	val expectedRevision: Long
)

class FakeEvaluationsApiDataSource(
	private val snapshot: RemoteEvaluationsSnapshot = RemoteEvaluationsSnapshot(
		evaluations = listOf(
			DEFAULT_REMOTE_PENDING_EVALUATION,
			DEFAULT_REMOTE_COMPLETED_EVALUATION
		)
	),
	private val addResult: RemoteEvaluation? = null,
	private val updateResult: RemoteEvaluation? = null,
	private val getEvaluationsThrowable: Throwable? = null,
	private val addThrowable: Throwable? = null,
	private val updateThrowable: Throwable? = null,
	private val removeThrowable: Throwable? = null
) : EvaluationsApiDataRepository {
	var getEvaluationsCalls = 0
	val addCalls = mutableListOf<AddEvaluationRemoteCall>()
	val updateCalls = mutableListOf<UpdateEvaluationRemoteCall>()
	val removeCalls = mutableListOf<RemoveEvaluationRemoteCall>()

	override suspend fun getEvaluations(): RemoteEvaluationsSnapshot {
		getEvaluationsCalls++
		getEvaluationsThrowable?.let { throw it }
		return snapshot
	}

	override suspend fun getEvaluation(eid: String): RemoteEvaluation? {
		return snapshot.evaluations.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun addEvaluation(
		add: EvaluationMutation.Add,
		mutationId: String
	): EvaluationMutationAck.Add {
		addThrowable?.let { throw it }
		addCalls += AddEvaluationRemoteCall(add, mutationId)
		val created = addResult ?: RemoteEvaluation(
			id = "real-${add.referenceId}",
			referenceId = add.referenceId,
			attemptId = add.attemptId,
			subjectCode = add.subjectCode,
			termId = add.termId,
			revision = 1L,
			scheduleMode = add.scheduleMode,
			grade = add.grade,
			maxGrade = add.maxGrade,
			date = add.date,
			type = add.type,
			isDone = add.grade != null
		)
		return EvaluationMutationAck.Add(
			mutationId = mutationId,
			evaluation = created
		)
	}

	override suspend fun updateEvaluation(
		update: EvaluationMutation.Update,
		mutationId: String,
		expectedRevision: Long
	): EvaluationMutationAck.Update {
		updateThrowable?.let { throw it }
		updateCalls += UpdateEvaluationRemoteCall(update, mutationId, expectedRevision)
		val saved = updateResult ?: DEFAULT_REMOTE_PENDING_EVALUATION.copy(
			id = update.evaluationId,
			revision = DEFAULT_REMOTE_PENDING_EVALUATION.revision + 1L,
			scheduleMode = update.scheduleMode ?: DEFAULT_REMOTE_PENDING_EVALUATION.scheduleMode,
			grade = update.grade,
			maxGrade = update.maxGrade ?: DEFAULT_REMOTE_PENDING_EVALUATION.maxGrade,
			date = update.date ?: DEFAULT_REMOTE_PENDING_EVALUATION.date,
			type = update.type ?: DEFAULT_REMOTE_PENDING_EVALUATION.type,
			isDone = update.grade != null
		)
		return EvaluationMutationAck.Update(
			mutationId = mutationId,
			evaluation = saved
		)
	}

	override suspend fun removeEvaluation(
		eid: String,
		mutationId: String,
		expectedRevision: Long
	): EvaluationMutationAck.Remove {
		removeThrowable?.let { throw it }
		removeCalls += RemoveEvaluationRemoteCall(eid, mutationId, expectedRevision)
		return EvaluationMutationAck.Remove(
			mutationId = mutationId,
			removedEvaluationId = eid
		)
	}
}

class FakeSettingsDataSource(
	private val onCooldown: Boolean
) : SettingsDataRepository {
	var cooldownMarked = false

	override suspend fun isGetEvaluationsOnCooldown(): Boolean = onCooldown

	override suspend fun setGetEvaluationsOnCooldown() {
		cooldownMarked = true
	}
}

class FakeIdentifierRepository(
	private val identifier: String = "generated-evaluation-id"
) : IdentifierRepository {
	override fun generateRandomIdentifier(): String = identifier
}

class RecordingReportingRepository : ReportingRepository {
	val exceptions = mutableListOf<Throwable>()

	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) {
		exceptions += throwable
	}

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

class FakeMutationEnvelopeStore<ScopeKey : Any, T : OutboxMutation>(
	initialPendingMutations: List<MutationEnvelope<ScopeKey, T>> = emptyList()
) : MutationEnvelopeStore<ScopeKey, T> {
	private val state = MutableStateFlow(initialPendingMutations)

	override fun observePendingMutations(scopeKey: ScopeKey): Flow<List<MutationEnvelope<ScopeKey, T>>> {
		return state.map { mutations ->
			mutations.filter { mutation ->
				mutation.scopeKey == scopeKey && mutation.status == PendingMutationStatus.Pending
			}
		}
	}

	override suspend fun getPendingMutations(scopeKey: ScopeKey): List<MutationEnvelope<ScopeKey, T>> {
		return state.value.filter { mutation ->
			mutation.scopeKey == scopeKey && mutation.status == PendingMutationStatus.Pending
		}
	}

	override suspend fun getPendingMutation(
		scopeKey: ScopeKey,
		mutationId: String
	): MutationEnvelope<ScopeKey, T>? {
		return state.value.firstOrNull { mutation ->
			mutation.scopeKey == scopeKey && mutation.mutationId == mutationId
		}
	}

	override suspend fun replacePendingMutation(mutation: MutationEnvelope<ScopeKey, T>) {
		state.value = state.value
			.filterNot { pending -> pending.replaceKey == mutation.replaceKey }
			.plus(mutation)
	}

	override suspend fun savePendingMutation(mutation: MutationEnvelope<ScopeKey, T>) {
		state.value = state.value
			.filterNot { pending -> pending.mutationId == mutation.mutationId }
			.plus(mutation)
	}

	override suspend fun deletePendingMutation(scopeKey: ScopeKey, mutationId: String) {
		state.value = state.value.filterNot { mutation ->
			mutation.scopeKey == scopeKey && mutation.mutationId == mutationId
		}
	}
}

fun createEvaluationsMutationEngine(
	store: MutationEnvelopeStore<String, EvaluationMutation> = FakeMutationEnvelopeStore(),
	coroutineScope: CoroutineScope? = null
): StoreBackedMutationEngine<String, EvaluationMutation, LocalEvaluationsSnapshot, List<LocalEvaluation>, EvaluationMutationAck> {
	return StoreBackedMutationEngine(
		storeId = EVALUATIONS_MUTATION_STORE_ID,
		outboxStore = store,
		coroutineScope = coroutineScope ?: testSessionCoroutineScope()
	)
}
