package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import kotlinx.coroutines.Dispatchers

open class AddEvaluationUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<Evaluation, Evaluation>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Evaluation): Evaluation? {
        val activeAuth = authRepository.getActiveAuth()
                ?: throw IllegalStateException("'activeAuth' is null")

        return databaseRepository.remoteTransaction {
            addEvaluation(uid = activeAuth.uid, evaluation = params)
        }
    }
}