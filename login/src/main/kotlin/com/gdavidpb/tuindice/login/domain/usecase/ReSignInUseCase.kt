package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.login.domain.error.SignInError
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.presentation.mapper.asUsbId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ReSignInUseCase(
	private val authRepository: AuthRepository,
	private val loginRepository: LoginRepository,
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
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