package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AcceptAssistantConsentUseCase(
	private val consentRepository: AssistantConsentRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Boolean, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Boolean): Flow<Unit> = flow {
		consentRepository.setAssistantConsentGranted(granted = params)

		emit(Unit)
	}
}
