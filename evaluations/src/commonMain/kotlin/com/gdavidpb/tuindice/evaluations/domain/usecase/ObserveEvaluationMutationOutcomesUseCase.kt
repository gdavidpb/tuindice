package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow

// Reads the local outcome flow only: nothing is sent and nothing is refreshed here.
class ObserveEvaluationMutationOutcomesUseCase(
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, MutationOutcome, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<MutationOutcome> {
		return evaluationRepository.observeMutationOutcomesFlow()
	}
}
