package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.subjects.data.repository.SubjectCatalogLocalDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectCatalogRemoteDataRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SubjectCatalogDataSourceTest {
	@Test
	fun observeSearchResults_readsFromLocalCacheOnly() = runTest {
		val cached = subjectSearchResult(subjectCode = "EC5333")
		val local = RecordingSubjectCatalogLocalDataRepository(results = listOf(cached))
		val remote = RecordingSubjectCatalogRemoteDataRepository(results = emptyList())
		val dataSource = SubjectCatalogDataSource(
			localDataRepository = local,
			remoteDataRepository = remote
		)

		val results = dataSource.observeSearchResults(query = "micro", limit = 20).first()

		assertEquals(listOf(cached), results)
		assertEquals(listOf(SubjectCatalogSearchCall(query = "micro", limit = 20)), local.observeCalls)
		assertEquals(emptyList(), remote.searchCalls)
	}

	@Test
	fun refreshSearchResults_savesRemoteResultsToLocalCache() = runTest {
		val remoteResult = subjectSearchResult(subjectCode = "EC5201")
		val local = RecordingSubjectCatalogLocalDataRepository(results = emptyList())
		val remote = RecordingSubjectCatalogRemoteDataRepository(results = listOf(remoteResult))
		val dataSource = SubjectCatalogDataSource(
			localDataRepository = local,
			remoteDataRepository = remote
		)

		dataSource.refreshSearchResults(query = "comunicaciones", limit = 20)

		assertEquals(listOf(SubjectCatalogSearchCall(query = "comunicaciones", limit = 20)), remote.searchCalls)
		assertEquals(listOf(listOf(remoteResult)), local.savedSubjects)
	}
}

private class RecordingSubjectCatalogLocalDataRepository(
	private val results: List<SubjectSearchResult>
) : SubjectCatalogLocalDataRepository {
	val observeCalls = mutableListOf<SubjectCatalogSearchCall>()
	val savedSubjects = mutableListOf<List<SubjectSearchResult>>()

	override fun observeSearchResults(
		query: String,
		limit: Int
	): Flow<List<SubjectSearchResult>> {
		observeCalls += SubjectCatalogSearchCall(query = query, limit = limit)
		return flowOf(results)
	}

	override suspend fun saveSubjects(subjects: List<SubjectSearchResult>) {
		savedSubjects += subjects
	}
}

private class RecordingSubjectCatalogRemoteDataRepository(
	private val results: List<SubjectSearchResult>
) : SubjectCatalogRemoteDataRepository {
	val searchCalls = mutableListOf<SubjectCatalogSearchCall>()

	override suspend fun searchSubjects(
		query: String,
		limit: Int
	): List<SubjectSearchResult> {
		searchCalls += SubjectCatalogSearchCall(query = query, limit = limit)
		return results
	}
}

private data class SubjectCatalogSearchCall(
	val query: String,
	val limit: Int
)

private fun subjectSearchResult(subjectCode: String): SubjectSearchResult {
	return SubjectSearchResult(
		subjectCode = subjectCode,
		name = "SISTEMAS DE COMUNICACIONES",
		credits = 3,
		gradingMode = GradingMode.NUMERIC
	)
}
