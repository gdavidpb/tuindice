package com.gdavidpb.tuindice.wizard.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CompleteWizardUseCase(
	private val settingsRepository: SettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		settingsRepository.setWizardCompleted()

		return flowOf(Unit)
	}
}
