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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class DebugSubjectsApiDataSourceTest {
	private val sharedJson = Json {
		explicitNulls = true
		ignoreUnknownKeys = true
		prettyPrint = false
	}

	@Test
	fun getSubjectDetail_overridesScenarioMetadataForRecordSubjects() = runTest {
		val client = HttpClient(
			engine = MockEngine {
				error("Debug subject should resolve from local resources before hitting the API")
			}
		) {
			expectSuccess = true
			install(ContentNegotiation) {
				json()
			}
		}

		val result = DebugSubjectsApiDataSource(
			apiDataSource = KtorSubjectsApiDataSource(client, sharedJson),
			json = sharedJson
		).getSubjectDetail("EC5344")

		val ready = assertIs<SubjectDetailResult.Ready>(result)
		assertEquals("EC5344", ready.detail.id)
		assertEquals("RADIACION Y ANTENAS", ready.detail.name)
		assertEquals(3, ready.detail.credits)
	}

	@Test
	fun getSubjectDetail_overridesScenarioMetadataForCurrentRecordSubjects() = runTest {
		val client = HttpClient(
			engine = MockEngine {
				error("Debug subject should resolve from local resources before hitting the API")
			}
		) {
			expectSuccess = true
			install(ContentNegotiation) {
				json()
			}
		}

		val result = DebugSubjectsApiDataSource(
			apiDataSource = KtorSubjectsApiDataSource(client, sharedJson),
			json = sharedJson
		).getSubjectDetail("CI4325")

		val ready = assertIs<SubjectDetailResult.Ready>(result)
		assertEquals("CI4325", ready.detail.id)
		assertEquals("INTERFACES CON EL USUARIO", ready.detail.name)
		assertEquals(5, ready.detail.credits)
	}

	@Test
	fun getSubjectDetail_fallsBackToApiDataSourceForUnknownCodes() = runTest {
		val client = HttpClient(
			engine = MockEngine { _ ->
				respond(
					content = """
						{
						  "id": "USB0001",
						  "name": "Materia externa",
						  "credits": 2,
						  "grading_mode": "numeric",
						  "generated_at": 1710000000000,
						  "expires_at": 1712592000000,
						  "global_segment": {
						    "sample_students": 10,
						    "closed_attempts": 12,
						    "numeric_latest_students": 10,
						    "latest_approved_count": 7,
						    "latest_failed_count": 2,
						    "latest_retired_count": 1,
						    "latest_unreported_count": 0,
						    "average_grade": 4.0,
						    "median_grade": 4.0,
						    "stddev_grade": 0.5,
						    "first_attempt_pass_rate": 0.7,
						    "approval_rate": 0.8,
						    "latest_failure_rate": 0.2,
						    "latest_withdrawal_rate": 0.1,
						    "retake_rate": 0.1,
						    "avg_attempts_to_pass": 1.1,
						    "median_attempts_to_pass": 1.0,
						    "difficulty_score": 23,
						    "difficulty_band": "low",
						    "first_closed_term_start_at": 1672444800000,
						    "last_closed_term_start_at": 1704067200000,
						    "latest_grade_bins": [{"grade": 4, "count": 6}, {"grade": 5, "count": 4}],
						    "all_grade_bins": [{"grade": 4, "count": 7}, {"grade": 5, "count": 5}],
						    "attempts_to_pass_bins": [{"bucket": "1", "count": 7}, {"bucket": "2", "count": 1}]
						  }
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

		val result = DebugSubjectsApiDataSource(
			apiDataSource = KtorSubjectsApiDataSource(client, sharedJson),
			json = sharedJson
		).getSubjectDetail("USB0001")

		val ready = assertIs<SubjectDetailResult.Ready>(result)
		assertEquals("USB0001", ready.detail.id)
		assertEquals("Materia externa", ready.detail.name)
		assertEquals(2, ready.detail.credits)
	}

	@Test
	fun searchSubjects_failsOnceForDebugRetryQuery() = runTest {
		val client = HttpClient(
			engine = MockEngine {
				error("Debug subject search should resolve from local metadata")
			}
		) {
			expectSuccess = true
			install(ContentNegotiation) {
				json()
			}
		}
		val dataSource = DebugSubjectsApiDataSource(
			apiDataSource = KtorSubjectsApiDataSource(client, sharedJson),
			json = sharedJson
		)

		assertFailsWith<IllegalStateException> {
			dataSource.searchSubjects(query = "rx", limit = 20)
		}

		val results = dataSource.searchSubjects(query = "rx", limit = 20)

		assertEquals("RX", results.single().subjectCode)
	}
}
