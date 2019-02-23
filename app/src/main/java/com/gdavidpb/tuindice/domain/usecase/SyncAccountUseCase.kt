package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.ENDPOINT_DST_RECORD_AUTH
import kotlinx.coroutines.Dispatchers

open class SyncAccountUseCase(
        private val dstRepository: DstRepository,
        private val authRepository: AuthRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository
) : ResultUseCase<Boolean, Account>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Boolean): Account? {
        val activeAccount = authRepository.getActiveAccount()
        val isCooldown = settingsRepository.isSyncCooldown()

        if (activeAccount != null) {
            /* params -> trySync */
            if (!isCooldown || params) {
                /* Set sync cooldown */
                settingsRepository.setSyncCooldown()

                /* Clear cookies */
                localStorageRepository.delete("cookies")

                /* Get credentials */
                val credentials = settingsRepository.getCredentials()

                /* Auth to get account */
                val recordAuthRequest = AuthRequest(
                        usbId = credentials.usbId,
                        password = credentials.password,
                        serviceUrl = ENDPOINT_DST_RECORD_AUTH
                )

                val recordAuthResponse = dstRepository.auth(recordAuthRequest)

                if (recordAuthResponse?.isSuccessful == true) {
                    /* Syncing */
                    val remoteAccount = dstRepository.getPersonal()

                    val remoteRecord = dstRepository.getRecord()

                    if (remoteAccount != null)
                        databaseRepository.updateAccount(remoteAccount)

                    if (remoteRecord != null)
                        databaseRepository.updateRecord(remoteRecord)
                }
            }
        }

        return activeAccount
    }
}