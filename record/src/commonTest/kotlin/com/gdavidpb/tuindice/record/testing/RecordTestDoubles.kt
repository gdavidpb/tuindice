package com.gdavidpb.tuindice.record.testing

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.record.data.source.QuarterLocalDataSource
import com.gdavidpb.tuindice.record.data.source.QuarterRemoteDataSource
import com.gdavidpb.tuindice.record.data.source.QuarterSettingsDataSource
import com.gdavidpb.tuindice.record.data.repository.mutation.RECORD_MUTATION_STORE_ID
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutationAck
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteAddQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSetSubjectGradeAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.SetSubjectGradeResult
import com.gdavidpb.tuindice.record.domain.model.QuarterAdd
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.repository.QuarterSelectionRepository
import com.gdavidpb.tuindice.record.domain.service.IndexComputationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

private const val DEFAULT_RECORD_START_DATE = 1_767_225_600_000L
private const val DEFAULT_RECORD_END_DATE = 1_777_420_800_000L
private const val DEFAULT_RECORD_REVISION = 1L

val DEFAULT_RECORD_SUBJECT = Subject(
	id = "subject-1",
	quarterId = "quarter-1",
	code = "INF-101",
	name = "Programacion",
	credits = 6,
	grade = 70
)

val DEFAULT_RECORD_QUARTER = Quarter(
	id = "quarter-1",
	name = "2026-1",
	startDate = DEFAULT_RECORD_START_DATE,
	endDate = DEFAULT_RECORD_END_DATE,
	grade = 70.0,
	gradeSum = 70.0,
	credits = 6,
	creditsSum = 6,
	isCurrent = true,
	isReadOnly = false,
	subjects = listOf(DEFAULT_RECORD_SUBJECT)
)

val DEFAULT_RECORD_LOCAL_SUBJECT = LocalSubject(
	id = DEFAULT_RECORD_SUBJECT.id,
	quarterId = DEFAULT_RECORD_SUBJECT.quarterId,
	code = DEFAULT_RECORD_SUBJECT.code,
	name = DEFAULT_RECORD_SUBJECT.name,
	credits = DEFAULT_RECORD_SUBJECT.credits,
	grade = DEFAULT_RECORD_SUBJECT.grade,
	revision = DEFAULT_RECORD_REVISION
)

val DEFAULT_RECORD_REMOTE_SUBJECT = RemoteSubject(
	id = DEFAULT_RECORD_SUBJECT.id,
	quarterId = DEFAULT_RECORD_SUBJECT.quarterId,
	code = DEFAULT_RECORD_SUBJECT.code,
	name = DEFAULT_RECORD_SUBJECT.name,
	credits = DEFAULT_RECORD_SUBJECT.credits,
	grade = DEFAULT_RECORD_SUBJECT.grade,
	revision = DEFAULT_RECORD_REVISION
)

val DEFAULT_RECORD_LOCAL_QUARTER = LocalQuarter(
	id = DEFAULT_RECORD_QUARTER.id,
	name = DEFAULT_RECORD_QUARTER.name,
	startDate = DEFAULT_RECORD_QUARTER.startDate,
	endDate = DEFAULT_RECORD_QUARTER.endDate,
	grade = DEFAULT_RECORD_QUARTER.grade,
	gradeSum = DEFAULT_RECORD_QUARTER.gradeSum,
	credits = DEFAULT_RECORD_QUARTER.credits,
	creditsSum = DEFAULT_RECORD_QUARTER.creditsSum,
	isCurrent = DEFAULT_RECORD_QUARTER.isCurrent,
	isReadOnly = DEFAULT_RECORD_QUARTER.isReadOnly,
	revision = DEFAULT_RECORD_REVISION,
	subjects = listOf(DEFAULT_RECORD_LOCAL_SUBJECT)
)

