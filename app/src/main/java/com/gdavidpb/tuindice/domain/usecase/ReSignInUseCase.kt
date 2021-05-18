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
import com.gdavidpb.tuindice.utils.mappers.asUsbId

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class ReSignInUseCase(
        private val databaseRepository: DatabaseRepository,
        private val authRepository: AuthRepository,
        private val networkRepository: NetworkRepository,
        private val functionsRepository: FunctionsRepository
) : EventUseCase<String, Boolean, SignInError>() {
    override suspend fun executeOnBackground(params: String): Boolean {
        val activeAuth = authRepository.getActiveAuth()
        val usbId = activeAuth.email.asUsbId()

        val credentials = Credentials(usbId = usbId, password = params)
        val functionsSignIn = functionsRepository.signIn(credentials = credentials)

        authRepository.signOut()

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