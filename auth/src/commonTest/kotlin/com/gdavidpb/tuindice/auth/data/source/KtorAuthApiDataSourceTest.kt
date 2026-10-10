package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.base.domain.exception.ServiceRetryWindowException
import com.gdavidpb.tuindice.base.utils.extension.isTooManyRequests
import com.gdavidpb.tuindice.base.utils.extension.isUnauthorized
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.security.data.source.network.AttestationHeaders
import com.gdavidpb.tuindice.security.domain.model.Attestation
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class KtorAuthApiDataSourceTest {
	@Test
	fun revokeTokens_sendsSessionCredentialsInRequestBody() = runTest {
		val httpClient = HttpClient(
			MockEngine { request ->
				assertEquals(HttpMethod.Post, request.method)
				assertEquals("/auth/v2/token/revoke", request.url.encodedPath)
				assertEquals(null, request.headers[HttpHeaders.Authorization])
				assertEquals("attestation-token", request.headers[AttestationHeaders.ATTESTATION_TOKEN])
				val body = (request.body as OutgoingContent.ByteArrayContent)
					.bytes()
					.decodeToString()
				val bodyJson = Json.parseToJsonElement(body).jsonObject

				assertEquals("session-123", bodyJson.getValue("session_id").jsonPrimitive.content)
				assertEquals("refresh-token", bodyJson.getValue("refresh_token").jsonPrimitive.content)

				respond(
					content = "",
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			expectSuccess = true

			install(DefaultRequest) {
				url("https://api.tuindice.app/")
				headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
			}

			install(ContentNegotiation) {
				json(
					Json {
						explicitNulls = true
						prettyPrint = false
					}
				)
			}
		}
		val dataSource = KtorAuthApiDataSource(
			ktorClient = httpClient,
			retryWindow = AuthRetryWindowDataSource()
		)

		dataSource.revokeTokens(
			sessionId = "session-123",
			refreshToken = "refresh-token",
			attestation = Attestation(token = "attestation-token")
		)
	}

	@Test
	fun bootstrap_after503WithRetryAfter_failsWithoutTouchingTheNetwork() = runTest {
		var networkCalls = 0
		val dataSource = dataSourceAnswering(status = HttpStatusCode.ServiceUnavailable, retryAfter = "30") {
			networkCalls++
		}

		val first = runCatching { dataSource.bootstrapSignIn(usbId = "20-26123", password = "secret") }

		assertIs<ResponseException>(first.exceptionOrNull())
		assertTrue(first.exceptionOrNull()!!.isUnavailable())
		assertEquals(1, networkCalls)

		val second = runCatching { dataSource.bootstrapSignIn(usbId = "20-26123", password = "secret") }

		val blocked = assertIs<ServiceRetryWindowException>(second.exceptionOrNull())
		assertTrue(blocked.isUnavailable())
		assertTrue(blocked.retryAfterMillis in 1L..30_000L)
		assertEquals(1, networkCalls)
	}

	@Test
	fun bootstrap_after429_waitsTheDefaultAndKeepsTheOriginalStatus() = runTest {
		var networkCalls = 0
		val dataSource = dataSourceAnswering(status = HttpStatusCode.TooManyRequests, retryAfter = null) {
			networkCalls++
		}

		val first = runCatching { dataSource.bootstrapSignIn(usbId = "20-26123", password = "secret") }

		assertTrue(first.exceptionOrNull()!!.isTooManyRequests())

		val second = runCatching { dataSource.bootstrapSignIn(usbId = "20-26123", password = "secret") }

		assertIs<ServiceRetryWindowException>(second.exceptionOrNull())
		assertEquals(1, networkCalls)
	}

	@Test
	fun otherStatuses_neverArmTheWait() = runTest {
		var networkCalls = 0
		val dataSource = dataSourceAnswering(status = HttpStatusCode.Unauthorized, retryAfter = "30") {
			networkCalls++
		}

		repeat(2) {
			val failure = runCatching { dataSource.bootstrapSignIn(usbId = "20-26123", password = "secret") }

			assertTrue(failure.exceptionOrNull()!!.isUnauthorized())
		}

		assertEquals(2, networkCalls)
	}

	private fun dataSourceAnswering(
		status: HttpStatusCode,
		retryAfter: String?,
		onCall: () -> Unit
	): KtorAuthApiDataSource {
		val httpClient = HttpClient(
			MockEngine {
				onCall()
				respond(
					content = "",
					status = status,
					headers = if (retryAfter != null) headersOf(HttpHeaders.RetryAfter, retryAfter) else headersOf()
				)
			}
		) {
			expectSuccess = true
		}

		return KtorAuthApiDataSource(
			ktorClient = httpClient,
			retryWindow = AuthRetryWindowDataSource()
		)
	}
}
