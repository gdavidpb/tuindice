package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.presentation.navigation.Destination2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetLastDestinationUseCase(
	private val settingsRepository: SettingsRepository
) : FlowUseCase<Destination2, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Destination2): Flow<Unit> {
		settingsRepository.setLastDestination(destination = params)

		return flowOf(Unit)
	}
}