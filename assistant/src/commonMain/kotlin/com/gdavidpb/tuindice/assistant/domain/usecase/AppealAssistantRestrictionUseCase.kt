package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantStandingRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantAppealError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.AssistantAppealExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.validator.AppealAssistantRestrictionParamsValidator
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AppealAssistantRestrictionUseCase(
	private val standingRepository: AssistantStandingRepository,
	override val reportingRepository: ReportingRepository,
	override val paramsValidator: AppealAssistantRestrictionParamsValidator,
	override val exceptionHandler: AssistantAppealExceptionHandler
) : FlowUseCase<String, Unit, AssistantAppealError>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> = flow {
		standingRepository.appeal(AbuseAppeal(text = params.trim()))

		emit(Unit)
	}
}
