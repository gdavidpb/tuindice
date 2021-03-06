package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase

class RemoveEvaluationUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : CompletableUseCase<String, Nothing>() {
    override suspend fun executeOnBackground(params: String) {
        val activeUId = authRepository.getActiveAuth().uid

        databaseRepository.removeEvaluation(uid = activeUId, eid = params)
    }
}