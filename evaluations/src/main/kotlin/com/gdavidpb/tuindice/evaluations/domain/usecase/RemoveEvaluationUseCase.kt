package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.CompletableUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository

class RemoveEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository
) : CompletableUseCase<String, Nothing>() {
	override suspend fun executeOnBackground(params: String) {
		val activeUId = authRepository.getActiveAuth().uid

		evaluationRepository.removeEvaluation(uid = activeUId, eid = params)
	}
}