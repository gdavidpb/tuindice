package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers

@IgnoredExceptions(CancellationException::class)
open class GetAccountUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<Unit, Account>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit): Account? {
        val activeUId = authRepository.getActiveAuth().uid

        return databaseRepository.getAccount(uid = activeUId)
    }
}