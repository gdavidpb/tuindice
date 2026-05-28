package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class OpenExternalUrlUseCase(
	private val browserRepository: BrowserRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		browserRepository.open(params)

		return flowOf(Unit)
	}
}
