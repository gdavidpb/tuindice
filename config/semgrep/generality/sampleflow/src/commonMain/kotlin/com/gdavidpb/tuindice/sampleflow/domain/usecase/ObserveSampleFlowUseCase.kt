package com.gdavidpb.tuindice.sampleflow.domain.usecase

import kotlinx.coroutines.flow.Flow

// Disparo de observe-usecase-local-only: un Observe*UseCase refresca del servidor y lee un valor de una vez.
class ObserveSampleFlowUseCase(
	private val sampleFlowRepository: SampleFlowRepository,
	private val settingsRepository: SampleFlowSettingsRepository
) : FlowUseCase<Unit, String, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		sampleFlowRepository.refreshSampleFlow()
		val notice = settingsRepository.getSampleFlowNotice()
		return sampleFlowRepository.observeSampleFlow()
	}
}
