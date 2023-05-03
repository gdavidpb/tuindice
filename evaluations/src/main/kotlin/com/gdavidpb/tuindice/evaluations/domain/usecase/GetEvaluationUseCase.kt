package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import kotlinx.coroutines.flow.Flow

class GetEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository
) : FlowUseCase<GetEvaluationParams, Evaluation, Nothing>() {
	override suspend fun executeOnBackground(params: GetEvaluationParams): Flow<Evaluation> {
		val activeUId = authRepository.getActiveAuth().uid

		return evaluationRepository.getEvaluation(uid = activeUId, eid = params.evaluationId)
	}
}