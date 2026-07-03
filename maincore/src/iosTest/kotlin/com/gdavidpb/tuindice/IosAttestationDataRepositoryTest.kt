package com.gdavidpb.tuindice

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.data.source.attestation.IosAttestationDataSource
import com.gdavidpb.tuindice.di.createSharedJson
import com.gdavidpb.tuindice.domain.model.IosPlatformAttestation
import com.gdavidpb.tuindice.domain.repository.SessionRecoveryRepository
import com.gdavidpb.tuindice.platform.IosAttestationCapability
import com.gdavidpb.tuindice.security.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.security.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.security.domain.model.AttestationProvider
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.model.AttestationTemporarilyUnavailableException
import com.gdavidpb.tuindice.security.domain.model.ProtectedOperationCodes
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IosAttestationDataRepositoryTest {
	@Test
	fun `attest rotates the key and retries when assertion generation fails`() = runTest {
		val capability = RecordingIosAttestationCapability(
			resolvedKeyIds = ArrayDeque(listOf("stale-key", "fresh-key")),
			requestFailures = ArrayDeque(listOf(IllegalStateException("stale assertion failed"), null)),
			issuedTokens = ArrayDeque(listOf("fresh-proof"))
		)
		val httpClient = appAttestHttpClient(
			sessionModes = ArrayDeque(
				listOf(
					AttestationEvidenceMode.APP_ATTEST_ASSERTION,
					AttestationEvidenceMode.APP_ATTEST_ATTESTATION
				)
			),
			tokenStatuses = ArrayDeque(listOf(HttpStatusCode.OK)),
			tokenValues = ArrayDeque(listOf("issued-token"))
		)
		val repository = IosAttestationDataSource(
			httpClient = httpClient,
			attestationCapability = capability,
			configRepository = FixedConfigRepository(iosEnforcementEnabled = true)
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = """{"refresh_token":"token"}""",
				authorization = AttestationAuthorization.Bearer(
					accessToken = "access-token"
				)
			)
		)

		assertEquals("issued-token", response.token)
		assertEquals(listOf("stale-key", "fresh-key"), capability.resolveCalls)
		assertEquals(1, capability.invalidateCalls)
		assertEquals(
			listOf(
				AttestationCall("stale-key", AttestationEvidenceMode.APP_ATTEST_ASSERTION.value),
				AttestationCall("fresh-key", AttestationEvidenceMode.APP_ATTEST_ATTESTATION.value)
			),
			capability.requestCalls
		)
	}

	@Test
	fun `attest uses local bypass when iOS enforcement is disabled`() = runTest {
		val capability = RecordingIosAttestationCapability(
			resolvedKeyIds = ArrayDeque(),
			requestFailures = ArrayDeque(),
			issuedTokens = ArrayDeque()
		)
		val httpClient = appAttestHttpClient(
			sessionModes = ArrayDeque(listOf(AttestationEvidenceMode.APP_ATTEST_ASSERTION)),
			tokenStatuses = ArrayDeque(listOf(HttpStatusCode.OK)),
			tokenValues = ArrayDeque(listOf("bypass-issued-token"))
		)
		val repository = IosAttestationDataSource(
			httpClient = httpClient,
			attestationCapability = capability,
			configRepository = FixedConfigRepository(iosEnforcementEnabled = false)
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthExchange,
				payloadJson = """{"usb_id":"12345678-9"}""",
				authorization = AttestationAuthorization.Bearer(
					accessToken = "access-token"
				)
			)
		)

		assertEquals("bypass-issued-token", response.token)
		assertEquals(emptyList<String>(), capability.resolveCalls)
		assertEquals(emptyList<AttestationCall>(), capability.requestCalls)
		assertEquals(0, capability.invalidateCalls)
	}

	@Test
	fun `attest rotates the key and retries when backend rejects the assertion`() = runTest {
		val capability = RecordingIosAttestationCapability(
			resolvedKeyIds = ArrayDeque(listOf("stale-key", "fresh-key")),
			requestFailures = ArrayDeque(listOf(null, null)),
			issuedTokens = ArrayDeque(listOf("stale-proof", "fresh-proof"))
		)
		val httpClient = appAttestHttpClient(
			sessionModes = ArrayDeque(
				listOf(
					AttestationEvidenceMode.APP_ATTEST_ASSERTION,
					AttestationEvidenceMode.APP_ATTEST_ATTESTATION
				)
			),
			tokenStatuses = ArrayDeque(listOf(HttpStatusCode.Forbidden, HttpStatusCode.OK)),
			tokenValues = ArrayDeque(listOf("ignored", "issued-token"))
		)
		val repository = IosAttestationDataSource(
			httpClient = httpClient,
			attestationCapability = capability,
			configRepository = FixedConfigRepository(iosEnforcementEnabled = true)
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = """{"refresh_token":"token"}""",
				authorization = AttestationAuthorization.Bearer(
					accessToken = "access-token"
				)
			)
		)

		assertEquals("issued-token", response.token)
		assertEquals(listOf("stale-key", "fresh-key"), capability.resolveCalls)
		assertEquals(1, capability.invalidateCalls)
		assertEquals(
			listOf(
				AttestationCall("stale-key", AttestationEvidenceMode.APP_ATTEST_ASSERTION.value),
				AttestationCall("fresh-key", AttestationEvidenceMode.APP_ATTEST_ATTESTATION.value)
			),
			capability.requestCalls
		)
	}

	@Test
	fun `attest rotates the key and retries when backend reports that the key belongs to another user`() = runTest {
		val capability = RecordingIosAttestationCapability(
			resolvedKeyIds = ArrayDeque(listOf("stale-key", "fresh-key")),
			requestFailures = ArrayDeque(listOf(null, null)),
			issuedTokens = ArrayDeque(listOf("bootstrap-proof", "business-proof"))
		)
		val httpClient = appAttestConflictRecoveryHttpClient()
		val repository = IosAttestationDataSource(
			httpClient = httpClient,
			attestationCapability = capability,
			configRepository = FixedConfigRepository(iosEnforcementEnabled = true)
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = """{"refresh_token":"token"}""",
				authorization = AttestationAuthorization.Bearer(
					accessToken = "access-token"
				)
			)
		)

		assertEquals("issued-token", response.token)
		assertEquals(listOf("stale-key", "fresh-key"), capability.resolveCalls)
		assertEquals(1, capability.invalidateCalls)
		assertEquals(
			listOf(
				AttestationCall("fresh-key", AttestationEvidenceMode.APP_ATTEST_ATTESTATION.value),
				AttestationCall("fresh-key", AttestationEvidenceMode.APP_ATTEST_ASSERTION.value)
			),
			capability.requestCalls
		)
	}

	@Test
	fun `attest bootstraps App Attest when the business session requires preparation`() = runTest {
		val capability = RecordingIosAttestationCapability(
			resolvedKeyIds = ArrayDeque(listOf("bootstrap-key")),
			requestFailures = ArrayDeque(listOf(null, null)),
			issuedTokens = ArrayDeque(listOf("bootstrap-proof", "business-proof"))
		)
		val httpClient = appAttestPreparationHttpClient()
		val repository = IosAttestationDataSource(
			httpClient = httpClient,
			attestationCapability = capability,
			configRepository = FixedConfigRepository(iosEnforcementEnabled = true)
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = """{"refresh_token":"token"}""",
				authorization = AttestationAuthorization.Bearer(
					accessToken = "access-token"
				)
			)
		)

		assertEquals("issued-token", response.token)
		assertEquals(listOf("bootstrap-key"), capability.resolveCalls)
		assertEquals(0, capability.invalidateCalls)
		assertEquals(
			listOf(
				AttestationCall("bootstrap-key", AttestationEvidenceMode.APP_ATTEST_ATTESTATION.value),
				AttestationCall("bootstrap-key", AttestationEvidenceMode.APP_ATTEST_ASSERTION.value)
			),
			capability.requestCalls
		)
	}

	@Test
	fun `attest throws a typed temporary unavailable error when app attest recovery is exhausted locally`() = runTest {
		val capability = RecordingIosAttestationCapability(
			resolvedKeyIds = ArrayDeque(listOf("stale-key", "fresh-key")),
			requestFailures = ArrayDeque(
				listOf(
					IllegalStateException("stale assertion failed"),
					IllegalStateException("fresh attestation failed")
				)
			),
			issuedTokens = ArrayDeque()
		)
		val httpClient = appAttestHttpClient(
			sessionModes = ArrayDeque(
				listOf(
					AttestationEvidenceMode.APP_ATTEST_ASSERTION,
					AttestationEvidenceMode.APP_ATTEST_ATTESTATION
				)
			),
			tokenStatuses = ArrayDeque(),
			tokenValues = ArrayDeque()
		)
		val repository = IosAttestationDataSource(
			httpClient = httpClient,
			attestationCapability = capability,
			configRepository = FixedConfigRepository(iosEnforcementEnabled = true)
		)

		val error = assertFailsWith<AttestationTemporarilyUnavailableException> {
			repository.attest(
				AttestationRequest(
					operationCode = ProtectedOperationCodes.AuthRefreshTokens,
					payloadJson = """{"refresh_token":"token"}""",
					authorization = AttestationAuthorization.Bearer(
						accessToken = "access-token"
					)
				)
			)
		}

		assertEquals("iOS", error.platform)
		assertEquals(ProtectedOperationCodes.AuthRefreshTokens.value, error.operationCode)
		assertEquals(1, capability.invalidateCalls)
	}

	@Test
	fun `attest throws a typed temporary unavailable error when backend keeps rejecting app attest after recovery`() = runTest {
		val capability = RecordingIosAttestationCapability(
			resolvedKeyIds = ArrayDeque(listOf("stale-key", "fresh-key")),
			requestFailures = ArrayDeque(listOf(null, null)),
			issuedTokens = ArrayDeque(listOf("stale-proof", "fresh-proof"))
		)
		val httpClient = appAttestHttpClient(
			sessionModes = ArrayDeque(
				listOf(
					AttestationEvidenceMode.APP_ATTEST_ASSERTION,
					AttestationEvidenceMode.APP_ATTEST_ATTESTATION
				)
			),
			tokenStatuses = ArrayDeque(listOf(HttpStatusCode.Forbidden, HttpStatusCode.Forbidden)),
			tokenValues = ArrayDeque(listOf("ignored", "ignored-again"))
		)
		val repository = IosAttestationDataSource(
			httpClient = httpClient,
			attestationCapability = capability,
			configRepository = FixedConfigRepository(iosEnforcementEnabled = true)
		)

		val error = assertFailsWith<AttestationTemporarilyUnavailableException> {
			repository.attest(
				AttestationRequest(
					operationCode = ProtectedOperationCodes.AuthRefreshTokens,
					payloadJson = """{"refresh_token":"token"}""",
					authorization = AttestationAuthorization.Bearer(
						accessToken = "access-token"
					)
				)
			)
		}

		assertEquals("iOS", error.platform)
		assertEquals(ProtectedOperationCodes.AuthRefreshTokens.value, error.operationCode)
		assertEquals(1, capability.invalidateCalls)
	}

	@Test
	fun `current session attestation recovers unauthorized token before retry`() = runTest {
		val capability = RecordingIosAttestationCapability(
			resolvedKeyIds = ArrayDeque(listOf("old-key", "fresh-key")),
			requestFailures = ArrayDeque(listOf(null)),
			issuedTokens = ArrayDeque(listOf("fresh-proof"))
		)
		val authorizationHeaders = mutableListOf<String?>()
		var sessionRequests = 0
		val httpClient = HttpClient(
			MockEngine { request ->
				authorizationHeaders += request.headers[HttpHeaders.Authorization]
				when (request.url.encodedPath) {
					"/attestation/v4/sessions" -> {
						sessionRequests++
						if (sessionRequests == 1) {
							respond(
								content = "",
								status = HttpStatusCode.Unauthorized,
								headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
							)
						} else {
							respond(
								content = """
									{
									 "session_id": "session-recovered",
									 "challenge": "challenge",
									 "expires_at": 1735689600000,
									 "evidence_mode": "app_attest_assertion"
									}
								""".trimIndent(),
								status = HttpStatusCode.OK,
								headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
							)
						}
					}

					"/attestation/v4/tokens" ->
						respond(
							content = """
								{
								 "token": "issued-token",
								 "expires_at": 1735689600000
								}
							""".trimIndent(),
							status = HttpStatusCode.OK,
							headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
						)

					else -> error("Unexpected path: ${request.url.encodedPath}")
				}
			}
		) {
			expectSuccess = true
			install(DefaultRequest) {
				headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
			}

			install(ContentNegotiation) {
				json(createSharedJson())
			}
		}
		val sessionRepository = FixedSessionRepository(
			SessionSnapshot(
				sessionId = "session-id",
				accessToken = "old-access-token",
				refreshToken = "old-refresh-token",
				usbId = "20-26123"
			)
		)
		val sessionRecoveryRepository = RecordingSessionRecoveryRepository(
			recoveredSnapshot = SessionSnapshot(
				sessionId = "session-id",
				accessToken = "fresh-access-token",
				refreshToken = "fresh-refresh-token",
				usbId = "20-26123"
			)
		)
		val repository = IosAttestationDataSource(
			httpClient = httpClient,
			attestationCapability = capability,
			configRepository = FixedConfigRepository(iosEnforcementEnabled = true),
			sessionRepository = sessionRepository,
			recoverUnauthorizedSession = sessionRecoveryRepository::recoverUnauthorizedSession
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthReissueTokens,
				payloadJson = """{"usb_id":"20-26123"}""",
				authorization = AttestationAuthorization.CurrentSession
			)
		)

		assertEquals("issued-token", response.token)
		assertEquals(
			listOf<String?>("Bearer old-access-token", "Bearer fresh-access-token", "Bearer fresh-access-token"),
			authorizationHeaders.toList()
		)
		assertEquals(
			listOf(
				RecoveryCall(
					attemptedAuthorizationAccessToken = "old-access-token",
					attemptedCachedAccessToken = "old-access-token",
					attemptedCachedRefreshToken = "old-refresh-token"
				)
			),
			sessionRecoveryRepository.calls
		)
		assertEquals(listOf("old-key", "fresh-key"), capability.resolveCalls)
		assertEquals(
			listOf(AttestationCall("fresh-key", AttestationEvidenceMode.APP_ATTEST_ASSERTION.value)),
			capability.requestCalls
		)
		assertEquals(0, capability.invalidateCalls)
	}
}

