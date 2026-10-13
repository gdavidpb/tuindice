package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.data.source.AuthDataSource
import com.gdavidpb.tuindice.auth.domain.model.AttestedTokenFlow
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.IssueTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.data.source.network.AuthErrorHeaders
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncStatusRepository
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testing.CountingSessionInvalidationRepository
import com.gdavidpb.tuindice.testing.ProductionLikeApplicationRepository
import com.gdavidpb.tuindice.testing.RealSessionStore
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.coroutines.testAppCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// After the server processed the revoke of a session, a request of the device that meets it is answered
// `session_superseded` (the session document is gone) and the app answers that with the recovery by the
// stored credentials. A sign-out that is under way, or already over, must not get a session back from it:
// the device would be authenticated after the user asked to leave. The real AuthDataSource persists what
// the recovery obtains.
@OptIn(ExperimentalCoroutinesApi::class)
class SessionRecoveryDuringSignOutTest {
	// The usual case: the request that meets the revoked session belongs to the session scope (the sync).
	// The local end of the sign-out cancels it while its recovery is in flight.
	@Test
	fun aSessionScopeRecoveryInFlight_isCancelledByTheLocalEndOfTheSignOut() = runTest {
		val rig = RecoveryRig(this)
		val revokeGate = CompletableDeferred<Unit>()
		val bootstrapGate = CompletableDeferred<Unit>()
		rig.api.onRevoke = { revokeGate.await() }
		rig.api.onBootstrap = { bootstrapGate.await() }

		val request = rig.sessionScope.launch {
			runCatching { rig.recovery.recoverUnauthorizedSession("access-old", null, null) }
		}
		runCurrent()
		assertEquals(1, rig.api.bootstrapCalls, "the recovery by credentials is in flight")

		var states: List<Any>? = null
		launch { states = rig.signOut.execute(Unit).toList() }
		runCurrent()
		revokeGate.complete(Unit)
		advanceUntilIdle()
		bootstrapGate.complete(Unit)
		advanceUntilIdle()

		assertTrue(request.isCancelled, "the local end cancelled the work of the session")
		assertTrue(states?.lastOrNull() is UseCaseState.Data<*>)
		assertTrue(rig.store.isGone(), "the session stays gone")
		assertNull(rig.credentials.password)
		assertEquals(0, rig.api.exchangeCalls)
		assertEquals(1, rig.application.clearCalls)
		assertEquals(0, rig.invalidations.raised)
	}

	// The same recovery from a request outside the session scope (a screen): nothing cancels it, and it ends
	// after the local end of the sign-out, which left no session for it to recover.
	@Test
	fun aScreenRecoveryThatEndsAfterTheLocalEnd_doesNotBringTheSessionBack() = runTest {
		val rig = RecoveryRig(this)
		val revokeGate = CompletableDeferred<Unit>()
		val bootstrapGate = CompletableDeferred<Unit>()
		rig.api.onRevoke = { revokeGate.await() }
		rig.api.onBootstrap = { bootstrapGate.await() }

		var recovered: SessionSnapshot? = null
		val request = launch { recovered = rig.recovery.recoverUnauthorizedSession("access-old", null, null) }
		runCurrent()
		assertEquals(1, rig.api.bootstrapCalls, "the recovery by credentials is in flight")

		var states: List<Any>? = null
		launch { states = rig.signOut.execute(Unit).toList() }
		runCurrent()
		revokeGate.complete(Unit)
		advanceUntilIdle()
		assertTrue(rig.store.isGone(), "the sign-out ended the session")
		bootstrapGate.complete(Unit)
		advanceUntilIdle()

		assertTrue(request.isCompleted)
		assertNull(recovered, "the recovery has nothing to hand to the request")
		assertTrue(states?.lastOrNull() is UseCaseState.Data<*>)
		assertTrue(rig.store.isGone(), "no session comes back after the sign-out")
		assertNull(rig.credentials.password, "and no password is left to open one")
		assertEquals(0, rig.api.exchangeCalls, "the tokens were never asked for")
		assertEquals(0, rig.invalidations.raised)
		assertNull(rig.settings.sessionResetNotice)
	}

