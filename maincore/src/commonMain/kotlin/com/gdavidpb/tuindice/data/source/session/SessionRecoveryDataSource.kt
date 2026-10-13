package com.gdavidpb.tuindice.data.source.session

import com.gdavidpb.tuindice.auth.domain.model.ExchangeTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokensAttestationPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.coroutine.AppCoroutineScope
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.exception.SessionRecoveryAttestationException
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionEndRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.session.announceInvalidation
import com.gdavidpb.tuindice.base.utils.extension.attempt
import com.gdavidpb.tuindice.base.utils.extension.authErrorCode
import com.gdavidpb.tuindice.base.utils.extension.isAccessRejected
import com.gdavidpb.tuindice.base.utils.extension.isSessionSuperseded
import com.gdavidpb.tuindice.base.utils.extension.runStep
import com.gdavidpb.tuindice.domain.model.SessionInvalidationReason
import com.gdavidpb.tuindice.domain.repository.SessionRecoveryRepository
import com.gdavidpb.tuindice.security.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.security.utils.canonicalAttestationPayloadJson
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
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
	private val settingsRepository: SettingsRepository,
	private val reportingRepository: ReportingRepository,
	private val sessionCoroutineScope: SessionCoroutineScope,
	private val appCoroutineScope: AppCoroutineScope
) : SessionRecoveryRepository, SessionEndRepository, SessionMemory {
	private val recoveryMutex = Mutex()

	// Taken by whatever ends the session locally (an invalidation, a sign-out), one at a time. What runs
	// under it (clearSessionMemory included, through the wipe) is local work only: it cannot make an
	// authenticated request nor ask for another invalidation, because that would wait for this same lock
	// forever.
	private val invalidationMutex = Mutex()
	private val invalidationsCompleted = MutableStateFlow(0)

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
				val refreshedSnapshot = try {
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

				// Only a completed refresh attempt can prove the new token is still
				// inside the margin (clock skew, a short-lived issuance); latching here
				// is what makes that verdict sticky for this exact token.
				if (refreshedSnapshot != null && isAccessTokenExpiring(refreshedSnapshot.accessToken)) {
					expiryArbitratedAccessToken = refreshedSnapshot.accessToken
				}

				refreshedSnapshot
			} catch (ignored: Throwable) {
				coroutineContext.ensureActive()
				// A proactive refresh precedes a live request: when the refresh chain fails
				// for non-session reasons (transport, attestation plumbing), keep the stored
				// token and let the reactive 401 path arbitrate instead of failing the request.
				// Not latched: a transient failure should not permanently disable proactive
				// refresh for this session — retry on the next request.
				attemptedSnapshot
			}

			recoveredSnapshot
		}
	}

	// The invalidation belongs to the app, not to whoever asked for it. The wipe empties the stores the
	// session is read from, so from its first step on the session is gone: a wipe cut short by the
	// cancellation of the caller (a screen that closed, a request of the session scope that was
	// cancelled) would leave the user inside the app with no session, no data and nobody told. It runs
	// in the scope of the app instead, and the caller only waits for it: a caller that is cancelled
	// stops waiting and the wipe goes on to its end. Nobody cancels the job, so it did not start, or it
	// ran every step: each one is skipped if it fails, and only the cancellation of the job itself
	// (the app scope closing) stops it. The notice, the signal and the counter close it.
	//
	// One invalidation at a time, and a call that arrives while another is in flight does not start a
	// second wipe. It waits for the one in progress and ends there, because that wipe is the one that
	// serves it: both callers want the session gone and the notice emitted once. A caller outside the
	// session scope gets its normal return when the session is gone. One inside it is cancelled with
	// the rest of the scope's work, and unwinds as a cancellation without writing anything afterwards.
	// This lock is taken by whatever ends the session locally (an invalidation, the local end of a
	// sign-out): no request holds it, and the recovery lock is never needed to take it.
	override suspend fun invalidateSession(
		sessionId: String?,
		reason: SessionInvalidationReason?,
		cause: Throwable?
	) {
		// Read here, from the caller: the job below runs outside of every scope it could be asked from.
		val insideTheScope = sessionCoroutineScope.callerBranch() != null
		val invalidationsSeen = invalidationsCompleted.value

		appCoroutineScope.async {
			invalidationMutex.withLock {
				// The wipe this call waited for is done: it is the one that serves this call.
				if (invalidationsCompleted.value != invalidationsSeen) return@withLock

				// The caller's branch is cancelled when the wipe starts and frees the recovery lock with the
				// session still readable, so a request that was waiting for it can start its own invalidation
				// of the same session. If that session is not the stored one any more, a wipe already ended
				// it (or a new sign-in replaced it): there is nothing left to end. A stored session that
				// cannot be read does not excuse the wipe.
				val stored = attempt { sessionRepository.getActiveSessionSnapshot() }
				if (sessionId != null && stored.isSuccess && stored.getOrNull()?.sessionId != sessionId) {
					return@withLock
				}

				// Everything the session still runs, the caller's branch included: it waits for this job in
				// a suspension that can be cancelled, so it ends here and does not wait for itself.
				runStep { sessionCoroutineScope.cancelActiveWork() }

				// No caller cancels this job, so no step is cut short by one; a step that fails does not keep
				// the next ones from running, because each one empties something different.
				runStep { applicationRepository.clearData() }
				runStep { sessionRepository.clear() }
				runStep { syncStatusRepository.reset() }
				sessionInvalidationRepository.announceInvalidation(
					settingsRepository = settingsRepository,
					reason = SessionResetNoticeReason.ServerEndedSession,
					sessionId = sessionId
				)
				reportInvalidation(reason = reason, cause = cause, insideTheScope = insideTheScope)

				// The wipe ran to its end, so it counts as the one that serves the callers waiting for it.
				invalidationsCompleted.value += 1
			}
		}.await()

		// Last: whatever follows the invalidation in a branch of the scope must not run. The wipe cancels
		// the branches that were there when it started; one that arrived later, while it was in
		// flight, is served by it and ends here.
		if (insideTheScope) sessionCoroutineScope.callerBranch()?.cancel()
		currentCoroutineContext().ensureActive()
	}

	// The sign-out belongs to the dialog that asked for it, and the dialog can be cleared at any moment, so
	// its local end runs in the scope of the app like an invalidation does, and under the same lock and
	// counter: a rejection that reaches a request in flight while the session is being wiped finds the end
	// already served instead of wiping a second time and telling the user their session was closed. The
	// mark of the intentional sign-out is withdrawn last, when the session is gone: left behind, it would
	// silence the next invalidation, which is not this one's. The server has revoked the session by now,
	// so the sign-out is done whatever happens to a step: each one is skipped if it fails, and its failure
	// is reported instead of handed to a caller that cannot do anything about it.
	override suspend fun endSignedOutSession(sessionId: String) {
		appCoroutineScope.async {
			invalidationMutex.withLock {
				// A rejection that reached a request in flight while the server was revoking the session can
				// have run its whole invalidation first, silenced by the mark of this sign-out: then the
				// session is already gone and the steps would find nothing to empty. Only that lets them be
				// skipped. Any session that is stored, the one asked for or another one (a recovery by
				// credentials of the same account stores one), is not the signed-out session, and the device
				// must not stay authenticated after a sign-out. A stored session that cannot be read does not
				// excuse the steps either.
				val stored = attempt { sessionRepository.getActiveSessionSnapshot() }
				val alreadyEnded = stored.isSuccess && stored.getOrNull() == null
				var failures = emptyList<Throwable>()

				if (!alreadyEnded) {
					failures = listOfNotNull(
						runStep { sessionCoroutineScope.cancelActiveWork() },
						runStep { applicationRepository.clearData() },
						runStep { sessionRepository.clear() },
						runStep { syncStatusRepository.reset() }
					)

					invalidationsCompleted.value += 1
				}

				// Withdrawn whether the steps ran or not, and before anything is reported: the sign-out is
				// over either way, and the mark left behind would silence the next invalidation.
				sessionInvalidationRepository.clearIntentionalSignOut(sessionId)

				// The report does not suspend, so a cancellation it throws is not the one of this job: it
				// cannot keep the end from finishing.
				failures.forEach { failure -> runCatching { reportingRepository.logException(failure) } }
			}
		}.await()
	}

	// What the logout leaves behind, once per invalidation: status and error code of the rejection
	// (response metadata only: no tokens, no identifiers) and where the request came from.
	private fun reportInvalidation(
		reason: SessionInvalidationReason?,
		cause: Throwable?,
		insideTheScope: Boolean
	) {
		val rejection = when (cause) {
			null -> "no rejection"
			is ResponseException -> "status=${cause.response.status.value} authError=${cause.authErrorCode() ?: "none"}"
			else -> "failure=${cause::class.simpleName}"
		}
		val origin = if (insideTheScope) "inside the session scope" else "outside the session scope"

		attempt {
			reportingRepository.logException(
				IllegalStateException("Session invalidated: reason=${reason?.name ?: "unspecified"} $rejection $origin")
			)
		}
	}

	// Not under recoveryMutex: the wipe can be reached from a recovery that still holds it, and
	// forgetting a token of a session that is gone needs no ordering with the next one.
	override suspend fun clearSessionMemory() {
		expiryArbitratedAccessToken = null
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

		val changedSnapshot = sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)

		// The sign-out under way is the one that ends this session: recovering it would hand the device a
		// session after the user asked to leave, and invalidating it would end it a second time.
		val isBeingSignedOut = changedSnapshot == null &&
				throwable.shouldAttemptCredentialRecovery() &&
				sessionInvalidationRepository.isIntentionalSignOut(attemptedSnapshot.sessionId)

		if (isBeingSignedOut) return null

		val recoveredSnapshot = changedSnapshot
			?: attemptCredentialRecovery(
				throwable = throwable,
				attemptedSnapshot = attemptedSnapshot,
				credentialsRepository = credentialsRepository,
				authRepository = authRepository
			)

		return recoveredSnapshot ?: invalidateAttemptedSession(attemptedSnapshot, throwable)
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
		val hasPassword = try {
			credentialsRepository.hasPassword()
		} catch (cancellation: CancellationException) {
			throw cancellation
		} catch (_: Exception) {
			false
		}

		if (!hasPassword) return null

		return runCatching {
			sessionRepository.getSessionMovedOn(attemptedSnapshot)?.let { movedOn ->
				return@runCatching movedOn.current
			}

			val password = credentialsRepository.getPassword()
			val bootstrapTokens = authRepository.bootstrapSignIn(
				usbId = attemptedSnapshot.usbId,
				password = password
			)

			sessionRepository.getSessionMovedOn(attemptedSnapshot)?.let { movedOn ->
				return@runCatching movedOn.current
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

			sessionRepository.getSessionMovedOn(attemptedSnapshot)?.let { movedOn ->
				return@runCatching movedOn.current
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
		attemptedSnapshot: SessionSnapshot,
		rejection: Throwable
	): SessionSnapshot? {
		sessionRepository.getSessionChangedSnapshot(attemptedSnapshot)?.let { snapshot ->
			return snapshot
		}

		invalidateSession(
			sessionId = attemptedSnapshot.sessionId,
			reason = SessionInvalidationReason.RefreshRejected,
			cause = rejection
		)
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

// The attempted session is not the stored one any more: [current] is the session stored instead, or null
// when there is none (it was ended). The tokens a recovery obtains belong to the attempted session, so
// they are not written over either.
private class SessionMovedOn(val current: SessionSnapshot?)

private suspend fun SessionRepository.getSessionMovedOn(
	attemptedSnapshot: SessionSnapshot
): SessionMovedOn? {
	val currentSnapshot = getActiveSessionSnapshot()

	return if (currentSnapshot != attemptedSnapshot) SessionMovedOn(currentSnapshot) else null
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
