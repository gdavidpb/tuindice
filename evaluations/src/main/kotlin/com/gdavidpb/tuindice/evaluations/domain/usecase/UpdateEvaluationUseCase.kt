package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.domain.exception.EvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.validator.UpdateEvaluationParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository,
	override val paramsValidator: UpdateEvaluationParamsValidator
) : FlowUseCase<UpdateEvaluationParams, Evaluation, EvaluationError>() {
	override suspend fun executeOnBackground(params: UpdateEvaluationParams): Flow<Evaluation> {
		val activeUId = authRepository.getActiveAuth().uid

		val evaluation = evaluationRepository.updateEvaluation(
			uid = activeUId,
			update = params.toEvaluationUpdate()
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