	// The recovery ends before the local end of the sign-out reads the stored session: what it stored is
	// another session, and the device is left clean all the same.
	@Test
	fun aRecoveryThatEndsBeforeTheLocalEnd_leavesTheDeviceCleanAnyway() = runTest {
		val rig = RecoveryRig(this)
		val revokeGate = CompletableDeferred<Unit>()
		val bootstrapGate = CompletableDeferred<Unit>()
		rig.api.onRevoke = { revokeGate.await() }
		rig.api.onBootstrap = { bootstrapGate.await() }

		val request = rig.sessionScope.launch {
			runCatching { rig.recovery.recoverUnauthorizedSession("access-old", null, null) }
		}
		runCurrent()

		var states: List<Any>? = null
		launch { states = rig.signOut.execute(Unit).toList() }
		runCurrent()
		bootstrapGate.complete(Unit)
		runCurrent()
		assertEquals(1, rig.api.exchangeCalls, "the recovery ended before the revoke returned")
		assertEquals("session-recovered", rig.store.repository.getActiveSessionSnapshot()?.sessionId)

		revokeGate.complete(Unit)
		advanceUntilIdle()

		assertTrue(request.isCompleted)
		assertTrue(states?.lastOrNull() is UseCaseState.Data<*>)
		assertTrue(rig.store.isGone(), "the device holds no session after a sign-out that said it was done")
		assertNull(rig.credentials.password)
		assertEquals(1, rig.application.clearCalls)
		assertEquals(0, rig.invalidations.raised)
	}

	// The sign-out already marked the session when the request met the rejection: the sign-out is the one that
	// ends it, so the recovery does not start, does not invalidate and does not tell the user anything.
	@Test
	fun aRejectionMeetingASessionBeingSignedOut_isNotRecovered() = runTest {
		val rig = RecoveryRig(this)
		val revokeGate = CompletableDeferred<Unit>()
		rig.api.onRevoke = { revokeGate.await() }

		var states: List<Any>? = null
		launch { states = rig.signOut.execute(Unit).toList() }
		runCurrent()

		var recovered: SessionSnapshot? = null
		val request = launch { recovered = rig.recovery.recoverUnauthorizedSession("access-old", null, null) }
		advanceUntilIdle()

		assertTrue(request.isCompleted)
		assertNull(recovered)
		assertEquals(0, rig.api.bootstrapCalls, "the stored credentials were not used")
		assertEquals(0, rig.application.clearCalls, "nothing was wiped by the request")
		assertEquals(0, rig.invalidations.raised)
		assertNull(rig.settings.sessionResetNotice)

		revokeGate.complete(Unit)
		advanceUntilIdle()

		assertTrue(states?.lastOrNull() is UseCaseState.Data<*>)
		assertTrue(rig.store.isGone())
		assertEquals(1, rig.application.clearCalls, "the sign-out wiped the device")
	}

	// Without a sign-out in the way, the recovery by credentials keeps doing what it is for.
	@Test
	fun aRecoveryByCredentialsWithNoSignOut_storesTheNewSession() = runTest {
		val rig = RecoveryRig(this)

		var recovered: SessionSnapshot? = null
		launch { recovered = rig.recovery.recoverUnauthorizedSession("access-old", null, null) }
		advanceUntilIdle()

		assertEquals(1, rig.api.exchangeCalls)
		assertEquals("session-recovered", recovered?.sessionId)
		assertEquals(recovered, rig.store.repository.getActiveSessionSnapshot(), "the new session is the stored one")
		assertEquals(0, rig.application.clearCalls)
		assertEquals(0, rig.invalidations.raised)
	}

