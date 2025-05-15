package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation.SignInAttestationPayload
import com.gdavidpb.tuindice.login.domain.repository.SignInRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.presentation.mapper.asUsbId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json

class UpdatePasswordUseCase(
	private val authRepository: AuthRepository,
	private val signInRepository: SignInRepository,
	private val attestationRepository: AttestationRepository,
	override val paramsValidator: UpdatePasswordParamsValidator,
	override val exceptionHandler: UpdatePasswordExceptionHandler
) : FlowUseCase<String, Unit, SignInUseCaseError>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val activeAuth = authRepository.getActiveAuth()
		val usbId = activeAuth.email.asUsbId()

		val attestation = SignInAttestationPayload(
			usbId = usbId,
			password = params
		).let { payload ->
			attestationRepository.getAttestation(
				payload = Json.encodeToString(payload)
			)
		}

		val bearerToken = signInRepository.auth(
			username = usbId,
			password = params,
			attestation = attestation
		)

		authRepository.revoke()

		authRepository.auth(token = bearerToken)

		return flowOf(Unit)
	}
}