private fun appAttestHttpClient(
	sessionModes: ArrayDeque<AttestationEvidenceMode>,
	tokenStatuses: ArrayDeque<HttpStatusCode>,
	tokenValues: ArrayDeque<String>
): HttpClient {
	return HttpClient(
		MockEngine { request ->
			assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
			when (request.url.encodedPath) {
				"/attestation/v4/sessions" -> {
					val evidenceMode = sessionModes.removeFirst()
					respond(
						content = """
							{
							  "session_id": "session-${sessionModes.size}",
							  "challenge": "challenge",
							  "expires_at": 1735689600000,
							  "evidence_mode": "${evidenceMode.value}"
							}
						""".trimIndent(),
						status = HttpStatusCode.OK,
						headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
					)
				}

				"/attestation/v4/tokens" -> {
					val status = tokenStatuses.removeFirst()
					val token = tokenValues.removeFirst()
					respond(
						content = """
							{
							  "token": "$token",
							  "expires_at": 1735689600000
							}
						""".trimIndent(),
						status = status,
						headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
					)
				}

				else -> error("Unexpected path: ${request.url.encodedPath}")
			}
		}
	) {
		expectSuccess = true
		install(DefaultRequest) {
			headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
		}
		install(ContentNegotiation) {
			json(createSharedJson())
		}
	}
}

