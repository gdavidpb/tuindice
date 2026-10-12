package com.gdavidpb.tuindice.di

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

	// A save cut before it gets the lock of the secure store (a reader holds it) writes nothing there. The
	// memory mirror is written before the store, as the save orders it, so the process goes on with the new
	// session while the store still holds the previous one.
	@Test
	fun aSaveCutBeforeTheStoreLock_writesNothingInTheSecureStore() = runTest {
		val rig = SaveRig()
		val old = rig.old()
		val readGate = rig.active.gateReads()

		val reader = launch { rig.preferences.hasActiveSession() }
		runCurrent()
		val request = launch {
			rig.repository.replaceSessionSnapshotIfCurrent(expectedSnapshot = old, newSnapshot = REFRESHED)
		}
		runCurrent()
		request.cancel()
		readGate.complete(Unit)
		advanceUntilIdle()

		assertEquals(true, reader.isCompleted && !reader.isCancelled)
		assertEquals(
			mapOf(
				PreferencesKeys.USER_USB_ID to "12-34567",
				PreferencesKeys.USER_SESSION_ID to "session-1",
				PreferencesKeys.USER_ACCESS_TOKEN to "access-old",
				PreferencesKeys.USER_REFRESH_TOKEN to "refresh-old"
			),
			rig.active.values,
			"nothing was written in the secure store"
		)
		assertEquals("access-new", rig.memory.getAccessToken(), "the memory mirror was written before")
	}

	private companion object {
		val REFRESHED = SessionSnapshot("session-1", "access-new", "refresh-new", "12-34567")
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
