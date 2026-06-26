package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.data.source.network.AuthErrorHeaders
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.utils.extension.isAccessRejected
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable

@OptIn(ExperimentalCoroutinesApi::class)
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
	fun isAccessRejected_matchesUnauthorizedForbiddenAndLocked() {
		assertTrue(clientRequestException(HttpStatusCode.Unauthorized).isAccessRejected())
		assertTrue(clientRequestException(HttpStatusCode.Forbidden).isAccessRejected())
		assertTrue(clientRequestException(HttpStatusCode.Locked).isAccessRejected())
		assertFalse(clientRequestException(HttpStatusCode.ServiceUnavailable).isAccessRejected())
	}

	@Test
	fun shouldSendBearerAuth_excludesAuthAndAttestationEndpoints_withOrWithoutLeadingSlash() {
		assertFalse("/auth/v1/token".shouldSendBearerAuth())
		assertFalse("auth/v1/token".shouldSendBearerAuth())
		assertFalse("/auth/v2/bootstrap".shouldSendBearerAuth())
		assertFalse("/auth/v2/token/exchange".shouldSendBearerAuth())
		assertFalse("/auth/v2/token/refresh".shouldSendBearerAuth())
		assertFalse("auth/v2/token/revoke".shouldSendBearerAuth())
		assertFalse("/attestation/v4/sessions".shouldSendBearerAuth())
		assertFalse("/attestation/v5/sessions".shouldSendBearerAuth())
		assertTrue("/record/v5".shouldSendBearerAuth())
		assertTrue("/record/v5/sync".shouldSendBearerAuth())
		assertTrue("/users/v1".shouldSendBearerAuth())
		assertTrue("/messaging/v1".shouldSendBearerAuth())
		assertTrue("/enrollment-proof/v1".shouldSendBearerAuth())
	}

	@Test
	fun recover_bootstrapExchangesAfterSupersededRefresh_withStoredCredentials() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val credentialsRepository = FakeCredentialsRepository(password = "secret")
		val attestationRepository = RecordingAttestationRepository()
		val authRepository = SupersededRefreshThenExchangePersistingAuthRepository(
			sessionRepository = sessionRepository
		)
		val dataSource = sessionRecoveryDataSource(
			sessionRepository = sessionRepository,
			attestationRepository = attestationRepository,
			authRepository = authRepository,
			credentialsRepository = credentialsRepository
		)

		val snapshot = dataSource.recoverUnauthorizedSession(
			attemptedAuthorizationAccessToken = "access-old",
			attemptedCachedAccessToken = "access-old",
			attemptedCachedRefreshToken = "refresh-old"
		)

		val bootstrapCall = authRepository.bootstrapCalls.single()
		val exchangeCall = authRepository.exchangeCalls.single()
		val attestationRequest = attestationRepository.requests.last()
		val authorization = attestationRequest.authorization

		assertEquals("12-34567", bootstrapCall.usbId)
		assertEquals("secret", bootstrapCall.password)
		assertEquals("bootstrap-access", exchangeCall.bootstrapAccessToken)
		assertEquals("access-exchanged", snapshot?.accessToken)
		assertEquals("refresh-exchanged", snapshot?.refreshToken)
		assertEquals(ProtectedOperationCodes.AuthExchange, attestationRequest.operationCode)
		assertEquals("{}", attestationRequest.payloadJson)
		assertTrue(authorization is AttestationAuthorization.Bearer)
		assertEquals("bootstrap-access", authorization.accessToken)
	}

	@Test
	fun recover_invalidatesSessionWhenPasswordIsUnavailableAfterSupersededRefresh() = runTest {
		val sessionRepository = FakeSessionRepository()
		val credentialsRepository = FakeCredentialsRepository()
		val attestationRepository = RecordingAttestationRepository()
		val authRepository = SupersededRefreshThenExchangePersistingAuthRepository(
			sessionRepository = sessionRepository
		)
		val dataSource = sessionRecoveryDataSource(
			sessionRepository = sessionRepository,
			attestationRepository = attestationRepository,
			authRepository = authRepository,
			credentialsRepository = credentialsRepository
		)

		val snapshot = dataSource.recoverUnauthorizedSession(
			attemptedAuthorizationAccessToken = "access-token",
			attemptedCachedAccessToken = "access-token",
			attemptedCachedRefreshToken = "refresh-token"
		)

		assertNull(snapshot)
		assertTrue(authRepository.bootstrapCalls.isEmpty())
		assertTrue(authRepository.exchangeCalls.isEmpty())
		assertEquals(1, attestationRepository.requests.size)
		assertTrue(sessionRepository.cleared)
	}

	@Test
	fun recover_invalidatesSessionWithoutBootstrap_whenUnauthorizedRefreshHasNoAuthErrorHeader() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val credentialsRepository = FakeCredentialsRepository(password = "secret")
		val authRepository = UnauthorizedRefreshAuthRepository()
		val dataSource = sessionRecoveryDataSource(
			sessionRepository = sessionRepository,
			attestationRepository = RecordingAttestationRepository(),
			authRepository = authRepository,
			credentialsRepository = credentialsRepository
		)

		val snapshot = dataSource.recoverUnauthorizedSession(
			attemptedAuthorizationAccessToken = "access-old",
			attemptedCachedAccessToken = "access-old",
			attemptedCachedRefreshToken = "refresh-old"
		)

		assertNull(snapshot)
		assertEquals(0, authRepository.bootstrapCalls)
		assertEquals(0, authRepository.exchangeCalls)
		assertTrue(sessionRepository.cleared)
	}

	@Test
	fun recover_invalidatesSessionWithoutBootstrap_whenRefreshTokenMismatches() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val credentialsRepository = FakeCredentialsRepository(password = "secret")
		val authRepository = RefreshTokenMismatchAuthRepository()
		val dataSource = sessionRecoveryDataSource(
			sessionRepository = sessionRepository,
			attestationRepository = RecordingAttestationRepository(),
			authRepository = authRepository,
			credentialsRepository = credentialsRepository
		)

		val snapshot = dataSource.recoverUnauthorizedSession(
			attemptedAuthorizationAccessToken = "access-old",
			attemptedCachedAccessToken = "access-old",
			attemptedCachedRefreshToken = "refresh-old"
		)

		assertNull(snapshot)
		assertEquals(0, authRepository.bootstrapCalls)
		assertEquals(0, authRepository.exchangeCalls)
		assertTrue(sessionRepository.cleared)
	}

	@Test
	fun unauthorizedSessionRecovery_clearsLocalSessionState_andEmitsInvalidation() = runTest {
		val sessionRepository = FakeSessionRepository()
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val applicationRepository = RecordingApplicationRepository()
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val sessionCoroutineScope = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))
		var activeSessionWorkCancelled = false
		val activeSessionWork = sessionCoroutineScope.launch {
			try {
				awaitCancellation()
			} finally {
				activeSessionWorkCancelled = true
			}
		}

		sessionRecoveryDataSource(
			sessionRepository = sessionRepository,
			syncStatusRepository = syncStatusRepository,
			applicationRepository = applicationRepository,
			sessionInvalidationRepository = sessionInvalidationRepository,
			sessionCoroutineScope = sessionCoroutineScope
		).invalidateSession()

		assertTrue(activeSessionWork.isCancelled)
		assertTrue(activeSessionWorkCancelled)
		assertTrue(sessionRepository.cleared)
		assertEquals(1, syncStatusRepository.resetCalls)
		assertEquals(SyncStatus.Healthy, syncStatusRepository.getSyncStatus())
		assertTrue(applicationRepository.cleared)
		assertEquals(1, sessionInvalidationRepository.invalidationCalls)
	}

	@Test
	fun unauthorizedSessionRecovery_suppressesInvalidation_afterIntentionalSignOut() = runTest {
		val sessionRepository = FakeSessionRepository()
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val applicationRepository = RecordingApplicationRepository()
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		sessionInvalidationRepository.markIntentionalSignOut("session-123")

		sessionRecoveryDataSource(
			sessionRepository = sessionRepository,
			syncStatusRepository = syncStatusRepository,
			applicationRepository = applicationRepository,
			sessionInvalidationRepository = sessionInvalidationRepository
		).invalidateSession(sessionId = "session-123")

		assertTrue(sessionRepository.cleared)
		assertEquals(1, syncStatusRepository.resetCalls)
		assertTrue(applicationRepository.cleared)
		assertEquals(0, sessionInvalidationRepository.invalidationCalls)
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
					sessionRecoveryRepository = sessionRecoveryDataSource(
						sessionRepository = sessionRepository
					)
				)
			}
		}

		try {
			client.get("https://api.tuindice.app/record/v5/sync")

			sessionRepository.setSessionSnapshot(
				SessionSnapshot(
					sessionId = "session-new",
					accessToken = "access-new",
					refreshToken = "refresh-new",
					usbId = "20261234"
				)
			)

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
	fun installSharedBearerAuth_attachesBearerToProtectedGetAndPostRequests() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-id",
			usbId = "12-34567",
			accessToken = "access-token",
			refreshToken = "refresh-token"
		)
		val authorizationHeadersByRequest = mutableMapOf<String, String?>()
		val client = HttpClient(
			MockEngine { request ->
				authorizationHeadersByRequest["${request.method.value} ${request.url.encodedPath}"] =
					request.headers[HttpHeaders.Authorization]
				respondOk()
			}
		) {
			install(Auth) {
				installSharedBearerAuth(
					sessionRepository = sessionRepository,
					sessionRecoveryRepository = sessionRecoveryDataSource(
						sessionRepository = sessionRepository
					)
				)
			}
		}

		try {
			client.post("https://api.tuindice.app/messaging/v1") {
				setBody("""{"token":"push-token"}""")
			}
			client.post("https://api.tuindice.app/record/v5/sync") {
				setBody("""{"password":"secret"}""")
			}
			client.get("https://api.tuindice.app/users/v1")
		} finally {
			client.close()
		}

		assertEquals("Bearer access-token", authorizationHeadersByRequest["POST /messaging/v1"])
		assertEquals("Bearer access-token", authorizationHeadersByRequest["POST /record/v5/sync"])
		assertEquals("Bearer access-token", authorizationHeadersByRequest["GET /users/v1"])
	}

	@Test
	fun installCurrentSessionBearerAuth_attachesBearerAfterDefaultRequestMergesRelativePaths() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-id",
			usbId = "12-34567",
			accessToken = "access-token",
			refreshToken = "refresh-token"
		)
		val authorizationHeadersByRequest = mutableMapOf<String, String?>()
		val client = HttpClient(
			MockEngine { request ->
				authorizationHeadersByRequest["${request.method.value} ${request.url.encodedPath}"] =
					request.headers[HttpHeaders.Authorization]
				respondOk()
			}
		) {
			install(Auth) {
				installSharedBearerAuth(
					sessionRepository = sessionRepository,
					sessionRecoveryRepository = sessionRecoveryDataSource(
						sessionRepository = sessionRepository
					)
				)
			}
			install(DefaultRequest) {
				url("https://api.tuindice.app/")
			}
			installCurrentSessionBearerAuth(sessionRepository)
		}

		try {
			client.post("messaging/v1") {
				setBody("""{"token":"push-token"}""")
			}
			client.post("record/v5/sync") {
				setBody("""{"password":"secret"}""")
			}
			client.get("users/v1")
		} finally {
			client.close()
		}

		assertEquals("Bearer access-token", authorizationHeadersByRequest["POST /messaging/v1"])
		assertEquals("Bearer access-token", authorizationHeadersByRequest["POST /record/v5/sync"])
		assertEquals("Bearer access-token", authorizationHeadersByRequest["GET /users/v1"])
	}

	@Test
	fun installCurrentSessionBearerAuth_doesNotAttachBearerToAuthOrAttestationRequests() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-id",
			usbId = "12-34567",
			accessToken = "access-token",
			refreshToken = "refresh-token"
		)
		val authorizationHeadersByRequest = mutableMapOf<String, String?>()
		val client = HttpClient(
			MockEngine { request ->
				authorizationHeadersByRequest["${request.method.value} ${request.url.encodedPath}"] =
					request.headers[HttpHeaders.Authorization]
				respondOk()
			}
		) {
			install(DefaultRequest) {
				url("https://api.tuindice.app/")
			}
			installCurrentSessionBearerAuth(sessionRepository)
		}

		try {
			client.post("auth/v2/token/exchange")
			client.post("attestation/v5/sessions")
		} finally {
			client.close()
		}

		assertNull(authorizationHeadersByRequest["POST /auth/v2/token/exchange"])
		assertNull(authorizationHeadersByRequest["POST /attestation/v5/sessions"])
	}

	@Test
	fun installSharedBearerAuth_retriesStaleUnauthorizedRequestWithCurrentTokens() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val authorizationHeaders = mutableListOf<String>()
		val client = HttpClient(
			MockEngine { request ->
				val authorization = checkNotNull(request.headers[HttpHeaders.Authorization])
				authorizationHeaders += authorization

				if (authorization == "Bearer access-old") {
					sessionRepository.setSessionSnapshot(
						SessionSnapshot(
							sessionId = "session-new",
							accessToken = "access-new",
							refreshToken = "refresh-new",
							usbId = "12-34567"
						)
					)
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
					sessionRecoveryRepository = sessionRecoveryDataSource(
						sessionRepository = sessionRepository
					)
				)
			}
		}

		val response = try {
			client.get("https://api.tuindice.app/record/v5/sync")
		} finally {
			client.close()
		}

		assertEquals(HttpStatusCode.OK, response.status)
		assertEquals(
			listOf("Bearer access-old", "Bearer access-new"),
			authorizationHeaders
		)
	}

	@Test
	fun installSharedBearerAuth_refreshesOnlyOnceForConcurrentUnauthorizedRequests() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val oldRequestsSeen = CompletableDeferred<Unit>()
		var oldRequestCount = 0
		val authorizationHeaders = mutableListOf<String>()
		val authRepository = RefreshPersistingAuthRepository(
			sessionRepository = sessionRepository,
			beforeRefresh = { oldRequestsSeen.await() }
		)
		val attestationRepository = RecordingAttestationRepository()
		val client = HttpClient(
			MockEngine { request ->
				val authorization = checkNotNull(request.headers[HttpHeaders.Authorization])
				authorizationHeaders += authorization

				if (authorization == "Bearer access-old") {
					oldRequestCount++

					if (oldRequestCount == 2) {
						oldRequestsSeen.complete(Unit)
					}

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
					sessionRecoveryRepository = sessionRecoveryDataSource(
						sessionRepository = sessionRepository,
						attestationRepository = attestationRepository,
						authRepository = authRepository
					)
				)
			}
		}

		val first = async { client.get("https://api.tuindice.app/record/v5/sync") }
		val second = async { client.get("https://api.tuindice.app/record/v5/sync") }

		try {
			assertEquals(HttpStatusCode.OK, first.await().status)
			assertEquals(HttpStatusCode.OK, second.await().status)
		} finally {
			client.close()
		}

		assertEquals(1, authRepository.refreshCalls.size)
		assertEquals(1, attestationRepository.requests.size)
		assertEquals(2, authorizationHeaders.count { header -> header == "Bearer access-old" })
		assertEquals(2, authorizationHeaders.count { header -> header == "Bearer access-new" })
	}

	@Test
	fun installSharedBearerAuth_doesNotClearNewSessionWhenOldRefreshFailsUnauthorized() = runTest {
		val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)
		val applicationRepository = RecordingApplicationRepository()
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val authorizationHeaders = mutableListOf<String>()
		val authRepository = FailingRefreshAfterSessionChangeAuthRepository(
			sessionRepository = sessionRepository
		)
		val client = HttpClient(
			MockEngine { request ->
				val authorization = checkNotNull(request.headers[HttpHeaders.Authorization])
				authorizationHeaders += authorization

				if (authorization == "Bearer access-old") {
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
					sessionRecoveryRepository = sessionRecoveryDataSource(
						sessionRepository = sessionRepository,
						applicationRepository = applicationRepository,
						sessionInvalidationRepository = sessionInvalidationRepository,
						attestationRepository = RecordingAttestationRepository(),
						authRepository = authRepository
					)
				)
			}
		}

		val response = try {
			client.get("https://api.tuindice.app/record/v5/sync")
		} finally {
			client.close()
		}

		assertEquals(HttpStatusCode.OK, response.status)
		assertEquals(
			listOf("Bearer access-old", "Bearer access-new"),
			authorizationHeaders
		)
		assertFalse(sessionRepository.cleared)
		assertEquals(0, applicationRepository.clearCalls)
		assertEquals(0, sessionInvalidationRepository.invalidationCalls)
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
					sessionRecoveryRepository = sessionRecoveryDataSource(
						sessionRepository = sessionRepository
					)
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
					sessionRecoveryRepository = sessionRecoveryDataSource(
						sessionRepository = sessionRepository
					)
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

