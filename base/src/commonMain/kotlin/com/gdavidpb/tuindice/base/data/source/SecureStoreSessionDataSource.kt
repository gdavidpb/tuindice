package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.PreferencesSessionDataRepository
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.exception.SecureStoreUnavailableException
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.base.utils.extension.attempt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SecureStoreSessionDataSource(
	private val secureStore: SecureKeyValueDataRepository,
	private val legacySecureStore: SecureKeyValueDataRepository,
	private val sessionInvalidationRepository: SessionInvalidationRepository,
	private val settingsRepository: SettingsRepository
) : PreferencesSessionDataRepository {
	private val migrationMutex = Mutex()

	suspend fun hasActiveSession(): Boolean {
		return resolveSession() != null
	}

	override suspend fun setUsbId(usbId: String) {
		writeActiveValue(PreferencesKeys.USER_USB_ID, usbId)
	}

	override suspend fun setSessionId(sessionId: String) {
		writeActiveValue(PreferencesKeys.USER_SESSION_ID, sessionId)
	}

	override suspend fun setAccessToken(accessToken: String) {
		writeActiveValue(PreferencesKeys.USER_ACCESS_TOKEN, accessToken)
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		writeActiveValue(PreferencesKeys.USER_REFRESH_TOKEN, refreshToken)
	}

	// The four keys are written back to back and the legacy copies are removed afterwards: the removal
	// really suspends, so a save cut by a cancellation at one of them must already have stored the whole
	// session, not a mix of the new and the previous one.
	override suspend fun setSession(usbId: String, sessionId: String, accessToken: String, refreshToken: String) {
		migrationMutex.withLock {
			secureStore.putString(key = PreferencesKeys.USER_USB_ID, value = usbId)
			secureStore.putString(key = PreferencesKeys.USER_SESSION_ID, value = sessionId)
			secureStore.putString(key = PreferencesKeys.USER_ACCESS_TOKEN, value = accessToken)
			secureStore.putString(key = PreferencesKeys.USER_REFRESH_TOKEN, value = refreshToken)

			sessionKeys.forEach { key -> attempt { legacySecureStore.remove(key) } }
		}
	}

	override suspend fun getUsbId(): String? {
		return resolveSession()?.usbId ?: readSingleValue(PreferencesKeys.USER_USB_ID)
	}

	override suspend fun getSessionId(): String? {
		return resolveSession()?.sessionId
	}

	override suspend fun getAccessToken(): String? {
		return resolveSession()?.accessToken
	}

	override suspend fun getRefreshToken(): String? {
		return resolveSession()?.refreshToken
	}

	override suspend fun clear() {
		sessionKeys.forEach { key ->
			attempt { secureStore.remove(key) }
			attempt { legacySecureStore.remove(key) }
		}
	}

	private suspend fun writeActiveValue(key: String, value: String) {
		migrationMutex.withLock {
			secureStore.putString(key = key, value = value)
			attempt { legacySecureStore.remove(key) }
		}
	}

	private suspend fun readSingleValue(key: String): String? {
		return migrationMutex.withLock {
			attempt { secureStore.getString(key)?.takeIf(String::isNotBlank) }
				.getOrNull()
				?: attempt { legacySecureStore.getString(key)?.takeIf(String::isNotBlank) }
					.getOrNull()
		}
	}

	private suspend fun resolveSession(): CompleteStoredSession? {
		return migrationMutex.withLock {
			val activeState = readSessionState(secureStore)

			when (activeState) {
				is SessionStoreState.Complete -> activeState.session
				is SessionStoreState.Empty -> resolveLegacySession()
				is SessionStoreState.Partial -> confirmPartialSession()
				is SessionStoreState.Failed -> recoverFromLegacyOrInvalidate(null)
			}
		}
	}

	// The wipe of the application empties the secure store without taking the lock of this source, so a
	// reader can land between two keys of a session that is being ended. A half-written session is
	// confirmed with a second read before it is called one: if the store is empty by then, the session
	// was not broken, it was being wiped, and whoever wipes it is the one that tells the user.
	private suspend fun confirmPartialSession(): CompleteStoredSession? {
		return when (val confirmed = readSessionState(secureStore)) {
			is SessionStoreState.Complete -> confirmed.session
			is SessionStoreState.Empty -> resolveLegacySession()
			is SessionStoreState.Partial -> recoverFromLegacyOrInvalidate(confirmed.sessionId)
			is SessionStoreState.Failed -> recoverFromLegacyOrInvalidate(null)
		}
	}

	private suspend fun resolveLegacySession(): CompleteStoredSession? {
		return when (val legacyState = readSessionState(legacySecureStore)) {
			is SessionStoreState.Complete -> migrateLegacySession(legacyState.session)
			is SessionStoreState.Empty -> null
			is SessionStoreState.Partial -> invalidateStoredSession(legacyState.sessionId)
			is SessionStoreState.Failed -> invalidateStoredSession(null)
		}
	}

	private suspend fun recoverFromLegacyOrInvalidate(activeSessionId: String?): CompleteStoredSession? {
		return when (val legacyState = readSessionState(legacySecureStore)) {
			is SessionStoreState.Complete -> {
				attempt { secureStore.clear() }
				migrateLegacySession(legacyState.session)
			}
			is SessionStoreState.Empty -> invalidateStoredSession(activeSessionId)
			is SessionStoreState.Partial -> invalidateStoredSession(activeSessionId ?: legacyState.sessionId)
			is SessionStoreState.Failed -> invalidateStoredSession(activeSessionId)
		}
	}

	private suspend fun migrateLegacySession(session: CompleteStoredSession): CompleteStoredSession? {
		// A cancellation is not a failed migration: attempt lets it through, so a reader that is cancelled
		// here leaves the legacy copy as it was instead of ending a session that is fine.
		val migrated = attempt {
			secureStore.putString(PreferencesKeys.USER_USB_ID, session.usbId)
			secureStore.putString(PreferencesKeys.USER_SESSION_ID, session.sessionId)
			secureStore.putString(PreferencesKeys.USER_ACCESS_TOKEN, session.accessToken)
			secureStore.putString(PreferencesKeys.USER_REFRESH_TOKEN, session.refreshToken)

			val verified = readSessionState(secureStore) as? SessionStoreState.Complete
			check(verified?.session == session)
		}

		if (migrated.isFailure) {
			attempt { secureStore.clear() }
			return invalidateStoredSession(session.sessionId)
		}

		// The session is written and verified in the active store: that is the migration. Removing the
		// legacy copy is cleaning up after it, so its failure does not undo the session, and a reader
		// cancelled here leaves a session that the next read finds whole in the active store.
		sessionKeys.forEach { key -> attempt { legacySecureStore.remove(key) } }

		return session
	}

	private suspend fun invalidateStoredSession(sessionId: String?): CompleteStoredSession? {
		// The session ends here, with nobody to ask: the notice goes with the signal, like the one of an
		// invalidation that comes from the server, so the sign-in screen does not appear unexplained.
		// A stored session that cannot be read is what the user is told. The notice is kept first: emptying
		// the legacy store really suspends, and a reader that is cancelled there would otherwise leave the
		// stores emptied with nothing to explain it at the next start.
		val notice = attempt {
			settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.UnreadableData)
		}.getOrNull()

		attempt { secureStore.clear() }
		attempt { legacySecureStore.clear() }

		val raised = attempt { sessionInvalidationRepository.notifySessionInvalidated(sessionId = sessionId) }
			.getOrDefault(false)

		// A signal that was suppressed (an intentional sign-out) owes the user no explanation.
		if (!raised && notice != null) attempt { settingsRepository.consumeSessionResetNotice(notice.stamp) }

		return null
	}

	private suspend fun readSessionState(
		store: SecureKeyValueDataRepository
	): SessionStoreState {
		val read = attempt {
			readStoredSession(store)
		}.recoverCatching { throwable ->
			if (throwable !is SecureStoreUnavailableException) throw throwable

			// One retry bridges transient keystore hiccups; a persistent outage keeps
			// the stored session untouched instead of degrading into an invalidation.
			delay(SECURE_STORE_RETRY_DELAY_MILLIS)
			readStoredSession(store)
		}

		// A reader that is cancelled while it reads (or waits to retry) did not find a broken session, and
		// an outage is not one either.
		read.exceptionOrNull()?.let { throwable ->
			if (throwable is CancellationException || throwable is SecureStoreUnavailableException) throw throwable
		}

		return read.map { session ->
			when {
				session.isComplete -> SessionStoreState.Complete(session.toComplete())
				session.isEmpty -> SessionStoreState.Empty
				else -> SessionStoreState.Partial(session.sessionId)
			}
		}.getOrElse { SessionStoreState.Failed }
	}

	private suspend fun readStoredSession(
		store: SecureKeyValueDataRepository
	): StoredSession {
		return StoredSession(
			usbId = store.getString(PreferencesKeys.USER_USB_ID)?.takeIf(String::isNotBlank),
			sessionId = store.getString(PreferencesKeys.USER_SESSION_ID)?.takeIf(String::isNotBlank),
			accessToken = store.getString(PreferencesKeys.USER_ACCESS_TOKEN)?.takeIf(String::isNotBlank),
			refreshToken = store.getString(PreferencesKeys.USER_REFRESH_TOKEN)?.takeIf(String::isNotBlank)
		)
	}

	private data class StoredSession(
		val usbId: String?,
		val sessionId: String?,
		val accessToken: String?,
		val refreshToken: String?
	) {
		val isEmpty: Boolean =
			usbId == null &&
					sessionId == null &&
					accessToken == null &&
					refreshToken == null

		val isComplete: Boolean =
			usbId != null &&
					sessionId != null &&
					accessToken != null &&
					refreshToken != null

		fun toComplete(): CompleteStoredSession {
			check(isComplete)
			return CompleteStoredSession(
				usbId = requireNotNull(usbId),
				sessionId = requireNotNull(sessionId),
				accessToken = requireNotNull(accessToken),
				refreshToken = requireNotNull(refreshToken)
			)
		}
	}

	private data class CompleteStoredSession(
		val usbId: String,
		val sessionId: String,
		val accessToken: String,
		val refreshToken: String
	)

	private sealed interface SessionStoreState {
		data object Empty : SessionStoreState
		data object Failed : SessionStoreState
		data class Partial(val sessionId: String?) : SessionStoreState
		data class Complete(val session: CompleteStoredSession) : SessionStoreState
	}

	private companion object {
		const val SECURE_STORE_RETRY_DELAY_MILLIS = 150L

		val sessionKeys = listOf(
			PreferencesKeys.USER_USB_ID,
			PreferencesKeys.USER_SESSION_ID,
			PreferencesKeys.USER_ACCESS_TOKEN,
			PreferencesKeys.USER_REFRESH_TOKEN
		)
	}
}
