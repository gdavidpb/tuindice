package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class GetUpdateInfoUseCase(
	private val configRepository: ConfigRepository,
	private val updateGateway: UpdateRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, UpdateAction, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<UpdateAction> {
		val stalenessDays = configRepository.getTimeUpdateStalenessDays()
		val updateAction = updateGateway.checkForUpdate(stalenessDays = stalenessDays)

		return if (updateAction != null) {
			flowOf(updateAction)
		} else {
			emptyFlow()
		}
	}
}
