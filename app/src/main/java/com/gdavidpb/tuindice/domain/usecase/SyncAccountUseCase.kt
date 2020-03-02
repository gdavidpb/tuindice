package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.model.service.DstData
import com.gdavidpb.tuindice.domain.model.service.DstEnrollment
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.domain.usecase.response.SyncResponse
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import com.gdavidpb.tuindice.utils.extensions.isUpdated
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException
import java.io.IOException

@IgnoredExceptions(
        CancellationException::class,
        IOException::class,
        HttpException::class,
        AuthenticationException::class
)
open class SyncAccountUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository
) : ResultUseCase<Unit, SyncResponse>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit): SyncResponse? {
        val activeUId = authRepository.getActiveAuth().uid
        val activeAccount = databaseRepository.getAccount(uid = activeUId)
        val hasDataInCache = databaseRepository.getQuarters(uid = activeUId).isNotEmpty()

        /* Check if account is up-to-date, return no update required */
        if (activeAccount.isUpdated())
            return SyncResponse(isDataUpdated = false, hasDataInCache = hasDataInCache)

        /* Collected data */
        val collectedData = mutableListOf<DstData>()

        /* Get credentials */
        val credentials = settingsRepository.getCredentials()

        /* Record service auth */
        collectedData.addRecordData(credentials)

        /* Enrollment service auth */
        collectedData.addEnrollmentData(credentials)

        /*  Check if the current quarter should be purged  */
        if (collectedData.shouldPurgeCurrentQuarter(uid = activeUId))
            localStorageRepository.delete("enrollments")

        /* Should responses more than one service */
        return (collectedData.size > 1).let { requireUpdate ->
            if (requireUpdate) {
                /* Update account */
                databaseRepository.updateData(uid = activeUId, data = collectedData)

                /* Sync account */
                databaseRepository.syncAccount(uid = activeUId)
            }

            SyncResponse(isDataUpdated = requireUpdate, hasDataInCache = true)
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

    private suspend fun MutableList<DstData>.shouldPurgeCurrentQuarter(uid: String): Boolean {
        val localCurrentSubjects = databaseRepository
                .getCurrentQuarter(uid)
                ?.subjects

        val remoteCurrentSubjects = firstOrNull { it is DstEnrollment }
                ?.let { it as DstEnrollment }
                ?.schedule

        val localCurrentCodes = localCurrentSubjects?.map { it.code }
        val remoteCurrentCodes = remoteCurrentSubjects?.map { it.code }

        val shouldPurge = localCurrentCodes != remoteCurrentCodes

        if (shouldPurge && localCurrentCodes != null && remoteCurrentCodes != null) {
            val codesToPurge = localCurrentCodes.subtract(remoteCurrentCodes)

            if (codesToPurge.isNotEmpty()) {
                val subjectMap = localCurrentSubjects.map { it.code to it.id }.toMap()
                val subjectIds = codesToPurge.mapNotNull { subjectMap[it] }.toTypedArray()

                databaseRepository.removeSubjects(uid = uid, ids = *subjectIds)
            }
        }

        return shouldPurge
    }
}