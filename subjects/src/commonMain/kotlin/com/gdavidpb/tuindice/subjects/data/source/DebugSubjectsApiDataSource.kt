package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.academiccore.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.subjects.data.mapper.toSubjectDetailResult
import com.gdavidpb.tuindice.subjects.data.model.GetSubjectResponse
import com.gdavidpb.tuindice.subjects.data.model.SubjectStatsUnavailableResponse
import com.gdavidpb.tuindice.subjects.data.repository.SubjectCatalogRemoteDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsApiDataRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tuindice.subjects.generated.resources.Res

class DebugSubjectsApiDataSource(
	private val apiDataSource: KtorSubjectsApiDataSource,
	private val json: Json
) : SubjectStatsApiDataRepository, SubjectCatalogRemoteDataRepository {
	private val failedSearchQueries = mutableSetOf<String>()

	@OptIn(ExperimentalResourceApi::class)
	override suspend fun getSubjectDetail(subjectCode: String): SubjectDetailResult {
		val normalizedSubjectCode = DebugSubjectScenarioResolver.normalize(subjectCode)
		val displaySubjectCode = normalizedSubjectCode.takeUnless { code ->
			code.startsWith("DBG-")
		}
		val resolution = DebugSubjectScenarioResolver.resolve(normalizedSubjectCode)
			?: return apiDataSource.getSubjectDetail(subjectCode)

		return when (resolution.scenario) {
			DebugSubjectScenario.EC5745,
			DebugSubjectScenario.MAT2230,
			DebugSubjectScenario.FIS2105,
			DebugSubjectScenario.EL2001,
			DebugSubjectScenario.EP3421,
			DebugSubjectScenario.QUI100,
			-> json.decodeFromString<GetSubjectResponse>(
				Res.readBytes(requireNotNull(resolution.scenario.resourcePath)).decodeToString()
			).let { response ->
				response.copy(
					id = resolution.metadata?.code ?: displaySubjectCode ?: response.id,
					name = resolution.metadata?.name ?: response.name,
					credits = resolution.metadata?.credits ?: response.credits,
					gradingMode = resolution.metadata?.gradingMode ?: response.gradingMode
				)
			}.toSubjectDetailResult()

			DebugSubjectScenario.UNAVAILABLE -> {
				val payload = json.decodeFromString<SubjectStatsUnavailableResponse>(
					Res.readBytes(requireNotNull(resolution.scenario.resourcePath)).decodeToString()
				)
				SubjectDetailResult.Unavailable(
					subjectCode = resolution.metadata?.code ?: displaySubjectCode ?: payload.subjectCode,
					expiresAt = payload.expiresAt
				)
			}

			DebugSubjectScenario.ERROR -> throw IllegalStateException("Debug subjects network error.")
		}
	}

	override suspend fun searchSubjects(
		query: String,
		limit: Int
	): List<SubjectSearchResult> {
		val normalizedQuery = SubjectCatalogSearchNormalizer.normalize(query)
		if (normalizedQuery == TransientSearchFailureQuery && failedSearchQueries.add(normalizedQuery)) {
			throw IllegalStateException("Debug subjects search network error.")
		}

		return DebugSubjectScenarioResolver.search(query = query, limit = limit)
			.map { metadata ->
				SubjectSearchResult(
					subjectCode = metadata.code,
					name = metadata.name,
					credits = metadata.credits,
					gradingMode = metadata.gradingMode
				)
			}
	}

	private companion object {
		const val TransientSearchFailureQuery = "RX"
	}
}
