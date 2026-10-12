package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SessionDataSource
import com.gdavidpb.tuindice.base.data.source.SessionInvalidationDataSource
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncStatusRepository
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testing.ForeignCancellationReporting
import com.gdavidpb.tuindice.testing.InMemorySecureStore
import com.gdavidpb.tuindice.testing.ProductionLikeApplicationRepository
import com.gdavidpb.tuindice.testing.RealSessionStore
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.coroutines.testAppCoroutineScope
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// One session ends once, whoever asks for it and however they meet: the wipe, the signal, the notice and
// the report happen once, and the notice says what happened.
@OptIn(ExperimentalCoroutinesApi::class)
class SessionInvalidationOneSessionOneWipeTest {
	// The sync (inside the scope) meets the dead session first and ends it. Its branch is cancelled when
	// the wipe STARTS, so it frees the recovery lock while the session is still readable. A screen request
	// that was waiting for that lock reads the dead session, asks for its own refresh, and gets the
	// rejection once the wipe is over: it invalidates again.
	@Test
	fun aScreenRequestWaitingForTheRecoveryLock_doesNotWipeASecondTime() = runTest {
		val rig = OneWipeRig(this)
		val secondRefresh = CompletableDeferred<Unit>()
		var refreshCalls = 0
		val recovery = rig.recovery(
			authRepository = OneWipeAuthRepository {
				refreshCalls += 1
				if (refreshCalls == 2) secondRefresh.await()
				throw clientRequestException(HttpStatusCode.Unauthorized)
			}
		)

		rig.sessionScope.launch {
			recovery.recoverUnauthorizedSession("access-old", null, null)
		}
		launch {
			recovery.recoverUnauthorizedSession("access-old", null, null)
		}
		runCurrent()

		assertEquals(1, rig.application.clearCalls, "the first wipe ran to its end")
		assertEquals(2, refreshCalls, "the screen request is asking for its own refresh of the dead session")

		secondRefresh.complete(Unit)
		advanceUntilIdle()

		assertEquals(1, rig.application.clearCalls, "one session, one wipe")
		assertEquals(1, rig.invalidations.raised, "and one signal")
	}

	// A reader of the session is caught between two keys by the wipe of the secure store (which does
	// not take the lock of the session store): it sees a half-written session and the store ends "it"
	// with its own notice and signal, on top of the invalidation that is running.
	@Test
	fun aReaderCaughtMidReadByTheWipe_doesNotRaiseASecondSignalNorAnotherNotice() = runTest {
		val settings = FakeSettingsRepository()
		val invalidations = NoticeRecordingSessionInvalidations(settings)
		val active = GatedReadSecureStore()
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
		active.values[PreferencesKeys.USER_USB_ID] = "12-34567"
		active.values[PreferencesKeys.USER_SESSION_ID] = "session-old"
		active.values[PreferencesKeys.USER_ACCESS_TOKEN] = "access-old"
		active.values[PreferencesKeys.USER_REFRESH_TOKEN] = "refresh-old"

		val dispatcher = StandardTestDispatcher(testScheduler)
		val gate = CompletableDeferred<Unit>()
		val testScope = this
		val application = ProductionLikeApplicationRepository(
			settings = settings,
			stores = listOf(active, legacy),
			memory = listOf(memory),
			database = {
				// A screen reads the session while the database is being wiped: it has read the first key.
				active.gateAtRead(2, gate)
				testScope.launch(start = CoroutineStart.UNDISPATCHED) { repository.getActiveSessionSnapshot() }
			}
		)
		val recovery = SessionRecoveryDataSource(
			sessionRepository = repository,
			applicationRepository = application,
			sessionInvalidationRepository = invalidations,
			syncStatusRepository = FakeSyncStatusRepository(),
			attestationRepository = OneWipeAttestation,
			authRepository = OneWipeAuthRepository { error("unused") },
			credentialsRepository = FakeCredentialsRepository(),
			settingsRepository = settings,
			reportingRepository = RecordingReportingRepository(),
			sessionCoroutineScope = testSessionCoroutineScope(dispatcher),
			appCoroutineScope = testAppCoroutineScope(dispatcher)
		)

		launch { recovery.invalidateSession(sessionId = "session-old") }
		runCurrent()
		gate.complete(Unit)
		advanceUntilIdle()

		assertEquals(1, invalidations.raised, "one session ended: one signal")
		assertEquals(
			listOf<SessionResetNoticeReason?>(SessionResetNoticeReason.ServerEndedSession),
			invalidations.noticesWhenRaised,
			"and the notice says the server ended it"
		)
	}

