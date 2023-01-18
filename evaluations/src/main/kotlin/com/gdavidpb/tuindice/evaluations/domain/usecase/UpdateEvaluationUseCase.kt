package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.error.EvaluationError

class UpdateEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository
) : ResultUseCase<UpdateEvaluationParams, Evaluation, EvaluationError>() {
	override suspend fun executeOnBackground(params: UpdateEvaluationParams): Evaluation {
		TODO()

		/*
		val activeUId = authRepository.getActiveAuth().uid

		return evaluationRepository.updateEvaluation(
			uid = activeUId,
			eid = params.evaluationId,
			evaluation = params.toEvaluationDTO()
		)
		*/
	}

	override suspend fun executeOnException(throwable: Throwable): EvaluationError? {
		return super.executeOnException(throwable)
	}
}