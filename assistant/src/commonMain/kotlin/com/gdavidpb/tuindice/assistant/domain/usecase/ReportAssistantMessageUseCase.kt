package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantFeedbackRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ReportAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.ReportAssistantMessageExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ReportAssistantMessageParams
import com.gdavidpb.tuindice.assistant.domain.usecase.validator.ReportAssistantMessageParamsValidator
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.feedback.MessageReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReportAssistantMessageUseCase(
	private val feedbackRepository: AssistantFeedbackRepository,
	override val reportingRepository: ReportingRepository,
	override val paramsValidator: ReportAssistantMessageParamsValidator,
	override val exceptionHandler: ReportAssistantMessageExceptionHandler
) : FlowUseCase<ReportAssistantMessageParams, Unit, ReportAssistantMessageError>() {
	override suspend fun executeOnBackground(params: ReportAssistantMessageParams): Flow<Unit> = flow {
		feedbackRepository.report(
			MessageReport(
				messageId = params.messageId,
				reason = params.reason,
				comment = params.comment?.trim()?.takeIf { it.isNotEmpty() }
			)
		)

		emit(Unit)
	}
}
