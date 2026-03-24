package com.gdavidpb.tuindice.record.testing

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.MutationOutboxRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.record.data.repository.QuarterLocalDataSource
import com.gdavidpb.tuindice.record.data.repository.QuarterRemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.QuarterSettingsDataSource
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteAddQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSetSubjectGradeAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.SetSubjectGradeResult
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
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

val UPDATED_RECORD_LOCAL_QUARTER = DEFAULT_RECORD_LOCAL_QUARTER.copy(
	grade = 85.0,
	gradeSum = 85.0,
	subjects = listOf(DEFAULT_RECORD_LOCAL_SUBJECT.copy(grade = 85))
)

class RecordingQuarterRepository(
	private val quarters: Flow<List<Quarter>> = flowOf(listOf(DEFAULT_RECORD_QUARTER)),
	private val updateThrowable: Throwable? = null,
	private val setSubjectGradeThrowable: Throwable? = null
) : QuarterRepository {
	val removeCalls = MutableStateFlow<List<QuarterRemove>>(emptyList())
	val setGradeCalls = MutableStateFlow<List<SubjectGradeSet>>(emptyList())
	val updateQuartersCalls = MutableStateFlow(0)

	override suspend fun observeQuartersFlow(): Flow<List<Quarter>> = quarters

	override suspend fun updateQuarters() {
		updateQuartersCalls.value += 1
		updateThrowable?.let { throw it }
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		removeCalls.update { calls -> calls + remove }
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		setGradeCalls.update { calls -> calls + set }
		setSubjectGradeThrowable?.let { throw it }
	}
}

class FakeQuarterLocalDataSource(
	initialQuarters: List<LocalQuarter> = listOf(DEFAULT_RECORD_LOCAL_QUARTER),
	setSubjectGradeResults: List<SetSubjectGradeResult> = emptyList()
) : QuarterLocalDataSource {
	private val quarterState = MutableStateFlow(initialQuarters)
	private val queuedSetSubjectGradeResults = ArrayDeque(setSubjectGradeResults)

	val savedQuarters = mutableListOf<List<LocalQuarter>>()
	val removedQuarterIds = mutableListOf<String>()
	val confirmedRemovedQuarterIds = mutableListOf<String>()
	val clearedPreviewArgs = mutableListOf<Pair<String, String>>()
	val setSubjectGradeCalls = mutableListOf<SetSubjectGradeCall>()
	var lastSetSubjectGradeArgs: SetSubjectGradeCall? = null

	override fun getQuartersFlow(): Flow<List<LocalQuarter>> = quarterState

	override suspend fun getQuarter(qid: String): LocalQuarter? {
		return quarterState.value.firstOrNull { quarter -> quarter.id == qid }
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
	val quarterId: String,
	val mutationId: String,
	val expectedRevision: Long
)

class FakeQuarterRemoteDataSource(
	private val quarters: List<RemoteQuarter> = listOf(DEFAULT_RECORD_REMOTE_QUARTER),
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
	val addedQuarters = mutableListOf<RemoteQuarter>()
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
		quarter: RemoteQuarter,
		mutationId: String,
		expectedRevision: Long
	): RemoteAddQuarterAck {
		addedQuarters += quarter
		addQuarterCalls += AddQuarterRemoteCall(
			quarterId = quarter.id,
			mutationId = mutationId,
			expectedRevision = expectedRevision
		)
		return addQuarterAck.copy(mutationId = mutationId, quarter = quarter)
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
	private val onCooldown: Boolean
) : QuarterSettingsDataSource {
	var cooldownMarked = false

	override suspend fun isGetQuartersOnCooldown(): Boolean = onCooldown

	override suspend fun setGetQuartersOnCooldown() {
		cooldownMarked = true
	}
}

class FakeMutationOutboxRepository<T : OutboxMutation>(
	initialPendingMutations: List<PendingMutation<T>> = emptyList()
) : MutationOutboxRepository<T> {
	private val state = MutableStateFlow(initialPendingMutations)

	override fun observePendingMutations(): Flow<List<PendingMutation<T>>> = state

	override suspend fun getPendingMutations(): List<PendingMutation<T>> = state.value

	override suspend fun getPendingMutation(mutationId: String): PendingMutation<T>? {
		return state.value.firstOrNull { mutation -> mutation.mutationId == mutationId }
	}

	override suspend fun replacePendingMutation(mutation: PendingMutation<T>) {
		state.value = state.value
			.filterNot { pending -> pending.mutation.replaceKey == mutation.mutation.replaceKey }
			.plus(mutation)
	}

	override suspend fun savePendingMutation(mutation: PendingMutation<T>) {
		state.value = state.value
			.filterNot { pending -> pending.mutationId == mutation.mutationId }
			.plus(mutation)
	}

	override suspend fun deletePendingMutation(mutationId: String) {
		state.value = state.value.filterNot { mutation -> mutation.mutationId == mutationId }
	}
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
