package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.request.ResetRequest
import kotlinx.coroutines.Dispatchers

open class ResetUseCase(
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository
) : CompletableUseCase<ResetRequest>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: ResetRequest) {
        settingsRepository.clearIsAwaitingForReset()
        settingsRepository.clearCountdown()

        authRepository.confirmPasswordReset(code = params.code, password = params.password)
    }
}