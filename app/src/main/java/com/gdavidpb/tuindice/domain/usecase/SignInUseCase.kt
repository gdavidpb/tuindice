package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.FunctionsRepository
import com.gdavidpb.tuindice.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.*

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class SignInUseCase(
        private val databaseRepository: DatabaseRepository,
        private val authRepository: AuthRepository,
        private val networkRepository: NetworkRepository,
        private val functionsRepository: FunctionsRepository
) : EventUseCase<Credentials, Boolean, SignInError>() {
    override suspend fun executeOnBackground(params: Credentials): Boolean {
        if (authRepository.isActiveAuth()) authRepository.signOut()

        val functionsSignIn = functionsRepository.signIn(credentials = params)
        val authSignIn = authRepository.signIn(token = functionsSignIn.token)

        databaseRepository.cache(uid = authSignIn.uid)

        return databaseRepository.hasCache(uid = authSignIn.uid)
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