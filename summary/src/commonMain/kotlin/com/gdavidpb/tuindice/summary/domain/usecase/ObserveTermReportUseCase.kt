package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.attempt
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.domain.mapper.recordReadingOf
import com.gdavidpb.tuindice.summary.domain.mapper.summaryRecordStateOf
import com.gdavidpb.tuindice.summary.domain.mapper.termReportOf
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordReading
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * The term report of one insight, read for the screen: the report from the service, with the figures of its term read from
 * the local record through the academic engines. The service is asked first, so a report written since the summary was
 * read is found. It emits null when there is no such report, or when the record holds no term of it, so the screen says it
 * has no report for that term. It fails when the report is unknown and the service could not be asked, or when the record
 * cannot be read yet: the screen offers to try again.
 */
class ObserveTermReportUseCase(
	private val insightsRepository: AssistantInsightsRepository,
	private val visibleAcademicRecordRepository: VisibleAcademicRecordRepository,
	private val recordDataPrerequisiteRepository: RecordDataPrerequisiteRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, SummaryTermReport?, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<SummaryTermReport?> = flow {
		val isRefreshed = attempt { insightsRepository.refreshInsights() }
			.onFailure { failure -> reportHandledException(failure) }
			.isSuccess

		emitAll(
			combine(insightsRepository.observeInsightsFlow(), observeRecordReading()) { insights, reading ->
				val insight = insights.firstOrNull { candidate -> candidate.id == params }

				// A report that is not in the list, when the list could not be asked for, may just not be read yet.
				if (insight == null && !isRefreshed) throw TermReportUnavailableException()

				when (reading) {
					is SummaryRecordReading.Ready -> insight?.let { found ->
						termReportOf(insight = found, projection = reading.projection, closed = reading.closed)
					}

					SummaryRecordReading.NewStudentWithoutRecord -> null

					SummaryRecordReading.NotReady -> throw TermReportUnavailableException()
				}
			}
		)
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

/** The report cannot be read at the moment: the service could not be asked, or the record is not ready to be read. */
class TermReportUnavailableException : IllegalStateException("The term report cannot be read now.")
