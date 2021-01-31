package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.annotations.IgnoredFromExceptionReporting
import com.gdavidpb.tuindice.utils.extensions.isInvalidCredentials
import com.gdavidpb.tuindice.utils.extensions.isUserNoFound
import com.gdavidpb.tuindice.utils.mappers.toDstCredentials
import com.gdavidpb.tuindice.utils.mappers.toUsbEmail
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
open class SignInUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository
) : ResultUseCase<AuthRequest, AuthResponse, Any>() {
    override suspend fun executeOnBackground(params: AuthRequest): AuthResponse? {
        val credentials = params.toDstCredentials()
        val email = params.usbId.toUsbEmail()

        authRepository.signOut()
        settingsRepository.clear()
        localStorageRepository.clear()

        val authResponse = dstRepository.auth(request = params)

        /* Dst auth success */
        if (authResponse != null) {
            /* Try to sign in to Firebase */
            runCatching {
                authRepository.signIn(email = email, password = params.password)
            }.onSuccess { activeAuth ->
                storeAccount(auth = activeAuth, credentials = credentials, response = authResponse)
            }.onFailure { throwable ->
                val cause = throwable.cause

                when {
                    cause.isUserNoFound() -> {
                        val activeAuth = authRepository.signUp(email = email, password = params.password)

                        storeAccount(auth = activeAuth, credentials = credentials, response = authResponse)
                    }
                    cause.isInvalidCredentials() -> {
                        authRepository.sendPasswordResetEmail(email = email)

                        settingsRepository.storeCredentials(credentials = credentials)
                    }
                    else -> throw throwable
                }
            }
        }

        return authResponse
    }

    private suspend fun storeAccount(auth: Auth, credentials: DstCredentials, response: AuthResponse) {
        val authData = DstAuth(
                usbId = credentials.usbId,
                email = auth.email,
                fullName = response.name
        )

        if (!authRepository.isEmailVerified()) authRepository.sendVerificationEmail()

        databaseRepository.setAuthData(uid = auth.uid, data = authData)

        settingsRepository.storeCredentials(credentials = credentials)
    }
}