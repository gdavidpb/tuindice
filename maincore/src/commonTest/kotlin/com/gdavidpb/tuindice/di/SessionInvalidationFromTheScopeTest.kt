package com.gdavidpb.tuindice.di

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.data.source.network.AuthErrorHeaders
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.model.SessionResetNotice
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.data.repository.sync.FakeAcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.data.repository.sync.FakeEvaluationsRevalidationRepository
import com.gdavidpb.tuindice.data.repository.sync.FakePensumRevalidationRepository
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncRemoteDataSource
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncSettingsLocalDataSource
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncStatusRepository
import com.gdavidpb.tuindice.data.repository.sync.FakeUserLocalDataRepository
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncRemoteException
import com.gdavidpb.tuindice.data.source.sync.SyncResultLocalDataSource
import com.gdavidpb.tuindice.domain.usecase.AcknowledgeSessionInvalidationUseCase
import com.gdavidpb.tuindice.domain.usecase.ObserveSessionInvalidationUseCase
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testing.NoOpRecordOutboxDataRepository
import com.gdavidpb.tuindice.testing.ProductionLikeApplicationRepository
import com.gdavidpb.tuindice.testing.RealSessionStore
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testAppCoroutineScope
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The path that caused the incidents, end to end: a request that runs inside the session scope
// (the sync, a revalidation) is the one that gets the 401, the refresh is rejected, and the session
// recovery ends the session from within the very branch that is still waiting for the answer.
@OptIn(ExperimentalCoroutinesApi::class)
class SessionInvalidationFromTheScopeTest {
	@Test
	fun aRequestInsideTheScopeThatEndsTheSession_cleansUp_notifies_andLeavesNothingWrittenAfterwards() = runTest {
		val rig = SessionInvalidationRig(this)
		var branchResumedAfterTheRequest = false
		var branchFinished = false

		val branch = rig.sessionScope.launch {
			try {
				rig.client.get("https://api.tuindice.app/record/v5/sync")
				branchResumedAfterTheRequest = true
			} catch (_: ClientRequestException) {
				branchResumedAfterTheRequest = true
			} finally {
				branchFinished = true
			}
		}
		branch.join()
		rig.finish()

		assertEquals(1, rig.applicationRepository.clearCalls, "the wipe ran")
		assertTrue(rig.store.isGone())
		assertEquals(1, rig.sessionInvalidationRepository.invalidationCalls, "the notice was emitted")
		assertEquals(listOf<String?>("session-old"), rig.sessionInvalidationRepository.notifiedSessionIds)
		assertTrue(branchFinished && branch.isCompleted, "the branch ended")
		assertTrue(branch.isCancelled, "it ended as a cancellation, so nothing after the 401 runs")
		assertFalse(branchResumedAfterTheRequest, "the 401 unwinds as a cancellation, not as a failure to handle")
	}

	@Test
	fun aSyncThatMeetsTheRejectedSession_leavesNoCooldownNorStatusInTheWipedSettings() = runTest {
		val rig = SessionInvalidationRig(this)
		val settings = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatus = FakeSyncStatusRepository(initialValue = SyncStatus.Healthy)
		val remote = FakeSyncRemoteDataSource()
		remote.onSyncStarted = {
			try {
				rig.client.get("https://api.tuindice.app/record/v5/sync")
			} catch (exception: ClientRequestException) {
				throw SyncRemoteException(
					statusCode = exception.response.status,
					syncReport = null,
					conflictReason = null,
					cause = exception
				)
			}
		}
		val sync = SyncDataSource(
			settingsDataSource = settings,
			syncStatusRepository = syncStatus,
			remoteDataSource = remote,
			syncResultLocalDataSource = SyncResultLocalDataSource(
				recordLocalDataSource = FakeAcademicRecordLocalDataRepository(),
				userLocalDataSource = FakeUserLocalDataRepository(),
				recordOutboxDataSource = NoOpRecordOutboxDataRepository
			),
			pensumRevalidationRepository = FakePensumRevalidationRepository(),
			evaluationsRevalidationRepository = FakeEvaluationsRevalidationRepository(),
			coroutineScope = rig.sessionScope
		)

		sync.scheduleSync(password = "secret", policy = SyncPolicy.ForceRefresh)
		rig.settle()
		rig.finish()

		assertEquals(1, rig.applicationRepository.clearCalls, "the wipe ran")
		assertEquals(1, rig.sessionInvalidationRepository.invalidationCalls)
		assertFalse(settings.cooldownMarked, "no sync cooldown written into the wiped settings")
		assertFalse(settings.syncRetryBackoffMarked)
		assertEquals(emptyList(), syncStatus.setStatuses, "no status written after the wipe")
		assertEquals(emptyList(), syncStatus.setReports)
	}

