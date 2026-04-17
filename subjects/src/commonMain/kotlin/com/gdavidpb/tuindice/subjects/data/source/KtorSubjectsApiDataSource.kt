package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.subjects.data.mapper.toSubjectDetailResult
import com.gdavidpb.tuindice.subjects.data.model.GetSubjectStatsResponse
import com.gdavidpb.tuindice.subjects.data.model.SubjectStatsUnavailableResponse
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsApiDataRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import kotlinx.serialization.json.Json

class KtorSubjectsApiDataSource(
	private val ktorClient: HttpClient,
	private val json: Json
) : SubjectStatsApiDataRepository {
	override suspend fun getSubjectDetail(subjectCode: String): SubjectDetailResult {
		return runCatching {
			ktorClient.get("subjects/v1") {
				url { appendPathSegments(subjectCode) }
			}
				.body<GetSubjectStatsResponse>()
				.toSubjectDetailResult()
		}.getOrElse { throwable ->
			val clientException = throwable as? ClientRequestException
			if (clientException?.response?.status == HttpStatusCode.NotFound) {
				clientException.response.toUnavailableResult(subjectCode)
			} else {
				throw throwable
			}
		}
	}

	private suspend fun HttpResponse.toUnavailableResult(subjectCode: String): SubjectDetailResult.Unavailable {
		val payload = runCatching {
			json.decodeFromString<SubjectStatsUnavailableResponse>(bodyAsText())
		}.getOrNull()

		return SubjectDetailResult.Unavailable(
			subjectCode = payload?.subjectCode ?: subjectCode,
			expiresAt = payload?.expiresAt ?: currentTimeMillis()
		)
	}
}
