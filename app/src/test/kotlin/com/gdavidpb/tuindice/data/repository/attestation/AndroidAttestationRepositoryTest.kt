package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.data.source.attestation.AndroidAttestationDataSource
import com.gdavidpb.tuindice.di.createSharedJson
import com.gdavidpb.tuindice.domain.repository.SessionRecoveryRepository
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation
import com.gdavidpb.tuindice.security.data.model.AttestationProofOfPossessionRequest
import com.gdavidpb.tuindice.security.domain.model.AttestationAuthorization
import com.gdavidpb.tuindice.security.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.security.domain.model.AttestationProofOfPossessionMode
import com.gdavidpb.tuindice.security.domain.model.AttestationProvider
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
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
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidAttestationDataSourceTest {
	@Test
	fun `attest includes proof of possession for the classic Android attestation flow`() = runTest {
		val proofCapability = RecordingAndroidProofOfPossessionCapability(
			resolvedKeyIds = ArrayDeque(listOf("classic-key")),
			proofFailures = ArrayDeque(listOf(null)),
			issuedSignatures = ArrayDeque(listOf("classic-signature"))
		)
		val providerDataSource = RecordingAttestationProviderDataSource()
		val httpClient = androidAttestationHttpClient(
			sessionModes = ArrayDeque(listOf(AttestationEvidenceMode.PLAY_INTEGRITY_CLASSIC)),
			proofModes = ArrayDeque(listOf(AttestationProofOfPossessionMode.ANDROID_KEYSTORE)),
			tokenStatuses = ArrayDeque(listOf(HttpStatusCode.OK)),
			tokenValues = ArrayDeque(listOf("issued-token"))
		)
		val repository = AndroidAttestationDataSource(
			ktorClient = httpClient,
			providerDataSource = providerDataSource,
			proofOfPossessionCapability = proofCapability,
			configRepository = FixedConfigRepository(androidEnforcementEnabled = true)
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

		assertEquals("issued-token", response.token)
		assertEquals(listOf("classic-key"), proofCapability.resolveCalls)
		assertEquals(listOf("classic-key"), proofCapability.proofCalls)
		assertEquals(0, proofCapability.invalidateCalls)
		assertEquals(
			listOf(AttestationEvidenceMode.PLAY_INTEGRITY_CLASSIC),
			providerDataSource.calls.map { call -> call.evidenceMode }
		)
	}

	@Test
	fun `attest uses local bypass when Android enforcement is disabled`() = runTest {
		val proofCapability = RecordingAndroidProofOfPossessionCapability(
			resolvedKeyIds = ArrayDeque(),
			proofFailures = ArrayDeque(),
			issuedSignatures = ArrayDeque()
		)
		val providerDataSource = RecordingAttestationProviderDataSource()
		val httpClient = androidAttestationHttpClient(
			sessionModes = ArrayDeque(listOf(AttestationEvidenceMode.PLAY_INTEGRITY_STANDARD)),
			proofModes = ArrayDeque(listOf(AttestationProofOfPossessionMode.ANDROID_KEYSTORE)),
			tokenStatuses = ArrayDeque(listOf(HttpStatusCode.OK)),
			tokenValues = ArrayDeque(listOf("bypass-issued-token"))
		)
		val repository = AndroidAttestationDataSource(
			ktorClient = httpClient,
			providerDataSource = providerDataSource,
			proofOfPossessionCapability = proofCapability,
			configRepository = FixedConfigRepository(androidEnforcementEnabled = false)
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
		assertEquals(emptyList<String>(), proofCapability.resolveCalls)
		assertEquals(emptyList<String>(), proofCapability.proofCalls)
		assertEquals(0, proofCapability.invalidateCalls)
		assertEquals(emptyList<RecordingAttestationProviderDataSource.Call>(), providerDataSource.calls)
	}

	@Test
	fun `attest rotates the key and retries when proof generation fails`() = runTest {
		val proofCapability = RecordingAndroidProofOfPossessionCapability(
			resolvedKeyIds = ArrayDeque(listOf("stale-key", "fresh-key")),
			proofFailures = ArrayDeque(listOf(IllegalStateException("stale signature failed"), null)),
			issuedSignatures = ArrayDeque(listOf("fresh-signature"))
		)
		val providerDataSource = RecordingAttestationProviderDataSource()
		val httpClient = androidAttestationHttpClient(
			sessionModes = ArrayDeque(
				listOf(
					AttestationEvidenceMode.PLAY_INTEGRITY_STANDARD,
					AttestationEvidenceMode.PLAY_INTEGRITY_STANDARD
				)
			),
			proofModes = ArrayDeque(
				listOf(
					AttestationProofOfPossessionMode.ANDROID_KEYSTORE,
					AttestationProofOfPossessionMode.ANDROID_KEYSTORE
				)
			),
			tokenStatuses = ArrayDeque(listOf(HttpStatusCode.OK)),
			tokenValues = ArrayDeque(listOf("issued-token"))
		)
		val repository = AndroidAttestationDataSource(
			ktorClient = httpClient,
			providerDataSource = providerDataSource,
			proofOfPossessionCapability = proofCapability,
			configRepository = FixedConfigRepository(androidEnforcementEnabled = true)
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
		assertEquals(listOf("stale-key", "fresh-key"), proofCapability.resolveCalls)
		assertEquals(1, proofCapability.invalidateCalls)
		assertEquals(listOf("stale-key", "fresh-key"), proofCapability.proofCalls)
		assertEquals(
			listOf(
				AttestationEvidenceMode.PLAY_INTEGRITY_STANDARD,
				AttestationEvidenceMode.PLAY_INTEGRITY_STANDARD
			),
			providerDataSource.calls.map { call -> call.evidenceMode }
		)
	}

	@Test
	fun `attest rotates the key and retries when backend rejects the proof`() = runTest {
		val proofCapability = RecordingAndroidProofOfPossessionCapability(
			resolvedKeyIds = ArrayDeque(listOf("stale-key", "fresh-key")),
			proofFailures = ArrayDeque(listOf(null, null)),
			issuedSignatures = ArrayDeque(listOf("stale-signature", "fresh-signature"))
		)
		val providerDataSource = RecordingAttestationProviderDataSource()
		val httpClient = androidAttestationHttpClient(
			sessionModes = ArrayDeque(
				listOf(
					AttestationEvidenceMode.PLAY_INTEGRITY_STANDARD,
					AttestationEvidenceMode.PLAY_INTEGRITY_STANDARD
				)
			),
			proofModes = ArrayDeque(
				listOf(
					AttestationProofOfPossessionMode.ANDROID_KEYSTORE,
					AttestationProofOfPossessionMode.ANDROID_KEYSTORE
				)
			),
			tokenStatuses = ArrayDeque(listOf(HttpStatusCode.Forbidden, HttpStatusCode.OK)),
			tokenValues = ArrayDeque(listOf("ignored", "issued-token"))
		)
		val repository = AndroidAttestationDataSource(
			ktorClient = httpClient,
			providerDataSource = providerDataSource,
			proofOfPossessionCapability = proofCapability,
			configRepository = FixedConfigRepository(androidEnforcementEnabled = true)
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
		assertEquals(listOf("stale-key", "fresh-key"), proofCapability.resolveCalls)
		assertEquals(1, proofCapability.invalidateCalls)
		assertEquals(listOf("stale-key", "fresh-key"), proofCapability.proofCalls)
		assertEquals(2, providerDataSource.calls.size)
	}

	@Test
	fun `attest rotates the key and retries when backend reports that the key belongs to another user`() = runTest {
		val proofCapability = RecordingAndroidProofOfPossessionCapability(
			resolvedKeyIds = ArrayDeque(listOf("stale-key", "fresh-key")),
			proofFailures = ArrayDeque(listOf(null, null)),
			issuedSignatures = ArrayDeque(listOf("bootstrap-signature", "business-signature"))
		)
		val providerDataSource = RecordingAttestationProviderDataSource()
		val httpClient = androidConflictRecoveryHttpClient()
		val repository = AndroidAttestationDataSource(
			ktorClient = httpClient,
			providerDataSource = providerDataSource,
			proofOfPossessionCapability = proofCapability,
			configRepository = FixedConfigRepository(androidEnforcementEnabled = true)
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
		assertEquals(listOf("stale-key", "fresh-key"), proofCapability.resolveCalls)
		assertEquals(1, proofCapability.invalidateCalls)
		assertEquals(listOf("fresh-key", "fresh-key"), proofCapability.proofCalls)
		assertEquals(
			listOf(
				AttestationEvidenceMode.PLAY_INTEGRITY_CLASSIC,
				AttestationEvidenceMode.PLAY_INTEGRITY_STANDARD
			),
			providerDataSource.calls.map { call -> call.evidenceMode }
		)
	}

	@Test
	fun `attest bootstraps Android attestation when the business session requires preparation`() = runTest {
		val proofCapability = RecordingAndroidProofOfPossessionCapability(
			resolvedKeyIds = ArrayDeque(listOf("bootstrap-key")),
			proofFailures = ArrayDeque(listOf(null, null)),
			issuedSignatures = ArrayDeque(listOf("bootstrap-signature", "business-signature"))
		)
		val providerDataSource = RecordingAttestationProviderDataSource()
		val httpClient = androidPreparationHttpClient()
		val repository = AndroidAttestationDataSource(
			ktorClient = httpClient,
			providerDataSource = providerDataSource,
			proofOfPossessionCapability = proofCapability,
			configRepository = FixedConfigRepository(androidEnforcementEnabled = true)
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
		assertEquals(listOf("bootstrap-key"), proofCapability.resolveCalls)
		assertEquals(listOf("bootstrap-key", "bootstrap-key"), proofCapability.proofCalls)
		assertEquals(0, proofCapability.invalidateCalls)
		assertEquals(
			listOf(
				AttestationEvidenceMode.PLAY_INTEGRITY_CLASSIC,
				AttestationEvidenceMode.PLAY_INTEGRITY_STANDARD
			),
			providerDataSource.calls.map { call -> call.evidenceMode }
		)
	}

	@Test
	fun `bootstrap preparation sends the same contract binding to Play Integrity and proof of possession`() = runTest {
		val proofCapability = RecordingAndroidProofOfPossessionCapability(
			resolvedKeyIds = ArrayDeque(listOf(CONTRACT_KEY_ID)),
			proofFailures = ArrayDeque(listOf(null, null)),
			issuedSignatures = ArrayDeque(listOf("bootstrap-signature", "business-signature"))
		)
		val providerDataSource = RecordingAttestationProviderDataSource()
		val httpClient = androidPreparationHttpClient(
			preparationSessionId = CONTRACT_SESSION_ID,
			preparationChallenge = CONTRACT_CHALLENGE
		)
		val repository = AndroidAttestationDataSource(
			ktorClient = httpClient,
			providerDataSource = providerDataSource,
			proofOfPossessionCapability = proofCapability,
			configRepository = FixedConfigRepository(androidEnforcementEnabled = true)
		)

		repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = """{"refresh_token":"token"}""",
				authorization = AttestationAuthorization.Bearer(
					accessToken = "access-token"
				)
			)
		)

		assertEquals(CONTRACT_BINDING_HASH, providerDataSource.calls.first().bindingHash)
		assertEquals(CONTRACT_BINDING_HASH, proofCapability.attestationInputs.first())
		assertEquals(
			providerDataSource.calls.map { call -> call.bindingHash },
			proofCapability.attestationInputs
		)
	}

	@Test
	fun `current session attestation recovers unauthorized token before retry`() = runTest {
		val proofCapability = RecordingAndroidProofOfPossessionCapability(
			resolvedKeyIds = ArrayDeque(listOf("old-key", "fresh-key")),
			proofFailures = ArrayDeque(),
			issuedSignatures = ArrayDeque()
		)
		val providerDataSource = RecordingAttestationProviderDataSource()
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
									 "evidence_mode": "play_integrity_standard"
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
		val repository = AndroidAttestationDataSource(
			ktorClient = httpClient,
			providerDataSource = providerDataSource,
			proofOfPossessionCapability = proofCapability,
			configRepository = FixedConfigRepository(androidEnforcementEnabled = true),
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
		assertEquals(listOf("old-key", "fresh-key"), proofCapability.resolveCalls)
		assertEquals(emptyList<String>(), proofCapability.proofCalls)
		assertEquals(0, proofCapability.invalidateCalls)
	}
}

private fun androidAttestationHttpClient(
	sessionModes: ArrayDeque<AttestationEvidenceMode>,
	proofModes: ArrayDeque<AttestationProofOfPossessionMode?>,
	tokenStatuses: ArrayDeque<HttpStatusCode>,
	tokenValues: ArrayDeque<String>
): HttpClient {
	return HttpClient(
		MockEngine { request ->
			assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
			when (request.url.encodedPath) {
				"/attestation/v4/sessions" -> {
					val evidenceMode = sessionModes.removeFirst()
					val proofMode = proofModes.removeFirst()
					respond(
						content = """
							{
							  "session_id": "session-${sessionModes.size}",
							  "challenge": "challenge",
							  "expires_at": 1735689600000,
							  "evidence_mode": "${evidenceMode.value}",
							  "proof_of_possession_mode": ${proofMode?.let { "\"${it.value}\"" } ?: "null"}
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

private fun androidPreparationHttpClient(
	preparationSessionId: String = "preparation-session",
	preparationChallenge: String = "preparation-challenge"
): HttpClient {
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
								  "evidence_mode": "play_integrity_standard",
								  "proof_of_possession_mode": "android_keystore"
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
							  "session_id": "$preparationSessionId",
							  "challenge": "$preparationChallenge",
							  "expires_at": 1735689600000,
							  "evidence_mode": "play_integrity_classic",
							  "proof_of_possession_mode": "android_keystore"
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

private fun androidConflictRecoveryHttpClient(): HttpClient {
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
								  "evidence_mode": "play_integrity_standard",
								  "proof_of_possession_mode": "android_keystore"
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
							  "evidence_mode": "play_integrity_classic",
							  "proof_of_possession_mode": "android_keystore"
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

private class RecordingAndroidProofOfPossessionCapability(
	private val resolvedKeyIds: ArrayDeque<String>,
	private val proofFailures: ArrayDeque<Throwable?>,
	private val issuedSignatures: ArrayDeque<String>,
	private val issuedAttestationCertificateChains: ArrayDeque<List<String>?> = ArrayDeque()
) : AndroidProofOfPossessionCapability {
	val resolveCalls = mutableListOf<String>()
	val proofCalls = mutableListOf<String>()
	val attestationInputs = mutableListOf<String>()
	var invalidateCalls = 0

	override suspend fun resolveProofOfPossessionKeyId(): String {
		val keyId = resolvedKeyIds.removeFirst()
		resolveCalls += keyId
		return keyId
	}

	override suspend fun invalidateProofOfPossessionKeyId() {
		invalidateCalls++
	}

	override suspend fun createProofOfPossession(
		attestationInput: String,
		keyId: String,
		requireKeyAttestation: Boolean
	): AttestationProofOfPossessionRequest {
		proofCalls += keyId
		attestationInputs += attestationInput
		proofFailures.removeFirstOrNull()?.let { throw it }

		return AttestationProofOfPossessionRequest(
			signature = issuedSignatures.removeFirst(),
			publicKey = "public-$keyId",
			attestationCertificateChain = if (requireKeyAttestation) {
				issuedAttestationCertificateChains.removeFirstOrNull() ?: listOf("chain-$keyId")
			} else {
				null
			}
		)
	}
}

private const val CONTRACT_KEY_ID = "eba7ac52-ba56-4faf-99e5-72edb82d5274"
private const val CONTRACT_SESSION_ID = "11111111-2222-3333-4444-555555555555"
private const val CONTRACT_CHALLENGE = "GS+cAPertlUa8E4inMJ9QFN7rNGF0I20Mv8bgRpHdAg="
private const val CONTRACT_BINDING_HASH = "Kl_NfMULpzI-_UxPCSCh3CLuPCPmZNnNvWQFpa-1Bc0"

private class RecordingAttestationProviderDataSource : AttestationProviderDataRepository {
	data class Call(
		val bindingHash: String,
		val evidenceMode: AttestationEvidenceMode
	)

	val calls = mutableListOf<Call>()

	override suspend fun getAttestation(
		bindingHash: String,
		evidenceMode: AttestationEvidenceMode
	): ProviderAttestation {
		calls += Call(
			bindingHash = bindingHash,
			evidenceMode = evidenceMode
		)

		return ProviderAttestation(
			token = "android-debug-attestation:$bindingHash",
			provider = AttestationProvider.PLAY_INTEGRITY
		)
	}
}

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
	private val androidEnforcementEnabled: Boolean
) : ConfigRepository {
	override suspend fun tryFetch() = Unit

	override fun getTimeout(): Long = 30_000L

	override fun getContactEmail(): String = "support@tuindice.app"

	override fun getContactSubject(): String = "Support"

	override fun getLoadingMessages(): List<String> = listOf("Cargando")

	override fun getTimeUpdateStalenessDays(): Int = 7

	override fun getSyncsToSuggestReview(): Int = 3

	override fun getAttestationAndroidEnforcementEnabled(): Boolean = androidEnforcementEnabled

	override fun getAttestationIosEnforcementEnabled(): Boolean = true

	override fun getAppAvailabilityNotice(): AppAvailabilityNotice {
		return AppAvailabilityNotice(
			enabled = false,
			title = "",
			message = ""
		)
	}
}
