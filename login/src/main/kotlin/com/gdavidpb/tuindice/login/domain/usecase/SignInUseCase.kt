package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.Topics
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.login.domain.error.SignInError
import com.gdavidpb.tuindice.login.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.login.domain.param.SignInParams
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.domain.validator.SignInParamsValidator

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class SignInUseCase(
	private val authRepository: AuthRepository,
	private val loginRepository: LoginRepository,
	private val messagingRepository: MessagingRepository,
	private val reportingRepository: ReportingRepository,
	private val networkRepository: NetworkRepository,
	override val paramsValidator: SignInParamsValidator
) : EventUseCase<SignInParams, Unit, SignInError>() {

	override suspend fun executeOnBackground(params: SignInParams) {
		val isActiveAuth = authRepository.isActiveAuth()

		if (isActiveAuth) authRepository.signOut()

		val messagingToken = messagingRepository.getToken()

		val bearerToken = loginRepository.signIn(
			username = params.usbId,
			password = params.password,
			messagingToken = messagingToken,
			refreshToken = false
		).token

		val authSignIn = authRepository.signIn(token = bearerToken)

		reportingRepository.setIdentifier(identifier = authSignIn.uid)

		messagingRepository.subscribeToTopic(topic = Topics.TOPIC_GENERAL)
	}

	override suspend fun executeOnException(throwable: Throwable): SignInError? {
		return when {
			throwable is SignInIllegalArgumentException -> throwable.error
			throwable.isForbidden() -> SignInError.AccountDisabled
			throwable.isUnavailable() -> SignInError.Unavailable
			throwable.isUnauthorized() -> SignInError.InvalidCredentials
			throwable.isTimeout() -> SignInError.Timeout
			throwable.isConnection() -> SignInError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}