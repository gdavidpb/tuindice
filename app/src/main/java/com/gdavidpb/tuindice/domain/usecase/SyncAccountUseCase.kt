package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.ENDPOINT_DST_RECORD_AUTH
import kotlinx.coroutines.Dispatchers
import java.util.*

open class SyncAccountUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository
) : ResultUseCase<Boolean, Account>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Boolean): Account? {
        val activeAuth = authRepository.getActiveAuth()
        val lastUpdate = settingsRepository.getLastSync()
        val isCooldown = settingsRepository.isSyncCooldown()

        return activeAuth?.let {
            val activeAccount = databaseRepository.localTransaction {
                getAccount(uid = it.uid, lastUpdate = lastUpdate)
            }

            /* params -> trySync */
            if (!isCooldown || params) {
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
                    /* Set sync cooldown */
                    settingsRepository.setSyncCooldown()

                    val personalData = dstRepository.getPersonalData()

                    val recordData = dstRepository.getRecordData()

                    /* Return updated account */
                    return databaseRepository.remoteTransaction {
                        if (personalData != null)
                            updatePersonalData(uid = it.uid, data = personalData)

                        if (recordData != null)
                            updateRecordData(uid = it.uid, data = recordData)

                        getAccount(uid = it.uid, lastUpdate = Date())
                    }
                }
            }

            activeAccount
        }
    }
}