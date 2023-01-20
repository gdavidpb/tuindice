package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.base.utils.mappers.asUsbId
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.domain.error.SignInError

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class ReSignInUseCase(
	private val authRepository: AuthRepository,
	private val loginRepository: LoginRepository,
	private val networkRepository: NetworkRepository
) : EventUseCase<String, Unit, SignInError>() {
	override suspend fun executeOnBackground(params: String) {
		val activeAuth = authRepository.getActiveAuth()
		val usbId = activeAuth.email.asUsbId()

		val bearerToken = loginRepository.signIn(
			username = usbId,
			password = params,
			refreshToken = true
		).token

		authRepository.signOut()

		authRepository.signIn(token = bearerToken)
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