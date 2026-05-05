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
				careerCode = 15,
				year = 2019,
				modalityId = "degree_project"
			)
		)

		assertEquals("/pensums/v1", capturedPath)
		assertEquals("career_code=15&year=2019&modality_id=degree_project", capturedQuery)
		assertEquals("computacion-2019-degree-project", result.selection.pensumId)
		assertEquals("EC5344", result.pensum.nodes.first().displayCode)
	}
}

private val sampleResponse = """
{
  "selection": {
    "pensum_id": "computacion-2019-degree-project",
    "career_code": 15,
    "career_name": "Computacion",
    "year": 2019,
    "modality_id": "degree_project",
    "modality_name": "Proyecto de Grado",
    "inferred": false
  },
  "available_pensums": [{"id":"15-2019","career_code":15,"career_name":"Computacion","year":2019}],
  "available_modalities": [{"id":"degree_project","name":"Proyecto de Grado","is_default":true}],
  "pensum": {
    "id": "computacion-2019-degree-project",
    "career_code": 15,
    "career_name": "Computacion",
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
