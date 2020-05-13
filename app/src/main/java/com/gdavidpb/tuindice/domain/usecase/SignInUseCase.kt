package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
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

@IgnoredExceptions(
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
) : ResultUseCase<AuthRequest, AuthResponse>() {
    override suspend fun executeOnBackground(params: AuthRequest): AuthResponse? {
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
                storeAccount(auth = activeAuth, request = params, response = authResponse)
            }.onFailure { throwable ->
                val cause = throwable.cause

                when {
                    cause.isUserNoFound() -> {
                        val auth = authRepository.signUp(email = email, password = params.password)

                        storeAccount(auth = auth, request = params, response = authResponse)
                    }
                    cause.isInvalidCredentials() -> {
                        settingsRepository.setIsAwaitingForReset(email = email, password = params.password)

                        authRepository.sendPasswordResetEmail(email = email, password = params.password)
                    }
                    else -> throw throwable
                }
            }
        }

        return authResponse
    }

    private suspend fun storeAccount(auth: Auth, request: AuthRequest, response: AuthResponse) {
        val authData = DstAuth(
                usbId = request.usbId,
                email = auth.email,
                fullName = response.name
        )

        if (!authRepository.isEmailVerified()) authRepository.sendVerificationEmail()

        settingsRepository.storeCredentials(credentials = request.toDstCredentials())

        databaseRepository.setAuthData(uid = auth.uid, data = authData)
    }
}