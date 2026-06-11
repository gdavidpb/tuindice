package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetEvaluationUseCase(
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : FlowUseCase<String, Evaluation?, EvaluationsUseCaseError>(
	reportingRepository = reportingRepository,
	dispatchers = dispatchers
) {
	override suspend fun executeOnBackground(params: String): Flow<Evaluation?> {
		val evaluation = evaluationRepository.getEvaluation(eid = params)

		return flowOf(evaluation)
	}
}
