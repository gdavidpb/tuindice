package com.gdavidpb.tuindice.wizard.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MarkCoachmarkSeenUseCase(
	private val settingsRepository: SettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<MarkCoachmarkSeenUseCase.Params, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Params): Flow<Unit> {
		if (params.coachmarkId in params.allCoachmarkIds) {
			settingsRepository.markCoachmarkSeen(params.coachmarkId)
		}
		return flowOf(Unit)
	}

	data class Params(
		val coachmarkId: String,
		val allCoachmarkIds: Set<String>
	)
}
