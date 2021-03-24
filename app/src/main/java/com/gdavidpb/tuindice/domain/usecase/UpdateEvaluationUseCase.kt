package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.utils.mappers.applyRequest

open class UpdateEvaluationUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<UpdateEvaluationRequest, Evaluation, Nothing>() {
    override suspend fun executeOnBackground(params: UpdateEvaluationRequest): Evaluation {
        val activeUId = authRepository.getActiveAuth().uid

        return if (params.dispatchChanges)
            databaseRepository
                    .updateEvaluation(uid = activeUId, request = params)
        else
            databaseRepository
                    .getEvaluation(uid = activeUId, eid = params.id)
                    .applyRequest(params)
    }
}