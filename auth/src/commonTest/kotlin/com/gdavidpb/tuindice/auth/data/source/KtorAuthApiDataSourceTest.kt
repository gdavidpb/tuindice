package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.base.data.source.network.AttestationHeaders
import com.gdavidpb.tuindice.base.domain.model.Attestation
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.content.OutgoingContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

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
			ktorClient = httpClient
		)

		dataSource.revokeTokens(
			sessionId = "session-123",
			refreshToken = "refresh-token",
			attestation = Attestation(token = "attestation-token")
		)
	}
}
