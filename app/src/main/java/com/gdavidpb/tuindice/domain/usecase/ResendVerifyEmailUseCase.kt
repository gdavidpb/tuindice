package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase

open class ResendVerifyEmailUseCase(
        private val authRepository: AuthRepository
) : CompletableUseCase<Unit>() {
    override suspend fun executeOnBackground(params: Unit) {
        authRepository.sendVerificationEmail()
    }
}