package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository

class AddEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository
) : ResultUseCase<Evaluation, Evaluation, Nothing>() {
	override suspend fun executeOnBackground(params: Evaluation): Evaluation {
		val activeUId = authRepository.getActiveAuth().uid

		evaluationRepository.addEvaluation(activeUId, params)

		return params
	}
}