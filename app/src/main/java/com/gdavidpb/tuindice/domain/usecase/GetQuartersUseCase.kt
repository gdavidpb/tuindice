package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import kotlinx.coroutines.Dispatchers

open class GetQuartersUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<Boolean, List<Quarter>>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Boolean): List<Quarter>? {
        val activeAuth = authRepository.getActiveAuth() ?: return null

        /* params -> fromLocal */
        return if (params)
            databaseRepository.localTransaction { getQuarters(uid = activeAuth.uid) }
        else
            databaseRepository.remoteTransaction { getQuarters(uid = activeAuth.uid) }
    }
}