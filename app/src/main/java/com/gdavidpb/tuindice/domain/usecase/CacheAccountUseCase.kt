package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import kotlinx.coroutines.Dispatchers

open class CacheAccountUseCase(
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository
) : ResultUseCase<Unit, Account>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit): Account? {
        val lastUpdate = settingsRepository.getLastSync()

        return databaseRepository.localTransaction {
            getActiveAccount(lastUpdate = lastUpdate)
        }
    }
}