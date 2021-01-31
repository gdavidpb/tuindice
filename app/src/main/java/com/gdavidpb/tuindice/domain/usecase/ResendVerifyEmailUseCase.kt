package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SendVerificationEmailError
import com.gdavidpb.tuindice.utils.extensions.causes
import com.gdavidpb.tuindice.utils.extensions.isAccountDisabled
import com.gdavidpb.tuindice.utils.extensions.isConnectionIssue

open class ResendVerifyEmailUseCase(
        private val authRepository: AuthRepository
) : CompletableUseCase<Unit, SendVerificationEmailError>() {
    override suspend fun executeOnBackground(params: Unit) {
        authRepository.sendVerificationEmail()
    }

    override suspend fun executeOnException(throwable: Throwable): SendVerificationEmailError? {
        val causes = throwable.causes()

        return when {
            causes.isAccountDisabled() -> SendVerificationEmailError.AccountDisabled
            throwable.isConnectionIssue() -> SendVerificationEmailError.NoConnection
            else -> null
        }
    }
}