	// Two requests of the scope end the session at once: the second arrives while the first is already
	// wiping and is served by that wipe, so the wipe and the notice happen once. (That the first
	// wipe is not cut short by the second is what the cancelled-wipe test below pins down.)
	@Test
	fun twoInvalidationsFromInsideTheScope_wipeOnce() = runTest {
		val rig = SessionInvalidationRig(this)
		val wipeStarted = CompletableDeferred<Unit>()
		val releaseWipe = CompletableDeferred<Unit>()
		val application = rig.productionLike(database = { gate(wipeStarted, releaseWipe) })
		val recovery = rig.recovery(application)

		val first = rig.sessionScope.launch { recovery.invalidateSession(sessionId = "session-old") }
		runCurrent()
		wipeStarted.await()
		val second = rig.sessionScope.launch {
			recovery.invalidateSession(sessionId = "session-old")
		}
		runCurrent()

		releaseWipe.complete(Unit)
		advanceUntilIdle()

		assertEquals(1, application.clearCalls, "the wipe runs exactly once")
		assertEquals(1, rig.sessionInvalidationRepository.invalidationCalls, "and the notice is emitted once")
		assertTrue(first.isCompleted && second.isCompleted)
		assertTrue(first.isCancelled, "the branch that asked first is cancelled when the wipe starts")
		assertTrue(second.isCancelled, "the branch that met the finished wipe ends too")
	}

	@Test
	fun anInsufficientScopeRejectionInsideTheScope_isAnOrdinaryFailure_andTheSessionStays() = runTest {
		val rig = SessionInvalidationRig(this, Rejection.InsufficientScope)
		var failure: Throwable? = null

		val branch = rig.sessionScope.launch {
			try {
				rig.client.get("https://api.tuindice.app/record/v5/sync")
			} catch (exception: ClientRequestException) {
				failure = exception
			}
		}
		branch.join()

		assertEquals(HttpStatusCode.Forbidden, (failure as? ClientRequestException)?.response?.status)
		assertFalse(branch.isCancelled, "the request fails with its own error; nothing cancels it")
		assertEquals(0, rig.applicationRepository.clearCalls, "nothing is wiped")
		assertFalse(rig.store.isGone(), "the session is intact")
		assertEquals(0, rig.sessionInvalidationRepository.invalidationCalls, "and nobody is told")
		assertEquals(emptyList(), rig.reportingRepository.loggedExceptions)
	}

