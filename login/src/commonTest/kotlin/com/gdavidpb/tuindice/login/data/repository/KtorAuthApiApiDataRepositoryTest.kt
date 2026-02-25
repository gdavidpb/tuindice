package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class KtorAuthApiApiDataRepositoryTest {
	@Test
	fun issueTokens_sendsAttestationProviderAndKeyIdHeaders() = runBlocking {
		var capturedRequest: HttpRequestData? = null
		val client = HttpClient(MockEngine { request ->
				capturedRequest = request

				respond(
					content = ISSUE_TOKENS_RESPONSE,
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}) {
			install(ContentNegotiation) {
				json(Json { ignoreUnknownKeys = true })
			}
		}
		val repository = KtorAuthApiApiDataRepository(ktorClient = client)

		val response = repository.issueTokens(
			usbId = "20320000",
			password = "password",
			attestation = Attestation(
				id = "attestation-id",
				token = "attestation-token",
				provider = AttestationProvider.APP_ATTEST,
				keyId = "key-id-1"
			)
		)

		val request = checkNotNull(capturedRequest)
		val expectedBasicToken = Base64.Default.encode("20320000:password".encodeToByteArray())

		assertEquals("u-1", response.uid)
		assertEquals("20320000", response.usbId)
		assertEquals("access-1", response.accessToken)
		assertEquals("refresh-1", response.refreshToken)
		assertEquals(3600L, response.expiresIn)
		assertEquals("Basic $expectedBasicToken", request.headers[HttpHeaders.Authorization])
		assertEquals(
			"Basic $expectedBasicToken",
			request.headers["X-Forwarded-Authorization"]
		)
		assertEquals("attestation-id", request.headers["Attestation-Id"])
		assertEquals("attestation-token", request.headers["Attestation"])
		assertEquals("APP_ATTEST", request.headers["Attestation-Provider"])
		assertEquals("key-id-1", request.headers["Attestation-Key-Id"])
	}

	@Test
	fun refreshTokens_withPlayIntegrity_omitsAttestationKeyIdHeaderWhenNull() = runBlocking {
		var capturedRequest: HttpRequestData? = null
		val client = HttpClient(MockEngine { request ->
				capturedRequest = request

				respond(
					content = REFRESH_TOKENS_RESPONSE,
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}) {
			install(ContentNegotiation) {
				json(Json { ignoreUnknownKeys = true })
			}
		}
		val repository = KtorAuthApiApiDataRepository(ktorClient = client)

		val response = repository.refreshTokens(
			accessToken = "access-1",
			refreshToken = "refresh-1",
			attestation = Attestation(
				id = "attestation-id",
				token = "attestation-token",
				provider = AttestationProvider.PLAY_INTEGRITY,
				keyId = null
			)
		)

		val request = checkNotNull(capturedRequest)

		assertEquals("access-2", response.accessToken)
		assertEquals("refresh-2", response.refreshToken)
		assertEquals(1800L, response.expiresIn)
		assertEquals("attestation-id", request.headers["Attestation-Id"])
		assertEquals("attestation-token", request.headers["Attestation"])
		assertEquals("PLAY_INTEGRITY", request.headers["Attestation-Provider"])
		assertNull(request.headers["Attestation-Key-Id"])
	}

	@Test
	fun refreshTokens_withAppAttestAndNullKeyId_throwsIllegalArgumentException() {
		runBlocking {
			val client = HttpClient(MockEngine {
				respond(
					content = REFRESH_TOKENS_RESPONSE,
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}) {
				install(ContentNegotiation) {
					json(Json { ignoreUnknownKeys = true })
				}
			}
			val repository = KtorAuthApiApiDataRepository(ktorClient = client)

			assertFailsWith<IllegalArgumentException> {
				repository.refreshTokens(
					accessToken = "access-1",
					refreshToken = "refresh-1",
					attestation = Attestation(
						id = "attestation-id",
						token = "attestation-token",
						provider = AttestationProvider.APP_ATTEST,
						keyId = null
					)
				)
			}
		}
	}

	@Test
	fun refreshTokens_withAppAttestAndBlankKeyId_throwsIllegalArgumentException() {
		runBlocking {
			val client = HttpClient(MockEngine {
				respond(
					content = REFRESH_TOKENS_RESPONSE,
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}) {
				install(ContentNegotiation) {
					json(Json { ignoreUnknownKeys = true })
				}
			}
			val repository = KtorAuthApiApiDataRepository(ktorClient = client)

			assertFailsWith<IllegalArgumentException> {
				repository.refreshTokens(
					accessToken = "access-1",
					refreshToken = "refresh-1",
					attestation = Attestation(
						id = "attestation-id",
						token = "attestation-token",
						provider = AttestationProvider.APP_ATTEST,
						keyId = "   "
					)
				)
			}
		}
	}

	private companion object {
		private const val ISSUE_TOKENS_RESPONSE = """
			{
			  "uid": "u-1",
			  "usb_id": "20320000",
			  "access_token": "access-1",
			  "refresh_token": "refresh-1",
			  "expires_in": 3600
			}
		"""

		private const val REFRESH_TOKENS_RESPONSE = """
			{
			  "access_token": "access-2",
			  "refresh_token": "refresh-2",
			  "expires_in": 1800
			}
		"""
	}
}
