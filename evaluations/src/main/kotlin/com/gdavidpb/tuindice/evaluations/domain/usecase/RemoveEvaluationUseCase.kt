package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.CompletableUseCase

class RemoveEvaluationUseCase(
	private val authRepository: AuthRepository,
	private val databaseRepository: DatabaseRepository
) : CompletableUseCase<String, Nothing>() {
	override suspend fun executeOnBackground(params: String) {
		val activeUId = authRepository.getActiveAuth().uid

		databaseRepository.removeEvaluation(uid = activeUId, eid = params)
	}
}