	@Test
	fun aSyncThatMeetsAnInsufficientScopeRejection_recordsAnOrdinaryFailure_andKeepsTheSession() = runTest {
		val rig = SessionInvalidationRig(this, Rejection.InsufficientScope)
		val settings = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatus = FakeSyncStatusRepository(initialValue = SyncStatus.Healthy)
		val remote = FakeSyncRemoteDataSource()
		remote.onSyncStarted = {
			try {
				rig.client.get("https://api.tuindice.app/record/v5/sync")
			} catch (exception: ClientRequestException) {
				throw SyncRemoteException(
					statusCode = exception.response.status,
					syncReport = null,
					conflictReason = null,
					cause = exception
				)
			}
		}
		val sync = SyncDataSource(
			settingsDataSource = settings,
			syncStatusRepository = syncStatus,
			remoteDataSource = remote,
			syncResultLocalDataSource = SyncResultLocalDataSource(
				recordLocalDataSource = FakeAcademicRecordLocalDataRepository(),
				userLocalDataSource = FakeUserLocalDataRepository(),
				recordOutboxDataSource = NoOpRecordOutboxDataRepository
			),
			pensumRevalidationRepository = FakePensumRevalidationRepository(),
			evaluationsRevalidationRepository = FakeEvaluationsRevalidationRepository(),
			coroutineScope = rig.sessionScope
		)

		sync.scheduleSync(password = "secret", policy = SyncPolicy.ForceRefresh)
		rig.settle()

		assertEquals(0, rig.applicationRepository.clearCalls, "nothing is wiped")
		assertFalse(rig.store.isGone())
		assertEquals(0, rig.sessionInvalidationRepository.invalidationCalls)
		assertEquals(listOf(SyncStatus.Failed), syncStatus.setStatuses, "the failure is recorded like any other")
		assertTrue(settings.cooldownMarked, "and so is its cooldown")
	}

	@Test
	fun anInsufficientScopeRejectionFromOutsideTheScope_stillCleansAndNotifies() = runTest {
		val rig = SessionInvalidationRig(this, Rejection.InsufficientScope)

		val failure = runCatching { rig.client.get("https://api.tuindice.app/record/v5/sync") }
			.exceptionOrNull()

		assertTrue(failure is ClientRequestException)
		assertEquals(1, rig.applicationRepository.clearCalls)
		assertTrue(rig.store.isGone())
		assertEquals(1, rig.sessionInvalidationRepository.invalidationCalls)
	}

	@Test
	fun theNoticeOfAnInvalidationNobodyShowed_survivesARestart() = runTest {
		val rig = SessionInvalidationRig(this)

		rig.recovery(rig.applicationRepository).invalidateSession(sessionId = "session-old")

		// The wipe erases the settings, so the notice has to be written after it.
		assertEquals(
			SessionResetNoticeReason.ServerEndedSession,
			rig.settingsRepository.getSessionResetNotice()?.reason,
			"the next start finds the notice pending"
		)
	}

	@Test
	fun theNoticeIsWrittenAfterTheWipe_soTheWipeCannotEraseIt() = runTest {
		val rig = SessionInvalidationRig(this)
		// A notice from before, which the wipe of the settings erases like everything else.
		rig.settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.UnreadableData)

		rig.recovery(rig.applicationRepository).invalidateSession(sessionId = "session-old")

