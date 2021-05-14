package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.model.exception.OutdatedPasswordException
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.asUsbEmail

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class SignInUseCase(
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val networkRepository: NetworkRepository
) : EventUseCase<Credentials, Boolean, SignInError>() {
    override suspend fun executeOnBackground(params: Credentials): Boolean {
        // TODO here dstRepository.checkCredentials(credentials = params)

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

        val activeAuth = authRepository.getActiveAuth()

        return databaseRepository.hasCache(uid = activeAuth.uid)
    }

    override suspend fun executeOnException(throwable: Throwable): SignInError? {
        val causes = throwable.causes()

        return when {
            causes.isAccountDisabled() -> SignInError.AccountDisabled
            throwable is OutdatedPasswordException -> SignInError.OutdatedPassword
            throwable.isTimeout() -> SignInError.Timeout
            throwable.isInvalidCredentials() -> SignInError.InvalidCredentials
            throwable.isConnection() -> SignInError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }

    private suspend fun handleUserExists(credentials: Credentials) {
        val activeAuth = authRepository.getActiveAuth()

        settingsRepository.storeCredentials(credentials)

        databaseRepository.cache(activeAuth.uid)
    }

    private suspend fun handleUserNoExists(credentials: Credentials) {
        authRepository.signUp(credentials)

        handleUserExists(credentials)
    }

    private suspend fun handleCredentialsChanged(credentials: Credentials) {
        val email = credentials.usbId.asUsbEmail()

        settingsRepository.storeCredentials(credentials)

        authRepository.sendPasswordResetEmail(email)

        throw OutdatedPasswordException()
    }
}