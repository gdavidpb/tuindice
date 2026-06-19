package com.gdavidpb.tuindice.wizard.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.wizard.domain.repository.WizardStartOverrideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ShouldStartWizardUseCase(
	private val settingsRepository: SettingsRepository,
	private val sessionRepository: SessionRepository,
	private val wizardStartOverrideRepository: WizardStartOverrideRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Boolean, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Boolean> {
		val shouldForceWizardStart = wizardStartOverrideRepository.isWizardStartForced()
		val shouldStartWizard =
			sessionRepository.hasActiveSession() && !settingsRepository.isWizardCompleted()

		return flowOf(shouldForceWizardStart || shouldStartWizard)
	}
}
