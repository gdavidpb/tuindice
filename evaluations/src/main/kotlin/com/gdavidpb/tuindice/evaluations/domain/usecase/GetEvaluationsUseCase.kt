package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow

class GetEvaluationsUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, List<Evaluation>, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<List<Evaluation>> {
		val activeUId = authRepository.getActiveAuth().uid

		return evaluationRepository.getEvaluations(uid = activeUId, sid = params)
	}
}