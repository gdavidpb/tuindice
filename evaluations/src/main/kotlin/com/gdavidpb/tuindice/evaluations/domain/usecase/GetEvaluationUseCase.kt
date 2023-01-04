package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.PersistenceRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase

class GetEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val persistenceRepository: PersistenceRepository
) : ResultUseCase<String, Evaluation, Nothing>() {
	override suspend fun executeOnBackground(params: String): Evaluation {
		val activeUId = authRepository.getActiveAuth().uid

		return persistenceRepository.getEvaluation(uid = activeUId, eid = params)
	}
}