package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.exception.NoAuthenticatedException
import com.gdavidpb.tuindice.domain.model.exception.NoDataException
import com.gdavidpb.tuindice.domain.model.exception.SynchronizationException
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.model.service.DstData
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.domain.usecase.request.SignInRequest
import com.gdavidpb.tuindice.utils.PATH_COOKIES
import com.gdavidpb.tuindice.utils.annotations.IgnoredFromExceptionReporting
import com.gdavidpb.tuindice.utils.extensions.causes
import com.gdavidpb.tuindice.utils.extensions.isAccountDisabled
import com.gdavidpb.tuindice.utils.extensions.isConnectionIssue
import com.gdavidpb.tuindice.utils.extensions.isUpdated
import retrofit2.HttpException
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
        AuthenticationException::class
)
open class SyncAccountUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository
) : ResultUseCase<Unit, Boolean, SyncError>() {
    override suspend fun executeOnBackground(params: Unit): Boolean? {
        val activeUId = authRepository.getActiveAuth().uid
        val activeAccount = databaseRepository.getAccount(uid = activeUId)
        val thereIsDataInCache = databaseRepository.getQuarters(uid = activeUId).isNotEmpty()

        /* Check if account is up-to-date, return no update required */
        if (activeAccount.isUpdated())
            if (thereIsDataInCache)
                return false
            else
                throw NoDataException()

        /* Collected data */
        val collectedData = mutableListOf<DstData>()

        /* Get credentials */
        val credentials = settingsRepository.getCredentials()

        /* Record service auth */
        collectedData.addRecordData(credentials)

        /* Enrollment service auth */
        collectedData.addEnrollmentData(credentials)

        /* Should responses more than one service */
        return (collectedData.size > 1).also { pendingUpdate ->
            if (pendingUpdate) {
                /* Sync account */
                databaseRepository.syncAccount(uid = activeUId, data = collectedData)
            }
        }
    }

    override suspend fun executeOnException(throwable: Throwable): SyncError? {
        val causes = throwable.causes()

        return when {
            throwable is NoAuthenticatedException -> SyncError.NoAuthenticated
            throwable is NoDataException -> SyncError.NoDataAvailable
            throwable is SynchronizationException -> SyncError.NoSynced
            causes.isAccountDisabled() -> SyncError.AccountDisabled
            throwable.isConnectionIssue() -> SyncError.NoConnection
            else -> null
        }
    }

    private suspend fun MutableList<DstData>.addRecordData(credentials: DstCredentials) {
        localStorageRepository.delete(PATH_COOKIES)

        val recordAuthRequest = SignInRequest(
                usbId = credentials.usbId,
                password = credentials.password,
                serviceUrl = BuildConfig.ENDPOINT_DST_RECORD_AUTH
        )

        val recordAuthResponse = dstRepository.signIn(recordAuthRequest)

        if (recordAuthResponse?.isSuccessful == true) {
            dstRepository.getPersonalData()?.let(::add)
            dstRepository.getRecordData()?.let(::add)
        }
    }

    private suspend fun MutableList<DstData>.addEnrollmentData(credentials: DstCredentials) {
        localStorageRepository.delete(PATH_COOKIES)

        val enrollmentAuthRequest = SignInRequest(
                usbId = credentials.usbId,
                password = credentials.password,
                serviceUrl = BuildConfig.ENDPOINT_DST_ENROLLMENT_AUTH
        )

        val enrollmentAuthResponse = dstRepository.signIn(enrollmentAuthRequest)

        if (enrollmentAuthResponse?.isSuccessful == true) {
            dstRepository.getEnrollment()?.let(::add)
        }
    }
}