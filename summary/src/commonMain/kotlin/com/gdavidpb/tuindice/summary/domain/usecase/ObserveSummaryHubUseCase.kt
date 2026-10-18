package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ScheduleClockRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.persistence.domain.repository.PensumProgressRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import com.gdavidpb.tuindice.summary.domain.mapper.buildSummaryHub
import com.gdavidpb.tuindice.summary.domain.mapper.recordReadingOf
import com.gdavidpb.tuindice.summary.domain.mapper.summaryRecordStateOf
import com.gdavidpb.tuindice.summary.domain.mapper.termReportEntryOf
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordReading
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportEntry
import com.gdavidpb.tuindice.summary.domain.repository.TermReportSeenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * What the hub of Summary says beyond the profile: the visible record, the evaluations as the
 * Evaluations tab lists them, the pensum progress, whether the record may be read yet, the moment
 * it is being looked at, which arrives again every minute, and the newest term report the student has not opened. It
 * only observes: it never asks for a download or a revalidation. A source that fails is reported and read as having
 * nothing (no record, no evaluations downloaded, no pensum), so the hub says less instead of nothing. A new student the
 * university has no record for yet is read as the first term to come, not as a failure. The record is projected once for
 * each emission of it, and a minute that passes does not project it again.
 */
class ObserveSummaryHubUseCase(
	private val visibleAcademicRecordRepository: VisibleAcademicRecordRepository,
	private val visibleEvaluationsRepository: VisibleEvaluationsRepository,
	private val pensumProgressRepository: PensumProgressRepository,
	private val recordDataPrerequisiteRepository: RecordDataPrerequisiteRepository,
	private val scheduleClockRepository: ScheduleClockRepository,
	private val assistantInsightsRepository: AssistantInsightsRepository,
	private val termReportSeenRepository: TermReportSeenRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, SummaryHub, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<SummaryHub> {
		return combine(
			observeRecordReading(),
			observeEvaluations(),
			observePensumProgress(),
			scheduleClockRepository.observeNow(),
			observeTermReportEntry()
		) { reading, evaluations, pensumProgress, now, termReport ->
			buildSummaryHub(
				reading = reading,
				evaluations = evaluations,
				now = now,
				pensumProgress = pensumProgress,
				termReport = termReport
			)
		}
	}

	// The record is read only when it emits something new. Its projection and the statistics of its closed terms do not
	// depend on the clock, so they are kept out of the combination above: a minute that passes reads only the day.
	private fun observeRecordReading(): Flow<SummaryRecordReading> {
		return combine(observeRecord(), observeRecordPrerequisite()) { record, prerequisite ->
			summaryRecordStateOf(record = record, prerequisite = prerequisite)
		}
			.distinctUntilChanged()
			.map { recordState -> recordReadingOf(recordState) }
	}

	// The newest report the student has not opened. It follows the reports the service has and the ids opened on this device,
	// so a report that arrives or is opened changes the hub at once, whatever the clock does.
	private fun observeTermReportEntry(): Flow<SummaryTermReportEntry?> {
		return combine(
			assistantInsightsRepository.observeInsightsFlow(),
			termReportSeenRepository.observeSeenTermReportIds()
		) { insights, seenIds ->
			termReportEntryOf(insights = insights, seenIds = seenIds)
		}.distinctUntilChanged()
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
