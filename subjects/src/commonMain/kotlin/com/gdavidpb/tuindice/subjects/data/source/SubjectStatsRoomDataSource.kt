package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectDetailDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectStatsAttemptBinDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectStatsGradeBinDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectStatsSegmentDao
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import com.gdavidpb.tuindice.subjects.data.mapper.toAttemptBinEntities
import com.gdavidpb.tuindice.subjects.data.mapper.toGradeBinEntities
import com.gdavidpb.tuindice.subjects.data.mapper.toSegmentEntities
import com.gdavidpb.tuindice.subjects.data.mapper.toSubjectCatalogCacheEntity
import com.gdavidpb.tuindice.subjects.data.mapper.toSubjectDetailEntity
import com.gdavidpb.tuindice.subjects.data.mapper.toSubjectDetailResult
import com.gdavidpb.tuindice.subjects.data.mapper.toSubjectSearchResult
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsLocalDataRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SubjectStatsRoomDataSource(
	private val subjectCatalogCacheDao: SubjectCatalogCacheDao,
	private val subjectDetailDao: SubjectDetailDao,
	private val subjectStatsSegmentDao: SubjectStatsSegmentDao,
	private val subjectStatsGradeBinDao: SubjectStatsGradeBinDao,
	private val subjectStatsAttemptBinDao: SubjectStatsAttemptBinDao,
	private val transactionRunner: PersistenceTransactionRunner
) : SubjectStatsLocalDataRepository {
	private val writeMutex = Mutex()
	private val cache = mutableMapOf<String, SubjectDetailResult>()

	override suspend fun getSubjectDetail(subjectCode: String): SubjectDetailResult? {
		cache[subjectCode]?.let { result ->
			return result
		}

		val detail = subjectDetailDao.getSubjectDetail(subjectCode) ?: return null
		val result = detail.toSubjectDetailResult(
			segments = subjectStatsSegmentDao.getSubjectSegments(subjectCode),
			gradeBins = subjectStatsGradeBinDao.getSubjectGradeBins(subjectCode),
			attemptBins = subjectStatsAttemptBinDao.getSubjectAttemptBins(subjectCode)
		) ?: return null

		cache[subjectCode] = result
		return result
	}

	override suspend fun saveSubjectDetail(result: SubjectDetailResult) {
		writeMutex.withLock {
			val subjectCode = when (result) {
				is SubjectDetailResult.Ready -> result.detail.id
				is SubjectDetailResult.Unavailable -> result.subjectCode
			}

			transactionRunner.immediate {
				subjectDetailDao.upsertEntity(result.toSubjectDetailEntity())
				subjectStatsSegmentDao.deleteBySubjectCode(subjectCode)
				subjectStatsGradeBinDao.deleteBySubjectCode(subjectCode)
				subjectStatsAttemptBinDao.deleteBySubjectCode(subjectCode)

				if (result is SubjectDetailResult.Ready) {
					subjectCatalogCacheDao.upsertEntity(
						result.detail.toSubjectSearchResult().toSubjectCatalogCacheEntity(
							updatedAt = currentTimeMillis()
						)
					)
					subjectStatsSegmentDao.upsertEntities(result.detail.toSegmentEntities())
					subjectStatsGradeBinDao.upsertEntities(result.detail.toGradeBinEntities())
					subjectStatsAttemptBinDao.upsertEntities(result.detail.toAttemptBinEntities())
				}
			}

			cache[subjectCode] = result
		}
	}
}
