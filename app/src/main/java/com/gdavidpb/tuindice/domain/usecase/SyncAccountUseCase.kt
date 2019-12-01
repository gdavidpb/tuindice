package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
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
) : ContinuousUseCase<Unit, Account>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit, liveData: LiveContinuous<Account>) {
        val activeAuth = authRepository.getActiveAuth()
                ?: return

        val activeAccount = databaseRepository.localTransaction { getAccount(uid = activeAuth.uid) }
                ?: return

        /* Return local account */
        liveData.postAccount(activeAccount)

        if (activeAccount.isUpdated()) return

        /* Collected data */
        val collectedData = mutableListOf<DstData>()

        /* Get credentials */
        val credentials = settingsRepository.getCredentials()

        /* Record service auth */
        collectedData.addRecordData(credentials)

        /* Clear cookies */
        localStorageRepository.delete("cookies")

        /* Enrollment service auth */
        collectedData.addEnrollmentData(credentials)

        /*  Check if the enrollment files should be purged  */
        if (collectedData.shouldPurgeEnrollments(uid = activeAuth.uid))
            localStorageRepository.delete("enrollments")

        /* If there is collected data */
        if (collectedData.isNotEmpty()) {
            /* Return updated account */
            databaseRepository.remoteTransaction {
                updateData(uid = activeAuth.uid, data = collectedData)
            }

            /* Return updated account */
            databaseRepository.localTransaction {
                getAccount(uid = activeAuth.uid)
            }?.also { updatedAccount -> liveData.postAccount(updatedAccount) }
        }
    }

    private suspend fun LiveContinuous<Account>.postAccount(account: Account) {
        withContext(foregroundContext) {
            postNext(account)
        }
    }

    private suspend fun MutableList<DstData>.addRecordData(credentials: DstCredentials) {
        val recordAuthRequest = AuthRequest(
                usbId = credentials.usbId,
                password = credentials.password,
                serviceUrl = BuildConfig.ENDPOINT_DST_RECORD_AUTH
        )

        val recordAuthResponse = dstRepository.auth(recordAuthRequest)

        if (recordAuthResponse?.isSuccessful == true) {
            dstRepository.getPersonalData()?.let(::add)
            dstRepository.getRecordData()?.let(::add)
        }
    }

    private suspend fun MutableList<DstData>.addEnrollmentData(credentials: DstCredentials) {
        val enrollmentAuthRequest = AuthRequest(
                usbId = credentials.usbId,
                password = credentials.password,
                serviceUrl = BuildConfig.ENDPOINT_DST_ENROLLMENT_AUTH
        )

        val enrollmentAuthResponse = dstRepository.auth(enrollmentAuthRequest)

        if (enrollmentAuthResponse?.isSuccessful == true) {
            dstRepository.getEnrollment()?.let(::add)
        }
    }

    private suspend fun MutableList<DstData>.shouldPurgeEnrollments(uid: String): Boolean {
        val localCurrentSubjects = databaseRepository
                .localTransaction { getCurrentQuarter(uid) }
                ?.subjects
                ?.map { it.toDstSubject() }

        val remoteCurrentSubjects = firstOrNull { it is DstRecord }
                ?.let {
                    it as DstRecord

                    it.quarters.firstOrNull { quarter -> quarter.status == STATUS_QUARTER_CURRENT }
                }?.subjects

        return localCurrentSubjects != remoteCurrentSubjects
    }
}