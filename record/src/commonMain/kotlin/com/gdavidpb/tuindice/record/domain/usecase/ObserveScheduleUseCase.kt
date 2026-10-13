package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.currentTermOrNull
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.classesStartLocalDate
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ScheduleClockRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import com.gdavidpb.tuindice.record.domain.mapper.evaluationMarksOn
import com.gdavidpb.tuindice.record.domain.model.ObservedSchedule
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.projectionFor
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.ScheduleSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

/**
 * The current term of the record with the last view the student chose for its schedule and the
 * moment it is being looked at, which arrives again every minute so the schedule keeps up. With
 * them, the week of the term it is and which meetings have an evaluation this calendar week. The
 * evaluations are only read, as the Evaluations tab lists them: this never asks for them to be
 * downloaded, and a failure reading them leaves the schedule without marks instead of without
 * schedule.
 */
class ObserveScheduleUseCase(
	private val academicRecordRepository: AcademicRecordRepository,
	private val scheduleSelectionRepository: ScheduleSelectionRepository,
	private val scheduleClockRepository: ScheduleClockRepository,
	private val visibleEvaluationsRepository: VisibleEvaluationsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, ObservedSchedule, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<ObservedSchedule> {
		return combine(
			academicRecordRepository.observeAcademicRecordSnapshotFlow(),
			scheduleSelectionRepository.observeScheduleViewMode(),
			scheduleClockRepository.observeNow(),
			observeEvaluations()
		) { recordSnapshot, viewMode, now, evaluations ->
			// The projection that keeps the current term; the schedule does not depend on overrides.
			val currentTerm = recordSnapshot.value
				.projectionFor(RecordViewMode.Projection)
				.terms
				.currentTermOrNull(
					kind = TermProjection::kind,
					termOrder = TermProjection::termOrder,
					id = TermProjection::id
				)

			ObservedSchedule(
				currentTerm = currentTerm,
				viewMode = viewMode,
				hasSyncedRecord = recordSnapshot.hasSynced,
				now = now,
				academicWeek = currentTerm?.let { term ->
					AcademicWeekEngine.weekOrNull(
						weekOneStart = AcademicWeekEngine.weekOneStart(
							periodYear = term.periodYear,
							periodCode = term.periodCode,
							classesStartDate = term.classesStartLocalDate()
						),
						date = now.date
					)
				},
				// Nothing downloaded yet is an empty list: no evaluation, no mark.
				evaluationMarks = currentTerm?.attempts.orEmpty().evaluationMarksOn(
					evaluations = evaluations.value,
					today = now.date
				)
			)
		}
	}

	// A failure reading the evaluations is reported and read as having none: the schedule is not
	// theirs to take down.
	private fun observeEvaluations(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> {
		return visibleEvaluationsRepository.observeVisibleEvaluationsFlow().catch { throwable ->
			reportHandledException(throwable)
			emit(ObservedSyncedSnapshot(value = emptyList(), hasSynced = false))
		}
	}
}
