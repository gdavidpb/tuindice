package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class KtorSubjectsApiDataSourceTest {
	private val sharedJson = Json {
		explicitNulls = true
		ignoreUnknownKeys = true
		prettyPrint = false
	}

	@Test
	fun getSubjectDetail_parsesReadyResponseAndUsesExpectedPath() = runTest {
		var capturedPath: String? = null
		val client = HttpClient(
			engine = MockEngine { request ->
				capturedPath = request.url.encodedPath
				respond(
					content = """
						{
						  "id": "MAT101",
						  "name": "Calculo I",
						  "credits": 5,
						  "grading_mode": "numeric",
						  "generated_at": 1710000000000,
						  "expires_at": 1712592000000,
						  "career_segment": {
						    "sample_students": 18,
						    "closed_attempts": 24,
						    "numeric_latest_students": 18,
						    "latest_approved_count": 12,
						    "latest_failed_count": 4,
						    "latest_retired_count": 1,
						    "latest_unreported_count": 1,
						    "average_grade": 3.7,
						    "median_grade": 4.0,
						    "stddev_grade": 0.8,
						    "first_attempt_pass_rate": 0.55,
						    "eventual_pass_rate": 0.72,
						    "retake_rate": 0.33,
						    "avg_attempts_to_pass": 1.4,
						    "median_attempts_to_pass": 1.0,
						    "first_closed_term_start_at": 1672444800000,
						    "last_closed_term_start_at": 1704067200000,
						    "latest_grade_bins": [{"grade": 1, "count": 1}, {"grade": 5, "count": 3}],
						    "all_grade_bins": [{"grade": 1, "count": 2}, {"grade": 5, "count": 4}],
						    "attempts_to_pass_bins": [{"bucket": "1", "count": 8}, {"bucket": "3_plus", "count": 2}]
						  },
						  "global_segment": null
						}
					""".trimIndent(),
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			expectSuccess = true
			install(ContentNegotiation) {
				json()
			}
		}

		val result = KtorSubjectsApiDataSource(client, sharedJson).getSubjectDetail("MAT101")

		val ready = assertIs<SubjectDetailResult.Ready>(result)
		assertEquals("/subjects/v1/MAT101", capturedPath)
		assertEquals("MAT101", ready.detail.id)
		assertEquals("Calculo I", ready.detail.name)
		assertEquals(18, ready.detail.careerSegment?.sampleStudents)
		assertEquals(null, ready.detail.globalSegment)
	}

	@Test
	fun getSubjectDetail_mapsNotFoundResponseToUnavailableResult() = runTest {
		val client = HttpClient(
			engine = MockEngine {
				respond(
					content = """
						{
						  "error_code": "subject_stats_unavailable",
						  "subject_code": "MAT404",
						  "expires_at": 1712592000000
						}
					""".trimIndent(),
					status = HttpStatusCode.NotFound,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			expectSuccess = true
			install(ContentNegotiation) {
				json()
			}
		}

		val result = KtorSubjectsApiDataSource(client, sharedJson).getSubjectDetail("MAT404")

		val unavailable = assertIs<SubjectDetailResult.Unavailable>(result)
		assertEquals("MAT404", unavailable.subjectCode)
		assertEquals(1712592000000, unavailable.expiresAt)
	}

	@Test
	fun getSubjectDetail_mapsPlainTextNotFoundResponseToUnavailableResult() = runTest {
		val client = HttpClient(
			engine = MockEngine {
				respond(
					content = "Subject not found",
					status = HttpStatusCode.NotFound,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
				)
			}
		) {
			expectSuccess = true
			install(ContentNegotiation) {
				json()
			}
		}

		val result = KtorSubjectsApiDataSource(client, sharedJson).getSubjectDetail("MAT404")

		val unavailable = assertIs<SubjectDetailResult.Unavailable>(result)
		assertEquals("MAT404", unavailable.subjectCode)
		assertTrue(unavailable.expiresAt > 0)
	}
}
