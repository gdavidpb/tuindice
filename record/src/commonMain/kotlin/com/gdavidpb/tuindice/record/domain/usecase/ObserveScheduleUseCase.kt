package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.model.ObservedSchedule
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.projectionFor
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.ScheduleSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** The current term of the record with the last view the student chose for its schedule. */
class ObserveScheduleUseCase(
	private val academicRecordRepository: AcademicRecordRepository,
	private val scheduleSelectionRepository: ScheduleSelectionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, ObservedSchedule, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<ObservedSchedule> {
		return combine(
			academicRecordRepository.observeAcademicRecordSnapshotFlow(),
			scheduleSelectionRepository.observeScheduleViewMode()
		) { recordSnapshot, viewMode ->
			ObservedSchedule(
				// The projection that keeps the current term; the schedule does not depend on overrides.
				currentTerm = recordSnapshot.value
					.projectionFor(RecordViewMode.Projection)
					.terms
					.firstOrNull { term -> term.kind.isCurrent },
				viewMode = viewMode,
				hasSyncedRecord = recordSnapshot.hasSynced
			)
		}
	}
}
