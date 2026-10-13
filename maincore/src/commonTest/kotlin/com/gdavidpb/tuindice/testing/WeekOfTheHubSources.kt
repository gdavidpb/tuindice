package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/** The Thursday the hub of [createWeekOfTheHubUseCase] is told it is, in week 3 of its term. */
val HubHostToday = LocalDate(2026, 10, 15)

/** The Monday of the week of [HubHostToday], which the evaluation of the hub falls in. */
val HubHostWeekStart = LocalDate(2026, 10, 12)

/**
 * The hub of Summary with a week to show: a class in progress, an evaluation on the day after, a term
 * in its third week and a pensum with progress, so that every line of the hub is on the screen.
 */
fun createWeekOfTheHubUseCase() = ObserveSummaryHubUseCase(
	visibleAcademicRecordRepository = object : VisibleAcademicRecordRepository {
		override fun observeVisibleAcademicRecordFlow(): Flow<AcademicRecord?> = flowOf(
			AcademicRecord(
				id = "record",
				terms = listOf(
					AcademicTerm(
						id = "term",
						periodYear = 2026,
						periodCode = AcademicTermPeriod.SEP_DEC,
						kind = TermKind.CURRENT,
						attempts = listOf(
							AcademicAttempt(
								id = "attempt-CI5437",
								subjectCode = "CI5437",
								subjectName = "Materia CI5437",
								credits = 3,
								schedule = listOf(
									AcademicScheduleEntry(
										dayOfWeek = 5,
										startBlock = 3,
										endBlock = 4,
										classroom = "MYS-210"
									)
								)
							)
						),
						classesStartDate = "2026-09-28"
					)
				)
			)
		)
	},
	visibleEvaluationsRepository = object : VisibleEvaluationsRepository {
		override fun observeVisibleEvaluationsFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> = flowOf(
			ObservedSyncedSnapshot(
				value = listOf(
					Evaluation(
						id = "friday",
						attemptId = "attempt-CI5437",
						subjectCode = "CI5437",
						termId = "term",
						scheduleMode = EvaluationScheduleMode.DATED,
						grade = null,
						maxGrade = 20.0,
						date = LocalDate(2026, 10, 16)
							.atStartOfDayIn(TimeZone.currentSystemDefault())
							.toEpochMilliseconds(),
						type = EvaluationType.TEST,
						state = EvaluationState.PENDING
					)
				),
				hasSynced = true
			)
		)
	},
	pensumProgressRepository = object : PensumProgressRepository {
		override fun observePensumProgressFlow(): Flow<PensumCreditsProgress?> =
			flowOf(PensumCreditsProgress(approvedCredits = 156, totalCredits = 240))
	},
	recordDataPrerequisiteRepository = object : RecordDataPrerequisiteRepository {
		override fun observeRecordDataPrerequisiteFlow(): Flow<RecordDataPrerequisiteState> =
			flowOf(RecordDataPrerequisiteState(isReady = true, hasFailed = false))

		override suspend fun isRecordDataReady(): Boolean = true
	},
	scheduleClockRepository = object : ScheduleClockRepository {
		override fun observeNow(): Flow<ScheduleNow> =
			flowOf(ScheduleNow(dayOfWeek = 5, minuteOfDay = 10 * 60, date = HubHostToday))
	},
	reportingRepository = RecordingReportingRepository()
)
