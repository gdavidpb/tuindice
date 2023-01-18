package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.evaluations.domain.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.exception.EvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.mapper.toNewEvaluation
import com.gdavidpb.tuindice.evaluations.domain.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.validator.AddEvaluationParamsValidator

class AddEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository,
	override val paramsValidator: AddEvaluationParamsValidator
) : ResultUseCase<AddEvaluationParams, Evaluation, EvaluationError>() {
	override suspend fun executeOnBackground(params: AddEvaluationParams): Evaluation {
		val activeUId = authRepository.getActiveAuth().uid

		return evaluationRepository.addEvaluation(
			uid = activeUId,
			evaluation = params.toNewEvaluation()
		)
	}

	override suspend fun executeOnException(throwable: Throwable): EvaluationError? {
		return when (throwable) {
			is EvaluationIllegalArgumentException -> throwable.error
			else -> null
		}
	}
}