package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ScheduleClockRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.persistence.domain.repository.PensumProgressRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import com.gdavidpb.tuindice.summary.domain.mapper.buildSummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

/**
 * What the hub of Summary says beyond the profile: the visible record, the evaluations as the
 * Evaluations tab lists them, the pensum progress, whether the record may be read yet and the moment
 * it is being looked at, which arrives again every minute. It only observes: it never asks for a
 * download or a revalidation. A source that fails is reported and read as having nothing (no record,
 * no evaluations downloaded, no pensum), so the hub says less instead of nothing. A new student the university has no
 * record for yet is read as the first term to come, not as a failure.
 */
class ObserveSummaryHubUseCase(
	private val visibleAcademicRecordRepository: VisibleAcademicRecordRepository,
	private val visibleEvaluationsRepository: VisibleEvaluationsRepository,
	private val pensumProgressRepository: PensumProgressRepository,
	private val recordDataPrerequisiteRepository: RecordDataPrerequisiteRepository,
	private val scheduleClockRepository: ScheduleClockRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, SummaryHub, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<SummaryHub> {
		return combine(
			observeRecord(),
			observeRecordPrerequisite(),
			observeEvaluations(),
			observePensumProgress(),
			scheduleClockRepository.observeNow()
		) { record, prerequisite, evaluations, pensumProgress, now ->
			buildSummaryHub(
				recordState = recordStateOf(record = record, prerequisite = prerequisite),
				evaluations = evaluations,
				now = now,
				pensumProgress = pensumProgress
			)
		}
	}

	// What the hub reads of the record: a ready one has the week and the term. A new student the university has no record
	// for yet has no term yet, which is not an error; any other state of the record leaves only the career.
	private fun recordStateOf(record: AcademicRecord?, prerequisite: RecordDataPrerequisiteState): SummaryRecordState {
		return when {
			record != null && prerequisite.isReady -> SummaryRecordState.Ready(record)
			prerequisite.isNewStudentWithoutRecord -> SummaryRecordState.NewStudentWithoutRecord
			else -> SummaryRecordState.NotReady
		}
	}

	private fun observeRecord(): Flow<AcademicRecord?> {
		return visibleAcademicRecordRepository.observeVisibleAcademicRecordFlow().catch { throwable ->
			reportHandledException(throwable)
			emit(null)
		}
	}

	private fun observeRecordPrerequisite(): Flow<RecordDataPrerequisiteState> {
		return recordDataPrerequisiteRepository.observeRecordDataPrerequisiteFlow().catch { throwable ->
			reportHandledException(throwable)
			emit(RecordDataPrerequisiteState(isReady = false, hasFailed = true))
		}
	}

	private fun observeEvaluations(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> {
		return visibleEvaluationsRepository.observeVisibleEvaluationsFlow().catch { throwable ->
			reportHandledException(throwable)
			emit(ObservedSyncedSnapshot(value = emptyList(), hasSynced = false))
		}
	}

	private fun observePensumProgress(): Flow<PensumCreditsProgress?> {
		return pensumProgressRepository.observePensumProgressFlow().catch { throwable ->
			reportHandledException(throwable)
			emit(null)
		}
	}
}
