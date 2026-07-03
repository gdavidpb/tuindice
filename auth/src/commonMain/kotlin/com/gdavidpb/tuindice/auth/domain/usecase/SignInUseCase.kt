package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.auth.domain.exception.AuthenticationStage
import com.gdavidpb.tuindice.auth.domain.exception.AuthenticationStageException
import com.gdavidpb.tuindice.auth.domain.model.ExchangeTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.utils.extension.toCanonicalUsbIdentifier
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.security.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.security.utils.canonicalAttestationPayloadJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignInUseCase(
	private val authRepository: AuthRepository,
	private val messagingRepository: MessagingRepository,
	private val syncRepository: SyncRepository,
	private val credentialsRepository: CredentialsRepository,
	private val syncStatusRepository: SyncStatusRepository,
	private val attestationRepository: AttestationRepository,
	override val reportingRepository: ReportingRepository,
	override val paramsValidator: SignInParamsValidator,
	override val exceptionHandler: SignInExceptionHandler
) : FlowUseCase<SignInParams, Unit, SignInUseCaseError>() {
	override suspend fun executeOnBackground(params: SignInParams): Flow<Unit> {
		val canonicalUsbId = params.usbId.toCanonicalUsbIdentifier()
		val bootstrapTokens = runCatching {
			authRepository.bootstrapSignIn(
				usbId = canonicalUsbId,
				password = params.password
			)
		}.getOrElse { throwable ->
			throw AuthenticationStageException(
				stage = AuthenticationStage.SignInBootstrap,
				cause = throwable
			)
		}

		val attestation = runCatching {
			attestationRepository.attest(
				request = AttestationRequest(
					operationCode = ProtectedOperationCodes.AuthExchange,
					payloadJson = canonicalAttestationPayloadJson(
						serializer = ExchangeTokensAttestationPayload.serializer(),
						value = ExchangeTokensAttestationPayload
					),
					authorization = AttestationAuthorization.Bearer(
						accessToken = bootstrapTokens.accessToken
					)
				)
			)
		}.getOrElse { throwable ->
			throw AuthenticationStageException(
				stage = AuthenticationStage.SignInAttestation,
				cause = throwable
			)
		}

		runCatching {
			authRepository.exchangeSignIn(
				bootstrapAccessToken = bootstrapTokens.accessToken,
				attestation = attestation
			)
		}.getOrElse { throwable ->
			throw AuthenticationStageException(
				stage = AuthenticationStage.SignInExchange,
				cause = throwable
			)
		}

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
