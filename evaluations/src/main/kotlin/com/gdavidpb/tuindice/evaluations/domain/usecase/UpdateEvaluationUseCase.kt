package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.UpdateEvaluationParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository,
	override val paramsValidator: UpdateEvaluationParamsValidator,
	override val exceptionHandler: UpdateEvaluationExceptionHandler
) : FlowUseCase<UpdateEvaluationParams, Evaluation, AddEvaluationError>() {
	override suspend fun executeOnBackground(params: UpdateEvaluationParams): Flow<Evaluation> {
		val activeUId = authRepository.getActiveAuth().uid

		val update = params.toEvaluationUpdate()

		val evaluation = evaluationRepository.updateEvaluation(
			uid = activeUId,
			update = update
		)

		return flowOf(evaluation)
	}
}