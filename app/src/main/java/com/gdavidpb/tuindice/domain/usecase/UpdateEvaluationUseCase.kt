package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.request.UpdateEvaluationRequest

open class UpdateEvaluationUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : CompletableUseCase<UpdateEvaluationRequest, Nothing>() {
    override suspend fun executeOnBackground(params: UpdateEvaluationRequest) {
        val activeUId = authRepository.getActiveAuth().uid

        databaseRepository.updateEvaluation(uid = activeUId, request = params)
    }
}