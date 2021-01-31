package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase

open class RemoveEvaluationUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : CompletableUseCase<String, Any>() {
    override suspend fun executeOnBackground(params: String) {
        val activeUId = authRepository.getActiveAuth().uid

        databaseRepository.removeEvaluation(uid = activeUId, id = params)
    }

    override suspend fun executeOnException(throwable: Throwable): Any? {
        TODO("Not yet implemented")
    }
}