package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.data.model.AttestationProofOfPossessionRequest
import com.gdavidpb.tuindice.base.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.AttestationProofOfPossessionMode
import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.di.createSharedJson
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import com.gdavidpb.tuindice.platform.android.model.ProviderAttestation
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
			proofOfPossessionCapability = proofCapability
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthIssueTokens,
				payloadJson = """{"usb_id":"12345678-9"}"""
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
			proofOfPossessionCapability = proofCapability
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = """{"refresh_token":"token"}"""
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
			proofOfPossessionCapability = proofCapability
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = """{"refresh_token":"token"}"""
			)
		)

		assertEquals("issued-token", response.token)
		assertEquals(listOf("stale-key", "fresh-key"), proofCapability.resolveCalls)
		assertEquals(1, proofCapability.invalidateCalls)
		assertEquals(listOf("stale-key", "fresh-key"), proofCapability.proofCalls)
		assertEquals(2, providerDataSource.calls.size)
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
			proofOfPossessionCapability = proofCapability
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = """{"refresh_token":"token"}"""
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
}

private fun androidAttestationHttpClient(
	sessionModes: ArrayDeque<AttestationEvidenceMode>,
	proofModes: ArrayDeque<AttestationProofOfPossessionMode?>,
	tokenStatuses: ArrayDeque<HttpStatusCode>,
	tokenValues: ArrayDeque<String>
): HttpClient {
	return HttpClient(
		MockEngine { request ->
			when (request.url.encodedPath) {
				"/attestation/v2/sessions" -> {
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

				"/attestation/v2/tokens" -> {
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

private fun androidPreparationHttpClient(): HttpClient {
	var sessionAttempts = 0

	return HttpClient(
		MockEngine { request ->
			when (request.url.encodedPath) {
				"/attestation/v2/sessions" -> {
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

				"/attestation/v2/preparations/sessions" -> {
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

				"/attestation/v2/preparations/complete" -> {
					respond(
						content = "",
						status = HttpStatusCode.NoContent,
						headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
					)
				}

				"/attestation/v2/tokens" -> {
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
