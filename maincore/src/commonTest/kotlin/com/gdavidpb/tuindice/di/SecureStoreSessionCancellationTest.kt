package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.testing.CountingSessionInvalidationRepository
import com.gdavidpb.tuindice.testing.InMemorySecureStore
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// A reader of the stored session that is cancelled did not find a broken session: the store lets the
// cancellation through instead of taking it for "the stored session cannot be read", so it neither ends a
// valid session nor leaves it half moved, and when it does end one it has left the notice first.
@OptIn(ExperimentalCoroutinesApi::class)
class SecureStoreSessionCancellationTest {
	// The store ends a half-written session: it keeps the notice first, then empties the active store and
	// the legacy one (a call that really suspends in production: KSafe). The reader is cancelled in between,
	// so the notice has to be there already.
	@Test
	fun aStoreInvalidationCutWhileItEmptiesTheLegacyStore_stillLeavesItsNotice() = runTest {
		val settings = FakeSettingsRepository()
		val invalidations = CountingSessionInvalidationRepository()
		val active = InMemorySecureStore()
		active.values[PreferencesKeys.USER_SESSION_ID] = "session-old"
		active.values[PreferencesKeys.USER_ACCESS_TOKEN] = "access-old"
		lateinit var reader: Job
		val legacy = object : SecureKeyValueDataRepository {
			override suspend fun getString(key: String): String? = null
			override suspend fun putString(key: String, value: String) = Unit
			override suspend fun remove(key: String) = Unit
			override suspend fun clear() {
				reader.cancel()
				yield()
			}
		}
		val store = SecureStoreSessionDataSource(
			secureStore = active,
			legacySecureStore = legacy,
			sessionInvalidationRepository = invalidations,
			settingsRepository = settings
		)

		reader = launch { store.hasActiveSession() }
		advanceUntilIdle()

		assertTrue(active.values.isEmpty(), "the stored session was emptied")
		assertNotNull(settings.sessionResetNotice, "so its notice must be there for the next start")
	}

	// A valid session kept in the legacy store (a user coming from the version before the new store)
	// is read for the first time, and the reader is cancelled while the legacy store answers.
	@Test
	fun aReaderCancelledWhileTheLegacyStoreIsRead_doesNotEndAValidSession() = runTest {
		val settings = FakeSettingsRepository()
		val invalidations = CountingSessionInvalidationRepository()
		val active = InMemorySecureStore()
		lateinit var reader: Job
		val legacy = CancellingStore(cancelOn = "read") { reader.cancel() }
		legacy.seed()
		val store = SecureStoreSessionDataSource(active, legacy, invalidations, settings)

		reader = launch { store.hasActiveSession() }
		advanceUntilIdle()

		assertEquals(4, legacy.values.size, "the legacy session is intact")
		assertNull(settings.sessionResetNotice, "and nobody is told that the data could not be read")
	}

	// The same session is being written to the new store and the reader is cancelled in the middle: the
	// migration did not fail, it was interrupted, so the session is not ended and the legacy copy stays.
	@Test
	fun aReaderCancelledWhileTheSessionIsWrittenToTheNewStore_doesNotEndIt() = runTest {
		val settings = FakeSettingsRepository()
		val invalidations = CountingSessionInvalidationRepository()
		lateinit var reader: Job
		val active = CancellingStore(cancelOn = "put") { reader.cancel() }
		val legacy = InMemorySecureStore()
		CancellingStore(cancelOn = "none") {}.also { seeded ->
			seeded.seed()
			legacy.values.putAll(seeded.values)
		}
		val store = SecureStoreSessionDataSource(active, legacy, invalidations, settings)

		reader = launch { store.hasActiveSession() }
		advanceUntilIdle()

		assertEquals(4, legacy.values.size, "the legacy session is intact")
		assertNull(settings.sessionResetNotice, "and nobody is told that the data could not be read")
		assertEquals(0, invalidations.raised)
	}

	// The same session is being moved to the new store: it was written and verified there, and the
	// reader is cancelled while the legacy copy is removed.
	@Test
	fun aReaderCancelledWhileTheLegacyCopyIsRemoved_doesNotLoseTheMigratedSession() = runTest {
		val settings = FakeSettingsRepository()
		val invalidations = CountingSessionInvalidationRepository()
		val active = InMemorySecureStore()
		lateinit var reader: Job
		val legacy = CancellingStore(cancelOn = "remove") { reader.cancel() }
		legacy.seed()
		val store = SecureStoreSessionDataSource(active, legacy, invalidations, settings)

		reader = launch { store.hasActiveSession() }
		advanceUntilIdle()

		assertTrue(active.values.size == 4 || legacy.values.size == 4, "the session is still whole in one of the stores")
	}

	// The legacy copy cannot be removed (a failure, not a cancellation) after the session was written and
	// verified in the active store: cleaning up is not the migration, so the session is kept and nobody is
	// told it was lost.
	@Test
	fun whenTheLegacyCopyCannotBeRemoved_theMigratedSessionIsKept() = runTest {
		val settings = FakeSettingsRepository()
		val invalidations = CountingSessionInvalidationRepository()
		val active = InMemorySecureStore()
		val legacy = object : SecureKeyValueDataRepository {
			val values = mutableMapOf(
				PreferencesKeys.USER_USB_ID to "12-34567",
				PreferencesKeys.USER_SESSION_ID to "session-old",
				PreferencesKeys.USER_ACCESS_TOKEN to "access-old",
				PreferencesKeys.USER_REFRESH_TOKEN to "refresh-old"
			)

			override suspend fun getString(key: String): String? = values[key]
			override suspend fun putString(key: String, value: String) {
				values[key] = value
			}

			override suspend fun remove(key: String): Unit = error("the legacy store refuses to remove")
			override suspend fun clear() {
				values.clear()
			}
		}
		val store = SecureStoreSessionDataSource(active, legacy, invalidations, settings)

		assertTrue(store.hasActiveSession(), "the migrated session is the session")
		assertEquals(4, active.values.size)
		assertEquals(0, invalidations.raised)
		assertNull(settings.sessionResetNotice)
	}
}

// A legacy store that really suspends (as KSafe does): the first read (or the first removal, after it
// took effect) is where the reader is cancelled.
private class CancellingStore(
	private val cancelOn: String,
	private val cancel: () -> Unit
) : SecureKeyValueDataRepository {
	val values = mutableMapOf<String, String>()
	private var fired = false

	fun seed() {
		values[PreferencesKeys.USER_USB_ID] = "12-34567"
		values[PreferencesKeys.USER_SESSION_ID] = "session-old"
		values[PreferencesKeys.USER_ACCESS_TOKEN] = "access-old"
		values[PreferencesKeys.USER_REFRESH_TOKEN] = "refresh-old"
	}

	override suspend fun getString(key: String): String? {
		if (cancelOn == "read" && !fired) {
			fired = true
			cancel()
			yield()
		}

		return values[key]
	}

	override suspend fun putString(key: String, value: String) {
		values[key] = value

		if (cancelOn == "put" && !fired) {
			fired = true
			cancel()
			yield()
		}
	}

	override suspend fun remove(key: String) {
		values.remove(key)

		if (cancelOn == "remove" && !fired) {
			fired = true
			cancel()
			yield()
		}
	}

	// Observes the cancellation before it changes anything, the kindest case for the session.
	override suspend fun clear() {
		if (cancelOn != "put") yield()
		values.clear()
	}
}
