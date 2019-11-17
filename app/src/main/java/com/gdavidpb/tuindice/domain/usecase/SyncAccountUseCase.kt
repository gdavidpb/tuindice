package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.service.DstData
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ContinuousUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        val activeAuth = authRepository.getActiveAuth() ?: return
        val lastUpdate = settingsRepository.getLastSync()
        val isCooldown = settingsRepository.isSyncCooldown()

        /* Return local account */
        databaseRepository.localTransaction {
            getAccount(uid = activeAuth.uid)?.copy(lastUpdate = lastUpdate)
        }?.also { activeAccount ->
            withContext(foregroundContext) {
                liveData.postNext(activeAccount)
            }
        }

        /* params -> trySync */
        if (isCooldown && !params) return

        /* Collected data */
        val collectedData = mutableListOf<DstData>()

        /* Get credentials */
        val credentials = settingsRepository.getCredentials()

        /* --- Record service auth --- */
        val recordAuthRequest = AuthRequest(
                usbId = credentials.usbId,
                password = credentials.password,
                serviceUrl = BuildConfig.ENDPOINT_DST_RECORD_AUTH
        )

        val recordAuthResponse = dstRepository.auth(recordAuthRequest)

        if (recordAuthResponse?.isSuccessful == true) {
            dstRepository.getPersonalData()?.let(collectedData::add)
            dstRepository.getRecordData()?.let(collectedData::add)
        }

        /* Clear cookies */
        localStorageRepository.delete("cookies")

        /* --- Enrollment service auth --- */
        val enrollmentAuthRequest = AuthRequest(
                usbId = credentials.usbId,
                password = credentials.password,
                serviceUrl = BuildConfig.ENDPOINT_DST_ENROLLMENT_AUTH
        )

        val enrollmentAuthResponse = dstRepository.auth(enrollmentAuthRequest)

        if (enrollmentAuthResponse?.isSuccessful == true) {
            dstRepository.getEnrollment()?.let(collectedData::add)
        }

        /* --- Check if the enrollment files should be purged --- */
        val localCurrentSubjects = databaseRepository
                .localTransaction { getCurrentQuarter(uid = activeAuth.uid) }
                ?.subjects
                ?.map { it.toDstSubject() }

        val remoteCurrentSubjects = collectedData
                .firstOrNull { it is DstRecord }
                ?.let {
                    it as DstRecord

                    it.quarters.firstOrNull { quarter -> quarter.status == STATUS_QUARTER_CURRENT }
                }?.subjects

        val shouldPurge = localCurrentSubjects != remoteCurrentSubjects

        if (shouldPurge) localStorageRepository.delete("enrollments")

        /* If there is collected data */
        if (collectedData.isNotEmpty()) {
            /* Set sync cooldown */
            settingsRepository.setSyncCooldown()

            /* Return updated account */
            databaseRepository.remoteTransaction {
                updateData(uid = activeAuth.uid, data = collectedData)

                getAccount(uid = activeAuth.uid)
            }?.also { updatedAccount ->
                withContext(foregroundContext) {
                    liveData.postNext(updatedAccount)
                }
            }
        }
    }
}