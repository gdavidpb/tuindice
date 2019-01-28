package com.gdavidpb.tuindice.domain.usecase

import android.content.Intent
import com.gdavidpb.tuindice.data.mapper.ResetMapper
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import kotlinx.coroutines.Dispatchers

open class StartUpUseCase(
        private val localDatabaseRepository: LocalDatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val resetMapper: ResetMapper
) : ResultUseCase<Intent, StartUpAction>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Intent): StartUpAction? {
        val link = params.dataString

        val activeAccount = localDatabaseRepository.getActiveAccount()
        val passwordReset = authRepository.isResetLink(link)
        val verifyEmail = authRepository.isVerifyLink(link)
        val awaitingForReset = settingsRepository.isAwaitingForReset()
        val awaitingForVerify = settingsRepository.isAwaitingForVerify()

        val email = settingsRepository.awaitingEmail()
        val requireVerify = authRepository.isSignedIn() && !authRepository.isEmailVerified()

        return when {
            awaitingForVerify || requireVerify -> StartUpAction.AwaitingForVerify(email = email)
            awaitingForReset -> StartUpAction.AwaitingForReset(email = email)
            activeAccount != null -> StartUpAction.Main(account = activeAccount)
            passwordReset -> StartUpAction.Reset(request = link.let(resetMapper::map))
            verifyEmail -> StartUpAction.Verified
            else -> StartUpAction.Login
        }
    }
}