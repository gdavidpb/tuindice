package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.login.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.presentation.mapper.asUsbId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignInUseCase(
	private val sessionRepository: SessionRepository,
	private val authApiRepository: AuthApiRepository,
	private val messagingApiRepository: MessagingApiRepository,
	private val reportingRepository: ReportingRepository,
	private val messagingRepository: MessagingRepository,
	override val paramsValidator: SignInParamsValidator,
	override val exceptionHandler: SignInExceptionHandler
) : FlowUseCase<SignInParams, Unit, SignInUseCaseError>() {
	override suspend fun executeOnBackground(params: SignInParams): Flow<Unit> {
		val tokens = authApiRepository.issueTokens(
			usbId = params.usbId,
			password = params.password
		)

		sessionRepository.setUsbId(
			usbId = tokens.email.asUsbId()
		)

		sessionRepository.setAccessToken(
			accessToken = tokens.accessToken
		)

		sessionRepository.setRefreshToken(
			refreshToken = tokens.refreshToken
		)

		reportingRepository.setIdentifier(
			id = tokens.uid
		)

		val messagingToken = messagingRepository.getToken()

		messagingApiRepository.subscribe(
			token = messagingToken
		)

		return flowOf(Unit)
	}
}