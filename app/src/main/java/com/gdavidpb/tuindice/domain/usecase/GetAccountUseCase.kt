package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import kotlinx.coroutines.Dispatchers

open class GetAccountUseCase(
        private val dstRepository: DstRepository,
        private val localDatabaseRepository: LocalDatabaseRepository,
        private val settingsRepository: SettingsRepository
) : ResultUseCase<Boolean, Account>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Boolean): Account? {
        val local = suspend { localDatabaseRepository.getActiveAccount() }
        val remote = suspend { dstRepository.getAccount() }
        val setCooldown = suspend { settingsRepository.setCooldown(key = "GetAccountUseCase") }
        val isCooldown = settingsRepository.isCooldown(key = "GetAccountUseCase")

        /* params -> tryRefresh */
        return if (isCooldown || !params)
            local()
        else {
            setCooldown()

            remote() ?: local()
        }
    }
}