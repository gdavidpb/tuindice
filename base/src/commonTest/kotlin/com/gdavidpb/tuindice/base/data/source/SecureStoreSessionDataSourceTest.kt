package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.exception.SecureStoreUnavailableException
import com.gdavidpb.tuindice.base.domain.model.SessionResetNotice
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
	fun transientUnavailableReadRetriesAndPreservesSession() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository(
			unavailableReads = 1,
			initialValues = completeSessionValues("active")
		)
		val legacyStore = FakeSecureKeyValueDataRepository()
		val invalidationRepository = RecordingSessionInvalidationRepository()
		val dataSource = secureStoreSessionDataSource(
			activeStore = activeStore,
			legacyStore = legacyStore,
			invalidationRepository = invalidationRepository
		)

		assertTrue(dataSource.hasActiveSession())

		assertEquals(0, activeStore.clearCalls)
		assertEquals(emptyList(), invalidationRepository.notifiedSessionIds)
	}

	@Test
	fun persistentUnavailableStorePropagatesWithoutWipingStores() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository(
			unavailableReads = Int.MAX_VALUE,
			initialValues = completeSessionValues("active")
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

		val thrown = runCatching { dataSource.hasActiveSession() }.exceptionOrNull()

		assertTrue(thrown is SecureStoreUnavailableException)
		assertEquals(0, activeStore.clearCalls)
		assertEquals(0, legacyStore.clearCalls)
		assertEquals(completeSessionValues("active"), activeStore.values)
		assertEquals(mapOf("legacy-value" to "left-over"), legacyStore.values)
		assertEquals(emptyList(), invalidationRepository.notifiedSessionIds)
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

	// The wipe of the session must not turn the cancellation of whoever runs it into a step that failed.
	@Test
	fun clear_doesNotSwallowACancellation() = runTest {
		val dataSource = secureStoreSessionDataSource(
			activeStore = FakeSecureKeyValueDataRepository(removeFailure = CancellationException("cancelled")),
			legacyStore = FakeSecureKeyValueDataRepository()
		)

		assertFailsWith<CancellationException> { dataSource.clear() }
	}

	// A session found half written ends from the store: the notice that explains the sign-in is
	// persisted before the signal, and taken back when the signal is suppressed.
	@Test
	fun aHalfWrittenSession_persistsItsNoticeBeforeTheSignal() = runTest {
		val settings = FakeSettingsRepository()
		var noticeWhenSignalled: SessionResetNotice? = null
		val invalidationRepository = object : SessionInvalidationRepository {
			override fun observeSessionInvalidation(): Flow<Unit> = emptyFlow()

			override fun markIntentionalSignOut(sessionId: String) = Unit

			override fun clearIntentionalSignOut(sessionId: String) = Unit

			override fun isIntentionalSignOut(sessionId: String) = false

			override fun notifySessionInvalidated(sessionId: String?): Boolean {
				noticeWhenSignalled = runBlocking { settings.getSessionResetNotice() }

				return true
			}
		}
		val dataSource = SecureStoreSessionDataSource(
			secureStore = FakeSecureKeyValueDataRepository(initialValues = partialSessionValues()),
			legacySecureStore = FakeSecureKeyValueDataRepository(),
			sessionInvalidationRepository = invalidationRepository,
			settingsRepository = settings
		)

		assertFalse(dataSource.hasActiveSession())

		assertEquals(SessionResetNoticeReason.UnreadableData, noticeWhenSignalled?.reason)
		assertEquals(noticeWhenSignalled, settings.getSessionResetNotice())
	}

	private fun secureStoreSessionDataSource(
		activeStore: FakeSecureKeyValueDataRepository,
		legacyStore: FakeSecureKeyValueDataRepository,
		invalidationRepository: RecordingSessionInvalidationRepository =
			RecordingSessionInvalidationRepository(),
		settingsRepository: SettingsRepository = FakeSettingsRepository()
	): SecureStoreSessionDataSource {
		return SecureStoreSessionDataSource(
			secureStore = activeStore,
			legacySecureStore = legacyStore,
			sessionInvalidationRepository = invalidationRepository,
			settingsRepository = settingsRepository
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
	private var unavailableReads: Int = 0,
	private val dropWrites: Boolean = false,
	private val removeFailure: Throwable? = null
) : SecureKeyValueDataRepository {
	val values = initialValues.toMutableMap()
	var clearCalls = 0
		private set

	override suspend fun getString(key: String): String? {
		if (unavailableReads > 0) {
			unavailableReads--
			throw SecureStoreUnavailableException("Secure store unavailable")
		}
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
		removeFailure?.let { throw it }
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

	override fun isIntentionalSignOut(sessionId: String) = false

	override fun notifySessionInvalidated(sessionId: String?): Boolean {
		notifiedSessionIds += sessionId

		return true
	}
}