private fun appAttestPreparationHttpClient(): HttpClient {
	var sessionAttempts = 0

	return HttpClient(
		MockEngine { request ->
			assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
			when (request.url.encodedPath) {
				"/attestation/v4/sessions" -> {
					sessionAttempts += 1
					if (sessionAttempts == 1) {
						respond(
							content = """
								{
								  "code": "attestation_not_prepared",
								  "required_preparation_code": "bootstrap"
								}
							""".trimIndent(),
							status = HttpStatusCode(428, "Precondition Required"),
							headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
						)
					} else {
						respond(
							content = """
								{
								  "session_id": "business-session",
								  "challenge": "business-challenge",
								  "expires_at": 1735689600000,
								  "evidence_mode": "app_attest_assertion"
								}
							""".trimIndent(),
							status = HttpStatusCode.OK,
							headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
						)
					}
				}

				"/attestation/v4/preparations/sessions" -> {
					respond(
						content = """
							{
							  "session_id": "preparation-session",
							  "challenge": "preparation-challenge",
							  "expires_at": 1735689600000,
							  "evidence_mode": "app_attest_attestation"
							}
						""".trimIndent(),
						status = HttpStatusCode.OK,
						headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
					)
				}

				"/attestation/v4/preparations/complete" -> {
					respond(
						content = "",
						status = HttpStatusCode.NoContent,
						headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
					)
				}

				"/attestation/v4/tokens" -> {
					respond(
						content = """
							{
							  "token": "issued-token",
							  "expires_at": 1735689600000
							}
						""".trimIndent(),
						status = HttpStatusCode.OK,
						headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
					)
				}

				else -> error("Unexpected path: ${request.url.encodedPath}")
			}
		}
	) {
		expectSuccess = true
		install(DefaultRequest) {
			headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
		}
		install(ContentNegotiation) {
			json(createSharedJson())
		}
	}
}

