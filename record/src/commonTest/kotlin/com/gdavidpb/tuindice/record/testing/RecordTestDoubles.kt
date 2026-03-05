package com.gdavidpb.tuindice.record.testing

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.record.data.repository.QuarterLocalDataSource
import com.gdavidpb.tuindice.record.data.repository.QuarterRemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.QuarterSettingsDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
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
	grade = DEFAULT_RECORD_SUBJECT.grade
)

val DEFAULT_RECORD_REMOTE_SUBJECT = RemoteSubject(
	id = DEFAULT_RECORD_SUBJECT.id,
	quarterId = DEFAULT_RECORD_SUBJECT.quarterId,
	code = DEFAULT_RECORD_SUBJECT.code,
	name = DEFAULT_RECORD_SUBJECT.name,
	credits = DEFAULT_RECORD_SUBJECT.credits,
	grade = DEFAULT_RECORD_SUBJECT.grade
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
	subjects = listOf(DEFAULT_RECORD_REMOTE_SUBJECT)
)

val UPDATED_RECORD_LOCAL_QUARTER = DEFAULT_RECORD_LOCAL_QUARTER.copy(
	grade = 85.0,
	gradeSum = 85.0,
	subjects = listOf(DEFAULT_RECORD_LOCAL_SUBJECT.copy(grade = 85))
)

class RecordingQuarterRepository(
	private val quarters: Flow<List<Quarter>> = flowOf(listOf(DEFAULT_RECORD_QUARTER))
) : QuarterRepository {
	val removeCalls = MutableStateFlow<List<QuarterRemove>>(emptyList())
	val setGradeCalls = MutableStateFlow<List<SubjectGradeSet>>(emptyList())

	override suspend fun getQuartersFlow(): Flow<List<Quarter>> = quarters

	override suspend fun getQuarters(): List<Quarter> = quarters.replayCacheOrEmpty()

	override suspend fun removeQuarter(remove: QuarterRemove) {
		removeCalls.update { calls -> calls + remove }
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		setGradeCalls.update { calls -> calls + set }
	}

	private fun Flow<List<Quarter>>.replayCacheOrEmpty(): List<Quarter> {
		var value: List<Quarter> = emptyList()
		if (this is MutableStateFlow<List<Quarter>>) value = this.value
		return value
	}
}

class FakeQuarterLocalDataSource(
	initialQuarters: List<LocalQuarter> = listOf(DEFAULT_RECORD_LOCAL_QUARTER),
	private val setSubjectGradeResult: SetSubjectGradeResult = SetSubjectGradeResult(
		updatedQuarters = initialQuarters,
		updatedTargetQuarter = null
	)
) : QuarterLocalDataSource {
	private val quarterState = MutableStateFlow(initialQuarters)

	val savedQuarters = mutableListOf<List<LocalQuarter>>()
	val removedQuarterIds = mutableListOf<String>()
	var lastSetSubjectGradeArgs: SetSubjectGradeCall? = null

	override fun getQuartersFlow(): Flow<List<LocalQuarter>> = quarterState

	override suspend fun getQuarter(qid: String): LocalQuarter? {
		return quarterState.value.firstOrNull { quarter -> quarter.id == qid }
	}

	override suspend fun removeQuarter(qid: String) {
		removedQuarterIds += qid
		quarterState.value = quarterState.value.filterNot { quarter -> quarter.id == qid }
	}

	override suspend fun saveQuarters(quarters: List<LocalQuarter>) {
		savedQuarters += quarters
		quarterState.value = quarters
	}

	override suspend fun saveSubjects(subjects: List<LocalSubject>) = Unit

	override suspend fun setSubjectGradeAndRecompute(
		qid: String,
		sid: String,
		grade: Int,
		commit: Boolean
	): SetSubjectGradeResult {
		lastSetSubjectGradeArgs = SetSubjectGradeCall(
			quarterId = qid,
			subjectId = sid,
			grade = grade,
			commit = commit
		)
		quarterState.value = setSubjectGradeResult.updatedQuarters
		return setSubjectGradeResult
	}
}

data class SetSubjectGradeCall(
	val quarterId: String,
	val subjectId: String,
	val grade: Int,
	val commit: Boolean
)

class FakeQuarterRemoteDataSource(
	private val quarters: List<RemoteQuarter> = listOf(DEFAULT_RECORD_REMOTE_QUARTER)
) : QuarterRemoteDataSource {
	var getQuartersCalls = 0
	val removedQuarterIds = mutableListOf<String>()
	val addedQuarters = mutableListOf<RemoteQuarter>()

	override suspend fun getQuarters(): List<RemoteQuarter> {
		getQuartersCalls++
		return quarters
	}

	override suspend fun getQuarter(qid: String): RemoteQuarter {
		return quarters.first { quarter -> quarter.id == qid }
	}

	override suspend fun removeQuarter(qid: String) {
		removedQuarterIds += qid
	}

	override suspend fun addQuarter(quarter: RemoteQuarter): List<RemoteQuarter> {
		addedQuarters += quarter
		return quarters
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
