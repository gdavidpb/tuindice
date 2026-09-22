package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.academiccore.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao
import com.gdavidpb.tuindice.subjects.data.mapper.toSubjectCatalogCacheEntity
import com.gdavidpb.tuindice.subjects.data.mapper.toSubjectSearchResult
import com.gdavidpb.tuindice.subjects.data.repository.SubjectCatalogLocalDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectSearchPensumStatusDataRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class SubjectCatalogRoomDataSource(
	private val subjectCatalogCacheDao: SubjectCatalogCacheDao,
	private val pensumStatusDataRepository: SubjectSearchPensumStatusDataRepository
) : SubjectCatalogLocalDataRepository {
	override fun observeSearchResults(
		query: String,
		limit: Int
	): Flow<List<SubjectSearchResult>> {
		val normalizedQuery = SubjectCatalogSearchNormalizer.normalize(query)
		if (normalizedQuery.length < 2) return flowOf(emptyList())

		return combine(
			subjectCatalogCacheDao.observeSearch(
				normalizedQuery = normalizedQuery,
				limit = limit.coerceIn(1, 50)
			),
			pensumStatusDataRepository.observePensumStatusResolver()
		) { entities, pensumStatus ->
			entities.map { entity ->
				entity.toSubjectSearchResult(
					pensumStatus = pensumStatus.statusOf(subjectCode = entity.subjectCode, credits = entity.credits)
				)
			}
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
