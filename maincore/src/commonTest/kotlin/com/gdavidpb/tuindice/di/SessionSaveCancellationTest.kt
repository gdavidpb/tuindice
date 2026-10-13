package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.repository.MemorySessionDataRepository
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SessionDataSource
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.testing.CountingSessionInvalidationRepository
import com.gdavidpb.tuindice.testing.InMemorySecureStore
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

// A refreshed session is saved after the server already rotated its tokens, so a save that is cut by the
// cancellation of the request that carries it must not leave the secure store with the tokens the server
// no longer accepts: the next start of the process reads from there.
@OptIn(ExperimentalCoroutinesApi::class)
class SessionSaveCancellationTest {
	// The legacy store really suspends (KSafe does) and the request is cancelled at its first removal.
	@Test
	fun aSaveCutAtTheFirstLegacyRemoval_leavesTheNewSessionWholeInTheSecureStore() = runTest {
		val rig = SaveRig()
		lateinit var request: Job
		rig.legacy.onRemove = { request.cancel() }

		request = launch {
			rig.repository.replaceSessionSnapshotIfCurrent(expectedSnapshot = rig.old(), newSnapshot = REFRESHED)
		}
		advanceUntilIdle()

		assertEquals("access-new", rig.memory.getAccessToken(), "the process goes on with the refreshed session")
		assertEquals(
			mapOf(
				PreferencesKeys.USER_USB_ID to "12-34567",
				PreferencesKeys.USER_SESSION_ID to "session-1",
				PreferencesKeys.USER_ACCESS_TOKEN to "access-new",
				PreferencesKeys.USER_REFRESH_TOKEN to "refresh-new"
			),
			rig.active.values,
			"and the next start finds the refresh token the server now expects"
		)
	}

	// The request is cancelled at each write of the memory mirror, before the store is touched. Through the
	// app's wiring there is no suspension point between the mirror and the four keys, so the store ends whole,
	// whether the legacy store suspends or not.
	@Test
	fun aSaveWhoseJobIsCancelledWhileTheMirrorIsWritten_stillStoresTheWholeSession() = runTest {
		for (cutAt in 1..4) for (suspendingLegacy in listOf(false, true)) {
			val label = "cutAt=$cutAt suspendingLegacy=$suspendingLegacy"
			val active = InMemorySecureStore().also { it.values += OLD_VALUES }
			val mirror = InMemorySessionDataSource()
			lateinit var request: Job
			var armed = false
			var writes = 0
			val cancelling = object : MemorySessionDataRepository by mirror {
				private fun cut() {
					if (armed && ++writes == cutAt) request.cancel()
				}

				override suspend fun setUsbId(usbId: String) {
					cut()
					mirror.setUsbId(usbId)
				}

				override suspend fun setSessionId(sessionId: String) {
					cut()
					mirror.setSessionId(sessionId)
				}

				override suspend fun setAccessToken(accessToken: String) {
					cut()
					mirror.setAccessToken(accessToken)
				}

				override suspend fun setRefreshToken(refreshToken: String) {
					cut()
					mirror.setRefreshToken(refreshToken)
				}
			}
			val repository = SessionDataSource(
				memorySessionDataSource = cancelling,
				preferencesSessionDataSource = SecureStoreSessionDataSource(
					secureStore = active,
					legacySecureStore = YieldingStore(yields = suspendingLegacy),
					sessionInvalidationRepository = CountingSessionInvalidationRepository(),
					settingsRepository = FakeSettingsRepository()
				)
			)
			val old = checkNotNull(repository.getActiveSessionSnapshot())
			armed = true

			request = launch { repository.replaceSessionSnapshotIfCurrent(expectedSnapshot = old, newSnapshot = REFRESHED) }
			advanceUntilIdle()

			assertEquals(true, request.isCancelled, "$label: the request was cancelled")
			assertEquals(NEW_VALUES, active.values.toMap(), "$label: the store holds the whole new session")
			assertEquals("access-new", mirror.getAccessToken(), label)
			assertEquals("refresh-new", mirror.getRefreshToken(), label)
		}
	}

	// A save cut while it waits for the lock of the session, the only wait it can meet before writing,
	// writes nothing anywhere: the mirror and the store stay with the previous session.
	@Test
	fun aSaveCutWhileItWaitsForTheSessionLock_writesNothingAnywhere() = runTest {
		val active = InMemorySecureStore().also { it.values += OLD_VALUES }
		val mirror = InMemorySessionDataSource()
		val repository = SessionDataSource(
			memorySessionDataSource = mirror,
			preferencesSessionDataSource = SecureStoreSessionDataSource(
				secureStore = active,
				legacySecureStore = InMemorySecureStore(),
				sessionInvalidationRepository = CountingSessionInvalidationRepository(),
				settingsRepository = FakeSettingsRepository()
			)
		)
		val old = SessionSnapshot("session-1", "access-old", "refresh-old", "12-34567")
		val readGate = active.gateReads()

		val reader = launch { repository.getActiveSessionSnapshot() }
		runCurrent()
		val request = launch { repository.replaceSessionSnapshotIfCurrent(expectedSnapshot = old, newSnapshot = REFRESHED) }
		runCurrent()
		request.cancel()
		readGate.complete(Unit)
		advanceUntilIdle()

		assertEquals(true, reader.isCompleted && !reader.isCancelled)
		assertEquals(OLD_VALUES, active.values.toMap(), "nothing written in the store")
		assertEquals("access-old", mirror.getAccessToken(), "nor in the mirror")
		assertEquals("refresh-old", mirror.getRefreshToken())
	}

