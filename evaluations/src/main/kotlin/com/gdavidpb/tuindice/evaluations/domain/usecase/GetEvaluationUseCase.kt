package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository

class GetEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val evaluationRepository: EvaluationRepository,
	override val configRepository: ConfigRepository,
	override val reportingRepository: ReportingRepository
) : ResultUseCase<String, Evaluation, Nothing>() {
	override suspend fun executeOnBackground(params: String): Evaluation {
		val activeUId = authRepository.getActiveAuth().uid

		return evaluationRepository.getEvaluation(uid = activeUId, eid = params)
	}
}