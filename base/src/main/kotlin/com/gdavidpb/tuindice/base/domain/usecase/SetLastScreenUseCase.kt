package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetLastScreenUseCase(
	private val settingsRepository: SettingsRepository
) : FlowUseCase<Int, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Int): Flow<Unit> {
		settingsRepository.setLastScreen(screen = params)

		return flowOf(Unit)
	}
}