private data class BootstrapSignInCall(
	val usbId: String,
	val password: String
)

private data class ExchangeSignInCall(
	val bootstrapAccessToken: String,
	val attestation: Attestation
)

private data class RefreshTokensCall(
	val sessionId: String,
	val refreshToken: String,
	val attestation: Attestation
)

private fun sessionRecoveryDataSource(
	sessionRepository: SessionRepository,
	applicationRepository: RecordingApplicationRepository = RecordingApplicationRepository(),
	sessionInvalidationRepository: FakeSessionInvalidationRepository = FakeSessionInvalidationRepository(),
	syncStatusRepository: FakeSyncStatusRepository = FakeSyncStatusRepository(),
	attestationRepository: AttestationRepository = ErrorAttestationRepository,
	authRepository: AuthRepository = ErrorAuthRepository,
	credentialsRepository: CredentialsRepository = FakeCredentialsRepository(),
	sessionCoroutineScope: SessionCoroutineScope = testSessionCoroutineScope()
): SessionRecoveryDataSource {
	return SessionRecoveryDataSource(
		sessionRepository = sessionRepository,
		applicationRepository = applicationRepository,
		sessionInvalidationRepository = sessionInvalidationRepository,
		syncStatusRepository = syncStatusRepository,
		attestationRepository = attestationRepository,
		authRepository = authRepository,
		credentialsRepository = credentialsRepository,
		sessionCoroutineScope = sessionCoroutineScope
	)
}

