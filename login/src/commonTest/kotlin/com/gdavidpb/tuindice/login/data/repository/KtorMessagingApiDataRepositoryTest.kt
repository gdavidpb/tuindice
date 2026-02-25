package com.gdavidpb.tuindice.login.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KtorMessagingApiDataRepositoryTest {
	@Test
	fun subscribe_withValidToken_postsMessagingRequest() = runBlocking {
		var calls = 0
		var capturedRequest: HttpRequestData? = null
		val repository = KtorMessagingApiDataRepository(
			ktorClient = HttpClient(MockEngine { request ->
				calls++
				capturedRequest = request
				respond(
					content = "{}",
					status = HttpStatusCode.OK,
					headers = headersOf("Content-Type", ContentType.Application.Json.toString())
				)
			}) {
				install(ContentNegotiation) {
					json(Json { ignoreUnknownKeys = true })
				}
			}
		)

		repository.subscribe("push-token-1")

		val request = checkNotNull(capturedRequest)
		assertEquals(1, calls)
		assertEquals(HttpMethod.Post, request.method)
		assertEquals("/messaging", request.url.encodedPath)
	}

	@Test
	fun subscribe_withBlankToken_throwsAndSkipsRequest() = runBlocking {
		var calls = 0
		val repository = KtorMessagingApiDataRepository(
			ktorClient = HttpClient(MockEngine {
				calls++
				respond(
					content = "{}",
					status = HttpStatusCode.OK,
					headers = headersOf("Content-Type", ContentType.Application.Json.toString())
				)
			}) {
				install(ContentNegotiation) {
					json(Json { ignoreUnknownKeys = true })
				}
			}
		)

		assertFailsWith<IllegalArgumentException> {
			repository.subscribe("   ")
		}
		assertEquals(0, calls)
	}
}