	// The four keys of an older layout would be read again by the next start if they stayed.
	@Test
	fun aSave_leavesNoCopyOfTheSessionInTheLegacyStore() = runTest {
		val active = InMemorySecureStore().also { it.values += OLD_VALUES }
		val legacy = InMemorySecureStore().also { it.values += OLD_VALUES }
		val repository = SessionDataSource(
			memorySessionDataSource = InMemorySessionDataSource(),
			preferencesSessionDataSource = SecureStoreSessionDataSource(
				secureStore = active,
				legacySecureStore = legacy,
				sessionInvalidationRepository = CountingSessionInvalidationRepository(),
				settingsRepository = FakeSettingsRepository()
			)
		)
		val old = checkNotNull(repository.getActiveSessionSnapshot())

		repository.replaceSessionSnapshotIfCurrent(expectedSnapshot = old, newSnapshot = REFRESHED)

		assertEquals(NEW_VALUES, active.values.toMap())
		assertEquals(emptyMap(), legacy.values.toMap(), "the legacy store is empty")
	}

	// Removing the legacy copy is cleaning up after the save: its failure does not fail it nor leave the
	// active store incomplete.
	@Test
	fun aLegacyRemovalThatFails_doesNotFailTheSaveNorLeaveTheStoreIncomplete() = runTest {
		val active = InMemorySecureStore().also { it.values += OLD_VALUES }
		val mirror = InMemorySessionDataSource()
		val repository = SessionDataSource(
			memorySessionDataSource = mirror,
			preferencesSessionDataSource = SecureStoreSessionDataSource(
				secureStore = active,
				legacySecureStore = FailingRemovalStore(),
				sessionInvalidationRepository = CountingSessionInvalidationRepository(),
				settingsRepository = FakeSettingsRepository()
			)
		)
		val old = checkNotNull(repository.getActiveSessionSnapshot())

		val replaced = repository.replaceSessionSnapshotIfCurrent(expectedSnapshot = old, newSnapshot = REFRESHED)

		assertEquals(true, replaced, "the save went through")
		assertEquals(NEW_VALUES, active.values.toMap(), "the active store holds the whole new session")
		assertEquals("access-new", mirror.getAccessToken())
	}

	private companion object {
		val REFRESHED = SessionSnapshot("session-1", "access-new", "refresh-new", "12-34567")
		val NEW_VALUES = mapOf(
			PreferencesKeys.USER_USB_ID to "12-34567",
			PreferencesKeys.USER_SESSION_ID to "session-1",
			PreferencesKeys.USER_ACCESS_TOKEN to "access-new",
			PreferencesKeys.USER_REFRESH_TOKEN to "refresh-new"
		)
		val OLD_VALUES = NEW_VALUES + mapOf(
			PreferencesKeys.USER_ACCESS_TOKEN to "access-old",
			PreferencesKeys.USER_REFRESH_TOKEN to "refresh-old"
		)
	}
}

private class SaveRig {
	val settings = FakeSettingsRepository()
	val invalidations = CountingSessionInvalidationRepository()
	val active = InMemorySecureStore()
	val legacy = SuspendingLegacyStore()
	val memory = InMemorySessionDataSource()
	val preferences = SecureStoreSessionDataSource(active, legacy, invalidations, settings)
	val repository = SessionDataSource(memorySessionDataSource = memory, preferencesSessionDataSource = preferences)

	init {
		active.values[PreferencesKeys.USER_USB_ID] = "12-34567"
		active.values[PreferencesKeys.USER_SESSION_ID] = "session-1"
		active.values[PreferencesKeys.USER_ACCESS_TOKEN] = "access-old"
		active.values[PreferencesKeys.USER_REFRESH_TOKEN] = "refresh-old"
	}

	suspend fun old(): SessionSnapshot = checkNotNull(repository.getActiveSessionSnapshot())
}

// A legacy store with nothing in it whose removal really suspends, as KSafe does; [onRemove] runs the
// first time it is asked to remove something.
private class SuspendingLegacyStore : SecureKeyValueDataRepository {
	var onRemove: () -> Unit = {}
	private var fired = false

	override suspend fun getString(key: String): String? = null
	override suspend fun putString(key: String, value: String) = Unit
	override suspend fun remove(key: String) {
		if (!fired) {
			fired = true
			onRemove()
		}

		yield()
	}

	override suspend fun clear() = Unit
}

// A legacy store with nothing in it whose removal suspends, as KSafe does, or not.
private class YieldingStore(private val yields: Boolean) : SecureKeyValueDataRepository {
	override suspend fun getString(key: String): String? = null
	override suspend fun putString(key: String, value: String) = Unit
	override suspend fun remove(key: String) {
		if (yields) yield()
	}

	override suspend fun clear() = Unit
}

private class FailingRemovalStore : SecureKeyValueDataRepository {
	override suspend fun getString(key: String): String? = null
	override suspend fun putString(key: String, value: String) = Unit
	override suspend fun remove(key: String) = error("the legacy store cannot remove")
	override suspend fun clear() = Unit
}
