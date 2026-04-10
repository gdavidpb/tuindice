package com.gdavidpb.tuindice.evaluations.testing

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
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
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private const val PAST_EVALUATION_DATE = 1_700_000_000_000L
private const val FUTURE_EVALUATION_DATE = 1_900_000_000_000L
const val DEFAULT_EVALUATIONS_ANCHOR_REVISION = 10L

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
	maxGrade = 100.0,
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
	grade = 82.0,
	maxGrade = 100.0,
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
	private val hasSyncedEvaluationsFlow: Flow<Boolean> = flowOf(true),
	private val availableSubjects: List<EditableAttemptDescriptor> = listOf(
		DEFAULT_EVALUATION_SUBJECT,
		SECOND_EVALUATION_SUBJECT
	)
) : EvaluationRepository {
	private val evaluationsState = MutableStateFlow(initialEvaluations)

	val addCalls = mutableListOf<EvaluationAdd>()
	val updateCalls = mutableListOf<EvaluationUpdate>()
	val removeCalls = mutableListOf<EvaluationRemove>()
	var updateEvaluationsCalls = 0

	override suspend fun observeEvaluationsFlow(): Flow<List<Evaluation>> = evaluationsFlow ?: evaluationsState

	override suspend fun observeHasSyncedEvaluationsFlow(): Flow<Boolean> = hasSyncedEvaluationsFlow

	override suspend fun updateEvaluations() {
		updateEvaluationsCalls++
		refreshThrowable?.let { throw it }
	}

	override suspend fun getEvaluation(eid: String): Evaluation? {
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

	override suspend fun getAvailableAttempts(): List<EditableAttemptDescriptor> = availableSubjects
}

class FakeDatabaseDataSource(
	initialSnapshot: LocalEvaluationsSnapshot = LocalEvaluationsSnapshot(
		anchorRevision = DEFAULT_EVALUATIONS_ANCHOR_REVISION,
		evaluations = listOf(
			DEFAULT_LOCAL_PENDING_EVALUATION,
			DEFAULT_LOCAL_COMPLETED_EVALUATION
		)
	),
	private val availableSubjects: List<LocalEditableAttemptDescriptor> = listOf(
		DEFAULT_LOCAL_EVALUATION_SUBJECT,
		SECOND_LOCAL_EVALUATION_SUBJECT
	)
) : DatabaseDataRepository {
	private val snapshotState = MutableStateFlow(initialSnapshot)
	private val hasSyncedEvaluationsState = MutableStateFlow(initialSnapshot.anchorRevision != 0L)

	val savedSnapshots = mutableListOf<LocalEvaluationsSnapshot>()
	val addedEvaluations = mutableListOf<Pair<Long, LocalEvaluation>>()
	val updatedEvaluations = mutableListOf<Pair<Long, LocalEvaluation>>()
	val removedEvaluations = mutableListOf<Pair<Long?, String>>()

	override fun observeEvaluationsFlow(): Flow<List<LocalEvaluation>> {
		return snapshotState.map { snapshot -> snapshot.evaluations }
	}

	override fun observeHasSyncedEvaluationsFlow(): Flow<Boolean> = hasSyncedEvaluationsState

	override suspend fun getEvaluation(eid: String): LocalEvaluation? {
		return snapshotState.value.evaluations.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun getConfirmedSnapshot(): LocalEvaluationsSnapshot = snapshotState.value

	override suspend fun getAvailableAttempts(): List<LocalEditableAttemptDescriptor> = availableSubjects

	override suspend fun confirmAddedEvaluation(evaluation: LocalEvaluation, anchorRevision: Long): LocalEvaluation {
		addedEvaluations += anchorRevision to evaluation
		hasSyncedEvaluationsState.value = true
		snapshotState.value = snapshotState.value.copy(
			anchorRevision = anchorRevision,
			evaluations = snapshotState.value.evaluations
				.filterNot { current ->
					current.id == evaluation.id || current.referenceId == evaluation.referenceId
				} + evaluation
		)
		return evaluation
	}

	override suspend fun confirmUpdatedEvaluation(evaluation: LocalEvaluation, anchorRevision: Long): LocalEvaluation {
		updatedEvaluations += anchorRevision to evaluation
		hasSyncedEvaluationsState.value = true
		snapshotState.value = snapshotState.value.copy(
			anchorRevision = anchorRevision,
			evaluations = snapshotState.value.evaluations.map { current ->
				if (current.id == evaluation.id) evaluation else current
			}
		)
		return evaluation
	}

	override suspend fun confirmRemovedEvaluation(eid: String, anchorRevision: Long) {
		removedEvaluations += anchorRevision to eid
		hasSyncedEvaluationsState.value = true
		snapshotState.value = snapshotState.value.copy(
			anchorRevision = anchorRevision,
			evaluations = snapshotState.value.evaluations.filterNot { evaluation -> evaluation.id == eid }
		)
	}

	override suspend fun removeConfirmedEvaluation(eid: String) {
		removedEvaluations += null to eid
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
	val mutationId: String,
	val expectedRevision: Long
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
		anchorRevision = DEFAULT_EVALUATIONS_ANCHOR_REVISION,
		evaluations = listOf(
			DEFAULT_REMOTE_PENDING_EVALUATION,
			DEFAULT_REMOTE_COMPLETED_EVALUATION
		)
	),
	private val addResult: RemoteEvaluation? = null,
	private val updateResult: RemoteEvaluation? = null,
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
		return snapshot
	}

	override suspend fun getEvaluation(eid: String): RemoteEvaluation? {
		return snapshot.evaluations.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun addEvaluation(
		add: EvaluationMutation.Add,
		mutationId: String,
		expectedRevision: Long
	): EvaluationMutationAck.Add {
		addThrowable?.let { throw it }
		addCalls += AddEvaluationRemoteCall(add, mutationId, expectedRevision)
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
			anchorRevision = snapshot.anchorRevision + 1L,
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
			anchorRevision = snapshot.anchorRevision + 1L,
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
			anchorRevision = snapshot.anchorRevision + 1L,
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
		coroutineScope = coroutineScope ?: CoroutineScope(kotlinx.coroutines.Dispatchers.Default)
	)
}
