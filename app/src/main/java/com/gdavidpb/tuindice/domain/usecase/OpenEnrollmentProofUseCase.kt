package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.utils.toQuarterTitle
import kotlinx.coroutines.Dispatchers
import java.io.File

open class OpenEnrollmentProofUseCase(
        private val authRepository: AuthRepository,
        private val dstRepository: DstRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val localStorageRepository: LocalStorageRepository
) : ResultUseCase<Unit, File>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit): File? {
        val activeAuth = authRepository.getActiveAuth()
        val quarters = activeAuth?.let { auth -> databaseRepository.localTransaction { getQuarters(uid = auth.uid) } }
        val currentQuarter = quarters?.firstOrNull { quarter -> quarter.status == STATUS_QUARTER_CURRENT }

        currentQuarter ?: return null

        /* Try to get current quarter enrollment proof file */
        val enrollmentName = "${currentQuarter.toQuarterTitle()}.pdf"
        val enrollmentFile = localStorageRepository.getFile(enrollmentName)

        if (!enrollmentFile.exists()) {
            /* Get credentials */
            val credentials = settingsRepository.getCredentials()

            /* Enrollment service auth */
            val enrollmentAuthRequest = AuthRequest(
                    usbId = credentials.usbId,
                    password = credentials.password,
                    serviceUrl = BuildConfig.ENDPOINT_DST_ENROLLMENT_AUTH
            )

            val enrollmentAuthResponse = dstRepository.auth(enrollmentAuthRequest) ?: return null

            if (!enrollmentAuthResponse.isSuccessful) return null

            /* Get enrollment proof file from dst service */

            val enrollmentData = dstRepository.getEnrollmentProof()?.byteStream() ?: return null

            /* Save file in local storage */

            localStorageRepository.put(enrollmentName, enrollmentData)
        }

        return enrollmentFile
    }
}