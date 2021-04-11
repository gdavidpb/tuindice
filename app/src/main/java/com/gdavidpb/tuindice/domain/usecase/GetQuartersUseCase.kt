package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase

class GetQuartersUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<Unit, List<Quarter>, Nothing>() {
    override suspend fun executeOnBackground(params: Unit): List<Quarter>? {
        val activeUId = authRepository.getActiveAuth().uid

        return databaseRepository.getQuarters(uid = activeUId)
    }
}