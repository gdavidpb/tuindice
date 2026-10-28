package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
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
//
// The write runs in the session rather than in the caller's scope, as the consent of the assistant does. A thumb the
// screen has moved away from is still sent, and a vote the service takes is kept on the device, so both say the same.
// Leaving the report does not stop anything either: the machine keeps listening to a thumb until its write ends. The
// flow only waits for the write and reports its outcome, so the refusal of a thumb the screen has moved away from is
// reported as well (see FlowUseCase), and it changes nothing on screen.
//
// The writes of one report queue up in the order of the thumbs (see TermReportVoteQueue): the last thumb is the last
// one the service receives.
class RateTermReportUseCase(
	private val insightsRepository: AssistantInsightsRepository,
	private val feedbackRepository: TermReportFeedbackRepository,
	private val sessionCoroutineScope: SessionCoroutineScope,
	private val termReportVoteQueue: TermReportVoteQueue,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RateTermReportExceptionHandler
) : FlowUseCase<RateTermReportParams, Unit, RateTermReportUseCaseError>() {
	override suspend fun executeOnBackground(params: RateTermReportParams): Flow<Unit> {
		val write = termReportVoteQueue.enqueue(scope = sessionCoroutineScope, insightId = params.insightId) {
			insightsRepository.rateInsight(insightId = params.insightId, useful = params.useful)

			feedbackRepository.saveTermReportFeedback(insightId = params.insightId, useful = params.useful)
		}

		return flow {
			write.await()

			emit(Unit)
		}
	}
}
