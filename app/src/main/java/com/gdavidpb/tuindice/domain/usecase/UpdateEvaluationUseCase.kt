package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase

open class UpdateEvaluationUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<Evaluation, Evaluation, Any>() {
    override suspend fun executeOnBackground(params: Evaluation): Evaluation {
        val activeUId = authRepository.getActiveAuth().uid

        databaseRepository.updateEvaluation(uid = activeUId, evaluation = params)

        return params
    }
}