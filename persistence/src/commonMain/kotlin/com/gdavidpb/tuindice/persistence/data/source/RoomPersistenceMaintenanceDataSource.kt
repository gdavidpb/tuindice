package com.gdavidpb.tuindice.persistence.data.source

import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptOverrideDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordSyncStateDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationSyncStateDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectDetailDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectStatsAttemptBinDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectStatsGradeBinDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectStatsSegmentDao
import com.gdavidpb.tuindice.persistence.data.room.daos.UserDao
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner

class RoomPersistenceMaintenanceDataSource(
	private val userDao: UserDao,
	private val academicRecordDao: AcademicRecordDao,
	private val academicRecordSyncStateDao: AcademicRecordSyncStateDao,
	private val academicTermDao: AcademicTermDao,
	private val academicAttemptDao: AcademicAttemptDao,
	private val academicAttemptOverrideDao: AcademicAttemptOverrideDao,
	private val evaluationDao: EvaluationDao,
	private val evaluationSyncStateDao: EvaluationSyncStateDao,
	private val pendingMutationDao: PendingMutationDao,
	private val subjectCatalogCacheDao: SubjectCatalogCacheDao,
	private val subjectDetailDao: SubjectDetailDao,
	private val subjectStatsSegmentDao: SubjectStatsSegmentDao,
	private val subjectStatsGradeBinDao: SubjectStatsGradeBinDao,
	private val subjectStatsAttemptBinDao: SubjectStatsAttemptBinDao,
	private val pensumCacheDao: PensumCacheDao,
	private val pensumSelectionDao: PensumSelectionDao,
	private val transactionRunner: PersistenceTransactionRunner
) : PersistenceMaintenanceRepository {
	override suspend fun clearAll() {
		transactionRunner.immediate {
			pendingMutationDao.deleteAll()
			evaluationSyncStateDao.deleteAll()
			evaluationDao.deleteAll()
			academicAttemptOverrideDao.deleteAll()
			academicAttemptDao.deleteAll()
			academicTermDao.deleteAll()
			academicRecordSyncStateDao.deleteAll()
			academicRecordDao.deleteAll()
			subjectStatsAttemptBinDao.deleteAll()
			subjectStatsGradeBinDao.deleteAll()
			subjectStatsSegmentDao.deleteAll()
			subjectDetailDao.deleteAll()
			subjectCatalogCacheDao.deleteAll()
			pensumSelectionDao.deleteAll()
			pensumCacheDao.deleteAll()
			userDao.deleteAll()
		}
	}
}
