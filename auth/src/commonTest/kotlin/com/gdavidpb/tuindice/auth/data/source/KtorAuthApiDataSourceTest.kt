package com.gdavidpb.tuindice.auth.data.source

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class KtorAuthApiDataSourceTest {
	@Test
	fun revokeTokens_sendsBearerAuthorizationHeader() = runTest {
		val httpClient = HttpClient(
			MockEngine { request ->
				assertEquals(HttpMethod.Post, request.method)
				assertEquals("/auth/v1/token/revoke", request.url.encodedPath)
				assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])

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

		dataSource.revokeTokens(accessToken = "access-token")
	}
}
