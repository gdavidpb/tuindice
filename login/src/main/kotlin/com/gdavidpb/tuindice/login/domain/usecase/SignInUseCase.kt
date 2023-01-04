package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.login.domain.model.SignInRequest
import com.gdavidpb.tuindice.base.utils.Topics
import com.gdavidpb.tuindice.login.domain.repository.RemoteRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError

@Timeout(key = ConfigKeys.TIME_OUT_SIGN_IN)
class SignInUseCase(
	private val authRepository: AuthRepository,
	private val remoteRepository: RemoteRepository,
	private val settingsRepository: SettingsRepository,
	private val messagingRepository: MessagingRepository,
	private val reportingRepository: ReportingRepository,
	private val networkRepository: NetworkRepository
) : EventUseCase<SignInRequest, Unit, SignInError>() {
	override suspend fun executeOnBackground(params: SignInRequest) {
		val isActiveAuth = authRepository.isActiveAuth()

		if (isActiveAuth) authRepository.signOut()

		val messagingToken = messagingRepository.getToken()

		val bearerToken = remoteRepository.signIn(
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
			throwable.isForbidden() -> SignInError.AccountDisabled
			throwable.isUnavailable() -> SignInError.Unavailable
			throwable.isUnauthorized() -> SignInError.InvalidCredentials
			throwable.isTimeout() -> SignInError.Timeout
			throwable.isConnection() -> SignInError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}