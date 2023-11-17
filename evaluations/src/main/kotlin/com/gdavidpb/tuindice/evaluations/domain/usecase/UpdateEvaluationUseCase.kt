package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository
) : FlowUseCase<UpdateEvaluationParams, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: UpdateEvaluationParams): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		val update = params.toEvaluationUpdate()

		evaluationRepository.updateEvaluation(
			uid = activeUId,
			update = update
		)

		return flowOf(Unit)
	}
}