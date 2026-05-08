package com.gdavidpb.tuindice.subjects.domain.repository

import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import kotlinx.coroutines.flow.Flow

interface SubjectCatalogRepository {
	fun observeSearchResults(
		query: String,
		limit: Int
	): Flow<List<SubjectSearchResult>>

	suspend fun refreshSearchResults(
		query: String,
		limit: Int
	)
}
