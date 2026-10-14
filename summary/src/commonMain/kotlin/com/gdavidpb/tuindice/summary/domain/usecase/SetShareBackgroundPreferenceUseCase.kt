package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Keeps the background the student chose last, so that the next card opens with it. */
class SetShareBackgroundPreferenceUseCase(
	private val shareBackgroundRepository: ShareBackgroundRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		shareBackgroundRepository.setPreferredBackgroundId(params)

		return flowOf(Unit)
	}
}
