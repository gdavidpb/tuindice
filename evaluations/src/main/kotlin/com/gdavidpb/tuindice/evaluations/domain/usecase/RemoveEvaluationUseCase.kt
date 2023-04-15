package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoveEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		val remove = EvaluationRemove(
			evaluationId = params
		)

		evaluationRepository.removeEvaluation(uid = activeUId, remove = remove)

		return flowOf(Unit)
	}
}