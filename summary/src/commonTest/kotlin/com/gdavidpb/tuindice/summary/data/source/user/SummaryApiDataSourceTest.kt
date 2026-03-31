package com.gdavidpb.tuindice.summary.data.source.user

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SummaryApiDataSourceTest {
	@Test
	fun uploadProfilePicture_sends_explicit_content_length_and_mime_type() = kotlinx.coroutines.test.runTest {
		val pictureBytes = byteArrayOf(1, 2, 3, 4)
		var capturedContentLength: String? = null
		var capturedContentType: String? = null
		var capturedBody: ByteArray? = null
		val client = HttpClient(
			engine = MockEngine { request ->
				val outgoingContent = request.body as OutgoingContent.ByteArrayContent

				capturedContentLength = request.headers[HttpHeaders.ContentLength]
				capturedContentType = outgoingContent.contentType?.toString()
				capturedBody = outgoingContent.bytes()

				respond(
					content = """{"url":"https://cdn.example.com/profile.jpg"}""",
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			install(ContentNegotiation) {
				json()
			}
		}

		val result = SummaryApiDataSource(ktorClient = client).uploadProfilePicture(
			content = pictureBytes,
			mimeType = "image/png"
		)

		assertEquals("https://cdn.example.com/profile.jpg", result.url)
		assertEquals(pictureBytes.size.toString(), capturedContentLength)
		assertEquals(ContentType.Image.PNG.toString(), capturedContentType)
		assertContentEquals(pictureBytes, capturedBody)
	}
}
