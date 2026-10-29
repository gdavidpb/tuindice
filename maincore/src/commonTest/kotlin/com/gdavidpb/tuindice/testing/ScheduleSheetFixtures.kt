package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.repository.ScheduleClockRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import com.gdavidpb.tuindice.record.domain.model.RecordRejection
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.ScheduleSelectionRepository
import com.gdavidpb.tuindice.record.domain.usecase.ObserveScheduleUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetScheduleViewModeUseCase
import com.gdavidpb.tuindice.record.presentation.machine.ScheduleMachine
import com.gdavidpb.tuindice.record.presentation.viewmodel.ScheduleViewModel
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/** Monday of week 6 of the Sep - Dec 2026 term that opens on Monday August 31st: the day of the test clock. */
val ScheduleSheetTestMonday = LocalDate(2026, 10, 5)

private const val ATTEMPT_ID = "attempt-CI5311"
private const val NOON_OFFSET_MILLIS = 12 * 60 * 60 * 1000L

// The attempt that meets on Mondays and the evaluation on this Monday that marks its cell.
const val SCHEDULE_SHEET_MARKED_ATTEMPT_ID = ATTEMPT_ID

/**
 * A Schedule sheet over a current term whose one subject meets on Mondays (blocks 1-2), with an
 * evaluation on [ScheduleSheetTestMonday] when [withEvaluation]: what the sheet needs, with no
 * record or evaluations screens behind it.
 */
fun createScheduleViewModel(withEvaluation: Boolean): ScheduleViewModel {
	val selectionRepository = FixedScheduleSelectionRepository()
	val reportingRepository = RecordingReportingRepository()

	return ScheduleViewModel(
		screenMachine = ScheduleMachine(
			observeScheduleUseCase = ObserveScheduleUseCase(
				academicRecordRepository = FixedAcademicRecordRepository(scheduledRecord()),
				scheduleSelectionRepository = selectionRepository,
				scheduleClockRepository = MondayScheduleClockRepository,
				visibleEvaluationsRepository = FixedVisibleEvaluationsRepository(
					if (withEvaluation) listOf(mondayEvaluation()) else emptyList()
				),
				reportingRepository = reportingRepository
			),
			setScheduleViewModeUseCase = SetScheduleViewModeUseCase(
				scheduleSelectionRepository = selectionRepository,
				reportingRepository = reportingRepository
			)
		),
		eventPublisher = NoOpEventPublisher
	)
}

private fun scheduledRecord() = AcademicRecord(
	id = "record",
	terms = listOf(
		AcademicTerm(
			id = "now",
			periodYear = 2026,
			periodCode = AcademicTermPeriod.SEP_DEC,
			kind = TermKind.CURRENT,
			attempts = listOf(
				AcademicAttempt(
					id = ATTEMPT_ID,
					subjectCode = "CI5311",
					subjectName = "CI5311",
					credits = 4,
					gradingMode = AttemptGradingMode.NUMERIC,
					academicScore = AttemptScore.empty(),
					academicOutcome = AttemptOutcome.APPROVED,
					section = 1,
					schedule = listOf(
						AcademicScheduleEntry(dayOfWeek = 2, startBlock = 1, endBlock = 2, classroom = "MYS-116")
					)
				)
			)
		)
	)
)

private fun mondayEvaluation() = Evaluation(
	id = "evaluation-monday",
	attemptId = ATTEMPT_ID,
	subjectCode = "",
	termId = "now",
	scheduleMode = EvaluationScheduleMode.DATED,
	grade = null,
	maxGrade = 20.0,
	date = ScheduleSheetTestMonday.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() + NOON_OFFSET_MILLIS,
	type = EvaluationType.TEST,
	state = EvaluationState.PENDING
)

private object MondayScheduleClockRepository : ScheduleClockRepository {
	override fun observeNow(): Flow<ScheduleNow> = flowOf(
		ScheduleNow(dayOfWeek = 2, minuteOfDay = 8 * 60, date = ScheduleSheetTestMonday)
	)
}

private class FixedScheduleSelectionRepository : ScheduleSelectionRepository {
	private val viewMode = MutableStateFlow(ScheduleViewMode.Table)

	override fun observeScheduleViewMode(): Flow<ScheduleViewMode> = viewMode

	override suspend fun setScheduleViewMode(viewMode: ScheduleViewMode) {
		this.viewMode.value = viewMode
	}
}

private class FixedVisibleEvaluationsRepository(
	evaluations: List<Evaluation>
) : VisibleEvaluationsRepository {
	private val snapshot = ObservedSyncedSnapshot(evaluations, true)

	override fun observeVisibleEvaluationsFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> = flowOf(snapshot)
}

private class FixedAcademicRecordRepository(
	private val record: AcademicRecord
) : AcademicRecordRepository {
	override suspend fun observeAcademicRecordSnapshotFlow() = combine(
		observeAcademicRecordFlow(),
		observeHasSyncedRecordFlow()
	) { record, hasSyncedRecord ->
		ObservedSyncedSnapshot(
			value = record,
			hasSynced = hasSyncedRecord
		)
	}

	override suspend fun updateAcademicRecord(forceRemote: Boolean) {
		updateAcademicRecord()
	}

	override suspend fun observeTerminallyRejectedMutationIdsFlow(): Flow<List<String>> = emptyFlow()

	override suspend fun observeTerminallyRejectedMutationsFlow(): Flow<List<RecordRejection>> = emptyFlow()

	override suspend fun acknowledgeTerminallyRejectedMutations(mutationIds: List<String>) = Unit

	override suspend fun getRecordRevision(): Long? = null

	override fun observeMutationOutcomesFlow(): Flow<MutationOutcome> = emptyFlow()

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = flowOf(record)

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = flowOf(true)

	override suspend fun getAcademicRecord(): AcademicRecord = record

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String?
	) = Unit

	override suspend fun deleteAttemptOverride(attemptId: String, mutationId: String?) = Unit

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) = Unit

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) = Unit

	override suspend fun deleteSyntheticTerm(termId: String, mutationId: String?) = Unit
}
