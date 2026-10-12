package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals

// One session ends once also through the real client and its Auth plugin, with real threads: the sync
// (inside the session scope) and a screen request meet the dead session at once.
class SessionInvalidationThroughTheAuthPluginTest {
	@Test
	fun theSyncAndAScreenRequestMeetingTheDeadSessionAtOnce_endItOnce() = runTest {
		withContext(Dispatchers.Default) {
			val settings = FakeSettingsRepository()
			val invalidations = CountingSessionInvalidationRepository()
			val store = RealSessionStore(settings = settings, invalidations = invalidations).also { it.seed() }
			val sessionScope = testSessionCoroutineScope(Dispatchers.Default)
			val application = ProductionLikeApplicationRepository(
				settings = settings,
				stores = listOf(store.active, store.legacy),
				memory = listOf(store.memory)
			)
			val firstRefresh = CompletableDeferred<Unit>()
			val secondRefresh = CompletableDeferred<Unit>()
			val refreshCalls = MutableStateFlow(0)
			val recovery = SessionRecoveryDataSource(
				sessionRepository = store.repository,
				applicationRepository = application,
				sessionInvalidationRepository = invalidations,
				syncStatusRepository = FakeSyncStatusRepository(),
				attestationRepository = object : AttestationRepository {
					override suspend fun attest(request: AttestationRequest) = Attestation(token = "token")
				},
				authRepository = object : AuthRepository {
					override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens =
						error("unused")

					override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation) =
						error("unused")

					override suspend fun reissueTokens(usbId: String, password: String, attestation: Attestation) =
						error("unused")

					override suspend fun refreshTokens(
						sessionId: String,
						refreshToken: String,
						attestation: Attestation
					): RefreshTokens {
						var call = 0
						refreshCalls.update { calls -> (calls + 1).also { call = it } }

						if (call == 1) firstRefresh.await() else secondRefresh.await()

						throw clientRequestException(HttpStatusCode.Unauthorized)
					}

					override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) =
						error("unused")
				},
				credentialsRepository = FakeCredentialsRepository(),
				settingsRepository = settings,
				reportingRepository = RecordingReportingRepository(),
				sessionCoroutineScope = sessionScope,
				appCoroutineScope = testAppCoroutineScope(Dispatchers.Default)
			)
			val client = HttpClient(
				MockEngine {
					respond(
						content = "",
						status = HttpStatusCode.Unauthorized,
						headers = headersOf(HttpHeaders.WWWAuthenticate, "Bearer")
					)
				}
			) {
				expectSuccess = true
				installInsufficientScopeSessionInvalidation(store.repository, recovery, sessionScope)
				install(Auth) { installSharedBearerAuth(store.repository, recovery) }
				installCurrentSessionBearerAuth(recovery)
			}

			// The sync first: it holds the recovery lock while its refresh is in flight.
			val sync = sessionScope.launch { runCatching { client.get("https://api.tuindice.app/record/v5/sync") } }
			withTimeout(5_000) { while (refreshCalls.value < 1) delay(10) }
			// The screen request meets the same 401 and waits behind it.
			val screen = async { runCatching { client.get("https://api.tuindice.app/record/v5/record") } }
			delay(500)

			firstRefresh.complete(Unit)
			withTimeout(5_000) { sync.join() }
			// The first wipe runs to its end while the screen's own refresh is still in flight.
			withTimeout(5_000) { while (invalidations.raised < 1) delay(10) }
			delay(300)

			secondRefresh.complete(Unit)
			withTimeout(5_000) { screen.await() }
			delay(300)

			assertEquals(1, application.clearCalls, "one session, one wipe")
			assertEquals(1, invalidations.raised, "and one signal")
		}
	}
}
