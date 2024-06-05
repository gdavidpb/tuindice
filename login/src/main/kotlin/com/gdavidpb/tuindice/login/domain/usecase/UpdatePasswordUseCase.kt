package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation.SignInAttestationPayload
import com.gdavidpb.tuindice.login.domain.repository.SignInRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.presentation.mapper.asUsbId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdatePasswordUseCase(
	private val authRepository: AuthRepository,
	private val signInRepository: SignInRepository,
	private val attestationRepository: AttestationRepository,
	override val paramsValidator: UpdatePasswordParamsValidator,
	override val exceptionHandler: UpdatePasswordExceptionHandler
) : FlowUseCase<String, Unit, SignInError>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val activeAuth = authRepository.getActiveAuth()
		val usbId = activeAuth.email.asUsbId()

		val attestationPayload = SignInAttestationPayload(
			usbId = usbId,
			password = params
		)

		val attestationToken = attestationRepository.getToken(
			operation = "sign-in",
			payload = attestationPayload
		)

		val bearerToken = signInRepository.signIn(
			username = usbId,
			password = params,
			attestation = attestationToken
		).token

		authRepository.signOut()

		authRepository.signIn(token = bearerToken)

		return flowOf(Unit)
	}
}