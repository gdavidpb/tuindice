package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
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
import com.gdavidpb.tuindice.testkit.coroutines.testAppCoroutineScope
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// One session ends once also through the real client and its Auth plugin, with real threads: the sync
// (inside the session scope) and a screen request meet the dead session at once. Nothing here waits for
// the clock: each step of the story starts when the previous one is known to have happened.
class SessionInvalidationThroughTheAuthPluginTest {
	@Test
	fun theSyncAndAScreenRequestMeetingTheDeadSessionAtOnce_endItOnce() = runTest {
		withContext(Dispatchers.Default) {
			val settings = FakeSettingsRepository()
			val invalidations = CountingSessionInvalidationRepository()
			val store = RealSessionStore(settings = settings, invalidations = invalidations).also { it.seed() }
			val sessionScope = testSessionCoroutineScope(Dispatchers.Default)
			val auth = RejectingAuthRepository()
			val firstWipeEnded = CompletableDeferred<Unit>()
			val application = ProductionLikeApplicationRepository(
				settings = settings,
				stores = listOf(store.active, store.legacy),
				memory = listOf(store.memory),
				// The wipe holds the session readable (the stores are emptied after this step) until the
				// screen request has asked for its refresh: the order of the story does not depend on
				// which thread gets there first.
				database = { auth.secondRefreshStarted.await() }
			)
			val recovery = SessionRecoveryDataSource(
				sessionRepository = store.repository,
				applicationRepository = application,
				sessionInvalidationRepository = object : SessionInvalidationRepository by invalidations {
					override fun notifySessionInvalidated(sessionId: String?): Boolean {
						return invalidations.notifySessionInvalidated(sessionId).also { raised ->
							if (raised) firstWipeEnded.complete(Unit)
						}
					}
				},
				syncStatusRepository = FakeSyncStatusRepository(),
				attestationRepository = object : AttestationRepository {
					override suspend fun attest(request: AttestationRequest) = Attestation(token = "token")
				},
				authRepository = auth,
				credentialsRepository = FakeCredentialsRepository(),
				settingsRepository = settings,
				reportingRepository = RecordingReportingRepository(),
				sessionCoroutineScope = sessionScope,
				appCoroutineScope = testAppCoroutineScope(Dispatchers.Default)
			)
			val client = clientOf(store.repository, recovery, sessionScope)

			// The sync first: it holds the recovery lock while its refresh is in flight.
			val sync = sessionScope.launch { runCatching { client.get("https://api.tuindice.app/record/v5/sync") } }
			auth.firstRefreshStarted.await()
			// The screen request meets the same 401 and waits behind it.
			val screen = async { runCatching { client.get("https://api.tuindice.app/record/v5/record") } }

			auth.firstRefresh.complete(Unit)
			sync.join()
			// The sync let go of the recovery lock when the wipe cancelled it: the screen request takes it,
			// reads the session that is still stored and asks for a refresh of its own. Then the first wipe
			// runs to its end with that refresh in flight.
			auth.secondRefreshStarted.await()
			assertEquals(2, auth.refreshCalls.value, "the screen request asked for a refresh of its own")
			firstWipeEnded.await()

			auth.secondRefresh.complete(Unit)
			auth.secondRefreshRejected.await()
			screen.await()
			assertTrue(auth.secondRefreshRejected.isCompleted, "the second refresh was rejected")

			assertEquals(2, auth.refreshCalls.value, "and only those two")
			assertEquals(1, application.clearCalls, "one session, one wipe")
			assertEquals(1, invalidations.raised, "and one signal")
		}
	}
}

// The real client with the plugins the app installs, over an engine that answers every request with a 401.
private fun clientOf(
	sessionRepository: SessionRepository,
	recovery: SessionRecoveryDataSource,
	sessionScope: SessionCoroutineScope
) = HttpClient(
	MockEngine {
		respond(
			content = "",
			status = HttpStatusCode.Unauthorized,
			headers = headersOf(HttpHeaders.WWWAuthenticate, "Bearer")
		)
	}
) {
	expectSuccess = true
	installInsufficientScopeSessionInvalidation(sessionRepository, recovery, sessionScope)
	install(Auth) { installSharedBearerAuth(sessionRepository, recovery) }
	installCurrentSessionBearerAuth(recovery)
}

// Every refresh is rejected, and each one is held where the story needs it: the first until the sync is
// let go, the second until the wipe of the first has ended.
private class RejectingAuthRepository : AuthRepository {
	override suspend fun exchangeRecoverySignIn(
		bootstrapAccessToken: String,
		attestation: Attestation,
		attemptedSnapshot: SessionSnapshot
	) = error("unused")

	val firstRefreshStarted = CompletableDeferred<Unit>()
	val firstRefresh = CompletableDeferred<Unit>()
	val secondRefreshStarted = CompletableDeferred<Unit>()
	val secondRefresh = CompletableDeferred<Unit>()
	val secondRefreshRejected = CompletableDeferred<Unit>()
	val refreshCalls = MutableStateFlow(0)

	override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation) = error("unused")

	override suspend fun reissueTokens(usbId: String, password: String, attestation: Attestation) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		var call = 0
		refreshCalls.update { calls -> (calls + 1).also { call = it } }

		if (call == 1) {
			firstRefreshStarted.complete(Unit)
			firstRefresh.await()
		} else {
			secondRefreshStarted.complete(Unit)
			secondRefresh.await()
		}

		try {
			throw clientRequestException(HttpStatusCode.Unauthorized)
		} finally {
			if (call > 1) secondRefreshRejected.complete(Unit)
		}
	}

	override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) =
		error("unused")
}
