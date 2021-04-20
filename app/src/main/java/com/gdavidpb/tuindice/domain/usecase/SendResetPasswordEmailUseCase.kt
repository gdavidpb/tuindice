package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SendResetPasswordEmailError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.isConnection
import com.gdavidpb.tuindice.utils.extensions.isTimeout

@Timeout(key = ConfigKeys.TIME_OUT_RESET_PASSWORD)
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
        return when {
            throwable.isTimeout() -> SendResetPasswordEmailError.Timeout
            throwable.isConnection() -> SendResetPasswordEmailError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }
}