package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.asUsbEmail
import com.gdavidpb.tuindice.utils.mappers.toDstCredentials

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class SignInUseCase(
        private val dstRepository: DstRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val networkRepository: NetworkRepository
) : EventUseCase<Credentials, Unit, SignInError>() {
    override suspend fun executeOnBackground(params: Credentials) {
        val dstCredentials = params.toDstCredentials(serviceUrl = BuildConfig.ENDPOINT_DST_SECURE_AUTH)

        dstRepository.signIn(credentials = dstCredentials)

        runCatching {
            authRepository.signIn(credentials = params)
        }.onSuccess {
            handleUserExists(credentials = params)
        }.onFailure { throwable ->
            val causes = throwable.causes()

            when {
                causes.isUserNotFound() -> handleUserNoExists(credentials = params)
                causes.haveCredentialsChanged() -> handleCredentialsChanged(credentials = params)
                else -> throw throwable
            }
        }
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

    private suspend fun handleUserExists(credentials: Credentials) {
        val activeAuth = authRepository.getActiveAuth()

        settingsRepository.storeCredentials(credentials)

        databaseRepository.cache(uid = activeAuth.uid)
    }

    private suspend fun handleUserNoExists(credentials: Credentials) {
        authRepository.signUp(credentials)

        handleUserExists(credentials)
    }

    private suspend fun handleCredentialsChanged(credentials: Credentials) {
        val email = credentials.usbId.asUsbEmail()

        authRepository.sendPasswordResetEmail(email)

        settingsRepository.storeCredentials(credentials)
    }
}