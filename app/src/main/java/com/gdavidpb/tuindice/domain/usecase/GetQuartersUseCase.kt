package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import kotlinx.coroutines.Dispatchers

open class GetQuartersUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<Unit, List<Quarter>>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit): List<Quarter>? {
        val activeAuth = authRepository.getActiveAuth()

        return activeAuth?.let {
            val getDataFromLocal = suspend {
                databaseRepository.localTransaction {
                    getQuarters(uid = it.uid)
                }
            }

            val getDataFromRemote = suspend {
                databaseRepository.remoteTransaction {
                    getQuarters(uid = it.uid)
                }
            }

            val local = getDataFromLocal()

            if (local.isNotEmpty())
                local
            else
                getDataFromRemote()
        }
    }
}