val DEFAULT_RECORD_REMOTE_QUARTER = RemoteQuarter(
	id = DEFAULT_RECORD_QUARTER.id,
	name = DEFAULT_RECORD_QUARTER.name,
	startDate = DEFAULT_RECORD_QUARTER.startDate,
	endDate = DEFAULT_RECORD_QUARTER.endDate,
	grade = DEFAULT_RECORD_QUARTER.grade,
	gradeSum = DEFAULT_RECORD_QUARTER.gradeSum,
	credits = DEFAULT_RECORD_QUARTER.credits,
	creditsSum = DEFAULT_RECORD_QUARTER.creditsSum,
	isCurrent = DEFAULT_RECORD_QUARTER.isCurrent,
	isReadOnly = DEFAULT_RECORD_QUARTER.isReadOnly,
	revision = DEFAULT_RECORD_REVISION,
	subjects = listOf(DEFAULT_RECORD_REMOTE_SUBJECT)
)

class RecordingQuarterRepository(
	private val quarters: Flow<List<Quarter>> = flowOf(listOf(DEFAULT_RECORD_QUARTER)),
	private val updateThrowable: Throwable? = null,
	private val setSubjectGradeThrowable: Throwable? = null
) : QuarterRepository {
	val addCalls = MutableStateFlow<List<QuarterAdd>>(emptyList())
	val removeCalls = MutableStateFlow<List<QuarterRemove>>(emptyList())
	val setGradeCalls = MutableStateFlow<List<SubjectGradeSet>>(emptyList())
	val updateQuartersCalls = MutableStateFlow(0)

	override suspend fun observeQuartersFlow(): Flow<List<Quarter>> = quarters

	override suspend fun updateQuarters() {
		updateQuartersCalls.value += 1
		updateThrowable?.let { throw it }
	}

	override suspend fun addQuarter(add: QuarterAdd) {
		addCalls.update { calls -> calls + add }
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		removeCalls.update { calls -> calls + remove }
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		setGradeCalls.update { calls -> calls + set }
		setSubjectGradeThrowable?.let { throw it }
	}
}

class RecordingQuarterSelectionRepository(
	initialSelectedQuarterId: String? = null
) : QuarterSelectionRepository {
	var selectedQuarterId: String? = initialSelectedQuarterId
	val setSelectedQuarterIdCalls = mutableListOf<String>()

	override suspend fun getSelectedQuarterId(): String? = selectedQuarterId

	override suspend fun setSelectedQuarterId(quarterId: String) {
		selectedQuarterId = quarterId
		setSelectedQuarterIdCalls += quarterId
	}
}

