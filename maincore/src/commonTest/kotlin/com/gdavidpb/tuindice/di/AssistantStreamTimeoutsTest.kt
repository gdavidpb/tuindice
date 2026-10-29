package com.gdavidpb.tuindice.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantStreamTimeoutsTest {
	private val sharedTimeoutMillis = 30_000L

	// Slower than the shared timeout: the answer of a turn that streams goes on for longer than that.
	private val slowAnswerMillis = 31_000L

	@Test
	fun aStreamAnswerSlowerThanTheSharedTimeoutIsNotCut() = runTest {
		val client = slowClient {
			installAssistantStreamTimeouts(timeoutMillis = sharedTimeoutMillis)
		}

		assertEquals("stream", client.get(URL).bodyAsText())
	}

	// The control: the same answer is cut by the request timeout the shared client sets. Without it, the test above
	// would prove nothing.
	@Test
	fun aRequestTimeoutOfTheSharedKindWouldCutTheSameAnswer() = runTest {
		val client = slowClient {
			install(HttpTimeout) {
				requestTimeoutMillis = sharedTimeoutMillis
			}
		}

		assertFailsWith<HttpRequestTimeoutException> {
			client.get(URL).bodyAsText()
		}
	}

	private fun slowClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient {
		val engine = MockEngine {
			delay(slowAnswerMillis)

			respond(content = "stream", status = HttpStatusCode.OK)
		}

		return HttpClient(engine) {
			this.configure()
		}
	}

	private companion object {
		const val URL = "https://assistant.test/assistant/v1/events/turns/turn-1"
	}
}
