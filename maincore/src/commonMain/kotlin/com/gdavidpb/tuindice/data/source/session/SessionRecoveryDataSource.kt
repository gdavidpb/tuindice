package com.gdavidpb.tuindice.data.source.session

import com.gdavidpb.tuindice.auth.domain.model.ExchangeTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.utils.canonicalAttestationPayloadJson
import com.gdavidpb.tuindice.base.utils.extension.isAccessRejected
import com.gdavidpb.tuindice.base.utils.extension.isSessionSuperseded
import com.gdavidpb.tuindice.domain.repository.SessionRecoveryRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionRecoveryDataSource(
	private val sessionRepository: SessionRepository,
	private val applicationRepository: ApplicationRepository,
	private val sessionInvalidationRepository: SessionInvalidationRepository,
	private val syncStatusRepository: SyncStatusRepository,
	private val attestationRepository: AttestationRepository,
	private val authRepository: AuthRepository,
	private val credentialsRepository: CredentialsRepository,
	private val sessionCoroutineScope: SessionCoroutineScope
) : SessionRecoveryRepository {
	private val recoveryMutex = Mutex()

	override suspend fun recoverUnauthorizedSession(
		attemptedAuthorizationAccessToken: String?,
		attemptedCachedAccessToken: String?,
		attemptedCachedRefreshToken: String?
	): SessionSnapshot? {
		val callbackSnapshot = sessionRepository.getActiveSessionSnapshot() ?: return null

		if (
			isStaleTokenAttempt(
				attemptedAuthorizationAccessToken = attemptedAuthorizationAccessToken,
				attemptedCachedAccessToken = attemptedCachedAccessToken,
				attemptedCachedRefreshToken = attemptedCachedRefreshToken,
				snapshot = callbackSnapshot
			)
		) {
			return callbackSnapshot
		}

		return recoveryMutex.withLock {
			val attemptedSnapshot = sessionRepository.getActiveSessionSnapshot() ?: return@withLock null

			if (
				isStaleTokenAttempt(
					attemptedAuthorizationAccessToken = attemptedAuthorizationAccessToken,
					attemptedCachedAccessToken = attemptedCachedAccessToken,
					attemptedCachedRefreshToken = attemptedCachedRefreshToken,
					snapshot = attemptedSnapshot
				)
			) {
				return@withLock attemptedSnapshot
			}

			val recoveredSnapshot = try {
				refreshAttemptedSession(
					attemptedSnapshot = attemptedSnapshot,
					attestationRepository = attestationRepository,
					authRepository = authRepository
				)
			} catch (throwable: Throwable) {
				recoverFromRefreshFailure(
					throwable = throwable,
					attemptedSnapshot = attemptedSnapshot,
					credentialsRepository = credentialsRepository,
					attestationRepository = attestationRepository,
					authRepository = authRepository
				)
			}

			recoveredSnapshot
		}
	}

	override suspend fun invalidateSession(sessionId: String?) {
		sessionCoroutineScope.cancelActiveWork()
		runCatching { sessionRepository.clear() }
		runCatching { syncStatusRepository.reset() }
		runCatching { applicationRepository.clearData() }
		runCatching { sessionInvalidationRepository.notifySessionInvalidated(sessionId = sessionId) }
	}

	private suspend fun refreshAttemptedSession(
		attemptedSnapshot: SessionSnapshot,
		attestationRepository: AttestationRepository,
		authRepository: AuthRepository
	): SessionSnapshot? {
		val attestationPayload = RefreshTokensAttestationPayload(
			sessionId = attemptedSnapshot.sessionId,
			refreshToken = attemptedSnapshot.refreshToken
		)
		val attestation = attestationRepository.attest(
			request = AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = canonicalAttestationPayloadJson(
					serializer = RefreshTokensAttestationPayload.serializer(),
					value = attestationPayload
				),
				authorization = AttestationAuthorization.Session(
					sessionId = attemptedSnapshot.sessionId,
					refreshToken = attemptedSnapshot.refreshToken
				)
			)
		)
		val refreshedTokens = authRepository.refreshTokens(
			sessionId = attemptedSnapshot.sessionId,
			refreshToken = attemptedSnapshot.refreshToken,
			attestation = attestation
		)

		return resolveRecoveredSnapshot(
			attemptedSnapshot = attemptedSnapshot,
			recoveredSnapshot = refreshedTokens.toSessionSnapshot(
				usbId = attemptedSnapshot.usbId
			)
		)
	}

	private suspend fun recoverFromRefreshFailure(
		throwable: Throwable,
		attemptedSnapshot: SessionSnapshot,
		credentialsRepository: CredentialsRepository,
		attestationRepository: AttestationRepository,
		authRepository: AuthRepository
	): SessionSnapshot? {
		if (!throwable.isAccessRejected()) throw throwable

		sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)?.let { snapshot ->
			return snapshot
		}

		if (throwable.shouldAttemptCredentialRecovery()) {
			tryBootstrapExchangeAttemptedSession(
				attemptedSnapshot = attemptedSnapshot,
				credentialsRepository = credentialsRepository,
				attestationRepository = attestationRepository,
				authRepository = authRepository
			)?.let { snapshot ->
				return snapshot
			}

			sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)?.let { snapshot ->
				return snapshot
			}
		}

		return invalidateAttemptedSession(attemptedSnapshot)
	}

	private suspend fun tryBootstrapExchangeAttemptedSession(
		attemptedSnapshot: SessionSnapshot,
		credentialsRepository: CredentialsRepository,
		attestationRepository: AttestationRepository,
		authRepository: AuthRepository
	): SessionSnapshot? {
		if (!credentialsRepository.hasPassword()) return null

		return runCatching {
			sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)?.let { snapshot ->
				return@runCatching snapshot
			}

			val password = credentialsRepository.getPassword()
			val bootstrapTokens = authRepository.bootstrapSignIn(
				usbId = attemptedSnapshot.usbId,
				password = password
			)

			sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)?.let { snapshot ->
				return@runCatching snapshot
			}

			val attestation = attestationRepository.attest(
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

			sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)?.let { snapshot ->
				return@runCatching snapshot
			}

			authRepository.exchangeSignIn(
				bootstrapAccessToken = bootstrapTokens.accessToken,
				attestation = attestation
			)

			sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)
		}.getOrElse { recoveryFailure ->
			if (recoveryFailure.isAccessRejected()) null else throw recoveryFailure
		}
	}

	private suspend fun resolveRecoveredSnapshot(
		attemptedSnapshot: SessionSnapshot,
		recoveredSnapshot: SessionSnapshot
	): SessionSnapshot? {
		val currentSnapshot = sessionRepository.getActiveSessionSnapshot()

		if (currentSnapshot == recoveredSnapshot) return currentSnapshot
		if (currentSnapshot != null && currentSnapshot != attemptedSnapshot) return currentSnapshot

		if (
			sessionRepository.replaceSessionSnapshotIfCurrent(
				expectedSnapshot = attemptedSnapshot,
				newSnapshot = recoveredSnapshot
			)
		) {
			return recoveredSnapshot
		}

		return sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)
	}

	private suspend fun invalidateAttemptedSession(
		attemptedSnapshot: SessionSnapshot
	): SessionSnapshot? {
		sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)?.let { snapshot ->
			return snapshot
		}

		invalidateSession(sessionId = attemptedSnapshot.sessionId)
		return null
	}

	private fun Throwable.shouldAttemptCredentialRecovery(): Boolean {
		return isSessionSuperseded()
	}
}

private fun isStaleTokenAttempt(
	attemptedAuthorizationAccessToken: String?,
	attemptedCachedAccessToken: String?,
	attemptedCachedRefreshToken: String?,
	snapshot: SessionSnapshot
): Boolean {
	if (attemptedAuthorizationAccessToken != null) {
		return attemptedAuthorizationAccessToken != snapshot.accessToken
	}

	return attemptedCachedAccessToken != null &&
			attemptedCachedRefreshToken != null &&
			(
				attemptedCachedAccessToken != snapshot.accessToken ||
						attemptedCachedRefreshToken != snapshot.refreshToken
			)
}

private fun RefreshTokens.toSessionSnapshot(usbId: String): SessionSnapshot {
	return SessionSnapshot(
		sessionId = sessionId,
		accessToken = accessToken,
		refreshToken = refreshToken,
		usbId = usbId
	)
}

private suspend fun SessionRepository.getSessionChangedSnapshot(
	attemptedSnapshot: SessionSnapshot
): SessionSnapshot? {
	val currentSnapshot = getActiveSessionSnapshot() ?: return null

	return if (currentSnapshot != attemptedSnapshot) {
		currentSnapshot
	} else {
		null
	}
}
