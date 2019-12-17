package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.model.service.DstData
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.coroutines.Dispatchers
import kotlin.IllegalStateException

open class SyncAccountUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository
) : ResultUseCase<Unit, Boolean>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit): Boolean? {
        val activeAuth = authRepository.getActiveAuth()
                ?: throw IllegalStateException("'activeAuth' is null")

        val activeAccount = databaseRepository.localTransaction { getAccount(uid = activeAuth.uid) }
                ?: throw IllegalStateException("'activeAccount' is null")

        /* Check if account is up-to-date, return no update required */
        if (activeAccount.isUpdated()) return false

        /* Collected data */
        val collectedData = mutableListOf<DstData>()

        /* Get credentials */
        val credentials = settingsRepository.getCredentials()

        /* Record service auth */
        collectedData.addRecordData(credentials)

        /* Enrollment service auth */
        collectedData.addEnrollmentData(credentials)

        /*  Check if the enrollment files should be purged  */
        if (collectedData.shouldPurgeEnrollments(uid = activeAuth.uid))
            localStorageRepository.delete("enrollments")

        /* If there is collected data */
        return collectedData.isNotEmpty().also { requireUpdate ->
            if (requireUpdate)
                databaseRepository.remoteTransaction {
                    updateData(uid = activeAuth.uid, data = collectedData)
                }
        }
    }

    private suspend fun MutableList<DstData>.addRecordData(credentials: DstCredentials) {
        localStorageRepository.delete("cookies")

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
        localStorageRepository.delete("cookies")

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