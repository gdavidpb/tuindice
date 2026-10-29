package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.RemoveEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.RemoveEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoveEvaluationUseCase(
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RemoveEvaluationExceptionHandler
) : FlowUseCase<RemoveEvaluationParams, Unit, RemoveEvaluationUseCaseError>() {
	override suspend fun executeOnBackground(params: RemoveEvaluationParams): Flow<Unit> {
		val remove = EvaluationRemove(id = params.evaluationId, mutationId = params.mutationId)

		evaluationRepository.removeEvaluation(remove = remove)

		return flowOf(Unit)
	}
}
