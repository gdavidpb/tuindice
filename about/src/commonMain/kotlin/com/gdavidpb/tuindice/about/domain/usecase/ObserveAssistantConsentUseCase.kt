package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow

class ObserveAssistantConsentUseCase(
	private val assistantConsentRepository: AssistantConsentRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Boolean, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Boolean> {
		return assistantConsentRepository.assistantConsentGranted
	}
}
