package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.mapper.termReportEntryOf
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportEntry
import com.gdavidpb.tuindice.summary.domain.repository.TermReportSeenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * The line of the summary for the newest term report the student has not opened. It follows the reports the service
 * has, as the update of them left them, and the ids opened on this device: opening a report takes its line away.
 */
class ObserveTermReportEntryUseCase(
	private val insightsRepository: AssistantInsightsRepository,
	private val seenRepository: TermReportSeenRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, SummaryTermReportEntry?, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<SummaryTermReportEntry?> {
		return combine(
			insightsRepository.observeInsightsFlow(),
			seenRepository.observeSeenTermReportIds()
		) { insights, seenIds ->
			termReportEntryOf(insights = insights, seenIds = seenIds)
		}.distinctUntilChanged()
	}
}
