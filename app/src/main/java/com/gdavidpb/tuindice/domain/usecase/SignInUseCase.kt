package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.SignInRequest
import com.gdavidpb.tuindice.domain.repository.ApiRepository
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.*
import okhttp3.Credentials

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class SignInUseCase(
    private val databaseRepository: DatabaseRepository,
    private val authRepository: AuthRepository,
    private val networkRepository: NetworkRepository,
    private val apiRepository: ApiRepository
) : EventUseCase<SignInRequest, Boolean, SignInError>() {
    override suspend fun executeOnBackground(params: SignInRequest): Boolean {
        if (authRepository.isActiveAuth()) authRepository.signOut()

        val basicToken = Credentials.basic(
            username = params.usbId,
            password = params.password
        )

        val bearerToken = apiRepository.signIn(
            basicToken = basicToken,
            refreshToken = false
        ).token

        val authSignIn = authRepository.signIn(token = bearerToken)

        databaseRepository.cache(uid = authSignIn.uid)

        return databaseRepository.hasCache(uid = authSignIn.uid)
    }

    override suspend fun executeOnException(throwable: Throwable): SignInError? {
        return when {
            throwable.isUnavailable() -> SignInError.Unavailable
            throwable.isUnauthorized() -> SignInError.InvalidCredentials
            throwable.isForbidden() -> SignInError.AccountDisabled
            throwable.isTimeout() -> SignInError.Timeout
            throwable.isConnection() -> SignInError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }
}