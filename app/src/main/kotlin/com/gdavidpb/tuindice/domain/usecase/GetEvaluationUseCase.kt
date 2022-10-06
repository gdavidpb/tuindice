package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase

class GetEvaluationUseCase(
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository
) : ResultUseCase<String, Evaluation, Nothing>() {
    override suspend fun executeOnBackground(params: String): Evaluation {
        val activeUId = authRepository.getActiveAuth().uid

        return databaseRepository.getEvaluation(uid = activeUId, eid = params)
    }
}