package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.base.domain.model.SignInRequest
import com.gdavidpb.tuindice.base.utils.mappers.asUsbId
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import okhttp3.Credentials

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class SignInUseCase(
	private val databaseRepository: DatabaseRepository,
	private val reportingRepository: ReportingRepository,
	private val authRepository: AuthRepository,
	private val networkRepository: NetworkRepository,
	private val serviceRepository: ServicesRepository
) : EventUseCase<SignInRequest, Boolean, SignInError>() {
	override suspend fun executeOnBackground(params: SignInRequest): Boolean {
		val isActiveAuth = authRepository.isActiveAuth()

		val usbId = if (isActiveAuth)
			authRepository.getActiveAuth().email.asUsbId()
		else
			params.usbId

		if (isActiveAuth) authRepository.signOut()

		val basicToken = Credentials.basic(
			username = usbId,
			password = params.password
		)

		val bearerToken = serviceRepository.signIn(
			basicToken = basicToken,
			refreshToken = false
		).token

		val authSignIn = authRepository.signIn(token = bearerToken)

		reportingRepository.setIdentifier(identifier = authSignIn.uid)

		databaseRepository.cache(uid = authSignIn.uid)

		return databaseRepository.hasCache(uid = authSignIn.uid)
	}

	override suspend fun executeOnException(throwable: Throwable): SignInError? {
		return when {
			throwable.isForbidden() -> SignInError.AccountDisabled
			throwable.isUnavailable() -> SignInError.Unavailable
			throwable.isUnauthorized() -> SignInError.InvalidCredentials
			throwable.isTimeout() -> SignInError.Timeout
			throwable.isConnection() -> SignInError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}