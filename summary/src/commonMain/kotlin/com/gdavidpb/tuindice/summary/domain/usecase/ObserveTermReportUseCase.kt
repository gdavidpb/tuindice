package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.domain.mapper.recordReadingOf
import com.gdavidpb.tuindice.summary.domain.mapper.summaryRecordStateOf
import com.gdavidpb.tuindice.summary.domain.mapper.termReportOf
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordReading
import com.gdavidpb.tuindice.summary.domain.model.TermReportObservation
import com.gdavidpb.tuindice.summary.domain.repository.TermReportFeedbackRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveTermReportUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateTermReportsUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ObserveTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.param.ObserveTermReportParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The term report of one insight, read for the screen: the report from the service, with the figures of its term read
 * from the local record through the academic engines, and the thumb the student's vote was confirmed with. The reports
 * are the ones the update of them left ([UpdateTermReportsUseCase]); [ObserveTermReportParams.isRefreshed] says whether
 * that update reached the service.
 *
 * The causes that keep a report from being read are values, not failures: the observation says it is unavailable, and
 * it stays alive, so the report arrives as soon as the record or the service can give it.
 */
class ObserveTermReportUseCase(
	private val insightsRepository: AssistantInsightsRepository,
	private val visibleAcademicRecordRepository: VisibleAcademicRecordRepository,
	private val recordDataPrerequisiteRepository: RecordDataPrerequisiteRepository,
	private val feedbackRepository: TermReportFeedbackRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: ObserveTermReportExceptionHandler
) : FlowUseCase<ObserveTermReportParams, TermReportObservation, ObserveTermReportUseCaseError>() {
	override suspend fun executeOnBackground(params: ObserveTermReportParams): Flow<TermReportObservation> {
		return combine(
			insightsRepository.observeInsightsFlow(),
			observeRecordReading(),
			feedbackRepository.observeTermReportFeedbacks()
		) { insights, reading, feedbacks ->
			val insight = insights.firstOrNull { candidate -> candidate.id == params.insightId }

			// A report that is not in the list, when the list could not be asked for, may just not be read yet.
			when {
				insight == null && !params.isRefreshed ->
					TermReportObservation.Unavailable(unavailableCauseOf(params.refreshError))

				reading is SummaryRecordReading.NotReady ->
					TermReportObservation.Unavailable(ObserveTermReportUseCaseError.RecordNotReady)

				reading is SummaryRecordReading.Ready && insight != null -> {
					val report = termReportOf(
						insight = insight,
						projection = reading.projection,
						closed = reading.closed,
						feedback = feedbacks[insight.id]
					)

					report?.let { TermReportObservation.Found(report = it) } ?: TermReportObservation.Missing
				}

				else -> TermReportObservation.Missing
			}
		}
	}

	// A refused session is told apart from a lost connection: anything else is the connection.
	private fun unavailableCauseOf(refreshError: UpdateTermReportsUseCaseError?): ObserveTermReportUseCaseError {
		return when (refreshError) {
			UpdateTermReportsUseCaseError.SessionEnded -> ObserveTermReportUseCaseError.SessionEnded
			UpdateTermReportsUseCaseError.NoConnection, null -> ObserveTermReportUseCaseError.NoConnection
		}
	}

	private fun observeRecordReading(): Flow<SummaryRecordReading> {
		return combine(
			visibleAcademicRecordRepository.observeVisibleAcademicRecordFlow(),
			recordDataPrerequisiteRepository.observeRecordDataPrerequisiteFlow()
		) { record, prerequisite ->
			summaryRecordStateOf(record = record, prerequisite = prerequisite)
		}
			.distinctUntilChanged()
			.map { recordState -> recordReadingOf(recordState) }
	}
}
