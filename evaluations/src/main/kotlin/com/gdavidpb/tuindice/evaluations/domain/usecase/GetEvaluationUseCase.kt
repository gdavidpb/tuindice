package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository
) : FlowUseCase<String, Evaluation?, EvaluationsError>() {
	override suspend fun executeOnBackground(params: String): Flow<Evaluation?> {
		val activeUId = authRepository.getActiveAuth().uid

		val evaluation = evaluationRepository.getEvaluation(uid = activeUId, eid = params)

		return flowOf(evaluation)
	}
}