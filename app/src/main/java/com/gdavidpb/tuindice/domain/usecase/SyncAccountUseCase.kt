package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.service.DstData
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ContinuousUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

open class SyncAccountUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository
) : ContinuousUseCase<Boolean, Account>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Boolean, liveData: LiveContinuous<Account>) {
        val activeAuth = authRepository.getActiveAuth()
        val lastUpdate = settingsRepository.getLastSync()
        val isCooldown = settingsRepository.isSyncCooldown()

        activeAuth?.also {
            val activeAccount = databaseRepository.localTransaction {
                getAccount(uid = it.uid, lastUpdate = lastUpdate)
            }

            if (activeAccount != null)
                withContext(foregroundContext) {
                    liveData.postNext(activeAccount)
                }

            /* params -> trySync */
            if (!isCooldown || params) {

                /* Get credentials */
                val credentials = settingsRepository.getCredentials()

                /* Collected data */
                val collectedData = mutableListOf<DstData>()

                /* Enrollment service auth */
                val enrollmentAuthRequest = AuthRequest(
                        usbId = credentials.usbId,
                        password = credentials.password,
                        serviceUrl = ENDPOINT_DST_ENROLLMENT_AUTH
                )

                val enrollmentAuthResponse = dstRepository.auth(enrollmentAuthRequest)

                if (enrollmentAuthResponse?.isSuccessful == true) {
                    val enrollmentData = dstRepository.getEnrollment()

                    if (enrollmentData != null)
                        collectedData.add(enrollmentData)
                }

                /* Clear cookies */
                localStorageRepository.delete("cookies")

                /* Record service auth */
                val recordAuthRequest = AuthRequest(
                        usbId = credentials.usbId,
                        password = credentials.password,
                        serviceUrl = ENDPOINT_DST_RECORD_AUTH
                )

                val recordAuthResponse = dstRepository.auth(recordAuthRequest)

                if (recordAuthResponse?.isSuccessful == true) {
                    val personalData = dstRepository.getPersonalData()

                    val recordData = dstRepository.getRecordData()

                    if (personalData != null)
                        collectedData.add(personalData)

                    if (recordData != null)
                        collectedData.add(recordData)
                }

                /* If there is collected data */
                if (collectedData.isNotEmpty()) {
                    /* Set sync cooldown */
                    settingsRepository.setSyncCooldown()

                    /* Return updated account */
                    val updatedAccount = databaseRepository.remoteTransaction {
                        updateData(uid = it.uid, data = collectedData)

                        getAccount(uid = it.uid, lastUpdate = Date())
                    }

                    if (updatedAccount != null)
                        withContext(foregroundContext) {
                            liveData.postNext(updatedAccount)
                        }
                }
            }
        }
    }
}