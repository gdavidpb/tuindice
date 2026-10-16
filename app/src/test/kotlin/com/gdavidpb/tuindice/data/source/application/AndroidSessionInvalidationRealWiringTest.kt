package com.gdavidpb.tuindice.data.source.application

import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SessionDataSource
import com.gdavidpb.tuindice.base.data.source.SessionInvalidationDataSource
import com.gdavidpb.tuindice.base.domain.coroutine.AppCoroutineScope
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import com.gdavidpb.tuindice.security.data.model.AttestationProofOfPossessionRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

// The end of the session with the classes the app really runs it with: the real session store over the
// real secure-store source and the in-memory mirror, and the real Android wipe. The wipe empties the
// stores the session is read from, so a caller that is cancelled while it runs must still find the end
// of the session whole: the session gone only together with its notice persisted and its signal emitted.
@OptIn(ExperimentalCoroutinesApi::class)
class AndroidSessionInvalidationRealWiringTest {
	@Test
	fun aCallerCancelledWhileTheDatabaseIsWiped_stillLeavesTheNoticeAndTheSignal() = runTest {
		val rig = Rig(this)
		rig.database = {
			rig.caller.cancel()
			// A step that checks cancellation, and one that does not, one after the other.
			yield()
		}

		rig.run()

		rig.assertEndedWhole()
	}

	@Test
	fun aCallerCancelledWhileTheSecureStoreIsCleared_stillLeavesTheNoticeAndTheSignal() = runTest {
		val rig = Rig(this)
		rig.onLegacyClear = {
			rig.caller.cancel()
			yield()
		}

		rig.run()

		rig.assertEndedWhole()
	}

	private class Rig(private val testScope: TestScope) {
		val settings = RecordingSettingsRepository(localDataOwner = "12-34567")
		val signals = CountingSignals(SessionInvalidationDataSource())
		val active = MapStore()
		val legacy = MapStore()
		val memory = InMemorySessionDataSource()
		val session = SessionDataSource(
			memorySessionDataSource = memory,
			preferencesSessionDataSource = SecureStoreSessionDataSource(
				secureStore = active,
				legacySecureStore = legacy,
				sessionInvalidationRepository = signals,
				settingsRepository = settings
			)
		)

		// What the invalidation called on a collaborator it does not use: its steps are guarded, so a throw
		// there would be swallowed, and the call is recorded to fail the test instead.
		private val unexpectedCalls = mutableListOf<String>()

		var database: suspend () -> Unit = {}
		var onLegacyClear: suspend () -> Unit
			get() = legacy.onClear
			set(value) {
				legacy.onClear = value
			}

		lateinit var caller: Job

		private val dispatcher = StandardTestDispatcher(testScope.testScheduler)
		private val recovery = SessionRecoveryDataSource(
			sessionRepository = session,
			applicationRepository = AndroidApplicationDataSource(
				context = TestContext(),
				persistenceMaintenanceRepository = object : PersistenceMaintenanceRepository {
					override suspend fun clearAll() = database()
				},
				settingsRepository = settings,
				secureStore = active,
				legacySecureStore = legacy,
				proofOfPossessionCapability = object : AndroidProofOfPossessionCapability {
					override suspend fun resolveProofOfPossessionKeyId(): String = "key-id"

					override suspend fun invalidateProofOfPossessionKeyId() = Unit

					override suspend fun createProofOfPossession(
						attestationInput: String,
						keyId: String,
						requireKeyAttestation: Boolean
					): AttestationProofOfPossessionRequest = error("not used")
				},
				sessionMemory = { listOf(memory) },
				sessionResidue = { emptyList() }
			),
			sessionInvalidationRepository = signals,
			syncStatusRepository = unused<SyncStatusRepository>(allowed = setOf("reset"), unexpected = unexpectedCalls),
			attestationRepository = unused<AttestationRepository>(unexpected = unexpectedCalls),
			authRepository = unused<AuthRepository>(unexpected = unexpectedCalls),
			credentialsRepository = unused<CredentialsRepository>(unexpected = unexpectedCalls),
			settingsRepository = settings,
			reportingRepository = unused<ReportingRepository>(allowed = setOf("logException"), unexpected = unexpectedCalls),
			sessionCoroutineScope = SessionCoroutineScope(TestDispatchers(dispatcher)),
			appCoroutineScope = AppCoroutineScope(TestDispatchers(dispatcher))
		)

		suspend fun run() {
			session.setSessionSnapshot(SessionSnapshot("session-old", "access-old", "refresh-old", "12-34567"))

			caller = testScope.launch(start = CoroutineStart.UNDISPATCHED) {
				recovery.invalidateSession(sessionId = "session-old")
			}
			testScope.advanceUntilIdle()
		}

		suspend fun assertEndedWhole() {
			assertTrue("the caller was cancelled", caller.isCancelled)
			assertNull("the session is gone", session.getActiveSessionSnapshot())
			assertEquals(
				"with its notice persisted",
				SessionResetNoticeReason.ServerEndedSession,
				settings.notice?.reason
			)
			assertEquals("and its signal emitted once", 1, signals.raised)
			assertEquals("no call to a collaborator it does not use", emptyList<String>(), unexpectedCalls)
		}
	}
}

private class CountingSignals(
	private val delegate: SessionInvalidationRepository
) : SessionInvalidationRepository by delegate {
	var raised = 0
		private set

	override fun notifySessionInvalidated(sessionId: String?): Boolean {
		return delegate.notifySessionInvalidated(sessionId).also { result -> if (result) raised++ }
	}
}

private class MapStore : SecureKeyValueDataRepository {
	val values = mutableMapOf<String, String>()
	var onClear: suspend () -> Unit = {}

	override suspend fun getString(key: String): String? = values[key]

	override suspend fun putString(key: String, value: String) {
		values[key] = value
	}

	override suspend fun remove(key: String) {
		values.remove(key)
	}

	// A store whose clear really suspends (as KSafe's does).
	override suspend fun clear() {
		onClear()
		values.clear()
	}
}

private class TestDispatchers(dispatcher: CoroutineDispatcher) : TuIndiceDispatchers {
	override val main: CoroutineDispatcher = dispatcher
	override val default: CoroutineDispatcher = dispatcher
	override val io: CoroutineDispatcher = dispatcher
	override val unconfined: CoroutineDispatcher = dispatcher
}

// A collaborator the invalidation does not use, except for the calls in [allowed] (which answer Unit):
// any other call is recorded in [unexpected] and fails.
private inline fun <reified T : Any> unused(
	allowed: Set<String> = emptySet(),
	unexpected: MutableList<String>
): T {
	return Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
		when (method.name) {
			"toString" -> "unused"
			"hashCode" -> 0
			"equals" -> false
			in allowed -> Unit
			else -> {
				unexpected += "${T::class.simpleName}.${method.name}"
				error("unexpected call to ${T::class.simpleName}.${method.name}")
			}
		}
	} as T
}
