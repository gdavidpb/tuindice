package com.gdavidpb.tuindice.testing

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

// The sources of the hub of Summary saying nothing: no record, no evaluations downloaded, no pensum.
// The host tests that mount Summary care for its profile and its navigation, not for the hub.
fun createEmptyObserveSummaryHubUseCase() = ObserveSummaryHubUseCase(
	visibleAcademicRecordRepository = object : VisibleAcademicRecordRepository {
		override fun observeVisibleAcademicRecordFlow(): Flow<AcademicRecord?> = flowOf(null)
	},
	visibleEvaluationsRepository = object : VisibleEvaluationsRepository {
		override fun observeVisibleEvaluationsFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> =
			flowOf(ObservedSyncedSnapshot(value = emptyList(), hasSynced = false))
	},
	pensumProgressRepository = object : PensumProgressRepository {
		override fun observePensumProgressFlow(): Flow<PensumCreditsProgress?> = flowOf(null)
	},
	recordDataPrerequisiteRepository = object : RecordDataPrerequisiteRepository {
		override fun observeRecordDataPrerequisiteFlow(): Flow<RecordDataPrerequisiteState> =
			flowOf(RecordDataPrerequisiteState(isReady = false, hasFailed = false))
	},
	scheduleClockRepository = object : ScheduleClockRepository {
		override fun observeNow(): Flow<ScheduleNow> =
			flowOf(ScheduleNow(dayOfWeek = 5, minuteOfDay = 10 * 60, date = LocalDate(2026, 10, 15)))
	},
	reportingRepository = RecordingReportingRepository()
)
