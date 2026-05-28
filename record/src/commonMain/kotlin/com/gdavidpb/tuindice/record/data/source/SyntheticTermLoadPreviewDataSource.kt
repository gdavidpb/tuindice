package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.data.room.daos.SyntheticTermLoadPreviewCacheDao
import com.gdavidpb.tuindice.record.data.mapper.syntheticTermLoadPreviewCacheKey
import com.gdavidpb.tuindice.record.data.mapper.toSyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.data.mapper.toSyntheticTermLoadPreviewCacheEntity
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermLoadPreviewRepository

class SyntheticTermLoadPreviewDataSource(
	private val remoteDataRepository: AcademicRecordRemoteDataRepository,
	private val cacheDao: SyntheticTermLoadPreviewCacheDao
) : SyntheticTermLoadPreviewRepository {
	override suspend fun loadSyntheticTermPreview(
		termKey: String,
		subjectCodes: List<String>
	): SyntheticTermLoadPreview {
		val cacheKey = syntheticTermLoadPreviewCacheKey(
			termKey = termKey,
			subjectCodes = subjectCodes
		)
		val now = currentTimeMillis()
		val freshCached = cacheDao.getFresh(
			cacheKey = cacheKey,
			now = now
		)

		if (freshCached != null) {
			return freshCached.toSyntheticTermLoadPreview()
		}

		val cached = cacheDao.getByCacheKey(cacheKey)
		return runCatching {
			remoteDataRepository.loadSyntheticTermPreview(subjectCodes).also { preview ->
				cacheDao.upsertEntity(
					preview.toSyntheticTermLoadPreviewCacheEntity(
						cacheKey = cacheKey,
						termKey = termKey,
						subjectCodes = subjectCodes,
						updatedAt = currentTimeMillis()
					)
				)
				cacheDao.deleteExpired(now)
			}
		}.getOrElse { throwable ->
			cached?.toSyntheticTermLoadPreview() ?: throw throwable
		}
	}
}
