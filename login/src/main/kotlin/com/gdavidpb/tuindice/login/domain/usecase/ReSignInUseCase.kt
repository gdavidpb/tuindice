package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.ReSignInExceptionHandler
import com.gdavidpb.tuindice.login.presentation.mapper.asUsbId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ReSignInUseCase(
	private val authRepository: AuthRepository,
	private val loginRepository: LoginRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: ReSignInExceptionHandler
) : FlowUseCase<String, Unit, SignInError>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val activeAuth = authRepository.getActiveAuth()
		val usbId = activeAuth.email.asUsbId()

		val bearerToken = loginRepository.signIn(
			username = usbId,
			password = params,
			refreshToken = true
		).token

		authRepository.signOut()

		authRepository.signIn(token = bearerToken)

		return flowOf(Unit)
	}
}