		assertTrue(rig.settingsRepository.cleared, "the settings were wiped")
		assertEquals(
			SessionResetNoticeReason.ServerEndedSession,
			rig.settingsRepository.getSessionResetNotice()?.reason
		)
	}

	@Test
	fun aNoticeShownLive_isNotShownAgainAtTheNextStart() = runTest {
		val rig = SessionInvalidationRig(this)
		val observe = ObserveSessionInvalidationUseCase(
			sessionInvalidationRepository = rig.sessionInvalidationRepository,
			settingsRepository = rig.settingsRepository,
			reportingRepository = rig.reportingRepository
		)

		observe.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())

			rig.recovery(rig.applicationRepository).invalidateSession(sessionId = "session-old")

			val signal = assertIs<UseCaseState.Data<SessionResetNotice?>>(awaitItem())
			val notice = checkNotNull(signal.value)
			assertEquals(SessionResetNoticeReason.ServerEndedSession, notice.reason)
			assertTrue(rig.settingsRepository.sessionResetNoticePending, "told is not shown: the copy is kept")

			// The interface showed it and says so.
			AcknowledgeSessionInvalidationUseCase(
				settingsRepository = rig.settingsRepository,
				reportingRepository = rig.reportingRepository
			).execute(notice.stamp).test { cancelAndIgnoreRemainingEvents() }
			assertNull(
				rig.settingsRepository.getSessionResetNotice(),
				"the interface showed it; the start must not show it again"
			)

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun anIntentionalSignOut_leavesNoNoticeBehind() = runTest {
		val rig = SessionInvalidationRig(this)
		rig.sessionInvalidationRepository.markIntentionalSignOut("session-old")

		rig.recovery(rig.applicationRepository).invalidateSession(sessionId = "session-old")

		assertEquals(0, rig.sessionInvalidationRepository.invalidationCalls)
		assertNull(rig.settingsRepository.getSessionResetNotice())
	}

	@Test
	fun eachInvalidationReportsItsCause_once_withoutTokens() = runTest {
		val rig = SessionInvalidationRig(this)

		val branch = rig.sessionScope.launch {
			runCatching { rig.client.get("https://api.tuindice.app/record/v5/sync") }
		}
		branch.join()
		rig.finish()

		val reported = rig.reportingRepository.loggedExceptions
		assertEquals(1, reported.size, "one report for the one invalidation")
		val message = reported.single().message.orEmpty()
		assertTrue("RefreshRejected" in message, message)
		assertTrue("status=401" in message, message)
		assertTrue("inside the session scope" in message, message)
		assertFalse("access-old" in message || "refresh-old" in message || "12-34567" in message, message)
	}

	@Test
	fun anInsufficientScopeInvalidationFromOutsideTheScope_reportsItsStatus_codeAndOrigin() = runTest {
		val rig = SessionInvalidationRig(this, Rejection.InsufficientScope)

		runCatching { rig.client.get("https://api.tuindice.app/record/v5/sync") }

		val message = rig.reportingRepository.loggedExceptions.single().message.orEmpty()
		assertTrue("InsufficientScope" in message, message)
		assertTrue("status=403" in message, message)
		assertTrue("authError=${AuthErrorHeaders.INSUFFICIENT_SCOPE}" in message, message)
		assertTrue("outside the session scope" in message, message)
	}

	@Test
	fun twoInvalidationsInFlight_reportOnce() = runTest {
		val rig = SessionInvalidationRig(this)
		val wipeStarted = CompletableDeferred<Unit>()
		val releaseWipe = CompletableDeferred<Unit>()
		val recovery = rig.recovery(rig.productionLike(database = { gate(wipeStarted, releaseWipe) }))

		val first = launch { recovery.invalidateSession(sessionId = "session-old") }
		runCurrent()
		wipeStarted.await()
		val second = launch { recovery.invalidateSession(sessionId = "session-old") }
		runCurrent()
		releaseWipe.complete(Unit)
		joinAll(first, second)

		assertEquals(1, rig.reportingRepository.loggedExceptions.size)
	}

	@Test
	fun codeThatFollowsAnInvalidationInABranchOfTheScope_neverRuns() = runTest {
		val rig = SessionInvalidationRig(this)
		val recovery = rig.recovery(rig.applicationRepository)
		var ranAfterTheInvalidation = false

		val branch = rig.sessionScope.launch {
			recovery.invalidateSession(sessionId = "session-old")
			ranAfterTheInvalidation = true
		}
		branch.join()
		rig.finish()

		assertEquals(1, rig.applicationRepository.clearCalls)
		assertFalse(ranAfterTheInvalidation, "the branch is cancelled before it can continue")
		assertTrue(branch.isCancelled)
	}

	@Test
	fun codeThatFollowsAnInvalidationServedByAnotherWipe_neverRunsEither() = runTest {
		val rig = SessionInvalidationRig(this)
		val wipeStarted = CompletableDeferred<Unit>()
		val releaseWipe = CompletableDeferred<Unit>()
		val recovery = rig.recovery(rig.productionLike(database = { gate(wipeStarted, releaseWipe) }))
		var ranAfterTheSecondInvalidation = false

		launch { recovery.invalidateSession(sessionId = "session-old") }
		runCurrent()
		wipeStarted.await()
		val second = rig.sessionScope.launch {
			recovery.invalidateSession(sessionId = "session-old")
			ranAfterTheSecondInvalidation = true
		}
		runCurrent()
		releaseWipe.complete(Unit)
		advanceUntilIdle()

		assertTrue(second.isCompleted)
		assertFalse(ranAfterTheSecondInvalidation)
	}

	// A caller cancelled while the wipe runs stops waiting, and the wipe goes on to its end: the one that
	// arrived behind it is served by that wipe, and the notice is told exactly once.
	@Test
	fun aCallerCancelledWhileTheWipeRuns_doesNotStopIt_andTheCallerBehindItIsServed() = runTest {
		val rig = SessionInvalidationRig(this)
		val wipeStarted = CompletableDeferred<Unit>()
		val releaseWipe = CompletableDeferred<Unit>()
		val application = rig.productionLike(database = { gate(wipeStarted, releaseWipe) })
		val recovery = rig.recovery(application)

		val first = launch { recovery.invalidateSession(sessionId = "session-old") }
		runCurrent()
		wipeStarted.await()
		var secondFinishedNormally = false
		val second = launch {
			recovery.invalidateSession(sessionId = "session-old")
			secondFinishedNormally = true
		}
		runCurrent()

		first.cancel()
		runCurrent()
		releaseWipe.complete(Unit)
		advanceUntilIdle()

		assertTrue(first.isCancelled)
		assertEquals(1, application.clearCalls, "the wipe went on and ran once")
		assertTrue(secondFinishedNormally && !second.isCancelled, "the caller behind it was served by that wipe")
		assertTrue(rig.store.isGone())
		assertEquals(1, rig.sessionInvalidationRepository.invalidationCalls, "and the notice is told exactly once")
		assertEquals(SessionResetNoticeReason.ServerEndedSession, rig.settingsRepository.sessionResetNoticeReason)
	}
}

