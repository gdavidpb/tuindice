package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationsExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateEvaluationsUseCase(
	private val evaluationRepository: EvaluationRepository,
	override val exceptionHandler: UpdateEvaluationsExceptionHandler
) : FlowUseCase<Unit, Unit, UpdateEvaluationsUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		evaluationRepository.updateEvaluations()
		return flowOf(Unit)
	}
}