class FakeQuarterLocalDataSource(
	initialQuarters: List<LocalQuarter> = listOf(DEFAULT_RECORD_LOCAL_QUARTER),
	setSubjectGradeResults: List<SetSubjectGradeResult> = emptyList()
) : QuarterLocalDataSource {
	private val indexComputationEngine = IndexComputationEngine()
	private val quarterState = MutableStateFlow(initialQuarters)
	private val queuedSetSubjectGradeResults = ArrayDeque(setSubjectGradeResults)

	val savedQuarters = mutableListOf<List<LocalQuarter>>()
	val removedQuarterIds = mutableListOf<String>()
	val confirmedRemovedQuarterIds = mutableListOf<String>()
	val confirmedAddedQuarters = mutableListOf<LocalQuarter>()
	val confirmedSubjectGradeMutations = mutableListOf<List<LocalQuarter>>()
	val clearedPreviewArgs = mutableListOf<Pair<String, String>>()
	val setSubjectGradeCalls = mutableListOf<SetSubjectGradeCall>()
	var lastSetSubjectGradeArgs: SetSubjectGradeCall? = null

	override fun getQuartersFlow(): Flow<List<LocalQuarter>> = quarterState

	override suspend fun getConfirmedQuarters(): List<LocalQuarter> = quarterState.value

	override suspend fun getQuarter(qid: String): LocalQuarter? {
		return quarterState.value.firstOrNull { quarter -> quarter.id == qid }
	}

	override suspend fun confirmQuarterAddition(
		addedQuarter: LocalQuarter,
		affectedQuarters: List<LocalQuarter>
	) {
		confirmedAddedQuarters += addedQuarter
		val currentByQuarterId = quarterState.value.associateBy { quarter -> quarter.id }
		val mergedAffectedById = buildMap {
			put(addedQuarter.id, addedQuarter)
			affectedQuarters.forEach { quarter ->
				val currentQuarter = currentByQuarterId[quarter.id]
				put(
					quarter.id,
					if (currentQuarter == null) {
						quarter
					} else {
						quarter.copy(
							revision = maxOf(currentQuarter.revision, quarter.revision),
							subjects = mergeSubjectsKeepingLatestRevision(
								currentSubjects = currentQuarter.subjects,
								incomingSubjects = quarter.subjects
							)
						)
					}
				)
			}
		}
		val patchedSnapshot = quarterState.value
			.map { quarter -> mergedAffectedById[quarter.id] ?: quarter }
			.toMutableList()

		mergedAffectedById.values.forEach { mergedQuarter ->
			if (patchedSnapshot.none { quarter -> quarter.id == mergedQuarter.id }) {
				patchedSnapshot += mergedQuarter
			}
		}

		quarterState.value = indexComputationEngine.recompute(
			quarters = patchedSnapshot,
			affectedStartDate = mergedAffectedById.values.minOf { quarter -> quarter.startDate }
		).quarters
	}

	override suspend fun removeQuarter(qid: String) {
		removedQuarterIds += qid
		quarterState.value = quarterState.value.filterNot { quarter -> quarter.id == qid }
	}

	override suspend fun confirmQuarterRemoval(qid: String, affectedQuarters: List<LocalQuarter>) {
		confirmedRemovedQuarterIds += qid
		val updatesById = affectedQuarters.associateBy { quarter -> quarter.id }
		quarterState.value = quarterState.value
			.filterNot { quarter -> quarter.id == qid }
			.map { quarter -> updatesById[quarter.id] ?: quarter }
	}

	override suspend fun confirmSubjectGradeMutation(affectedQuarters: List<LocalQuarter>) {
		confirmedSubjectGradeMutations += affectedQuarters
		if (affectedQuarters.isEmpty()) return

		val currentByQuarterId = quarterState.value.associateBy { quarter -> quarter.id }
		val mergedAffectedById = affectedQuarters
			.map { incomingQuarter ->
				val currentQuarter = currentByQuarterId[incomingQuarter.id]
					?: return@map incomingQuarter
				incomingQuarter.copy(
					revision = maxOf(currentQuarter.revision, incomingQuarter.revision),
					subjects = mergeSubjectsKeepingLatestRevision(
						currentSubjects = currentQuarter.subjects,
						incomingSubjects = incomingQuarter.subjects
					)
				)
			}
			.associateBy { quarter -> quarter.id }
		val patchedSnapshot = quarterState.value
			.map { quarter -> mergedAffectedById[quarter.id] ?: quarter }
			.toMutableList()

		mergedAffectedById.values.forEach { mergedQuarter ->
			if (patchedSnapshot.none { quarter -> quarter.id == mergedQuarter.id }) {
				patchedSnapshot += mergedQuarter
			}
		}

		val recomputed = indexComputationEngine.recompute(
			quarters = patchedSnapshot,
			affectedStartDate = mergedAffectedById.values.minOf { quarter -> quarter.startDate }
		)

		quarterState.value = recomputed.quarters
	}

	override suspend fun saveQuarters(quarters: List<LocalQuarter>) {
		savedQuarters += quarters
		val updatesById = quarters.associateBy { quarter -> quarter.id }
		quarterState.value = quarterState.value
			.map { quarter -> updatesById[quarter.id] ?: quarter }
			.plus(quarters.filter { update -> quarterState.value.none { quarter -> quarter.id == update.id } })
	}

	override suspend fun saveSubjects(subjects: List<LocalSubject>) = Unit

	override suspend fun clearSubjectGradePreview(qid: String, sid: String) {
		clearedPreviewArgs += qid to sid
	}

	override suspend fun setSubjectGradeAndRecompute(
		qid: String,
		sid: String,
		grade: Int,
		commit: Boolean
	): SetSubjectGradeResult {
		val call = SetSubjectGradeCall(
			quarterId = qid,
			subjectId = sid,
			grade = grade,
			commit = commit
		)
		lastSetSubjectGradeArgs = call
		setSubjectGradeCalls += call

		queuedSetSubjectGradeResults.removeFirstOrNull()?.let { result ->
			if (commit && result is SetSubjectGradeResult.Applied) {
				quarterState.value = result.updatedQuarters
			}
			return result
		}

		val expectedRevision = quarterState.value
			.firstOrNull { quarter -> quarter.id == qid }
			?.subjects
			?.firstOrNull { subject -> subject.id == sid }
			?.revision

		val updatedQuarters = quarterState.value.map { quarter ->
			if (quarter.id != qid || !commit) return@map quarter

			val updatedSubjects = quarter.subjects.map { subject ->
				if (subject.id == sid) subject.copy(grade = grade) else subject
			}

			quarter.copy(
				grade = grade.toDouble(),
				gradeSum = grade.toDouble(),
				subjects = updatedSubjects
			)
		}

		if (commit) {
			quarterState.value = updatedQuarters
		}

		return SetSubjectGradeResult.Applied(
			updatedQuarters = if (commit) updatedQuarters else quarterState.value,
			updatedTargetQuarter = updatedQuarters.firstOrNull { quarter -> quarter.id == qid }
				?: quarterState.value.first { quarter -> quarter.id == qid },
			expectedRevision = expectedRevision ?: DEFAULT_RECORD_REVISION
		)
	}

	private fun mergeSubjectsKeepingLatestRevision(
		currentSubjects: List<LocalSubject>,
		incomingSubjects: List<LocalSubject>
	): List<LocalSubject> {
		val incomingById = incomingSubjects.associateBy { subject -> subject.id }
		val mergedSubjects = currentSubjects
			.map { currentSubject ->
				val incomingSubject = incomingById[currentSubject.id]
					?: return@map currentSubject

				if (currentSubject.revision > incomingSubject.revision) {
					currentSubject
				} else {
					incomingSubject
				}
			}
			.toMutableList()

		incomingSubjects.forEach { incomingSubject ->
			if (mergedSubjects.none { subject -> subject.id == incomingSubject.id }) {
				mergedSubjects += incomingSubject
			}
		}

		return mergedSubjects
	}
}