	// A step of the wipe throws a CancellationException that is not the cancellation of the job (a
	// timeout inside the step, a cancelled child it waited for): it is one more failure of that step, and
	// the invalidation goes on to the other steps, the notice and the signal.
	@Test
	fun aStepThatThrowsACancellationOfItsOwn_doesNotCutTheInvalidation() = runTest {
		val rig = OneWipeRig(this)
		rig.database = { withTimeout(1) { delay(1_000) } }
		val recovery = rig.recovery(authRepository = OneWipeAuthRepository { error("unused") })

		val caller = launch { recovery.invalidateSession(sessionId = "session-old") }
		advanceUntilIdle()

		assertFalse(caller.isCancelled, "nobody cancelled the caller")
		assertTrue(rig.store.isGone(), "the session is gone")
		assertEquals(1, rig.invalidations.raised, "and told")
	}

	// A CancellationException that is not the job's, thrown from inside cancelActiveWork() (a child that
	// ignores its cancellation makes the pass time out and the report of it throws): one more failure of
	// that step, so the wipe, the notice and the signal still happen.
	@Test
	fun aForeignCancellationInsideCancelActiveWork_doesNotCutTheInvalidation() = runTest {
		val foreign = ForeignCancellationReporting()
		val rig = OneWipeRig(this, scopeReporting = foreign)
		val never = CompletableDeferred<Unit>()
		rig.sessionScope.launch { withContext(NonCancellable) { never.await() } }
		runCurrent()
		val recovery = rig.recovery(authRepository = OneWipeAuthRepository { error("unused") })

		val caller = launch { recovery.invalidateSession(sessionId = "session-old") }
		advanceUntilIdle()

		assertTrue(foreign.thrown > 0, "the foreign cancellation was thrown inside cancelActiveWork")
		assertTrue(caller.isCompleted && !caller.isCancelled, "nobody cancelled the caller")
		assertTrue(rig.store.isGone(), "the session is gone")
		assertEquals(1, rig.invalidations.raised, "and told")
		never.complete(Unit)
	}

	// A stored session that cannot be read is not a session that is gone already: the wipe goes on.
	@Test
	fun whenTheStoredSessionCannotBeRead_theWipeGoesOn() = runTest {
		val rig = OneWipeRig(this)
		var failReads = false
		val unreadable = object : SessionRepository by rig.store.repository {
			override suspend fun getActiveSessionSnapshot(): SessionSnapshot? {
				if (failReads) error("unreadable")
				return rig.store.repository.getActiveSessionSnapshot()
			}
		}
		val recovery = rig.recovery(
			authRepository = OneWipeAuthRepository { error("unused") },
			sessionRepository = unreadable
		)

		failReads = true
		launch { recovery.invalidateSession(sessionId = "session-old") }
		advanceUntilIdle()
		failReads = false

		assertEquals(1, rig.application.clearCalls, "the wipe ran")
		assertEquals(1, rig.invalidations.raised, "and the user is told")
	}

	// An invalidation that does not say which session it is about is never skipped; one that names a
	// session that is not the stored one is, because a wipe already ended it or a sign-in replaced it.
	@Test
	fun anInvalidationWithoutASessionIsNeverSkipped_andAnotherSessionsIs() = runTest {
		val rig = OneWipeRig(this)
		val recovery = rig.recovery(authRepository = OneWipeAuthRepository { error("unused") })

		val other = launch { recovery.invalidateSession(sessionId = "session-of-somebody-else") }
		advanceUntilIdle()
		assertTrue(other.isCompleted && !other.isCancelled)
		assertEquals(0, rig.application.clearCalls, "the stored session is not the one named: nothing ends")
		assertEquals(0, rig.invalidations.raised)
		assertFalse(rig.store.isGone(), "the stored session survives")

		launch { recovery.invalidateSession(sessionId = null) }
		advanceUntilIdle()
		assertEquals(1, rig.application.clearCalls)
		assertEquals(1, rig.invalidations.raised)
	}
}