private fun appAttestConflictRecoveryHttpClient(): HttpClient {
	var sessionAttempts = 0

	return HttpClient(
		MockEngine { request ->
			assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
			when (request.url.encodedPath) {
				"/attestation/v4/sessions" -> {
					sessionAttempts += 1
					when (sessionAttempts) {
						1 -> respond(
							content = """
								{
								  "code": "attestation_key_user_mismatch"
								}
							""".trimIndent(),
							status = HttpStatusCode.Conflict,
							headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
						)

						2 -> respond(
							content = """
								{
								  "code": "attestation_not_prepared",
								  "required_preparation_code": "bootstrap"
								}
							""".trimIndent(),
							status = HttpStatusCode(428, "Precondition Required"),
							headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
						)

						else -> respond(
							content = """
								{
								  "session_id": "business-session",
								  "challenge": "business-challenge",
								  "expires_at": 1735689600000,
								  "evidence_mode": "app_attest_assertion"
								}
							""".trimIndent(),
							status = HttpStatusCode.OK,
							headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
						)
					}
				}

				"/attestation/v4/preparations/sessions" -> {
					respond(
						content = """
							{
							  "session_id": "preparation-session",
							  "challenge": "preparation-challenge",
							  "expires_at": 1735689600000,
							  "evidence_mode": "app_attest_attestation"
							}
						""".trimIndent(),
						status = HttpStatusCode.OK,
						headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
					)
				}

				"/attestation/v4/preparations/complete" -> {
					respond(
						content = "",
						status = HttpStatusCode.NoContent,
						headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
					)
				}

				"/attestation/v4/tokens" -> {
					respond(
						content = """
							{
							  "token": "issued-token",
							  "expires_at": 1735689600000
							}
						""".trimIndent(),
						status = HttpStatusCode.OK,
						headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
					)
				}

				else -> error("Unexpected path: ${request.url.encodedPath}")
			}
		}
	) {
		expectSuccess = true
		install(DefaultRequest) {
			headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
		}
		install(ContentNegotiation) {
			json(createSharedJson())
		}
	}
}

