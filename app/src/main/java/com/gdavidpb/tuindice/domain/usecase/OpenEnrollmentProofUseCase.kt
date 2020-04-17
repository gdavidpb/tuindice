package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.exception.EnrollmentNotFoundException
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.PATH_ENROLLMENT
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import com.gdavidpb.tuindice.utils.mappers.formatQuarterTitle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException
import java.io.File
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

@IgnoredExceptions(
        CancellationException::class,
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
) : EventUseCase<Unit, File>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
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
            val enrollmentAuthRequest = AuthRequest(
                    usbId = credentials.usbId,
                    password = credentials.password,
                    serviceUrl = BuildConfig.ENDPOINT_DST_ENROLLMENT_AUTH
            )

            val enrollmentAuthResponse = dstRepository.auth(enrollmentAuthRequest)
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