private object ErrorAttestationRepository : AttestationRepository {
	override suspend fun attest(request: AttestationRequest): Attestation = error("unused in this test")
}

private object ErrorAuthRepository : AuthRepository {
	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens = error("unused in this test")

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) = error("unused in this test")

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = error("unused in this test")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens = error("unused in this test")

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused in this test")
}

private class RecordingAttestationRepository : AttestationRepository {
	val requests = mutableListOf<AttestationRequest>()

	override suspend fun attest(request: AttestationRequest): Attestation {
		requests += request
		return Attestation(token = "attestation-token")
	}
}

private class RefreshPersistingAuthRepository(
	private val sessionRepository: SessionRepository,
	private val beforeRefresh: suspend () -> Unit = {}
) : AuthRepository {
	val refreshCalls = mutableListOf<RefreshTokensCall>()

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
	) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		beforeRefresh()
		refreshCalls += RefreshTokensCall(
			sessionId = sessionId,
			refreshToken = refreshToken,
			attestation = attestation
		)
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

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused")
}

private class FailingRefreshAfterSessionChangeAuthRepository(
	private val sessionRepository: SessionRepository
) : AuthRepository {
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
	) = error("unused")

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
		throw clientRequestException(HttpStatusCode.Unauthorized)
	}

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused")
}

