package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.login.domain.model.RefreshTokenAttestationPayload
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdatePasswordUseCase(
	private val authApiRepository: AuthApiRepository,
	private val sessionRepository: SessionRepository,
	private val attestationRepository: AttestationRepository,
	override val paramsValidator: UpdatePasswordParamsValidator,
	override val exceptionHandler: UpdatePasswordExceptionHandler
) : FlowUseCase<String, Unit, SignInUseCaseError>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val usbId = sessionRepository.getUsbId()

		val accessToken = sessionRepository.getAccessToken()
		val refreshToken = sessionRepository.getRefreshToken()

		val attestationPayload = RefreshTokenAttestationPayload(
			accessToken = accessToken,
			refreshToken = refreshToken
		)

		val attestation = attestationRepository.getAttestation(
			payload = attestationPayload
		)

		val tokens = authApiRepository.issueTokens(
			usbId = usbId,
			password = params,
			attestation = attestation
		)

		sessionRepository.setAccessToken(
			accessToken = tokens.accessToken
		)

		sessionRepository.setRefreshToken(
			refreshToken = tokens.refreshToken
		)

		return flowOf(Unit)
	}
}