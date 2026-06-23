package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams
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

class KtorPensumApiDataSourceTest {
	@Test
	fun getPensum_usesExpectedPathAndQueryAndParsesPayload() = runTest {
		var capturedPath: String? = null
		var capturedQuery: String? = null
		val client = HttpClient(
			engine = MockEngine { request ->
				capturedPath = request.url.encodedPath
				capturedQuery = request.url.encodedQuery
				respond(
					content = sampleResponse,
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			expectSuccess = true
			install(ContentNegotiation) { json() }
		}

		val result = KtorPensumApiDataSource(client).getPensum(
			PensumSelectionParams(
				year = 2019,
				modalityId = "degree_project"
			)
		)

		assertEquals("/pensums/v4", capturedPath)
		assertEquals("year=2019&modality_id=degree_project", capturedQuery)
		assertEquals("Ingenieria de Computacion", result.careerName)
		assertEquals("0800-2019-degree_project", result.selectedPensumId)
		assertEquals(listOf(2016, 2017, 2018, 2019), result.availablePensums.map { option -> option.year })
		assertEquals(
			listOf("degree_project", "long_internship", "exclusive_degree_project"),
			result.availableModalities.map { modality -> modality.id }
		)
		assertEquals("EC5344", result.pensum.nodes.first().displayCode)
	}

	@Test
	fun getPensum_withEmptySelection_omitsQueryParameters() = runTest {
		var capturedQuery: String? = null
		val client = HttpClient(
			engine = MockEngine { request ->
				capturedQuery = request.url.encodedQuery
				respond(
					content = sampleResponse,
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			expectSuccess = true
			install(ContentNegotiation) { json() }
		}

		KtorPensumApiDataSource(client).getPensum(PensumSelectionParams())

		assertEquals("", capturedQuery)
	}
}

private val sampleResponse = """
{
  "career_name": "Ingenieria de Computacion",
  "selected_pensum_id": "0800-2019-degree_project",
  "inferred": false,
  "available_pensums": [{"year": 2016}, {"year": 2017}, {"year": 2018}, {"year": 2019}],
  "available_modalities": [
    {"id": "degree_project", "name": "Proyecto de Grado", "is_default": true},
    {"id": "long_internship", "name": "Pasantia Larga", "is_default": false},
    {"id": "exclusive_degree_project", "name": "Proyecto de Grado a Dedicacion Exclusiva", "is_default": false}
  ],
  "pensum": {
    "id": "0800-2019-degree_project",
    "year": 2019,
    "modality_id": "degree_project",
    "modality_name": "Proyecto de Grado",
    "total_credits": 170,
    "canvas": {"width": 1200.0, "height": 900.0},
    "terms": [{"id":"T1","label":"T1","x":0.0,"width":200.0}],
    "nodes": [{"id":"ec5344","node_type":"COURSE","display_code":"EC5344","subject_code":"EC5344","name":"Radiacion y Antenas","credits":3,"category":"PROFESSIONAL","term_id":"T1","x":40.0,"y":80.0,"width":160.0,"height":120.0,"fulfillment_rules":[]}],
    "edges": []
  }
}
""".trimIndent()
