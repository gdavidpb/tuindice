package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.exception.NoEnrolledException
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.GetEnrollmentError
import com.gdavidpb.tuindice.domain.usecase.request.SignInRequest
import com.gdavidpb.tuindice.utils.PATH_ENROLLMENT
import com.gdavidpb.tuindice.utils.annotations.IgnoredFromExceptionReporting
import com.gdavidpb.tuindice.utils.extensions.copyTo
import com.gdavidpb.tuindice.utils.extensions.isConnectionIssue
import com.gdavidpb.tuindice.utils.extensions.isInvalidCredentials
import com.gdavidpb.tuindice.utils.extensions.isNotEnrolled
import com.gdavidpb.tuindice.utils.mappers.formatQuarterTitle
import retrofit2.HttpException
import java.io.File
import java.io.InterruptedIOException
import java.io.StreamCorruptedException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

@Suppress("BlockingMethodInNonBlockingContext")
@IgnoredFromExceptionReporting(
        SocketException::class,
        InterruptedIOException::class,
        UnknownHostException::class,
        ConnectException::class,
        SSLHandshakeException::class,
        HttpException::class,
        AuthenticationException::class
)
open class GetEnrollmentProofUseCase(
        private val authRepository: AuthRepository,
        private val dstRepository: DstRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val storageRepository: StorageRepository<File>
) : EventUseCase<Unit, File, GetEnrollmentError>() {
    override suspend fun executeOnBackground(params: Unit): File {
        val activeUId = authRepository.getActiveAuth().uid

        val currentQuarter = databaseRepository.getCurrentQuarter(uid = activeUId)
                ?: throw NoEnrolledException()

        /* Try to get current quarter enrollment proof file */
        val enrollmentTitle = with(currentQuarter) {
            (startDate to endDate).formatQuarterTitle()
        }

        val enrollmentName = File(PATH_ENROLLMENT, "$enrollmentTitle.pdf").path
        val enrollmentFile = storageRepository.get(enrollmentName)

        if (!enrollmentFile.exists()) {
            /* Get credentials */
            val credentials = settingsRepository.getCredentials()

            /* Enrollment service auth */
            val enrollmentAuthRequest = SignInRequest(
                    usbId = credentials.usbId,
                    password = credentials.password,
                    serviceUrl = BuildConfig.ENDPOINT_DST_ENROLLMENT_AUTH
            )

            dstRepository.signIn(enrollmentAuthRequest)

            /* Get enrollment proof file from dst service */

            val inputStream = dstRepository.getEnrollmentProof().byteStream()

            /* Save file in local storage */

            val outputStream = storageRepository.outputStream(enrollmentName)

            inputStream.copyTo(outputStream = outputStream, autoFlush = true, autoClose = true)
        }

        return enrollmentFile
    }

    override suspend fun executeOnException(throwable: Throwable): GetEnrollmentError? {
        return when {
            throwable is StreamCorruptedException -> GetEnrollmentError.NotFound
            throwable.isInvalidCredentials() -> GetEnrollmentError.InvalidCredentials
            throwable.isNotEnrolled() -> GetEnrollmentError.NotEnrolled
            throwable.isConnectionIssue() -> GetEnrollmentError.NoConnection
            else -> null
        }
    }
}