private class RecordingIosAttestationCapability(
	private val resolvedKeyIds: ArrayDeque<String>,
	private val requestFailures: ArrayDeque<Throwable?>,
	private val issuedTokens: ArrayDeque<String>
) : IosAttestationCapability {
	val resolveCalls = mutableListOf<String>()
	val requestCalls = mutableListOf<AttestationCall>()
	var invalidateCalls = 0

	override fun sha256Base64Url(value: String): String = "hash:$value"

	override suspend fun resolveAttestationKeyId(): String {
		val keyId = resolvedKeyIds.removeFirst()
		resolveCalls += keyId
		return keyId
	}

	override suspend fun invalidateAttestationKeyId() {
		invalidateCalls++
	}

	override suspend fun requestAttestation(
		attestationInput: String,
		keyId: String,
		evidenceMode: String
	): IosPlatformAttestation {
		requestCalls += AttestationCall(keyId = keyId, evidenceMode = evidenceMode)
		requestFailures.removeFirstOrNull()?.let { throw it }

		return IosPlatformAttestation(
			token = issuedTokens.removeFirst(),
			keyId = keyId,
			provider = AttestationProvider.APP_ATTEST
		)
	}
}

private data class AttestationCall(
	val keyId: String,
	val evidenceMode: String
)

private class FixedSessionRepository(
	private val snapshot: SessionSnapshot?
) : SessionRepository {
	override suspend fun hasActiveSession(): Boolean = snapshot != null

	override suspend fun getActiveSessionSnapshot(): SessionSnapshot? = snapshot

	override suspend fun setSessionSnapshot(snapshot: SessionSnapshot) = error("Unexpected session write.")

	override suspend fun replaceSessionSnapshotIfCurrent(
		expectedSnapshot: SessionSnapshot,
		newSnapshot: SessionSnapshot
	): Boolean = error("Unexpected session replacement.")

	override suspend fun setUsbId(usbId: String) = error("Unexpected usbId write.")

	override suspend fun setSessionId(sessionId: String) = error("Unexpected sessionId write.")

	override suspend fun setAccessToken(accessToken: String) = error("Unexpected accessToken write.")

	override suspend fun setRefreshToken(refreshToken: String) = error("Unexpected refreshToken write.")

	override suspend fun getUsbId(): String = snapshot?.usbId ?: error("Unexpected usbId read.")

	override suspend fun getSessionId(): String = snapshot?.sessionId ?: error("Unexpected sessionId read.")

	override suspend fun getAccessToken(): String = snapshot?.accessToken ?: error("Unexpected accessToken read.")

	override suspend fun getRefreshToken(): String = snapshot?.refreshToken ?: error("Unexpected refreshToken read.")

	override suspend fun clear() = error("Unexpected session clear.")
}

