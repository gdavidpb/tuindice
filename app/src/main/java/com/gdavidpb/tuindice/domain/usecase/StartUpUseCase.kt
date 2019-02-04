package com.gdavidpb.tuindice.domain.usecase

import android.content.Intent
import com.gdavidpb.tuindice.data.mapper.ResetMapper
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import kotlinx.coroutines.Dispatchers

open class StartUpUseCase(
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val resetMapper: ResetMapper
) : ResultUseCase<Intent, StartUpAction>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Intent): StartUpAction? {
        val link = params.dataString

        val activeAccount = authRepository.getActiveAccount()
        val passwordReset = authRepository.isResetLink(link)
        val awaitingForReset = settingsRepository.isAwaitingForReset()

        val email = settingsRepository.awaitingEmail()

        return when {
            passwordReset -> {
                settingsRepository.clearIsAwaitingForReset()

                val request = link.let(resetMapper::map)

                authRepository.confirmPasswordReset(request.code, request.password)

                val account = authRepository.signIn(email = request.email, password = request.password)

                StartUpAction.Main(account = account)
            }
            awaitingForReset -> StartUpAction.Reset(email = email)
            activeAccount != null -> {
                databaseRepository.setToken()

                if (authRepository.isEmailVerified())
                    StartUpAction.Main(account = activeAccount)
                else
                    StartUpAction.Verify(email = activeAccount.email)

            }
            else -> StartUpAction.Login
        }
    }
}