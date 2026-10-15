package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Asks the server for the share backgrounds and saves them, so that the share sheet observes the new catalog. */
class UpdateShareBackgroundsUseCase(
	private val shareBackgroundRepository: ShareBackgroundRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		shareBackgroundRepository.refreshShareBackgrounds()

		return flowOf(Unit)
	}
}