private enum class Rejection { Unauthorized, InsufficientScope }

// The step of the wipe that waits: it says it started and does not go on until it is released.
private suspend fun gate(started: CompletableDeferred<Unit>, release: CompletableDeferred<Unit>) {
	started.complete(Unit)
	release.await()
}

@OptIn(ExperimentalCoroutinesApi::class)
private class SessionInvalidationRig(
	private val testScope: TestScope,
	rejection: Rejection = Rejection.Unauthorized
) {
	val sessionScope: SessionCoroutineScope =
		testSessionCoroutineScope(StandardTestDispatcher(testScope.testScheduler))
	val sessionInvalidationRepository = FakeSessionInvalidationRepository()
	val settingsRepository = FakeSettingsRepository()
	val reportingRepository = RecordingReportingRepository()

	// The session as the app keeps it: the real store over the real secure-store source.
	val store = RealSessionStore(settings = settingsRepository, invalidations = sessionInvalidationRepository)
		.also { store -> store.seed() }
	val sessionRepository: SessionRepository = store.repository
	val applicationRepository = productionLike()
	private val syncStatusRepository = FakeSyncStatusRepository()
	private val authRepository: AuthRepository = when (rejection) {
		Rejection.Unauthorized -> RejectingRefreshAuthRepository

		// The refresh works, so the retried request is the one that is still short of scope.
		Rejection.InsufficientScope -> ScopeRefreshingAuthRepository(store.repository)
	}

	val client: HttpClient

	// The branch that ended the session is cancelled when the wipe starts, not when it ends: the wipe
	// belongs to the app and goes on by itself, so the test lets it run to its end.
	fun finish() {
		testScope.testScheduler.advanceUntilIdle()
	}

	// The mock engine answers from its own threads, so the test waits on the scope's branches.
	suspend fun settle() {
		testScope.testScheduler.runCurrent()
		while (true) {
			val children = sessionScope.coroutineContext[Job]?.children?.toList().orEmpty()
			if (children.isEmpty()) return
			children.joinAll()
		}
	}

	init {
		val recovery = recovery(applicationRepository)

		// The configuration of createSharedHttpClient that matters here: the Auth plugin, the current
		// session bearer and the insufficient-scope invalidation, on a client that throws on non-2xx.
		client = HttpClient(
			MockEngine {
				when (rejection) {
					Rejection.Unauthorized -> respond(
						content = "",
						status = HttpStatusCode.Unauthorized,
						headers = headersOf(HttpHeaders.WWWAuthenticate, "Bearer")
					)

					Rejection.InsufficientScope -> respond(
						content = "",
						status = HttpStatusCode.Forbidden,
						headers = headersOf(AuthErrorHeaders.HEADER, AuthErrorHeaders.INSUFFICIENT_SCOPE)
					)
				}
			}
		) {
			expectSuccess = true

			installInsufficientScopeSessionInvalidation(
				sessionRepository = sessionRepository,
				sessionRecoveryRepository = recovery,
				sessionCoroutineScope = sessionScope
			)

			install(Auth) {
				installSharedBearerAuth(
					sessionRepository = sessionRepository,
					sessionRecoveryRepository = recovery
				)
			}

			installCurrentSessionBearerAuth(recovery)
		}
	}

	// A wipe that does what the platforms' do; [database] is where a test stands for the database.
	fun productionLike(database: suspend () -> Unit = {}) = ProductionLikeApplicationRepository(
		settings = settingsRepository,
		stores = listOf(store.active, store.legacy),
		memory = listOf(store.memory),
		database = database
	)

	fun recovery(applicationRepository: ApplicationRepository) = SessionRecoveryDataSource(
		sessionRepository = sessionRepository,
		applicationRepository = applicationRepository,
		sessionInvalidationRepository = sessionInvalidationRepository,
		syncStatusRepository = syncStatusRepository,
		attestationRepository = object : AttestationRepository {
			override suspend fun attest(request: AttestationRequest) = Attestation(token = "attestation-token")
		},
		authRepository = authRepository,
		credentialsRepository = FakeCredentialsRepository(),
		settingsRepository = settingsRepository,
		reportingRepository = reportingRepository,
		sessionCoroutineScope = sessionScope,
		appCoroutineScope = testAppCoroutineScope(StandardTestDispatcher(testScope.testScheduler))
	)
}

