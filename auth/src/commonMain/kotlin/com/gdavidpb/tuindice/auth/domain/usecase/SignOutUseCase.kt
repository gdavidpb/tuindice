package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.auth.domain.model.RevokeTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.coroutine.AppCoroutineScope
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.runStep
import com.gdavidpb.tuindice.security.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.security.utils.canonicalAttestationPayloadJson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignOutUseCase(
	private val authRepository: AuthRepository,
	private val attestationRepository: AttestationRepository,
	private val sessionRepository: SessionRepository,
	private val sessionInvalidationRepository: SessionInvalidationRepository,
	private val applicationRepository: ApplicationRepository,
	private val syncStatusRepository: SyncStatusRepository,
	private val sessionCoroutineScope: SessionCoroutineScope,
	private val appCoroutineScope: AppCoroutineScope,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
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

		// The sign-out belongs to the dialog that asked for it, and the dialog can be cleared at any moment
		// (the activity of the app destroyed with the process alive). The server has revoked the session by
		// now, so the local end of it cannot depend on the caller: it runs in the scope of the app, the
		// caller only waits for it, and one that is cancelled stops waiting while the job goes on. The mark
		// of the intentional sign-out is withdrawn last, when the session is gone: left behind, it would
		// silence the next invalidation, which is not this one's.
		// The steps all run whatever happens to one of them, and the first failure is the caller's to know.
		val failure = appCoroutineScope.async {
			val failures = listOf(
				runStep { sessionCoroutineScope.cancelActiveWork() },
				runStep { applicationRepository.clearData() },
				runStep { sessionRepository.clear() },
				runStep { syncStatusRepository.reset() }
			)

			sessionInvalidationRepository.clearIntentionalSignOut(sessionId)

			failures.firstOrNull { step -> step != null && step !is CancellationException }
		}.await()

		failure?.let { throw it }

		return flowOf(Unit)
	}
}
