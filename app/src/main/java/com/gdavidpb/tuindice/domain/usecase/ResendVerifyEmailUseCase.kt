package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase

open class ResendVerifyEmailUseCase(
        private val authRepository: AuthRepository
) : CompletableUseCase<Unit, Any>() {
    override suspend fun executeOnBackground(params: Unit) {
        authRepository.sendVerificationEmail()
    }

    override suspend fun executeOnException(throwable: Throwable): Any? {
        TODO("Not yet implemented")
    }
}