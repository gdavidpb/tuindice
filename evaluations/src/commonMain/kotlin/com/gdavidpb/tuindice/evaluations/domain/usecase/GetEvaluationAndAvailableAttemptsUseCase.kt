package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAndAvailableAttempts
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetEvaluationAndAvailableAttemptsUseCase(
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<GetEvaluationParams, EvaluationAndAvailableAttempts, Nothing>() {
	override suspend fun executeOnBackground(params: GetEvaluationParams): Flow<EvaluationAndAvailableAttempts> {
		val evaluation = evaluationRepository
			.getEvaluation(eid = params.evaluationId)

		val availableAttempts = evaluationRepository
			.getAvailableAttempts()

		val currentTerm = evaluationRepository
			.getCurrentTerm()

		val evaluationAndAvailableAttempts = EvaluationAndAvailableAttempts(
			evaluation = evaluation,
			availableAttempts = availableAttempts,
			currentTerm = currentTerm
		)

		return flowOf(evaluationAndAvailableAttempts)
	}
}
