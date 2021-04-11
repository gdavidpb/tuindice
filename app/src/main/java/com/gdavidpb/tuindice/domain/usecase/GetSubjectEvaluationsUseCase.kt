package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase

class GetSubjectEvaluationsUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<String, List<Evaluation>, Nothing>() {
    override suspend fun executeOnBackground(params: String): List<Evaluation> {
        val activeUId = authRepository.getActiveAuth().uid

        return databaseRepository.getSubjectEvaluations(uid = activeUId, sid = params)
    }
}