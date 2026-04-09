package com.gdavidpb.tuindice.persistence.data.source

import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptOverrideDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationSyncStateDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.UserDao
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner

class RoomPersistenceMaintenanceDataSource(
	private val userDao: UserDao,
	private val academicRecordDao: AcademicRecordDao,
	private val academicTermDao: AcademicTermDao,
	private val academicAttemptDao: AcademicAttemptDao,
	private val academicAttemptOverrideDao: AcademicAttemptOverrideDao,
	private val evaluationDao: EvaluationDao,
	private val evaluationSyncStateDao: EvaluationSyncStateDao,
	private val pendingMutationDao: PendingMutationDao,
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
			academicRecordDao.deleteAll()
			userDao.deleteAll()
		}
	}
}
