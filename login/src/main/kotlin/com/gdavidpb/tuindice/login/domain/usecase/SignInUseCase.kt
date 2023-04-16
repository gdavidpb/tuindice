package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.login.domain.param.SignInParams
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.utils.SubscriptionTopics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignInUseCase(
	private val authRepository: AuthRepository,
	private val loginRepository: LoginRepository,
	private val messagingRepository: MessagingRepository,
	override val paramsValidator: SignInParamsValidator,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<SignInParams, Unit, SignInError>() {
	override suspend fun executeOnBackground(params: SignInParams): Flow<Unit> {
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

		messagingRepository.subscribeToTopic(topic = SubscriptionTopics.TOPIC_GENERAL)

		return flowOf(Unit)
	}
}