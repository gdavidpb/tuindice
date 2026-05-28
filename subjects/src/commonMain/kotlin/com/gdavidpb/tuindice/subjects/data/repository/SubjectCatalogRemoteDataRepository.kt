package com.gdavidpb.tuindice.subjects.data.repository

import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult

interface SubjectCatalogRemoteDataRepository {
	suspend fun searchSubjects(
		query: String,
		limit: Int
	): List<SubjectSearchResult>
}
