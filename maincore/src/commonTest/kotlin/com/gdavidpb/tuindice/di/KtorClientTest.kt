package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable

class KtorClientTest {
	@Test
	fun createSharedJson_ignoresUnknownTopLevelFields() {
		val response = createSharedJson().decodeFromString(
			deserializer = TestResponse.serializer(),
			"""{"revision":1,"id":"server-added-field"}"""
		)

		assertEquals(1L, response.revision)
	}

	@Test
	fun isSessionInvalidatingRefreshFailure_matchesUnauthorizedForbiddenAndLocked() {
		assertTrue(clientRequestException(HttpStatusCode.Unauthorized).isSessionInvalidatingRefreshFailure())
		assertTrue(clientRequestException(HttpStatusCode.Forbidden).isSessionInvalidatingRefreshFailure())
		assertTrue(clientRequestException(HttpStatusCode.Locked).isSessionInvalidatingRefreshFailure())
		assertFalse(clientRequestException(HttpStatusCode.ServiceUnavailable).isSessionInvalidatingRefreshFailure())
	}

	@Test
	fun shouldSendBearerAuth_excludesAuthAndAttestationEndpoints_withOrWithoutLeadingSlash() {
		assertFalse("/auth/v1/token".shouldSendBearerAuth())
		assertFalse("auth/v1/token".shouldSendBearerAuth())
		assertFalse("/auth/v2/token/refresh".shouldSendBearerAuth())
		assertFalse("auth/v2/token/revoke".shouldSendBearerAuth())
		assertFalse("/attestation/v4/sessions".shouldSendBearerAuth())
		assertTrue("/record/v5".shouldSendBearerAuth())
	}

	@Test
	fun handleUnauthorizedTokenRefresh_clearsLocalSessionState_andEmitsInvalidation() = runTest {
		val sessionRepository = FakeSessionRepository()
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val applicationRepository = RecordingApplicationRepository()
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()

		handleUnauthorizedTokenRefresh(
			sessionRepository = sessionRepository,
			syncStatusRepository = syncStatusRepository,
			applicationRepository = applicationRepository,
			sessionInvalidationRepository = sessionInvalidationRepository
		)

		assertTrue(sessionRepository.cleared)
		assertEquals(1, syncStatusRepository.resetCalls)
		assertEquals(SyncStatus.Healthy, syncStatusRepository.getSyncStatus())
		assertTrue(applicationRepository.cleared)
		assertEquals(1, sessionInvalidationRepository.invalidationCalls)
	}

	@Test
	fun installSharedBearerAuth_usesLatestPersistedTokens_withoutRecreatingTheClient() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val authorizationHeaders = mutableListOf<String>()
		val client = HttpClient(
			MockEngine { request ->
				authorizationHeaders += checkNotNull(request.headers[HttpHeaders.Authorization])
				respondOk()
			}
		) {
			install(Auth) {
				installSharedBearerAuth(
					sessionRepository = sessionRepository,
					applicationRepository = RecordingApplicationRepository(),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					syncStatusRepository = FakeSyncStatusRepository(),
					attestationRepositoryProvider = { error("unused in this test") },
					authRepositoryProvider = { error("unused in this test") },
					credentialsRepositoryProvider = { FakeCredentialsRepository() },
					syncRepositoryProvider = { FakeSyncRepository() }
				)
			}
		}

		try {
			client.get("https://api.tuindice.app/record/v5/sync")

			sessionRepository.setSessionId("session-new")
			sessionRepository.setAccessToken("access-new")
			sessionRepository.setRefreshToken("refresh-new")

			client.get("https://api.tuindice.app/record/v5/sync")
		} finally {
			client.close()
		}

		assertEquals(
			listOf("Bearer access-old", "Bearer access-new"),
			authorizationHeaders
		)
	}

	@Test
	fun installSharedBearerAuth_doesNotAttachBearerToIssueTokenEndpoint() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-id",
			accessToken = "access-token",
			refreshToken = "refresh-token"
		)
		val authorizationHeadersByPath = mutableMapOf<String, String?>()
		val client = HttpClient(
			MockEngine { request ->
				authorizationHeadersByPath[request.url.encodedPath] =
					request.headers[HttpHeaders.Authorization]
				respondOk()
			}
		) {
			install(Auth) {
				installSharedBearerAuth(
					sessionRepository = sessionRepository,
					applicationRepository = RecordingApplicationRepository(),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					syncStatusRepository = FakeSyncStatusRepository(),
					attestationRepositoryProvider = { error("unused in this test") },
					authRepositoryProvider = { error("unused in this test") },
					credentialsRepositoryProvider = { FakeCredentialsRepository() },
					syncRepositoryProvider = { FakeSyncRepository() }
				)
			}
		}

		try {
			client.get("https://api.tuindice.app/auth/v1/token")
			client.get("https://api.tuindice.app/record/v5")
		} finally {
			client.close()
		}

		assertEquals(null, authorizationHeadersByPath["/auth/v1/token"])
		assertEquals("Bearer access-token", authorizationHeadersByPath["/record/v5"])
	}

	@Test
	fun installSharedBearerAuth_doesNotReauthorizeIssueTokenEndpoint() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-id",
			accessToken = "access-token",
			refreshToken = "refresh-token"
		)
		var tokenRequestCount = 0
		val client = HttpClient(
			MockEngine { request ->
				if (request.url.encodedPath == "/auth/v1/token") {
					tokenRequestCount++
					respond(
						content = "",
						status = HttpStatusCode.Unauthorized,
						headers = headersOf(HttpHeaders.WWWAuthenticate, "Bearer")
					)
				} else {
					respondOk()
				}
			}
		) {
			install(Auth) {
				installSharedBearerAuth(
					sessionRepository = sessionRepository,
					applicationRepository = RecordingApplicationRepository(),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					syncStatusRepository = FakeSyncStatusRepository(),
					attestationRepositoryProvider = { error("unused in this test") },
					authRepositoryProvider = { error("unused in this test") },
					credentialsRepositoryProvider = { FakeCredentialsRepository() },
					syncRepositoryProvider = { FakeSyncRepository() }
				)
			}
		}

		val response = try {
			client.get("https://api.tuindice.app/auth/v1/token")
		} finally {
			client.close()
		}

		assertEquals(HttpStatusCode.Unauthorized, response.status)
		assertEquals(1, tokenRequestCount)
	}
}

@Serializable
private data class TestResponse(
	val revision: Long
)