data class SetSubjectGradeCall(
	val quarterId: String,
	val subjectId: String,
	val grade: Int,
	val commit: Boolean,
	val mutationId: String? = null,
	val expectedRevision: Long? = null
)

data class RemoveQuarterRemoteCall(
	val quarterId: String,
	val mutationId: String,
	val expectedRevision: Long
)

data class AddQuarterRemoteCall(
	val quarter: Int,
	val year: Int,
	val subjects: List<RecordMutation.AddQuarter.SubjectSeed>,
	val mutationId: String,
	val expectedRevision: Long
)

class FakeQuarterRemoteDataSource(
	private val quarters: List<RemoteQuarter> = listOf(DEFAULT_RECORD_REMOTE_QUARTER),
	private val addQuarterThrowable: Throwable? = null,
	private val removeQuarterThrowable: Throwable? = null,
	private val setSubjectGradeThrowable: Throwable? = null,
	private val addQuarterAck: RemoteAddQuarterAck = RemoteAddQuarterAck(
		mutationId = "mutation-add-1",
		quarter = DEFAULT_RECORD_REMOTE_QUARTER,
		affectedQuarters = listOf(DEFAULT_RECORD_REMOTE_QUARTER)
	),
	private val removeQuarterAck: RemoteDeleteQuarterAck = RemoteDeleteQuarterAck(
		mutationId = "mutation-remove-1",
		removedQuarterId = DEFAULT_RECORD_QUARTER.id,
		affectedQuarters = emptyList()
	),
	private val setSubjectGradeAck: RemoteSetSubjectGradeAck = RemoteSetSubjectGradeAck(
		mutationId = "mutation-grade-1",
		subject = DEFAULT_RECORD_REMOTE_SUBJECT.copy(grade = 85),
		affectedQuarters = listOf(DEFAULT_RECORD_REMOTE_QUARTER.copy(
			grade = 85.0,
			gradeSum = 85.0,
			subjects = listOf(DEFAULT_RECORD_REMOTE_SUBJECT.copy(grade = 85))
		))
	)
) : QuarterRemoteDataSource {
	var getQuartersCalls = 0
	val removeQuarterCalls = mutableListOf<RemoveQuarterRemoteCall>()
	val addQuarterCalls = mutableListOf<AddQuarterRemoteCall>()
	val setSubjectGradeCalls = mutableListOf<SetSubjectGradeCall>()

	override suspend fun getQuarters(): List<RemoteQuarter> {
		getQuartersCalls++
		return quarters
	}

	override suspend fun getQuarter(qid: String): RemoteQuarter {
		return quarters.first { quarter -> quarter.id == qid }
	}

	override suspend fun removeQuarter(
		qid: String,
		mutationId: String,
		expectedRevision: Long
	): RemoteDeleteQuarterAck {
		removeQuarterThrowable?.let { throw it }
		removeQuarterCalls += RemoveQuarterRemoteCall(
			quarterId = qid,
			mutationId = mutationId,
			expectedRevision = expectedRevision
		)
		return removeQuarterAck.copy(mutationId = mutationId)
	}

	override suspend fun addQuarter(
		add: RecordMutation.AddQuarter,
		mutationId: String,
		expectedRevision: Long
	): RemoteAddQuarterAck {
		addQuarterThrowable?.let { throw it }
		addQuarterCalls += AddQuarterRemoteCall(
			quarter = add.quarter,
			year = add.year,
			subjects = add.subjects,
			mutationId = mutationId,
			expectedRevision = expectedRevision
		)
		return addQuarterAck.copy(mutationId = mutationId)
	}

	override suspend fun setSubjectGrade(
		qid: String,
		sid: String,
		grade: Int,
		mutationId: String,
		expectedRevision: Long
	): RemoteSetSubjectGradeAck {
		setSubjectGradeThrowable?.let { throw it }
		setSubjectGradeCalls += SetSubjectGradeCall(
			quarterId = qid,
			subjectId = sid,
			grade = grade,
			commit = true,
			mutationId = mutationId,
			expectedRevision = expectedRevision
		)
		return setSubjectGradeAck.copy(mutationId = mutationId)
	}
}