private class RecordingSessionRecoveryRepository(
	private val recoveredSnapshot: SessionSnapshot?
) : SessionRecoveryRepository {
	val calls = mutableListOf<RecoveryCall>()

	override suspend fun recoverUnauthorizedSession(
		attemptedAuthorizationAccessToken: String?,
		attemptedCachedAccessToken: String?,
		attemptedCachedRefreshToken: String?
	): SessionSnapshot? {
		calls += RecoveryCall(
			attemptedAuthorizationAccessToken = attemptedAuthorizationAccessToken,
			attemptedCachedAccessToken = attemptedCachedAccessToken,
			attemptedCachedRefreshToken = attemptedCachedRefreshToken
		)
		return recoveredSnapshot
	}

	override suspend fun invalidateSession(sessionId: String?) = error("Unexpected session invalidation.")
}

private data class RecoveryCall(
	val attemptedAuthorizationAccessToken: String?,
	val attemptedCachedAccessToken: String?,
	val attemptedCachedRefreshToken: String?
)

private class FixedConfigRepository(
	private val iosEnforcementEnabled: Boolean
) : ConfigRepository {
	override suspend fun tryFetch() = Unit

	override fun getTimeout(): Long = 30_000L

	override fun getContactEmail(): String = "support@tuindice.app"

	override fun getContactSubject(): String = "Support"

	override fun getLoadingMessages(): List<String> = listOf("Cargando")

	override fun getTimeUpdateStalenessDays(): Int = 7

	override fun getSyncsToSuggestReview(): Int = 3

	override fun getAttestationAndroidEnforcementEnabled(): Boolean = false

	override fun getAttestationIosEnforcementEnabled(): Boolean = iosEnforcementEnabled

	override fun getAppAvailabilityNotice(): AppAvailabilityNotice {
		return AppAvailabilityNotice(
			enabled = false,
			title = "",
			message = ""
		)
	}
}
