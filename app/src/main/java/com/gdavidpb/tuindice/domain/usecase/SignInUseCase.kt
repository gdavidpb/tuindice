package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.SignInResponse
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.domain.usecase.request.SignInRequest
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toDstCredentials
import com.gdavidpb.tuindice.utils.mappers.toUsbEmail
import java.io.File

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
open class SignInUseCase(
        private val dstRepository: DstRepository,
        private val storageRepository: StorageRepository<File>,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val networkRepository: NetworkRepository
) : EventUseCase<SignInRequest, SignInResponse, SignInError>() {
    override suspend fun executeOnBackground(params: SignInRequest): SignInResponse? {
        val credentials = params.toDstCredentials()
        val email = params.usbId.toUsbEmail()

        authRepository.signOut()
        settingsRepository.clear()
        storageRepository.clear()

        val authResponse = dstRepository.signIn(request = params)

        /* Try to sign in to Firebase */
        runCatching {
            authRepository.signIn(email = email, password = params.password)
        }.onSuccess { activeAuth ->
            storeAccount(auth = activeAuth, credentials = credentials, response = authResponse)
        }.onFailure { throwable ->
            val cause = throwable.cause

            when {
                cause.isUserNotFound() -> {
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

        return authResponse
    }

    override suspend fun executeOnException(throwable: Throwable): SignInError? {
        val causes = throwable.causes()

        return when {
            throwable.isInvalidCredentials() -> SignInError.InvalidCredentials
            causes.isAccountDisabled() -> SignInError.AccountDisabled
            throwable.isConnectionIssue() -> SignInError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }

    private suspend fun storeAccount(auth: Auth, credentials: DstCredentials, response: SignInResponse) {
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