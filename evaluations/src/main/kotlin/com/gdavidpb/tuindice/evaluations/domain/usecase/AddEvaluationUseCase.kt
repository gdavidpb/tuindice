package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AddEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository,
	override val paramsValidator: AddEvaluationParamsValidator,
	override val exceptionHandler: AddEvaluationExceptionHandler
) : FlowUseCase<AddEvaluationParams, Unit, EvaluationError>() {
	override suspend fun executeOnBackground(params: AddEvaluationParams): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		val name = "${params.type}" // TODO compute name

		val add = params.toEvaluationAdd(name)

		evaluationRepository.addEvaluation(
			uid = activeUId,
			add = add
		)

		return flowOf(Unit)
	}
}