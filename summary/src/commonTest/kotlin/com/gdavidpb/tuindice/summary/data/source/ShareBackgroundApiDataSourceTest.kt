package com.gdavidpb.tuindice.summary.data.source

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShareBackgroundApiDataSourceTest {
	@Test
	fun when_theCatalogIsRequested_then_itReadsTheBackgroundsOfTheShareEndpoint() = runTest {
		var requestedPath = ""
		val client = clientAnswering(status = HttpStatusCode.OK, content = CATALOG_JSON) { path ->
			requestedPath = path
		}

		val backgrounds = ShareBackgroundApiDataSource(ktorClient = client).getShareBackgrounds()

		assertTrue(requestedPath.endsWith("users/v1/share-backgrounds"), "Requested $requestedPath")
		assertEquals(listOf("fuente-hidrocinetica", "biblioteca-central"), backgrounds.map { it.id })
		assertEquals("Gabriel Martín Landrove", backgrounds.first().author)
	}

	@Test
	fun when_aBackgroundHasNoAuthor_then_itsAuthorIsNull() = runTest {
		val client = clientAnswering(status = HttpStatusCode.OK, content = CATALOG_JSON)

		val biblioteca = ShareBackgroundApiDataSource(ktorClient = client)
			.getShareBackgrounds()
			.single { background -> background.id == "biblioteca-central" }

		assertNull(biblioteca.author)
	}

	@Test
	fun when_theCatalogIsUnavailable_then_theReadFails() = runTest {
		val client = clientAnswering(status = HttpStatusCode.ServiceUnavailable, content = "")

		assertFailsWith<Exception> {
			ShareBackgroundApiDataSource(ktorClient = client).getShareBackgrounds()
		}
	}

	private fun clientAnswering(
		status: HttpStatusCode,
		content: String,
		onRequest: (String) -> Unit = {}
	): HttpClient {
		return HttpClient(
			engine = MockEngine { request ->
				onRequest(request.url.encodedPath)

				respond(
					content = content,
					status = status,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			expectSuccess = true

			install(ContentNegotiation) {
				json()
			}
		}
	}

	private companion object {
		val CATALOG_JSON = """
			{
			  "backgrounds": [
			    {
			      "id": "fuente-hidrocinetica",
			      "order": 2,
			      "version": 1,
			      "title": "Estructura Hidrocinética",
			      "author": "Gabriel Martín Landrove",
			      "shortName": "Hidrocinética",
			      "card": "https://storage.googleapis.com/tu-indice-share-backgrounds/fuente-hidrocinetica/v1/card.webp",
			      "thumb": "https://storage.googleapis.com/tu-indice-share-backgrounds/fuente-hidrocinetica/v1/thumb.webp"
			    },
			    {
			      "id": "biblioteca-central",
			      "order": 4,
			      "version": 1,
			      "title": "Biblioteca Central",
			      "author": null,
			      "shortName": "Biblioteca",
			      "card": "https://storage.googleapis.com/tu-indice-share-backgrounds/biblioteca-central/v1/card.webp",
			      "thumb": "https://storage.googleapis.com/tu-indice-share-backgrounds/biblioteca-central/v1/thumb.webp"
			    }
			  ]
			}
		""".trimIndent()
	}
}
