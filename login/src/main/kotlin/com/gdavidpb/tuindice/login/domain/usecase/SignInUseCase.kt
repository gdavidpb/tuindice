package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.Topics
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.login.domain.model.SignInRequest
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class SignInUseCase(
	private val authRepository: AuthRepository,
	private val loginRepository: LoginRepository,
	private val settingsRepository: SettingsRepository,
	private val messagingRepository: MessagingRepository,
	private val reportingRepository: ReportingRepository,
	private val networkRepository: NetworkRepository
) : EventUseCase<SignInRequest, Unit, SignInError>() {

	private object IllegalArguments {
		const val EmptyUsbId = "Empty usbId"
		const val InvalidUsbId = "Invalid usbId"
		const val EmptyPassword = "Empty password"
	}

	override suspend fun executeOnBackground(params: SignInRequest) {
		require(params.usbId.isNotEmpty()) { IllegalArguments.EmptyUsbId }
		require(params.usbId.isUsbId()) { IllegalArguments.InvalidUsbId }
		require(params.password.isNotEmpty()) { IllegalArguments.EmptyPassword }

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

		if (!settingsRepository.isSubscribedToTopic(Topics.TOPIC_GENERAL)) {
			messagingRepository.subscribeToTopic(Topics.TOPIC_GENERAL)
			settingsRepository.saveSubscriptionTopic(Topics.TOPIC_GENERAL)
		}
	}

	override suspend fun executeOnException(throwable: Throwable): SignInError? {
		return when {
			throwable.isIllegalArgument(IllegalArguments.EmptyUsbId) -> SignInError.EmptyUsbId
			throwable.isIllegalArgument(IllegalArguments.InvalidUsbId) -> SignInError.InvalidUsbId
			throwable.isIllegalArgument(IllegalArguments.EmptyPassword) -> SignInError.EmptyPassword
			throwable.isForbidden() -> SignInError.AccountDisabled
			throwable.isUnavailable() -> SignInError.Unavailable
			throwable.isUnauthorized() -> SignInError.InvalidCredentials
			throwable.isTimeout() -> SignInError.Timeout
			throwable.isConnection() -> SignInError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}