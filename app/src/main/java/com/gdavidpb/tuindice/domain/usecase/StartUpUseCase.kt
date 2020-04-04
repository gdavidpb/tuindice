package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import com.gdavidpb.tuindice.utils.mappers.toResetRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers

@IgnoredExceptions(CancellationException::class)
open class StartUpUseCase(
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val identifierRepository: IdentifierRepository
) : ResultUseCase<String, StartUpAction>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: String): StartUpAction? {
        val isActiveAuth = authRepository.isActiveAuth()
        val isPasswordResetLink = authRepository.isResetLink(params)
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
            isAwaitingForPasswordReset -> {
                StartUpAction.Reset(email = email)
            }
            isActiveAuth -> {
                val activeAuth = authRepository.getActiveAuth()
                val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)

                reportingRepository.setIdentifier(activeAuth.uid)

                val token = identifierRepository.getIdentifier()

                if (token != null)
                    databaseRepository.updateToken(uid = activeAuth.uid, token = token)

                if (authRepository.isEmailVerified())
                    StartUpAction.Main(screen = lastScreen, account = activeAccount)
                else
                    StartUpAction.Verify(email = activeAuth.email)
            }
            else -> StartUpAction.Login
        }
    }
}