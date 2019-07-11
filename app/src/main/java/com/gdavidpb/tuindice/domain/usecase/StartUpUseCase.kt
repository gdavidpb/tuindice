package com.gdavidpb.tuindice.domain.usecase

import android.content.Intent
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.utils.toResetRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class StartUpUseCase(
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val identifierRepository: IdentifierRepository
) : ResultUseCase<Intent, StartUpAction>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Intent): StartUpAction? {
        val link = params.dataString

        val activeAuth = authRepository.getActiveAuth()
        val passwordReset = authRepository.isResetLink(link)
        val awaitingForReset = settingsRepository.isAwaitingForReset()

        val email = settingsRepository.awaitingEmail()

        val lastScreen = settingsRepository.getLastScreen()

        return when {
            passwordReset -> {
                settingsRepository.clearIsAwaitingForReset()

                val request = link!!.toResetRequest()

                authRepository.confirmPasswordReset(request.code, request.password)

                authRepository.signIn(email = request.email, password = request.password)

                StartUpAction.Main(screen = lastScreen)
            }
            awaitingForReset -> StartUpAction.Reset(email = email)
            activeAuth != null -> {
                val token = identifierRepository.getIdentifier()

                CoroutineScope(backgroundContext).launch {
                    runCatching {
                        databaseRepository.updateToken(uid = activeAuth.uid, token = token)
                    }
                }

                if (authRepository.isEmailVerified())
                    StartUpAction.Main(screen = lastScreen)
                else
                    StartUpAction.Verify(email = activeAuth.email)

            }
            else -> StartUpAction.Login
        }
    }
}