package com.gdavidpb.tuindice.record

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.record.data.repository.quarter.LocalDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.RemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.SettingsDataSource
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

object RecordFixtures {
	fun subject(
		id: String = "s1",
		quarterId: String = "q1",
		code: String = "MA1111",
		name: String = "MATEMATICAS I",
		credits: Int = 4,
		grade: Int = 5
	) = Subject(
		id = id,
		quarterId = quarterId,
		code = code,
		name = name,
		credits = credits,
		grade = grade
	)

	fun quarter(
		id: String = "q1",
		name: String = "Enero - Marzo 2024",
		startDate: Long = 1704067200000L,
		endDate: Long = 1711843200000L,
		grade: Double = 4.5,
		gradeSum: Double = 4.2,
		credits: Int = 6,
		creditsSum: Int = 12,
		isCurrent: Boolean = false,
		isReadOnly: Boolean = false,
		subjects: List<Subject> = listOf(subject(quarterId = id))
	) = Quarter(
		id = id,
		name = name,
		startDate = startDate,
		endDate = endDate,
		grade = grade,
		gradeSum = gradeSum,
		credits = credits,
		creditsSum = creditsSum,
		isCurrent = isCurrent,
		isReadOnly = isReadOnly,
		subjects = subjects
	)

	fun localSubject(
		id: String = "s1",
		quarterId: String = "q1",
		code: String = "MA1111",
		name: String = "MATEMATICAS I",
		credits: Int = 4,
		grade: Int = 5
	) = LocalSubject(
		id = id,
		quarterId = quarterId,
		code = code,
		name = name,
		credits = credits,
		grade = grade
	)

	fun localQuarter(
		id: String = "q1",
		name: String = "Enero - Marzo 2024",
		startDate: Long = 1704067200000L,
		endDate: Long = 1711843200000L,
		grade: Double = 4.5,
		gradeSum: Double = 4.2,
		credits: Int = 6,
		creditsSum: Int = 12,
		isCurrent: Boolean = false,
		isReadOnly: Boolean = false,
		subjects: List<LocalSubject> = listOf(localSubject(quarterId = id))
	) = LocalQuarter(
		id = id,
		name = name,
		startDate = startDate,
		endDate = endDate,
		grade = grade,
		gradeSum = gradeSum,
		credits = credits,
		creditsSum = creditsSum,
		isCurrent = isCurrent,
		isReadOnly = isReadOnly,
		subjects = subjects
	)

	fun remoteSubject(
		id: String = "s1",
		quarterId: String = "q1",
		code: String = "MA1111",
		name: String = "MATEMATICAS I",
		credits: Int = 4,
		grade: Int = 5
	) = RemoteSubject(
		id = id,
		quarterId = quarterId,
		code = code,
		name = name,
		credits = credits,
		grade = grade
	)

	fun remoteQuarter(
		id: String = "q1",
		name: String = "Enero - Marzo 2024",
		startDate: Long = 1704067200000L,
		endDate: Long = 1711843200000L,
		grade: Double = 4.5,
		gradeSum: Double = 4.2,
		credits: Int = 6,
		creditsSum: Int = 12,
		isCurrent: Boolean = false,
		isReadOnly: Boolean = false,
		subjects: List<RemoteSubject> = listOf(remoteSubject(quarterId = id))
	) = RemoteQuarter(
		id = id,
		name = name,
		startDate = startDate,
		endDate = endDate,
		grade = grade,
		gradeSum = gradeSum,
		credits = credits,
		creditsSum = creditsSum,
		isCurrent = isCurrent,
		isReadOnly = isReadOnly,
		subjects = subjects
	)
}

class FakeQuarterRepository : QuarterRepository {
	var quartersFlow: Flow<List<Quarter>> = flowOf(emptyList())
	var quarters: List<Quarter> = emptyList()
	var removeQuarterThrowable: Throwable? = null
	var setSubjectGradeThrowable: Throwable? = null

	var getQuartersFlowCalls: Int = 0
	val removeQuarterCalls = mutableListOf<QuarterRemove>()
	val setSubjectGradeCalls = mutableListOf<SubjectGradeSet>()

	override suspend fun getQuartersFlow(): Flow<List<Quarter>> {
		getQuartersFlowCalls++
		return quartersFlow
	}

	override suspend fun getQuarters(): List<Quarter> {
		return quarters
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		removeQuarterCalls += remove
		removeQuarterThrowable?.let { throw it }
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		setSubjectGradeCalls += set
		setSubjectGradeThrowable?.let { throw it }
	}
}

