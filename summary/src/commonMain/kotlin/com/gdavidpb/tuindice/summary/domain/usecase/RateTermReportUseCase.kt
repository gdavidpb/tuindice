package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.TermReportFeedbackRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.RateTermReportUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RateTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.param.RateTermReportParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// The thumbs of a report. The vote is kept on the device once the service has confirmed it. A refusal of the service
// fails the use case, and the screen takes the thumb back.
class RateTermReportUseCase(
	private val insightsRepository: AssistantInsightsRepository,
	private val feedbackRepository: TermReportFeedbackRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RateTermReportExceptionHandler
) : FlowUseCase<RateTermReportParams, Unit, RateTermReportUseCaseError>() {
	override suspend fun executeOnBackground(params: RateTermReportParams): Flow<Unit> = flow {
		insightsRepository.rateInsight(insightId = params.insightId, useful = params.useful)

		feedbackRepository.saveTermReportFeedback(insightId = params.insightId, useful = params.useful)

		emit(Unit)
	}
}
