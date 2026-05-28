package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
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
import kotlin.test.assertNull
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
	fun tryReissueTokensAfterUnauthorizedRefresh_usesStoredCredentialsAndBearerAttestation() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val credentialsRepository = FakeCredentialsRepository(password = "secret")
		val attestationRepository = RecordingAttestationRepository()
		val authRepository = ReissuePersistingAuthRepository(
			sessionRepository = sessionRepository
		)

		val tokens = tryReissueTokensAfterUnauthorizedRefresh(
			sessionRepository = sessionRepository,
			credentialsRepository = credentialsRepository,
			attestationRepository = attestationRepository,
			authRepository = authRepository,
			oldAccessToken = "access-old"
		)

		val reissueCall = authRepository.reissueCalls.single()
		val attestationRequest = attestationRepository.requests.single()
		val authorization = attestationRequest.authorization

		assertEquals("12-34567", reissueCall.usbId)
		assertEquals("secret", reissueCall.password)
		assertEquals("access-reissued", tokens?.accessToken)
		assertEquals("refresh-reissued", tokens?.refreshToken)
		assertEquals(ProtectedOperationCodes.AuthReissueTokens, attestationRequest.operationCode)
		assertTrue(attestationRequest.payloadJson.contains("\"usb_id\":\"12-34567\""))
		assertTrue(attestationRequest.payloadJson.contains("\"password\":\"secret\""))
		assertTrue(attestationRequest.payloadJson.contains("\"attested_flow\":\"reissue_tokens\""))
		assertTrue(authorization is AttestationAuthorization.Bearer)
		assertEquals("access-old", authorization.accessToken)
	}

	@Test
	fun tryReissueTokensAfterUnauthorizedRefresh_returnsNullWhenPasswordIsUnavailable() = runTest {
		val sessionRepository = FakeSessionRepository()
		val credentialsRepository = FakeCredentialsRepository()
		val attestationRepository = RecordingAttestationRepository()
		val authRepository = ReissuePersistingAuthRepository(
			sessionRepository = sessionRepository
		)

		val tokens = tryReissueTokensAfterUnauthorizedRefresh(
			sessionRepository = sessionRepository,
			credentialsRepository = credentialsRepository,
			attestationRepository = attestationRepository,
			authRepository = authRepository,
			oldAccessToken = "access-token"
		)

		assertNull(tokens)
		assertTrue(authRepository.reissueCalls.isEmpty())
		assertTrue(attestationRepository.requests.isEmpty())
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

private data class ReissueTokensCall(
	val usbId: String,
	val password: String,
	val attestation: Attestation
)

private class RecordingAttestationRepository : AttestationRepository {
	val requests = mutableListOf<AttestationRequest>()

	override suspend fun attest(request: AttestationRequest): Attestation {
		requests += request
		return Attestation(token = "attestation-token")
	}
}

private class ReissuePersistingAuthRepository(
	private val sessionRepository: SessionRepository,
	private val throwable: Throwable? = null
) : AuthRepository {
	val reissueCalls = mutableListOf<ReissueTokensCall>()

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) {
		reissueCalls += ReissueTokensCall(
			usbId = usbId,
			password = password,
			attestation = attestation
		)
		throwable?.let { throw it }
		sessionRepository.setSessionId("session-reissued")
		sessionRepository.setUsbId(usbId)
		sessionRepository.setAccessToken("access-reissued")
		sessionRepository.setRefreshToken("refresh-reissued")
	}

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens = error("unused")

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused")
}
