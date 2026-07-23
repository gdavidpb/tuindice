package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.PreferencesSessionDataRepository
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.exception.SecureStoreUnavailableException
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SecureStoreSessionDataSource(
	private val secureStore: SecureKeyValueDataRepository,
	private val legacySecureStore: SecureKeyValueDataRepository,
	private val sessionInvalidationRepository: SessionInvalidationRepository
) : PreferencesSessionDataRepository {
	private val migrationMutex = Mutex()

	override suspend fun hasActiveSession(): Boolean {
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
			runCatching { secureStore.remove(key) }
			runCatching { legacySecureStore.remove(key) }
		}
	}

	private suspend fun writeActiveValue(key: String, value: String) {
		migrationMutex.withLock {
			secureStore.putString(key = key, value = value)
			runCatching { legacySecureStore.remove(key) }
		}
	}

	private suspend fun readSingleValue(key: String): String? {
		return migrationMutex.withLock {
			runCatching { secureStore.getString(key)?.takeIf(String::isNotBlank) }
				.getOrNull()
				?: runCatching { legacySecureStore.getString(key)?.takeIf(String::isNotBlank) }
					.getOrNull()
		}
	}

	private suspend fun resolveSession(): CompleteStoredSession? {
		return migrationMutex.withLock {
			val activeState = readSessionState(secureStore)

			when (activeState) {
				is SessionStoreState.Complete -> activeState.session
				is SessionStoreState.Empty -> resolveLegacySession()
				is SessionStoreState.Partial -> recoverFromLegacyOrInvalidate(activeState.sessionId)
				is SessionStoreState.Failed -> recoverFromLegacyOrInvalidate(null)
			}
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
				runCatching { secureStore.clear() }
				migrateLegacySession(legacyState.session)
			}
			is SessionStoreState.Empty -> invalidateStoredSession(activeSessionId)
			is SessionStoreState.Partial -> invalidateStoredSession(activeSessionId ?: legacyState.sessionId)
			is SessionStoreState.Failed -> invalidateStoredSession(activeSessionId)
		}
	}

	private suspend fun migrateLegacySession(session: CompleteStoredSession): CompleteStoredSession? {
		val migrated = runCatching {
			secureStore.putString(PreferencesKeys.USER_USB_ID, session.usbId)
			secureStore.putString(PreferencesKeys.USER_SESSION_ID, session.sessionId)
			secureStore.putString(PreferencesKeys.USER_ACCESS_TOKEN, session.accessToken)
			secureStore.putString(PreferencesKeys.USER_REFRESH_TOKEN, session.refreshToken)

			val verified = readSessionState(secureStore) as? SessionStoreState.Complete
			check(verified?.session == session)

			sessionKeys.forEach { key -> legacySecureStore.remove(key) }
			session
		}

		return migrated.getOrElse {
			runCatching { secureStore.clear() }
			invalidateStoredSession(session.sessionId)
		}
	}

	private suspend fun invalidateStoredSession(sessionId: String?): CompleteStoredSession? {
		runCatching { secureStore.clear() }
		runCatching { legacySecureStore.clear() }
		sessionInvalidationRepository.notifySessionInvalidated(sessionId = sessionId)
		return null
	}

	private suspend fun readSessionState(
		store: SecureKeyValueDataRepository
	): SessionStoreState {
		return runCatching {
			readStoredSession(store)
		}.recoverCatching { throwable ->
			if (throwable !is SecureStoreUnavailableException) throw throwable

			// One retry bridges transient keystore hiccups; a persistent outage keeps
			// the stored session untouched instead of degrading into an invalidation.
			delay(SECURE_STORE_RETRY_DELAY_MILLIS)
			readStoredSession(store)
		}.map { session ->
			when {
				session.isComplete -> SessionStoreState.Complete(session.toComplete())
				session.isEmpty -> SessionStoreState.Empty
				else -> SessionStoreState.Partial(session.sessionId)
			}
		}.getOrElse { throwable ->
			if (throwable is SecureStoreUnavailableException) throw throwable

			SessionStoreState.Failed
		}
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
