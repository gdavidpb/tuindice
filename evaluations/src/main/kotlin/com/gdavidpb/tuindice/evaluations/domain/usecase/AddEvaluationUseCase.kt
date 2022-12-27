package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase

class AddEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val databaseRepository: DatabaseRepository
) : ResultUseCase<Evaluation, Evaluation, Nothing>() {
	override suspend fun executeOnBackground(params: Evaluation): Evaluation {
		val activeUId = authRepository.getActiveAuth().uid

		databaseRepository.addEvaluation(activeUId, params)

		return params
	}
}