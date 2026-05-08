package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.subjects.data.repository.SubjectCatalogLocalDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectCatalogRemoteDataRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectCatalogRepository
import kotlinx.coroutines.flow.Flow

class SubjectCatalogDataSource(
	private val localDataRepository: SubjectCatalogLocalDataRepository,
	private val remoteDataRepository: SubjectCatalogRemoteDataRepository
) : SubjectCatalogRepository {
	override fun observeSearchResults(
		query: String,
		limit: Int
	): Flow<List<SubjectSearchResult>> {
		return localDataRepository.observeSearchResults(query = query, limit = limit)
	}

	override suspend fun refreshSearchResults(
		query: String,
		limit: Int
	) {
		localDataRepository.saveSubjects(
			remoteDataRepository.searchSubjects(
				query = query,
				limit = limit
			)
		)
	}
}
