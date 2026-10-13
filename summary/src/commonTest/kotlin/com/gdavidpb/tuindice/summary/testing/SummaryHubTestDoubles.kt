package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ScheduleClockRepository
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.persistence.domain.repository.PensumProgressRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSummaryHubUseCase
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

/** The moment of the hub's mocks: a Thursday ([dayOfWeek] 5) at [minuteOfDay]. */
fun scheduleNow(
	date: LocalDate = HUB_FIXTURE_TODAY,
	dayOfWeek: Int = 5,
	minuteOfDay: Int = 10 * 60
) = ScheduleNow(dayOfWeek = dayOfWeek, minuteOfDay = minuteOfDay, date = date)

class ControllableVisibleAcademicRecordRepository(
	initialRecord: AcademicRecord? = null,
	var failure: Throwable? = null
) : VisibleAcademicRecordRepository {
	val recordFlow = MutableStateFlow(initialRecord)

	override fun observeVisibleAcademicRecordFlow(): Flow<AcademicRecord?> {
		val throwable = failure ?: return recordFlow

		return flow { throw throwable }
	}
}

// The evaluations the Evaluations tab lists, as the test sets them: none and downloaded unless told
// otherwise. [failure] makes reading them fail, the way a broken local database would.
class ControllableVisibleEvaluationsRepository(
	initialEvaluations: List<Evaluation> = emptyList(),
	initialHasSynced: Boolean = true,
	var failure: Throwable? = null
) : VisibleEvaluationsRepository {
	val snapshotFlow = MutableStateFlow(ObservedSyncedSnapshot(initialEvaluations, initialHasSynced))

	override fun observeVisibleEvaluationsFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> {
		val throwable = failure ?: return snapshotFlow

		return flow { throw throwable }
	}
}

class ControllablePensumProgressRepository(
	initialProgress: PensumCreditsProgress? = null,
	var failure: Throwable? = null
) : PensumProgressRepository {
	val progressFlow = MutableStateFlow(initialProgress)

	override fun observePensumProgressFlow(): Flow<PensumCreditsProgress?> {
		val throwable = failure ?: return progressFlow

		return flow { throw throwable }
	}
}

class ControllableRecordDataPrerequisiteRepository(
	isReady: Boolean = true
) : RecordDataPrerequisiteRepository {
	val stateFlow = MutableStateFlow(RecordDataPrerequisiteState(isReady = isReady, hasFailed = false))

	override fun observeRecordDataPrerequisiteFlow(): Flow<RecordDataPrerequisiteState> = stateFlow

	override suspend fun isRecordDataReady(): Boolean = stateFlow.value.isReady
}

class ControllableScheduleClockRepository(
	initialNow: ScheduleNow = scheduleNow()
) : ScheduleClockRepository {
	val nowFlow = MutableStateFlow(initialNow)

	override fun observeNow(): Flow<ScheduleNow> = nowFlow
}

// A clock whose ticker is asleep: every observation reads the moment once and then says nothing,
// like the real one while the process is suspended. What it would say at the next tick is
// [reading], so a test moves it and sees whether the screen read it again.
class SleepingScheduleClockRepository(
	var reading: ScheduleNow = scheduleNow()
) : ScheduleClockRepository {
	var observations = 0
		private set

	override fun observeNow(): Flow<ScheduleNow> = flow {
		observations += 1
		emit(reading)
		awaitCancellation()
	}
}

// The use case of the hub over sources that say nothing: no record, no evaluations and no pensum.
fun createObserveSummaryHubUseCase(
	record: VisibleAcademicRecordRepository = ControllableVisibleAcademicRecordRepository(),
	evaluations: VisibleEvaluationsRepository = ControllableVisibleEvaluationsRepository(),
	pensumProgress: PensumProgressRepository = ControllablePensumProgressRepository(),
	prerequisite: RecordDataPrerequisiteRepository = ControllableRecordDataPrerequisiteRepository(),
	clock: ScheduleClockRepository = ControllableScheduleClockRepository()
) = ObserveSummaryHubUseCase(
	visibleAcademicRecordRepository = record,
	visibleEvaluationsRepository = evaluations,
	pensumProgressRepository = pensumProgress,
	recordDataPrerequisiteRepository = prerequisite,
	scheduleClockRepository = clock,
	reportingRepository = RecordingReportingRepository()
)
