package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoveEvaluationUseCase(
	private val evaluationRepository: EvaluationRepository
) : FlowUseCase<String, Unit, EvaluationsUseCaseError>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val remove = EvaluationRemove(id = params)

		evaluationRepository.removeEvaluation(remove = remove)

		return flowOf(Unit)
	}
}