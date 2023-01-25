package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.evaluations.domain.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.exception.EvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.validator.UpdateEvaluationParamsValidator

class UpdateEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository,
	override val paramsValidator: UpdateEvaluationParamsValidator
) : ResultUseCase<UpdateEvaluationParams, Evaluation, EvaluationError>() {
	override suspend fun executeOnBackground(params: UpdateEvaluationParams): Evaluation {
		val activeUId = authRepository.getActiveAuth().uid

		return evaluationRepository.updateEvaluation(
			uid = activeUId,
			update = params.toEvaluationUpdate()
		)
	}

	override suspend fun executeOnException(throwable: Throwable): EvaluationError? {
		return when (throwable) {
			is EvaluationIllegalArgumentException -> throwable.error
			else -> super.executeOnException(throwable)
		}
	}
}