package com.gdavidpb.tuindice

import com.gdavidpb.tuindice.base.domain.model.AttestationEvidenceMode
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.ProtectedOperationCodes
import com.gdavidpb.tuindice.data.repository.attestation.IosAttestationDataRepository
import com.gdavidpb.tuindice.di.createSharedJson
import com.gdavidpb.tuindice.domain.model.IosPlatformAttestation
import com.gdavidpb.tuindice.platform.IosAttestationCapability
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

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
		val repository = IosAttestationDataRepository(
			httpClientProvider = { httpClient },
			attestationCapability = capability
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = """{"refresh_token":"token"}"""
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
		val repository = IosAttestationDataRepository(
			httpClientProvider = { httpClient },
			attestationCapability = capability
		)

		val response = repository.attest(
			AttestationRequest(
				operationCode = ProtectedOperationCodes.AuthRefreshTokens,
				payloadJson = """{"refresh_token":"token"}"""
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
}

private fun appAttestHttpClient(
	sessionModes: ArrayDeque<AttestationEvidenceMode>,
	tokenStatuses: ArrayDeque<HttpStatusCode>,
	tokenValues: ArrayDeque<String>
): HttpClient {
	return HttpClient(
		MockEngine { request ->
			when (request.url.encodedPath) {
				"/attestation/v2/sessions" -> {
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