private class SupersededRefreshThenExchangePersistingAuthRepository(
	private val sessionRepository: SessionRepository
) : AuthRepository {
	val bootstrapCalls = mutableListOf<BootstrapSignInCall>()
	val exchangeCalls = mutableListOf<ExchangeSignInCall>()

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens {
		bootstrapCalls += BootstrapSignInCall(
			usbId = usbId,
			password = password
		)

		return BootstrapTokens(
			uid = "uid-1",
			usbId = usbId,
			accessToken = "bootstrap-access",
			expiresIn = 300
		)
	}

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) {
		exchangeCalls += ExchangeSignInCall(
			bootstrapAccessToken = bootstrapAccessToken,
			attestation = attestation
		)
		sessionRepository.setSessionSnapshot(
			SessionSnapshot(
				sessionId = "session-exchanged",
				accessToken = "access-exchanged",
				refreshToken = "refresh-exchanged",
				usbId = "12-34567"
			)
		)
	}

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		throw clientRequestException(
			statusCode = HttpStatusCode.Unauthorized,
			headers = mapOf(AuthErrorHeaders.HEADER to AuthErrorHeaders.SESSION_SUPERSEDED)
		)
	}

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused")
}

private class UnauthorizedRefreshAuthRepository : AuthRepository {
	var bootstrapCalls = 0
	var exchangeCalls = 0

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens {
		bootstrapCalls++
		error("bootstrapSignIn should not be called for unclassified unauthorized refresh")
	}

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) {
		exchangeCalls++
		error("exchangeSignIn should not be called for unclassified unauthorized refresh")
	}

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		throw clientRequestException(HttpStatusCode.Unauthorized)
	}

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused")
}

private class RefreshTokenMismatchAuthRepository : AuthRepository {
	var bootstrapCalls = 0
	var exchangeCalls = 0

	override suspend fun bootstrapSignIn(
		usbId: String,
		password: String
	): BootstrapTokens {
		bootstrapCalls++
		error("bootstrapSignIn should not be called for refresh token mismatch")
	}

	override suspend fun exchangeSignIn(
		bootstrapAccessToken: String,
		attestation: Attestation
	) {
		exchangeCalls++
		error("exchangeSignIn should not be called for refresh token mismatch")
	}

	override suspend fun reissueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = error("unused")

	override suspend fun refreshTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	): RefreshTokens {
		throw clientRequestException(
			statusCode = HttpStatusCode.Unauthorized,
			headers = mapOf(AuthErrorHeaders.HEADER to REFRESH_TOKEN_MISMATCH)
		)
	}

	override suspend fun revokeTokens(
		sessionId: String,
		refreshToken: String,
		attestation: Attestation
	) = error("unused")
}

private const val REFRESH_TOKEN_MISMATCH = "refresh_token_mismatch"
