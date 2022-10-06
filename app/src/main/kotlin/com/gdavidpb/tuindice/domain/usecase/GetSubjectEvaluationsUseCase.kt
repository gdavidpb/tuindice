package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase

class GetSubjectEvaluationsUseCase(
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository
) : ResultUseCase<String, List<Evaluation>, Nothing>() {
    override suspend fun executeOnBackground(params: String): List<Evaluation> {
        val activeUId = authRepository.getActiveAuth().uid

        return databaseRepository.getSubjectEvaluations(uid = activeUId, sid = params)
    }
}