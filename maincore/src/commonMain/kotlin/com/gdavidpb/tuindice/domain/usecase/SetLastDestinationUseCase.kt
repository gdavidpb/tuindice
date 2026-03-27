package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetLastDestinationUseCase(
	private val settingsRepository: SettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Destination, Unit, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Destination): Flow<Unit> {
		settingsRepository.setLastDestination(destination = params)

		return flowOf(Unit)
	}
}
