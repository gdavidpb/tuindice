package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
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
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testing.NoOpRecordOutboxDataRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.github.vinceglb.filekit.PlatformFile
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

		assertEquals(1, rig.applicationRepository.clearCalls, "the wipe ran")
		assertTrue(rig.sessionRepository.cleared)
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

		assertEquals(1, rig.applicationRepository.clearCalls, "the wipe ran")
		assertEquals(1, rig.sessionInvalidationRepository.invalidationCalls)
		assertFalse(settings.cooldownMarked, "no sync cooldown written into the wiped settings")
		assertFalse(settings.syncRetryBackoffMarked)
		assertEquals(emptyList(), syncStatus.setStatuses, "no status written after the wipe")
		assertEquals(emptyList(), syncStatus.setReports)
	}

	// Two requests of the scope end the session at once: neither may cancel the other's cleanup, and
	// the wipe runs once. The second arrives while the first is already wiping.
	@Test
	fun twoInvalidationsFromInsideTheScope_wipeOnce() = runTest {
		val rig = SessionInvalidationRig(this)
		val wipeStarted = CompletableDeferred<Unit>()
		val releaseWipe = CompletableDeferred<Unit>()
		val application = GatedApplicationRepository(wipeStarted, releaseWipe)
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
		assertTrue(first.isCancelled, "the branch that wiped ends last")
		assertTrue(second.isCancelled, "the branch that met the finished wipe ends too")
	}
}

private class GatedApplicationRepository(
	private val started: CompletableDeferred<Unit>,
	private val release: CompletableDeferred<Unit>
) : ApplicationRepository {
	var clearCalls = 0
		private set

	override suspend fun clearSessionResidue() = Unit

	override suspend fun clearData() {
		clearCalls++
		started.complete(Unit)
		release.await()
	}

	override suspend fun canOpen(file: PlatformFile): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
private class SessionInvalidationRig(private val testScope: TestScope) {
	val sessionScope: SessionCoroutineScope =
		testSessionCoroutineScope(StandardTestDispatcher(testScope.testScheduler))
	val sessionRepository = FakeSessionRepository(
		sessionId = "session-old",
		usbId = "12-34567",
		accessToken = "access-old",
		refreshToken = "refresh-old"
	)
	val applicationRepository = RecordingApplicationRepository()
	val sessionInvalidationRepository = FakeSessionInvalidationRepository()
	private val syncStatusRepository = FakeSyncStatusRepository()

	val client: HttpClient

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
				respond(
					content = "",
					status = HttpStatusCode.Unauthorized,
					headers = headersOf(HttpHeaders.WWWAuthenticate, "Bearer")
				)
			}
		) {
			expectSuccess = true

			installInsufficientScopeSessionInvalidation(
				sessionRepository = sessionRepository,
				sessionRecoveryRepository = recovery
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

	fun recovery(applicationRepository: ApplicationRepository) = SessionRecoveryDataSource(
		sessionRepository = sessionRepository,
		applicationRepository = applicationRepository,
		sessionInvalidationRepository = sessionInvalidationRepository,
		syncStatusRepository = syncStatusRepository,
		attestationRepository = object : AttestationRepository {
			override suspend fun attest(request: AttestationRequest) = Attestation(token = "attestation-token")
		},
		authRepository = RejectingRefreshAuthRepository,
		credentialsRepository = FakeCredentialsRepository(),
		sessionCoroutineScope = sessionScope
	)
}

// The server rejects the refresh token: the session is over.
private object RejectingRefreshAuthRepository : AuthRepository {
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
