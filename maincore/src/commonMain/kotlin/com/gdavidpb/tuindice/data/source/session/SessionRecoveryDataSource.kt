package com.gdavidpb.tuindice.data.source.session

import com.gdavidpb.tuindice.auth.domain.model.ExchangeTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.utils.extension.authErrorCode
import com.gdavidpb.tuindice.base.utils.extension.isAccessRejected
import com.gdavidpb.tuindice.base.utils.extension.isSessionSuperseded
import com.gdavidpb.tuindice.domain.repository.SessionRecoveryRepository
import com.gdavidpb.tuindice.security.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.security.utils.canonicalAttestationPayloadJson
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

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

	// Guarded by recoveryMutex. Marks a token that still reported near-expiry after the
	// freshest refresh this device can obtain, so the gateway arbitrates it instead of
	// this data source refreshing again before every request.
	private var expiryArbitratedAccessToken: String? = null

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
					authRepository = authRepository
				)
			} catch (throwable: Throwable) {
				recoverFromRefreshFailure(
					throwable = throwable,
					attemptedSnapshot = attemptedSnapshot,
					credentialsRepository = credentialsRepository,
					authRepository = authRepository
				)
			}

			recoveredSnapshot
		}
	}

	override suspend fun ensureFreshSession(): SessionSnapshot? {
		val activeSnapshot = sessionRepository.getActiveSessionSnapshot()

		if (activeSnapshot == null || !isAccessTokenExpiring(activeSnapshot.accessToken)) {
			return activeSnapshot
		}

		return recoveryMutex.withLock {
			val attemptedSnapshot = sessionRepository.getActiveSessionSnapshot()
				?: return@withLock null

			if (!isAccessTokenExpiring(attemptedSnapshot.accessToken)) return@withLock attemptedSnapshot
			if (attemptedSnapshot.accessToken == expiryArbitratedAccessToken) return@withLock attemptedSnapshot

			val recoveredSnapshot = try {
				try {
					refreshAttemptedSession(
						attemptedSnapshot = attemptedSnapshot,
						authRepository = authRepository
					)
				} catch (throwable: Throwable) {
					recoverFromRefreshFailure(
						throwable = throwable,
						attemptedSnapshot = attemptedSnapshot,
						credentialsRepository = credentialsRepository,
						authRepository = authRepository
					)
				}
			} catch (ignored: Throwable) {
				coroutineContext.ensureActive()
				// A proactive refresh precedes a live request: when the refresh chain fails
				// for non-session reasons (transport, attestation plumbing), keep the stored
				// token and let the reactive 401 path arbitrate instead of failing the request.
				attemptedSnapshot
			}

			if (recoveredSnapshot != null && isAccessTokenExpiring(recoveredSnapshot.accessToken)) {
				expiryArbitratedAccessToken = recoveredSnapshot.accessToken
			}

			recoveredSnapshot
		}
	}

	override suspend fun invalidateSession(sessionId: String?) {
		sessionCoroutineScope.cancelActiveWork()
		runCatching { applicationRepository.clearData() }
		runCatching { sessionRepository.clear() }
		runCatching { syncStatusRepository.reset() }
		runCatching { sessionInvalidationRepository.notifySessionInvalidated(sessionId = sessionId) }
	}

	private suspend fun refreshAttemptedSession(
		attemptedSnapshot: SessionSnapshot,
		authRepository: AuthRepository
	): SessionSnapshot? {
		val attestationPayload = RefreshTokensAttestationPayload(
			sessionId = attemptedSnapshot.sessionId,
			refreshToken = attemptedSnapshot.refreshToken
		)
		val attestation = attestForRecovery(
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
		authRepository: AuthRepository
	): SessionSnapshot? {
		if (!throwable.isAccessRejected()) throw throwable

		val recoveredSnapshot = sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)
			?: attemptCredentialRecovery(
				throwable = throwable,
				attemptedSnapshot = attemptedSnapshot,
				credentialsRepository = credentialsRepository,
				authRepository = authRepository
			)

		return recoveredSnapshot ?: invalidateAttemptedSession(attemptedSnapshot)
	}

	private suspend fun attemptCredentialRecovery(
		throwable: Throwable,
		attemptedSnapshot: SessionSnapshot,
		credentialsRepository: CredentialsRepository,
		authRepository: AuthRepository
	): SessionSnapshot? {
		if (!throwable.shouldAttemptCredentialRecovery()) return null

		return tryBootstrapExchangeAttemptedSession(
			attemptedSnapshot = attemptedSnapshot,
			credentialsRepository = credentialsRepository,
			authRepository = authRepository
		) ?: sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)
	}

	private suspend fun tryBootstrapExchangeAttemptedSession(
		attemptedSnapshot: SessionSnapshot,
		credentialsRepository: CredentialsRepository,
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

			val attestation = attestForRecovery(
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

	private suspend fun attestForRecovery(request: AttestationRequest) = try {
		attestationRepository.attest(request)
	} catch (throwable: Throwable) {
		if (throwable.isAccessRejected() && throwable.authErrorCode() == null) {
			// An unclassified attestation rejection is an integrity-plumbing failure,
			// not a session verdict; surface it without burning the stored session.
			throw SessionRecoveryAttestationException(throwable)
		}

		throw throwable
	}
}

private fun Throwable.shouldAttemptCredentialRecovery(): Boolean {
	return isSessionSuperseded()
}

private class SessionRecoveryAttestationException(
	cause: Throwable
) : Exception("Attestation rejected during session recovery.", cause)

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
