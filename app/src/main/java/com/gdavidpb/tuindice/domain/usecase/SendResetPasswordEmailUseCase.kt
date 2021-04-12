package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SendResetPasswordEmailError
import com.gdavidpb.tuindice.utils.extensions.causes
import com.gdavidpb.tuindice.utils.extensions.isAccountDisabled
import com.gdavidpb.tuindice.utils.extensions.isConnectionIssue

class SendResetPasswordEmailUseCase(
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository,
        private val networkRepository: NetworkRepository
) : CompletableUseCase<Unit, SendResetPasswordEmailError>() {
    override suspend fun executeOnBackground(params: Unit) {
        val email = settingsRepository.getEmail()

        authRepository.sendPasswordResetEmail(email)
    }

    override suspend fun executeOnException(throwable: Throwable): SendResetPasswordEmailError? {
        val causes = throwable.causes()

        return when {
            causes.isAccountDisabled() -> SendResetPasswordEmailError.AccountDisabled
            throwable.isConnectionIssue() -> SendResetPasswordEmailError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }
}