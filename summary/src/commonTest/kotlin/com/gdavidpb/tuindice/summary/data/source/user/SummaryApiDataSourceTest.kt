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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SummaryApiDataSourceTest {
	@Test
	fun getUser_maps_null_picture_url_to_blank() = runTest {
		val client = HttpClient(
			engine = MockEngine {
				respond(
					content = """
						{
						  "id": "user-1",
						  "c_id": 12345678,
						  "usb_id": "11-11111",
						  "email": "11-11111@usb.ve",
						  "full_name": "Luis Perez",
						  "first_names": "Luis",
						  "last_names": "Perez",
						  "picture_url": null,
						  "career_name": "Ingenieria de Computacion",
						  "career_code": 800,
						  "scholarship": false,
						  "grade": 4.2308,
						  "enrolled_subjects": 26,
						  "enrolled_credits": 88,
						  "approved_subjects": 23,
						  "approved_credits": 78,
						  "approved_relation": 0.88,
						  "retired_subjects": 1,
						  "retired_credits": 3,
						  "retired_relation": 0.04,
						  "failed_subjects": 2,
						  "failed_credits": 7,
						  "failed_relation": 0.08,
						  "last_update": 1674101643920
						}
					""".trimIndent(),
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			install(ContentNegotiation) {
				json()
			}
		}

		val result = SummaryApiDataSource(ktorClient = client).getUser()

		assertEquals("", result.pictureUrl)
		assertEquals("Luis Perez", result.fullName)
	}

	@Test
	fun uploadProfilePicture_sends_explicit_content_length_and_mime_type() = runTest {
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
