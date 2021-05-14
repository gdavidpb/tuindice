package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.FunctionsRepository
import com.gdavidpb.tuindice.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.UpdatePasswordError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.isConnection
import com.gdavidpb.tuindice.utils.extensions.isInvalidCredentials
import com.gdavidpb.tuindice.utils.extensions.isTimeout

@Timeout(key = ConfigKeys.TIME_OUT_UPDATE_PASSWORD)
class UpdatePasswordUseCase(
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository,
        private val networkRepository: NetworkRepository,
        private val functionsRepository: FunctionsRepository
) : CompletableUseCase<String, UpdatePasswordError>() {
    override suspend fun executeOnBackground(params: String) {
        val oldCredentials = settingsRepository.getCredentials()
        val newCredentials = oldCredentials.copy(password = params)

        functionsRepository.checkCredentials(credentials = newCredentials)

        authRepository.reSignIn(credentials = oldCredentials)
        authRepository.updatePassword(newPassword = newCredentials.password)

        settingsRepository.storeCredentials(newCredentials)
    }

    override suspend fun executeOnException(throwable: Throwable): UpdatePasswordError? {
        return when {
            throwable.isTimeout() -> UpdatePasswordError.Timeout
            throwable.isInvalidCredentials() -> UpdatePasswordError.InvalidCredentials
            throwable.isConnection() -> UpdatePasswordError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }
}