package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAndAvailableSubjects
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetEvaluationAndAvailableSubjectsUseCase(
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<GetEvaluationParams, EvaluationAndAvailableSubjects, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: GetEvaluationParams): Flow<EvaluationAndAvailableSubjects> {
		val evaluation = evaluationRepository
			.getEvaluation(eid = params.evaluationId)

		val availableSubjects = evaluationRepository
			.getAvailableSubjects()

		val evaluationAndAvailableSubjects = EvaluationAndAvailableSubjects(
			evaluation = evaluation,
			availableSubjects = availableSubjects
		)

		return flowOf(evaluationAndAvailableSubjects)
	}
}
