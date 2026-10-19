package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Stores the consent the student chose on this device; the server is told through the repository.
class SetAssistantConsentUseCase(
	private val assistantConsentRepository: AssistantConsentRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Boolean, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Boolean): Flow<Unit> {
		assistantConsentRepository.setAssistantConsentGranted(granted = params)

		return flowOf(Unit)
	}
}
