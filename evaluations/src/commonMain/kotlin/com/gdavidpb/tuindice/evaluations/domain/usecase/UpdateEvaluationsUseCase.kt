package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsRefreshResult
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateEvaluationsUseCase(
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, EvaluationsRefreshResult, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<EvaluationsRefreshResult> {
		val hasLocalContent = evaluationRepository.getEvaluationsSnapshot().value.isNotEmpty()
		return flowOf(evaluationRepository.updateEvaluations(forceRemote = !hasLocalContent))
	}
}
