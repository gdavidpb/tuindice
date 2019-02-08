package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import kotlinx.coroutines.Dispatchers

open class SyncAccountUseCase(
        private val dstRepository: DstRepository,
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository
) : ResultUseCase<Boolean, Account>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Boolean): Account? {
        val local = suspend { authRepository.getActiveAccount() }
        val remote = suspend { dstRepository.getAccount() }
        val setCooldown = suspend { settingsRepository.setCooldown(key = "SyncAccountUseCase") }
        val isCooldown = settingsRepository.isCooldown(key = "SyncAccountUseCase")

        /* params -> trySync */
        return if (isCooldown || !params)
            local()
        else {
            setCooldown()

            remote() ?: local()
        }
    }
}