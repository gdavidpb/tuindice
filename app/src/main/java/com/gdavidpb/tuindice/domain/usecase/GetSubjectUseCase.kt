package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase

class GetSubjectUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<String, Subject, Nothing>() {
    override suspend fun executeOnBackground(params: String): Subject? {
        val activeUId = authRepository.getActiveAuth().uid

        return databaseRepository.getSubject(uid = activeUId, sid = params)
    }
}