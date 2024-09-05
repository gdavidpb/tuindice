package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation.SignInAttestationPayload
import com.gdavidpb.tuindice.login.domain.repository.SignInRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SignInUseCase(
	private val authRepository: AuthRepository,
	private val signInRepository: SignInRepository,
	private val attestationRepository: AttestationRepository,
	private val messagingRepository: MessagingRepository,
	private val reportingRepository: ReportingRepository,
	override val paramsValidator: SignInParamsValidator,
	override val exceptionHandler: SignInExceptionHandler
) : FlowUseCase<SignInParams, Unit, SignInUseCaseError>() {
	override suspend fun executeOnBackground(params: SignInParams): Flow<Unit> {
		val isActiveAuth = authRepository.isActiveAuth()

		if (isActiveAuth) authRepository.signOut()

		val attestationToken = SignInAttestationPayload(
			usbId = params.usbId,
			password = params.password
		).let { payload ->
			attestationRepository.getToken(
				payload = Json.encodeToString(payload)
			)
		}

		val bearerToken = signInRepository.signIn(
			username = params.usbId,
			password = params.password,
			attestation = attestationToken
		).token

		val authSignIn = authRepository.signIn(token = bearerToken)

		reportingRepository.setIdentifier(identifier = authSignIn.uid)

		messagingRepository.subscribe()

		return flowOf(Unit)
	}
}