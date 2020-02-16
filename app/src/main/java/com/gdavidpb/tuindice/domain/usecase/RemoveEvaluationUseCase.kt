package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import kotlinx.coroutines.Dispatchers

open class RemoveEvaluationUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : CompletableUseCase<String>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: String) {
        val activeUId = authRepository.getActiveAuth().uid

        databaseRepository.removeEvaluation(uid = activeUId, id = params)
    }
}