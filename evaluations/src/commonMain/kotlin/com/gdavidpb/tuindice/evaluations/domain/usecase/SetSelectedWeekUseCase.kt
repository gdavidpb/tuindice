package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetSelectedWeekUseCase(
	private val evaluationsSelectionRepository: EvaluationsSelectionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		evaluationsSelectionRepository.setSelectedWeekKey(weekKey = params)

		return flowOf(Unit)
	}
}
