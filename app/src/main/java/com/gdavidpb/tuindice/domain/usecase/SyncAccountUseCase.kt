package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.ENDPOINT_DST_RECORD_AUTH
import kotlinx.coroutines.Dispatchers

open class SyncAccountUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository
) : ResultUseCase<Boolean, Account>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Boolean): Account? {
        val lastUpdate = settingsRepository.getLastSync()
        val isCooldown = settingsRepository.isSyncCooldown()
        val activeAccount = databaseRepository.getActiveAccount()
                ?.copy(lastUpdate = lastUpdate)

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

                /* Syncing */
                if (recordAuthResponse?.isSuccessful == true) {
                    val personalData = dstRepository.getPersonalData()

                    val recordData = dstRepository.getRecordData()

                    /* Return updated account */
                    return databaseRepository.networkTransaction {
                        if (personalData != null)
                            updatePersonalData(data = personalData)

                        if (recordData != null)
                            updateRecordData(data = recordData)

                        getActiveAccount()
                    }
                }
            }
        }

        return activeAccount
    }
}