package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import kotlinx.coroutines.Dispatchers

open class ResendVerifyEmailUseCase(
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository
) : CompletableUseCase<Unit>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit) {
        settingsRepository.startCountdown()

        //authRepository.sendEmailVerification()
    }
}