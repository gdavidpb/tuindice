package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.auth.domain.exception.AuthenticationStage
import com.gdavidpb.tuindice.auth.domain.exception.AuthenticationStageException
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.canonicalAttestationPayloadJson
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdatePasswordUseCase(
	private val authRepository: AuthRepository,
	private val sessionRepository: SessionRepository,
	private val syncRepository: SyncRepository,
	private val credentialsRepository: CredentialsRepository,
	private val syncStatusRepository: SyncStatusRepository,
	private val attestationRepository: AttestationRepository,
	override val reportingRepository: ReportingRepository,
	override val paramsValidator: UpdatePasswordParamsValidator,
	override val exceptionHandler: UpdatePasswordExceptionHandler,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : FlowUseCase<String, Unit, SignInUseCaseError>(
	reportingRepository = reportingRepository,
	dispatchers = dispatchers
) {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val usbId = sessionRepository.getUsbId()
		val accessToken = sessionRepository.getAccessToken()
		val flow = AttestedTokenFlow.ReissueTokens
		val attestationPayload = IssueTokensAttestationPayload(
			usbId = usbId,
			password = params,
			attestedFlow = flow.headerValue
		)

		val attestation = runCatching {
			attestationRepository.attest(
				request = AttestationRequest(
					operationCode = flow.operationCode,
					payloadJson = canonicalAttestationPayloadJson(
						serializer = IssueTokensAttestationPayload.serializer(),
						value = attestationPayload
					),
					authorization = AttestationAuthorization.Bearer(
						accessToken = accessToken
					)
				)
			)
		}.getOrElse { throwable ->
			throw AuthenticationStageException(
				stage = AuthenticationStage.UpdatePasswordAttestation,
				cause = throwable
			)
		}

		runCatching {
			authRepository.reissueTokens(
				usbId = usbId,
				password = params,
				attestation = attestation
			)
		}.getOrElse { throwable ->
			throw AuthenticationStageException(
				stage = AuthenticationStage.UpdatePasswordReissue,
				cause = throwable
			)
		}

		credentialsRepository.setPassword(
			password = params
		)

		if (syncStatusRepository.getSyncStatus() == SyncStatus.OutdatedCredentials) {
			syncStatusRepository.setSyncStatus(SyncStatus.Failed)
		}

		syncRepository.scheduleSync(
			password = params
		)

		return flowOf(Unit)
	}
}
