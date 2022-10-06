package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase

class GetProfileUseCase(
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository
) : ResultUseCase<Unit, Account, Nothing>() {
    override suspend fun executeOnBackground(params: Unit): Account {
        val activeUId = authRepository.getActiveAuth().uid

        return databaseRepository.getAccount(uid = activeUId)
    }
}