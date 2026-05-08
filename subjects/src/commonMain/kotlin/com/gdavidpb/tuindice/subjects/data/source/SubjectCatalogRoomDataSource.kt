package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao
import com.gdavidpb.tuindice.persistence.data.room.mapper.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.subjects.data.mapper.toSubjectCatalogCacheEntity
import com.gdavidpb.tuindice.subjects.data.mapper.toSubjectSearchResult
import com.gdavidpb.tuindice.subjects.data.repository.SubjectCatalogLocalDataRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class SubjectCatalogRoomDataSource(
	private val subjectCatalogCacheDao: SubjectCatalogCacheDao
) : SubjectCatalogLocalDataRepository {
	override fun observeSearchResults(
		query: String,
		limit: Int
	): Flow<List<SubjectSearchResult>> {
		val normalizedQuery = SubjectCatalogSearchNormalizer.normalize(query)
		if (normalizedQuery.length < 2) return flowOf(emptyList())

		return subjectCatalogCacheDao.observeSearch(
			normalizedQuery = normalizedQuery,
			limit = limit.coerceIn(1, 50)
		).map { entities ->
			entities.map { entity -> entity.toSubjectSearchResult() }
		}
	}

	override suspend fun saveSubjects(subjects: List<SubjectSearchResult>) {
		if (subjects.isEmpty()) return
		val now = currentTimeMillis()
		subjectCatalogCacheDao.upsertEntities(
			subjects
				.distinctBy { subject -> subject.subjectCode }
				.map { subject -> subject.toSubjectCatalogCacheEntity(updatedAt = now) }
		)
	}
}
