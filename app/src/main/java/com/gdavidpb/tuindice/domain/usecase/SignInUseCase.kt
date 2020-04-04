package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.PATH_COOKIES
import com.gdavidpb.tuindice.utils.PATH_ENROLLMENT
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import com.gdavidpb.tuindice.utils.mappers.toDstCredentials
import com.gdavidpb.tuindice.utils.mappers.toUsbEmail
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException
import java.io.IOException

@IgnoredExceptions(
        CancellationException::class,
        IOException::class,
        HttpException::class,
        AuthenticationException::class
)
open class SignInUseCase(
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
        localStorageRepository.delete(PATH_COOKIES)
        localStorageRepository.delete(PATH_ENROLLMENT)

        val authResponse = dstRepository.auth(request = params)

        /* Dst auth success */
        if (authResponse != null) {
            /* Try to sign in to Firebase */
            runCatching {
                authRepository.signIn(email = email, password = params.password)
            }.onSuccess { activeAuth ->
                storeAccount(auth = activeAuth, request = params, response = authResponse)
            }.onFailure { exception ->
                when (exception.cause) {
                    is FirebaseAuthInvalidUserException -> {
                        val auth = authRepository.signUp(email = email, password = params.password)

                        storeAccount(auth = auth, request = params, response = authResponse)
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        settingsRepository.setIsAwaitingForReset(email = email, password = params.password)

                        authRepository.sendPasswordResetEmail(email = email, password = params.password)
                    }
                    else -> throw exception
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

        with(databaseRepository) {
            updateAuthData(uid = auth.uid, data = authData)

            syncAccount(uid = auth.uid)
        }
    }
}