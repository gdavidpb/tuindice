package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest

class SecureStoreSessionDataSourceTest {
	@Test
	fun readsCompleteSessionFromActiveStore() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository(
			initialValues = completeSessionValues("active")
		)
		val legacyStore = FakeSecureKeyValueDataRepository(
			initialValues = completeSessionValues("legacy")
		)
		val invalidationRepository = RecordingSessionInvalidationRepository()
		val dataSource = secureStoreSessionDataSource(
			activeStore = activeStore,
			legacyStore = legacyStore,
			invalidationRepository = invalidationRepository
		)

		assertTrue(dataSource.hasActiveSession())
		assertEquals("active-usb-id", dataSource.getUsbId())
		assertEquals("active-session-id", dataSource.getSessionId())
		assertEquals("active-access-token", dataSource.getAccessToken())
		assertEquals("active-refresh-token", dataSource.getRefreshToken())
		assertEquals(emptyList(), invalidationRepository.notifiedSessionIds)
		assertEquals("legacy-session-id", legacyStore.values[PreferencesKeys.USER_SESSION_ID])
	}

	@Test
	fun migratesCompleteLegacySessionAndDeletesLegacyValues() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository()
		val legacyStore = FakeSecureKeyValueDataRepository(
			initialValues = completeSessionValues("legacy")
		)
		val dataSource = secureStoreSessionDataSource(
			activeStore = activeStore,
			legacyStore = legacyStore
		)

		assertTrue(dataSource.hasActiveSession())

		assertEquals(completeSessionValues("legacy"), activeStore.values)
		assertFalse(legacyStore.values.containsKey(PreferencesKeys.USER_USB_ID))
		assertFalse(legacyStore.values.containsKey(PreferencesKeys.USER_SESSION_ID))
		assertFalse(legacyStore.values.containsKey(PreferencesKeys.USER_ACCESS_TOKEN))
		assertFalse(legacyStore.values.containsKey(PreferencesKeys.USER_REFRESH_TOKEN))
	}

	@Test
	fun recoversFromUnreadableActiveStoreUsingCompleteLegacySession() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository(
			readFailures = 1,
			initialValues = partialSessionValues()
		)
		val legacyStore = FakeSecureKeyValueDataRepository(
			initialValues = completeSessionValues("legacy")
		)
		val invalidationRepository = RecordingSessionInvalidationRepository()
		val dataSource = secureStoreSessionDataSource(
			activeStore = activeStore,
			legacyStore = legacyStore,
			invalidationRepository = invalidationRepository
		)

		assertTrue(dataSource.hasActiveSession())

		assertEquals(1, activeStore.clearCalls)
		assertEquals(completeSessionValues("legacy"), activeStore.values)
		assertEquals(emptyList(), invalidationRepository.notifiedSessionIds)
	}

	@Test
	fun partialSessionClearsStoresAndNotifiesInvalidation() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository(
			initialValues = partialSessionValues()
		)
		val legacyStore = FakeSecureKeyValueDataRepository(
			initialValues = mapOf("legacy-value" to "left-over")
		)
		val invalidationRepository = RecordingSessionInvalidationRepository()
		val dataSource = secureStoreSessionDataSource(
			activeStore = activeStore,
			legacyStore = legacyStore,
			invalidationRepository = invalidationRepository
		)

		assertFalse(dataSource.hasActiveSession())

		assertEquals(emptyMap<String, String>(), activeStore.values)
		assertEquals(emptyMap<String, String>(), legacyStore.values)
		assertEquals(listOf<String?>("partial-session-id"), invalidationRepository.notifiedSessionIds)
	}

	@Test
	fun migrationVerificationFailureInvalidatesSession() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository(dropWrites = true)
		val legacyStore = FakeSecureKeyValueDataRepository(
			initialValues = completeSessionValues("legacy")
		)
		val invalidationRepository = RecordingSessionInvalidationRepository()
		val dataSource = secureStoreSessionDataSource(
			activeStore = activeStore,
			legacyStore = legacyStore,
			invalidationRepository = invalidationRepository
		)

		assertFalse(dataSource.hasActiveSession())

		assertEquals(2, activeStore.clearCalls)
		assertEquals(emptyMap<String, String>(), legacyStore.values)
		assertEquals(listOf<String?>("legacy-session-id"), invalidationRepository.notifiedSessionIds)
	}

	private fun secureStoreSessionDataSource(
		activeStore: FakeSecureKeyValueDataRepository,
		legacyStore: FakeSecureKeyValueDataRepository,
		invalidationRepository: RecordingSessionInvalidationRepository =
			RecordingSessionInvalidationRepository()
	): SecureStoreSessionDataSource {
		return SecureStoreSessionDataSource(
			secureStore = activeStore,
			legacySecureStore = legacyStore,
			sessionInvalidationRepository = invalidationRepository
		)
	}

	private fun completeSessionValues(prefix: String): Map<String, String> {
		return mapOf(
			PreferencesKeys.USER_USB_ID to "$prefix-usb-id",
			PreferencesKeys.USER_SESSION_ID to "$prefix-session-id",
			PreferencesKeys.USER_ACCESS_TOKEN to "$prefix-access-token",
			PreferencesKeys.USER_REFRESH_TOKEN to "$prefix-refresh-token"
		)
	}

	private fun partialSessionValues(): Map<String, String> {
		return mapOf(
			PreferencesKeys.USER_SESSION_ID to "partial-session-id",
			PreferencesKeys.USER_ACCESS_TOKEN to "partial-access-token"
		)
	}
}

private class FakeSecureKeyValueDataRepository(
	initialValues: Map<String, String> = emptyMap(),
	private var readFailures: Int = 0,
	private val dropWrites: Boolean = false
) : SecureKeyValueDataRepository {
	val values = initialValues.toMutableMap()
	var clearCalls = 0
		private set

	override suspend fun getString(key: String): String? {
		if (readFailures > 0) {
			readFailures--
			throw IllegalStateException("Unreadable secure store")
		}
		return values[key]
	}

	override suspend fun putString(key: String, value: String) {
		if (!dropWrites) {
			values[key] = value
		}
	}

	override suspend fun remove(key: String) {
		values.remove(key)
	}

	override suspend fun clear() {
		values.clear()
		clearCalls++
	}
}

private class RecordingSessionInvalidationRepository : SessionInvalidationRepository {
	val notifiedSessionIds = mutableListOf<String?>()

	override fun observeSessionInvalidation(): Flow<Unit> = emptyFlow()

	override fun markIntentionalSignOut(sessionId: String) = Unit

	override fun clearIntentionalSignOut(sessionId: String) = Unit

	override fun notifySessionInvalidated(sessionId: String?) {
		notifiedSessionIds += sessionId
	}
}
