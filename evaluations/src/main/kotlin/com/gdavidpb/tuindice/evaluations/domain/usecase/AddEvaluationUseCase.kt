package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.exception.EvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.validator.AddEvaluationParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AddEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository,
	override val paramsValidator: AddEvaluationParamsValidator
) : FlowUseCase<AddEvaluationParams, Evaluation, EvaluationError>() {
	override suspend fun executeOnBackground(params: AddEvaluationParams): Flow<Evaluation> {
		val activeUId = authRepository.getActiveAuth().uid

		val evaluation = evaluationRepository.addEvaluation(
			uid = activeUId,
			add = params.toEvaluationAdd()
		)

		return flowOf(evaluation)
	}

	override suspend fun executeOnException(throwable: Throwable): EvaluationError? {
		return when (throwable) {
			is EvaluationIllegalArgumentException -> throwable.error
			else -> super.executeOnException(throwable)
		}
	}
}