class FakeQuarterSettingsDataSource(
	private val onCooldown: Boolean,
	initialSelectedQuarterId: String? = null
) : QuarterSettingsDataSource {
	var cooldownMarked = false
	var selectedQuarterId: String? = initialSelectedQuarterId
	val selectedQuarterIdWrites = mutableListOf<String?>()

	override suspend fun isGetQuartersOnCooldown(): Boolean = onCooldown

	override suspend fun setGetQuartersOnCooldown() {
		cooldownMarked = true
	}

	override fun getSelectedQuarterId(): String? = selectedQuarterId

	override fun setSelectedQuarterId(quarterId: String) {
		selectedQuarterId = quarterId
		selectedQuarterIdWrites += quarterId
	}
}

class FakeMutationEnvelopeStore<ScopeKey : Any, T : OutboxMutation>(
	initialPendingMutations: List<MutationEnvelope<ScopeKey, T>> = emptyList()
) : MutationEnvelopeStore<ScopeKey, T> {
	private val state = MutableStateFlow(initialPendingMutations)

	override fun observePendingMutations(scopeKey: ScopeKey): Flow<List<MutationEnvelope<ScopeKey, T>>> = state

	override suspend fun getPendingMutations(scopeKey: ScopeKey): List<MutationEnvelope<ScopeKey, T>> = state.value

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

fun createRecordMutationEngine(
	store: MutationEnvelopeStore<String, RecordMutation> = FakeMutationEnvelopeStore()
): StoreBackedMutationEngine<String, RecordMutation, List<LocalQuarter>, List<LocalQuarter>, RecordMutationAck> {
	return StoreBackedMutationEngine(
		storeId = RECORD_MUTATION_STORE_ID,
		outboxStore = store
	)
}

class FakeNetworkRepository(
	private val isAvailable: Boolean
) : NetworkRepository {
	override fun isAvailable(): Boolean = isAvailable
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
