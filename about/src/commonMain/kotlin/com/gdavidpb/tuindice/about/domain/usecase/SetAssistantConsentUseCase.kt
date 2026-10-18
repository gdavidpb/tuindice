package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Sets the consent the device keeps: the student's choice, which the server decides on, or the one the server already
// holds, for a device that has not heard it yet.
class SetAssistantConsentUseCase(
	private val assistantConsentRepository: AssistantConsentRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<AssistantConsentSetting, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: AssistantConsentSetting): Flow<Unit> {
		when (params) {
			is AssistantConsentSetting.Chosen ->
				assistantConsentRepository.setAssistantConsentGranted(granted = params.granted)

			AssistantConsentSetting.FromServer -> assistantConsentRepository.refreshAssistantConsent()
		}

		return flowOf(Unit)
	}
}

sealed interface AssistantConsentSetting {
	data class Chosen(val granted: Boolean) : AssistantConsentSetting

	data object FromServer : AssistantConsentSetting
}
