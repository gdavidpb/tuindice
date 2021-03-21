package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.request.UpdateSubjectRequest

open class UpdateSubjectUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : CompletableUseCase<UpdateSubjectRequest, Nothing>() {
    override suspend fun executeOnBackground(params: UpdateSubjectRequest) {
        val activeUId = authRepository.getActiveAuth().uid

        databaseRepository.updateSubject(uid = activeUId, request = params)
    }
}