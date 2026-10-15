package com.gdavidpb.tuindice.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Control negativo: la lectura puntual del aviso al llegar la señal es el diseño documentado del caso de uso, y la
// exención por archivo de observe-usecase-local-only lo deja fuera.
class ObserveSessionInvalidationUseCase(
	private val sessionInvalidationRepository: SessionInvalidationRepository,
	private val settingsRepository: SettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Any?, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Any?> {
		return sessionInvalidationRepository.observeSessionInvalidation().map {
			settingsRepository.getSessionResetNotice()
		}
	}
}
