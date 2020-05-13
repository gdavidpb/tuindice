package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase

open class ResendResetEmailUseCase(
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository
) : CompletableUseCase<Unit>() {
    override suspend fun executeOnBackground(params: Unit) {
        val email = settingsRepository.awaitingEmail()
        val password = settingsRepository.awaitingPassword()

        authRepository.sendPasswordResetEmail(email = email, password = password)
    }
}