	// The session was replaced by another one while the recovery waited for the server: the recovery hands
	// that one back and does not write over it.
	@Test
	fun aRecoveryWhoseSessionWasReplacedMeanwhile_doesNotOverwriteTheNewOne() = runTest {
		val rig = RecoveryRig(this)
		val bootstrapGate = CompletableDeferred<Unit>()
		rig.api.onBootstrap = { bootstrapGate.await() }

		var recovered: SessionSnapshot? = null
		val request = launch { recovered = rig.recovery.recoverUnauthorizedSession("access-old", null, null) }
		runCurrent()
		assertEquals(1, rig.api.bootstrapCalls)

		val newSession = SessionSnapshot("session-new", "access-new", "refresh-new", "12-34567")
		rig.store.repository.setSessionSnapshot(newSession)
		bootstrapGate.complete(Unit)
		advanceUntilIdle()

		assertTrue(request.isCompleted)
		assertEquals(newSession, recovered)
		assertEquals(newSession, rig.store.repository.getActiveSessionSnapshot(), "the new session is intact")
		assertEquals(0, rig.api.exchangeCalls, "the tokens of the old session were never asked for")
	}
}

private class RecoveryRig(testScope: TestScope) {
	val settings = FakeSettingsRepository()
	val invalidations = CountingSessionInvalidationRepository()
	val reporting = RecordingReportingRepository()
	val store = RealSessionStore(settings = settings, invalidations = invalidations).also { it.seed() }
	val credentials = FakeCredentialsRepository(password = "secret")
	val api = ServerAfterTheRevokeApi()

	private val dispatcher = StandardTestDispatcher(testScope.testScheduler)
	val sessionScope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher), null)
	val application = ProductionLikeApplicationRepository(
		settings = settings,
		stores = listOf(store.active, store.legacy),
		memory = listOf(store.memory),
		// The wipe of the app empties the secure store the password lives in.
		database = { credentials.password = null }
	)
	private val attestation = object : AttestationRepository {
		override suspend fun attest(request: AttestationRequest) = Attestation(token = "token")
	}
	val auth = AuthDataSource(
		authApiDataSource = api,
		sessionRepository = store.repository,
		reportingRepository = reporting
	)
	val recovery = SessionRecoveryDataSource(
		sessionRepository = store.repository,
		applicationRepository = application,
		sessionInvalidationRepository = invalidations,
		syncStatusRepository = FakeSyncStatusRepository(),
		attestationRepository = attestation,
		authRepository = auth,
		credentialsRepository = credentials,
		settingsRepository = settings,
		reportingRepository = reporting,
		sessionCoroutineScope = sessionScope,
		appCoroutineScope = testAppCoroutineScope(dispatcher)
	)
	val signOut = SignOutUseCase(
		authRepository = auth,
		attestationRepository = attestation,
		sessionRepository = store.repository,
		sessionInvalidationRepository = invalidations,
		sessionEndRepository = recovery,
		reportingRepository = reporting
	)
}

// The server after the revoke: the refresh of the revoked session is answered `session_superseded`, and the
// stored credentials still open a new session.
private class ServerAfterTheRevokeApi : AuthApiDataRepository {
	var onBootstrap: suspend () -> Unit = {}
	var onRevoke: suspend () -> Unit = {}
	var bootstrapCalls = 0
	var exchangeCalls = 0

	override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens {
		bootstrapCalls += 1
		onBootstrap()
		return BootstrapTokens(uid = "uid", usbId = usbId, accessToken = "bootstrap-access", expiresIn = 3600)
	}

	override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation): IssueTokens {
		exchangeCalls += 1
		return IssueTokens(
			uid = "uid",
			usbId = "12-34567",
			sessionId = "session-recovered",
			accessToken = "access-recovered",
			refreshToken = "refresh-recovered",
			expiresIn = 3600
		)
	}

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestedFlow: AttestedTokenFlow,
		attestation: Attestation
	): IssueTokens = error("unused")

	override suspend fun refreshTokens(sessionId: String, refreshToken: String, attestation: Attestation): RefreshTokens {
		throw clientRequestException(
			statusCode = HttpStatusCode.Unauthorized,
			headers = mapOf(AuthErrorHeaders.HEADER to AuthErrorHeaders.SESSION_SUPERSEDED)
		)
	}

	override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) = onRevoke()
}
