package com.gdavidpb.tuindice.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

// The client the app builds logs what the plugin writes for each request, except the bytes of an attachment.
class KtorAttachmentLoggingTest {
	@Test
	fun theUploadOfAnAttachmentWritesNothingToTheLog() = runTest {
		val lines = mutableListOf<String>()
		val client = clientLoggingInto(lines)

		client.post("https://api.tuindice.app/assistant/v1/attachments") {
			setBody(ByteArray(PHOTO_BYTES) { it.toByte() })
		}

		assertTrue(lines.isEmpty(), lines.joinToString("\n"))
	}

	@Test
	fun anotherRequestOfTheAssistantIsWrittenToTheLog() = runTest {
		val lines = mutableListOf<String>()
		val client = clientLoggingInto(lines)

		client.get("https://api.tuindice.app/assistant/v1/status")

		assertTrue(lines.any { line -> line.contains("assistant/v1/status") }, lines.joinToString("\n"))
	}

	private fun clientLoggingInto(lines: MutableList<String>): HttpClient {
		val logger = object : Logger {
			override fun log(message: String) {
				lines += message
			}
		}

		val engine = MockEngine { request ->
			respond(
				content = "",
				status = if (request.method.value == "POST") HttpStatusCode.Created else HttpStatusCode.OK
			)
		}

		return HttpClient(engine) {
			installKtorLogging(logger = logger)
		}
	}

	private companion object {
		const val PHOTO_BYTES = 4_096
	}
}
