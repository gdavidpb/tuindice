package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.canonicalAttestationPayloadJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignInUseCase(
	private val authRepository: AuthRepository,
	private val messagingRepository: MessagingRepository,
	private val syncRepository: SyncRepository,
	private val credentialsRepository: CredentialsRepository,
	private val syncStatusRepository: SyncStatusRepository,
	private val attestationRepository: AttestationRepository,
	override val paramsValidator: SignInParamsValidator,
	override val exceptionHandler: SignInExceptionHandler
) : FlowUseCase<SignInParams, Unit, SignInUseCaseError>() {
	override suspend fun executeOnBackground(params: SignInParams): Flow<Unit> {
		val flow = AttestedTokenFlow.IssueTokens
		val attestationPayload = IssueTokensAttestationPayload(
			usbId = params.usbId,
			password = params.password,
			attestedFlow = flow.headerValue
		)

		val attestation = attestationRepository.attest(
			request = AttestationRequest(
				operation = flow.operation,
				payloadJson = canonicalAttestationPayloadJson(
					serializer = IssueTokensAttestationPayload.serializer(),
					value = attestationPayload
				)
			)
		)

		authRepository.issueTokens(
			usbId = params.usbId,
			password = params.password,
			attestedFlow = flow,
			attestation = attestation
		)

		credentialsRepository.setPassword(
			password = params.password
		)

		if (syncStatusRepository.getSyncStatus() == SyncStatus.OutdatedCredentials) {
			syncStatusRepository.setSyncStatus(SyncStatus.Failed)
		}

		syncRepository.scheduleSync(
			password = params.password
		)

		messagingRepository.subscribe()

		return flowOf(Unit)
	}
}
