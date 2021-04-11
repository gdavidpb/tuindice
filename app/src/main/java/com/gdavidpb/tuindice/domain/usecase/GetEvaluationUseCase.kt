package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase

class GetEvaluationUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<String, Evaluation, Nothing>() {
    override suspend fun executeOnBackground(params: String): Evaluation? {
        val activeUId = authRepository.getActiveAuth().uid

        return databaseRepository.getEvaluation(uid = activeUId, eid = params)
    }
}