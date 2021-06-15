package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.ApiRepository
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.asUsbId
import okhttp3.Credentials

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class ReSignInUseCase(
    private val authRepository: AuthRepository,
    private val networkRepository: NetworkRepository,
    private val apiRepository: ApiRepository
) : EventUseCase<String, Boolean, SignInError>() {
    override suspend fun executeOnBackground(params: String): Boolean {
        val activeAuth = authRepository.getActiveAuth()
        val usbId = activeAuth.email.asUsbId()

        val basicToken = Credentials.basic(
            username = usbId,
            password = params
        )

        val bearerToken = apiRepository.signIn(
            basicToken = basicToken,
            refreshToken = true
        ).token

        authRepository.signOut()

        authRepository.signIn(token = bearerToken)

        return false
    }

    override suspend fun executeOnException(throwable: Throwable): SignInError? {
        return when {
            throwable.isForbidden() -> SignInError.AccountDisabled
            throwable.isUnauthorized() -> SignInError.InvalidCredentials
            throwable.isUnavailable() -> SignInError.Unavailable
            throwable.isTimeout() -> SignInError.Timeout
            throwable.isConnection() -> SignInError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }
}