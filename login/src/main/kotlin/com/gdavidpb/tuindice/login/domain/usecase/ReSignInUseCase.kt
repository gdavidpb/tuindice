package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ServicesRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.base.utils.mappers.asUsbId
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import okhttp3.Credentials

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class ReSignInUseCase(
	private val authRepository: AuthRepository,
	private val networkRepository: NetworkRepository,
	private val apiRepository: ServicesRepository
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