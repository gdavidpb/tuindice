package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SessionDataSource
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys

// The session store as the app builds it: the real source over the in-memory mirror and the real
// secure-store source over two in-memory stores. A session is gone when this says so, not when a
// double was told to clear.
internal class RealSessionStore(
	settings: SettingsRepository,
	invalidations: SessionInvalidationRepository
) {
	val active = InMemorySecureStore()
	val legacy = InMemorySecureStore()
	val memory = InMemorySessionDataSource()
	val repository = SessionDataSource(
		memorySessionDataSource = memory,
		preferencesSessionDataSource = SecureStoreSessionDataSource(
			secureStore = active,
			legacySecureStore = legacy,
			sessionInvalidationRepository = invalidations,
			settingsRepository = settings
		)
	)

	// What a session leaves in the secure store, read from there the first time it is asked for.
	fun seed(
		sessionId: String = "session-old",
		usbId: String = "12-34567",
		accessToken: String = "access-old",
		refreshToken: String = "refresh-old"
	) {
		active.values[PreferencesKeys.USER_USB_ID] = usbId
		active.values[PreferencesKeys.USER_SESSION_ID] = sessionId
		active.values[PreferencesKeys.USER_ACCESS_TOKEN] = accessToken
		active.values[PreferencesKeys.USER_REFRESH_TOKEN] = refreshToken
	}

	suspend fun isGone(): Boolean = repository.getActiveSessionSnapshot() == null
}
