package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.exception.EnrollmentNotFoundException
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.request.SignInRequest
import com.gdavidpb.tuindice.utils.PATH_ENROLLMENT
import com.gdavidpb.tuindice.utils.annotations.IgnoredFromExceptionReporting
import com.gdavidpb.tuindice.utils.mappers.formatQuarterTitle
import retrofit2.HttpException
import java.io.File
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

@IgnoredFromExceptionReporting(
        SocketException::class,
        InterruptedIOException::class,
        UnknownHostException::class,
        ConnectException::class,
        SSLHandshakeException::class,
        HttpException::class,
        AuthenticationException::class,
        EnrollmentNotFoundException::class
)
open class OpenEnrollmentProofUseCase(
        private val authRepository: AuthRepository,
        private val dstRepository: DstRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val localStorageRepository: LocalStorageRepository
) : EventUseCase<Unit, File, Any>() {
    override suspend fun executeOnBackground(params: Unit): File {
        val activeUId = authRepository.getActiveAuth().uid

        val currentQuarter = databaseRepository.getCurrentQuarter(uid = activeUId)
                ?: throw EnrollmentNotFoundException()

        /* Try to get current quarter enrollment proof file */
        val enrollmentTitle = with(currentQuarter) {
            (startDate to endDate).formatQuarterTitle()
        }

        val enrollmentName = File(PATH_ENROLLMENT, "$enrollmentTitle.pdf").path
        val enrollmentFile = localStorageRepository.get(enrollmentName)
        val enrollmentExists = localStorageRepository.exists(enrollmentName)

        if (!enrollmentExists) {
            /* Get credentials */
            val credentials = settingsRepository.getCredentials()

            /* Enrollment service auth */
            val enrollmentAuthRequest = SignInRequest(
                    usbId = credentials.usbId,
                    password = credentials.password,
                    serviceUrl = BuildConfig.ENDPOINT_DST_ENROLLMENT_AUTH
            )

            val enrollmentAuthResponse = dstRepository.signIn(enrollmentAuthRequest)
                    ?: throw EnrollmentNotFoundException()

            if (!enrollmentAuthResponse.isSuccessful)
                throw EnrollmentNotFoundException()

            /* Get enrollment proof file from dst service */

            val enrollmentData = dstRepository.getEnrollmentProof()?.byteStream()
                    ?: throw EnrollmentNotFoundException()

            /* Save file in local storage */

            localStorageRepository.put(enrollmentName, enrollmentData)
        }

        return enrollmentFile
    }
}