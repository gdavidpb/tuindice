package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAndAvailableSubjects
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetEvaluationAndAvailableSubjectsUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository
) : FlowUseCase<GetEvaluationParams, EvaluationAndAvailableSubjects, Nothing>() {
	override suspend fun executeOnBackground(params: GetEvaluationParams): Flow<EvaluationAndAvailableSubjects> {
		val activeUId = authRepository.getActiveAuth().uid

		val evaluation = if (params.evaluationId.isNotEmpty())
			evaluationRepository
				.getEvaluation(uid = activeUId, eid = params.evaluationId)
		else
			null

		val availableSubjects = evaluationRepository
			.getAvailableSubjects(uid = activeUId)

		val evaluationAndAvailableSubjects = EvaluationAndAvailableSubjects(
			evaluation = evaluation,
			availableSubjects = availableSubjects
		)

		return flowOf(evaluationAndAvailableSubjects)
	}
}