package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DstRepository
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
        private val dstRepository: DstRepository,
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository,
        private val networkRepository: NetworkRepository
) : CompletableUseCase<String, UpdatePasswordError>() {
    override suspend fun executeOnBackground(params: String) {
        val oldCredentials = settingsRepository.getCredentials()
        val newCredentials = oldCredentials.copy(password = params)

        newCredentials.auth(serviceUrl = BuildConfig.ENDPOINT_DST_SECURE_AUTH)

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

    private suspend fun Credentials.auth(serviceUrl: String): DstAuth {
        val credentials = DstCredentials(
                usbId = usbId,
                password = password,
                serviceUrl = serviceUrl
        )

        return dstRepository.signIn(credentials)
    }
}