// The server rejects the refresh token: the session is over.
private object RejectingRefreshAuthRepository : AuthRepository {
	override suspend fun exchangeRecoverySignIn(
		bootstrapAccessToken: String,
		attestation: Attestation,
		attemptedSnapshot: SessionSnapshot
	) = error("unused")

	override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation) = error("unused")

	override suspend fun reissueTokens(usbId: String, password: String, attestation: Attestation) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens = throw clientRequestException(HttpStatusCode.Unauthorized)

	override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) =
		error("unused")
}

private class ScopeRefreshingAuthRepository(
	private val sessionRepository: SessionRepository
) : AuthRepository {
	override suspend fun exchangeRecoverySignIn(
		bootstrapAccessToken: String,
		attestation: Attestation,
		attemptedSnapshot: SessionSnapshot
	) = error("unused")

	override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation) = error("unused")

	override suspend fun reissueTokens(usbId: String, password: String, attestation: Attestation) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		sessionRepository.setSessionSnapshot(
			SessionSnapshot(
				sessionId = "session-new",
				accessToken = "access-new",
				refreshToken = "refresh-new",
				usbId = "12-34567"
			)
		)

		return RefreshTokens(
			sessionId = "session-new",
			accessToken = "access-new",
			refreshToken = "refresh-new",
			expiresIn = 3600
		)
	}

	override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) =
		error("unused")
}
