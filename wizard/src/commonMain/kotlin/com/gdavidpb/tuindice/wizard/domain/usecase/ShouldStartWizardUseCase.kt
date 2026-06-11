package com.gdavidpb.tuindice.wizard.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ShouldStartWizardUseCase(
	private val settingsRepository: SettingsRepository,
	private val sessionRepository: SessionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Boolean, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<Boolean> {
		return flowOf(
			sessionRepository.hasActiveSession() &&
					!settingsRepository.isWizardCompleted()
		)
	}
}
