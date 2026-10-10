package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.domain.repository.OutdatedAppEventRepository
import kotlinx.coroutines.flow.Flow

/**
 * The server refusing this version of the app, told as it happens: any request may be the one
 * that learns it, long after the startup check passed. Nothing is replayed, so whoever needs
 * every refusal has to be observing before the request that brings it.
 */
class ObserveOutdatedAppUseCase(
	private val outdatedAppEventRepository: OutdatedAppEventRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, OutdatedAppState, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<OutdatedAppState> {
		return outdatedAppEventRepository.observeOutdatedApp()
	}
}
