package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Adopts the consent the server already holds, for a device that has not heard it yet.
class RefreshAssistantConsentUseCase(
	private val assistantConsentRepository: AssistantConsentRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		assistantConsentRepository.refreshAssistantConsent()

		return flowOf(Unit)
	}
}
