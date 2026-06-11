package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.auth.domain.model.RevokeTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.canonicalAttestationPayloadJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignOutUseCase(
	private val authRepository: AuthRepository,
	private val attestationRepository: AttestationRepository,
	private val sessionRepository: SessionRepository,
	private val sessionInvalidationRepository: SessionInvalidationRepository,
	private val applicationRepository: ApplicationRepository,
	private val syncStatusRepository: SyncStatusRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		val sessionId = sessionRepository.getSessionId()
		val refreshToken = sessionRepository.getRefreshToken()
		val attestationPayload = RevokeTokensAttestationPayload(
			sessionId = sessionId,
			refreshToken = refreshToken
		)

		val attestation = attestationRepository.attest(
			request = AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRevokeTokens,
				payloadJson = canonicalAttestationPayloadJson(
					serializer = RevokeTokensAttestationPayload.serializer(),
					value = attestationPayload
				),
				authorization = AttestationAuthorization.Session(
					sessionId = sessionId,
					refreshToken = refreshToken
				)
			)
		)

		sessionInvalidationRepository.markIntentionalSignOut(sessionId)

		runCatching {
			authRepository.revokeTokens(
				sessionId = sessionId,
				refreshToken = refreshToken,
				attestation = attestation
			)
		}.onFailure { throwable ->
			sessionInvalidationRepository.clearIntentionalSignOut(sessionId)
			throw throwable
		}

		sessionRepository.clear()
		applicationRepository.clearData()
		syncStatusRepository.reset()

		return flowOf(Unit)
	}
}
