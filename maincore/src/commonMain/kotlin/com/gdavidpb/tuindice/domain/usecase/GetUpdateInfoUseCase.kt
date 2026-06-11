package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
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
	override val reportingRepository: ReportingRepository,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : FlowUseCase<Unit, UpdateAction, Nothing>(
	reportingRepository = reportingRepository,
	dispatchers = dispatchers
) {
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