private class OneWipeRig(val testScope: TestScope, scopeReporting: ReportingRepository? = null) {
	val settings = FakeSettingsRepository()
	val invalidations = NoticeRecordingSessionInvalidations(settings)
	val store = RealSessionStore(settings = settings, invalidations = invalidations).also { store -> store.seed() }
	var database: suspend () -> Unit = {}
	private val dispatcher = StandardTestDispatcher(testScope.testScheduler)
	val sessionScope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher), scopeReporting)
	val application = ProductionLikeApplicationRepository(
		settings = settings,
		stores = listOf(store.active, store.legacy),
		memory = listOf(store.memory),
		database = { database() }
	)

	fun recovery(
		authRepository: AuthRepository,
		sessionRepository: SessionRepository = store.repository,
		applicationRepository: ApplicationRepository = application
	) = SessionRecoveryDataSource(
		sessionRepository = sessionRepository,
		applicationRepository = applicationRepository,
		sessionInvalidationRepository = invalidations,
		syncStatusRepository = FakeSyncStatusRepository(),
		attestationRepository = OneWipeAttestation,
		authRepository = authRepository,
		credentialsRepository = FakeCredentialsRepository(),
		settingsRepository = settings,
		reportingRepository = RecordingReportingRepository(),
		sessionCoroutineScope = sessionScope,
		appCoroutineScope = testAppCoroutineScope(dispatcher)
	)
}

// The real signal, with the notice that was persisted at the moment it was raised.
private class NoticeRecordingSessionInvalidations(
	private val settings: FakeSettingsRepository,
	private val delegate: SessionInvalidationRepository = SessionInvalidationDataSource()
) : SessionInvalidationRepository by delegate {
	var raised = 0
		private set
	val noticesWhenRaised = mutableListOf<SessionResetNoticeReason?>()

	override fun notifySessionInvalidated(sessionId: String?): Boolean {
		val result = delegate.notifySessionInvalidated(sessionId)

		if (result) {
			raised++
			noticesWhenRaised += settings.sessionResetNoticeReason
		}

		return result
	}
}

// A secure store whose Nth read (counted from when it is armed) waits: a reader caught between two keys.
private class GatedReadSecureStore : SecureKeyValueDataRepository {
	val values = mutableMapOf<String, String>()
	private var readsUntilGate = -1
	private var gate: CompletableDeferred<Unit>? = null

	fun gateAtRead(read: Int, gate: CompletableDeferred<Unit>) {
		readsUntilGate = read
		this.gate = gate
	}

	override suspend fun getString(key: String): String? {
		if (readsUntilGate > 0) {
			readsUntilGate -= 1

			if (readsUntilGate == 0) {
				val held = gate
				gate = null
				held?.await()
			}
		}

		return values[key]
	}

	override suspend fun putString(key: String, value: String) {
		values[key] = value
	}

	override suspend fun remove(key: String) {
		values.remove(key)
	}

	override suspend fun clear() {
		values.clear()
	}
}

private object OneWipeAttestation : AttestationRepository {
	override suspend fun attest(request: AttestationRequest) = Attestation(token = "token")
}

private class OneWipeAuthRepository(
	private val onRefresh: suspend () -> RefreshTokens
) : AuthRepository {
	override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation) = error("unused")

	override suspend fun reissueTokens(usbId: String, password: String, attestation: Attestation) = error("unused")

	override suspend fun refreshTokens(sessionId: String, refreshToken: String, attestation: Attestation): RefreshTokens =
		onRefresh()

	override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) =
		error("unused")
}
