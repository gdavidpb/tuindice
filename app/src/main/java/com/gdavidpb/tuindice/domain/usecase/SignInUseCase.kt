package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.asUsbEmail
import com.gdavidpb.tuindice.utils.mappers.toDstCredentials
import java.io.File

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
open class SignInUseCase(
        private val dstRepository: DstRepository,
        private val databaseRepository: DatabaseRepository,
        private val storageRepository: StorageRepository<File>,
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val networkRepository: NetworkRepository
) : CompletableUseCase<Credentials, SignInError>() {
    override suspend fun executeOnBackground(params: Credentials) {
        authRepository.signOut()
        settingsRepository.clear()
        storageRepository.clear()

        runCatching {
            authRepository.signIn(credentials = params)
        }.onSuccess {
            handleUserExists(credentials = params)
        }.onFailure { throwable ->
            val causes = throwable.causes()

            when {
                causes.isUserNotFound() -> handleUserNoExists(credentials = params)
                causes.isCredentialsChanged() -> handleCredentialsChanged(credentials = params)
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

        if (!authRepository.isEmailVerified()) authRepository.sendVerificationEmail()

        settingsRepository.storeCredentials(credentials)

        databaseRepository.cache(uid = activeAuth.uid)
    }

    private suspend fun handleUserNoExists(credentials: Credentials) {
        val dstCredentials = credentials.toDstCredentials(serviceUrl = BuildConfig.ENDPOINT_DST_SECURE_AUTH)

        dstRepository.signIn(credentials = dstCredentials)

        authRepository.signUp(credentials)

        handleUserExists(credentials = credentials)
    }

    private suspend fun handleCredentialsChanged(credentials: Credentials) {
        val email = credentials.usbId.asUsbEmail()

        authRepository.sendPasswordResetEmail(email)

        settingsRepository.storeCredentials(credentials)
    }
}