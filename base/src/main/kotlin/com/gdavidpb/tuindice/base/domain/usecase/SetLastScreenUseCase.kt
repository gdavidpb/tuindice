package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetLastScreenUseCase(
	private val settingsRepository: SettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Int, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Int): Flow<Unit> {
		settingsRepository.setLastScreen(screen = params)

		return flowOf(Unit)
	}
}