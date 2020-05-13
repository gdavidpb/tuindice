package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.utils.mappers.toResetRequest
import com.gdavidpb.tuindice.utils.mappers.toVerifyCode

open class StartUpUseCase(
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val identifierRepository: IdentifierRepository,
        private val reportingRepository: ReportingRepository
) : ResultUseCase<String, StartUpAction>() {
    override suspend fun executeOnBackground(params: String): StartUpAction? {
        val isActiveAuth = authRepository.isActiveAuth()
        val isPasswordResetLink = authRepository.isResetLink(params)
        val isVeryEmailLink = authRepository.isVeryLink(params)
        val isAwaitingForPasswordReset = settingsRepository.isAwaitingForReset()

        val email = settingsRepository.awaitingEmail()
        val lastScreen = settingsRepository.getLastScreen()

        return when {
            isPasswordResetLink && isActiveAuth -> {
                val activeAuth = authRepository.getActiveAuth()
                val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)

                settingsRepository.clearIsAwaitingForReset()

                val request = params.toResetRequest()

                authRepository.confirmPasswordReset(request.code, request.password)

                authRepository.signIn(email = request.email, password = request.password)

                StartUpAction.Main(screen = lastScreen, account = activeAccount)
            }
            isVeryEmailLink && isActiveAuth -> {
                val activeAuth = authRepository.getActiveAuth()
                val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)
                val isEmailVerified = authRepository.isEmailVerified()

                if (!isEmailVerified) {
                    val verifyCode = params.toVerifyCode()

                    authRepository.confirmVerifyEmail(code = verifyCode)
                }

                StartUpAction.Main(screen = lastScreen, account = activeAccount)
            }
            isAwaitingForPasswordReset -> {
                StartUpAction.Reset(email = email)
            }
            isActiveAuth -> {
                val activeAuth = authRepository.getActiveAuth()
                val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)
                val isEmailVerified = authRepository.isEmailVerified()

                reportingRepository.setIdentifier(activeAuth.uid)

                val token = identifierRepository.getIdentifier()

                if (token != null)
                    databaseRepository.setToken(uid = activeAuth.uid, token = token)

                if (isEmailVerified)
                    StartUpAction.Main(screen = lastScreen, account = activeAccount)
                else
                    StartUpAction.Verify(email = activeAuth.email)
            }
            else -> StartUpAction.Login
        }
    }
}