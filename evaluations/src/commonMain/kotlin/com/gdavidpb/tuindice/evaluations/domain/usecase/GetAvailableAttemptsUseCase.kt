package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAndAvailableAttempts
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetAvailableAttemptsUseCase(
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, EvaluationAndAvailableAttempts, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<EvaluationAndAvailableAttempts> {
		val availableAttempts = evaluationRepository
			.getAvailableAttempts()

		val currentTerm = evaluationRepository
			.getCurrentTerm()

		// The add editor has no evaluation yet; it only needs the attempts and the
		// current term to bound the date picker.
		val evaluationAndAvailableAttempts = EvaluationAndAvailableAttempts(
			evaluation = null,
			availableAttempts = availableAttempts,
			currentTerm = currentTerm
		)

		return flowOf(evaluationAndAvailableAttempts)
	}
}
