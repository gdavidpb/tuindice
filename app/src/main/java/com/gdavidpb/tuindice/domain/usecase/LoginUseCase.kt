package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.ENDPOINT_DST_RECORD_AUTH
import com.gdavidpb.tuindice.utils.toDstCredentials
import com.gdavidpb.tuindice.utils.toUsbEmail
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.Dispatchers

open class LoginUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository
) : ResultUseCase<AuthRequest, AuthResponse>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: AuthRequest): AuthResponse? {
        val email = params.usbId.toUsbEmail()

        authRepository.signOut()
        settingsRepository.clear()
        localStorageRepository.delete("cookies")

        val authResponse = dstRepository.auth(params.copy(serviceUrl = ENDPOINT_DST_RECORD_AUTH))

        /* Dst auth success */
        if (authResponse != null) {
            /* Try to sign in to Firebase */
            runCatching {
                authRepository.signIn(email = email, password = params.password)
            }.onSuccess { account ->
                storeAccount(account = account, request = params, response = authResponse)
            }.onFailure { exception ->
                if (exception is FirebaseAuthException) {
                    when (exception.errorCode) {
                        "ERROR_USER_NOT_FOUND" -> {
                            val account = authRepository.signUp(email = email, password = params.password)

                            storeAccount(account = account, request = params, response = authResponse)
                        }
                        "ERROR_WRONG_PASSWORD" -> {
                            settingsRepository.setIsAwaitingForReset(email = email, password = params.password)

                            authRepository.sendPasswordResetEmail(email = email, password = params.password)
                        }
                        else -> throw exception
                    }
                } else
                    throw exception
            }
        }

        return authResponse
    }

    private suspend fun storeAccount(account: Account, request: AuthRequest, response: AuthResponse) {
        val authData = DstAuth(
                usbId = request.usbId,
                email = account.email,
                fullName = response.name
        )

        if (!authRepository.isEmailVerified())
            authRepository.sendEmailVerification()

        settingsRepository.storeCredentials(credentials = request.toDstCredentials())

        databaseRepository.updateAuthData(data = authData)
    }
}