class FakeReportingRepository : ReportingRepository {
	var currentIdentifier: String = ""
	val messages = mutableListOf<String>()
	val exceptions = mutableListOf<Throwable>()
	val customKeys = mutableMapOf<String, Any>()

	override fun setIdentifier(identifier: String) {
		this.currentIdentifier = identifier
	}

	override fun logException(throwable: Throwable) {
		exceptions += throwable
	}

	override fun logMessage(message: String) {
		messages += message
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		customKeys[key] = value
	}
}

class FakeNetworkRepository(
	var available: Boolean = true
) : NetworkRepository {
	override fun isAvailable(): Boolean = available
}

data class SetSubjectGradeAndRecomputeCall(
	val qid: String,
	val sid: String,
	val grade: Int,
	val commit: Boolean
)

class FakeLocalDataSource(
	initialQuarters: List<LocalQuarter> = emptyList(),
	private val eventsSink: MutableList<String>? = null
) : LocalDataSource {
	val quartersFlow = MutableStateFlow(initialQuarters)
	val quartersById = initialQuarters.associateBy { it.id }.toMutableMap()

	val removedQuarterIds = mutableListOf<String>()
	val savedQuartersCalls = mutableListOf<List<LocalQuarter>>()
	val savedSubjectsCalls = mutableListOf<List<LocalSubject>>()
	val setSubjectGradeAndRecomputeCalls = mutableListOf<SetSubjectGradeAndRecomputeCall>()
	val events = mutableListOf<String>()

	var setSubjectGradeAndRecomputeResult = SetSubjectGradeResult(
		updatedQuarters = emptyList(),
		updatedTargetQuarter = null
	)

	override fun getQuartersFlow(): Flow<List<LocalQuarter>> = quartersFlow

	override suspend fun getQuarter(qid: String): LocalQuarter? {
		return quartersById[qid]
	}

	override suspend fun removeQuarter(qid: String) {
		events += "local.remove:$qid"
		eventsSink?.add("local.remove:$qid")
		removedQuarterIds += qid
		quartersById.remove(qid)
	}

	override suspend fun saveQuarters(quarters: List<LocalQuarter>) {
		events += "local.save"
		eventsSink?.add("local.save")
		savedQuartersCalls += quarters
		quartersById.clear()
		quartersById.putAll(quarters.associateBy { it.id })
		quartersFlow.value = quarters
	}

	override suspend fun saveSubjects(subjects: List<LocalSubject>) {
		savedSubjectsCalls += subjects
	}

	override suspend fun setSubjectGradeAndRecompute(
		qid: String,
		sid: String,
		grade: Int,
		commit: Boolean
	): SetSubjectGradeResult {
		setSubjectGradeAndRecomputeCalls += SetSubjectGradeAndRecomputeCall(qid, sid, grade, commit)
		return setSubjectGradeAndRecomputeResult
	}
}

class FakeRemoteDataSource(
	private val eventsSink: MutableList<String>? = null
) : RemoteDataSource {
	var quarters: List<RemoteQuarter> = emptyList()
	var quarter: RemoteQuarter = RecordFixtures.remoteQuarter()
	var addQuarterResult: List<RemoteQuarter> = emptyList()

	var getQuartersCalls = 0
	val getQuarterCalls = mutableListOf<String>()
	val removedQuarterIds = mutableListOf<String>()
	val addQuarterCalls = mutableListOf<RemoteQuarter>()
	val events = mutableListOf<String>()

	override suspend fun getQuarters(): List<RemoteQuarter> {
		getQuartersCalls++
		events += "remote.get"
		eventsSink?.add("remote.get")
		return quarters
	}

	override suspend fun getQuarter(qid: String): RemoteQuarter {
		getQuarterCalls += qid
		return quarter
	}

	override suspend fun removeQuarter(qid: String) {
		events += "remote.remove:$qid"
		eventsSink?.add("remote.remove:$qid")
		removedQuarterIds += qid
	}

	override suspend fun addQuarter(quarter: RemoteQuarter): List<RemoteQuarter> {
		addQuarterCalls += quarter
		events += "remote.add:${quarter.id}"
		eventsSink?.add("remote.add:${quarter.id}")
		return addQuarterResult
	}
}

class FakeSettingsDataSource(
	var isOnCooldown: Boolean = false
) : SettingsDataSource {
	var isGetCooldownCalls: Int = 0
	var setCooldownCalls: Int = 0

	override suspend fun isGetQuartersOnCooldown(): Boolean {
		isGetCooldownCalls++
		return isOnCooldown
	}

	override suspend fun setGetQuartersOnCooldown() {
		setCooldownCalls++
	}
}