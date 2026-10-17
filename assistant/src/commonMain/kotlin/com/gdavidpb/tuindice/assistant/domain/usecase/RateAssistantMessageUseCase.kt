package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantFeedbackRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.param.RateAssistantMessageParams
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.feedback.MessageFeedback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RateAssistantMessageUseCase(
	private val feedbackRepository: AssistantFeedbackRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<RateAssistantMessageParams, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: RateAssistantMessageParams): Flow<Unit> = flow {
		feedbackRepository.rate(MessageFeedback(messageId = params.messageId, useful = params.useful))

		emit(Unit)
	}
}
