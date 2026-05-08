package com.gdavidpb.tuindice.subjects.data.repository

import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import kotlinx.coroutines.flow.Flow

interface SubjectCatalogLocalDataRepository {
	fun observeSearchResults(
		query: String,
		limit: Int
	): Flow<List<SubjectSearchResult>>

	suspend fun saveSubjects(subjects: List<SubjectSearchResult>)
}
