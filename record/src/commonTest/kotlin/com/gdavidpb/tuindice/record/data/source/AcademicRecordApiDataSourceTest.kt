package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.utils.extension.isConflict
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.record.data.model.AcademicRecordConflictException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.ResponseException
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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AcademicRecordApiDataSourceTest {
	@Test
	fun upsertAttemptOverride_staleConflict_carriesTheReasonAndTheRevisionToRetryWith() = runTest {
		val dataSource = dataSourceAnswering(
			status = HttpStatusCode.Conflict,
			body = """{"message":"stale","reason":"STALE_PRECONDITION","current_revision":9}"""
		)

		val failure = runCatching { dataSource.upsertAttempt() }.exceptionOrNull()

		val conflict = assertIs<AcademicRecordConflictException>(failure)
		assertEquals("STALE_PRECONDITION", conflict.reason)
		assertEquals(9L, conflict.currentRevision)
		// Still the response exception every status check looks at.
		assertTrue(conflict.isConflict())
	}

	@Test
	fun deleteAttemptOverride_concurrentWrite_hasNoRevision() = runTest {
		val dataSource = dataSourceAnswering(
			status = HttpStatusCode.Conflict,
			body = """{"message":"busy","reason":"CONCURRENT_WRITE"}"""
		)

		val failure = runCatching {
			dataSource.deleteAttemptOverride(attemptId = "attempt-1", mutationId = "m-1", expectedRevision = 3L)
		}.exceptionOrNull()

		val conflict = assertIs<AcademicRecordConflictException>(failure)
		assertEquals("CONCURRENT_WRITE", conflict.reason)
		assertNull(conflict.currentRevision)
	}

	@Test
	fun conflictWithAnUnreadableBody_isStillAConflict_withNothingToRebaseOn() = runTest {
		val dataSource = dataSourceAnswering(status = HttpStatusCode.Conflict, body = "")

		val failure = runCatching { dataSource.upsertAttempt() }.exceptionOrNull()

		val conflict = assertIs<AcademicRecordConflictException>(failure)
		assertNull(conflict.reason)
		assertNull(conflict.currentRevision)
	}

	@Test
	fun otherStatuses_reachTheCallerUntouched() = runTest {
		val dataSource = dataSourceAnswering(status = HttpStatusCode.NotFound, body = "")

		val failure = runCatching { dataSource.upsertAttempt() }.exceptionOrNull()

		assertIs<ResponseException>(failure)
		assertTrue(failure !is AcademicRecordConflictException)
		assertTrue(failure.isNotFound())
	}

	private suspend fun AcademicRecordApiDataSource.upsertAttempt() = upsertAttemptOverride(
		attemptId = "attempt-1",
		score = AttemptScore.numeric(4),
		outcome = null,
		mutationId = "m-1",
		expectedRevision = 3L
	)

	private fun dataSourceAnswering(status: HttpStatusCode, body: String) = AcademicRecordApiDataSource(
		ktorClient = HttpClient(
			MockEngine {
				respond(
					content = body,
					status = status,
					headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
				)
			}
		) {
			expectSuccess = true

			install(DefaultRequest) {
				url("https://api.tuindice.test/")
				headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
			}

			install(ContentNegotiation) {
				json(Json { ignoreUnknownKeys = true })
			}
		}
	)
}
