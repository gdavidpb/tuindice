package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.QuarterSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetSelectedQuarterIdUseCase(
	private val quarterSelectionRepository: QuarterSelectionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		quarterSelectionRepository.setSelectedQuarterId(params)

		return flowOf(Unit)
	}
}
