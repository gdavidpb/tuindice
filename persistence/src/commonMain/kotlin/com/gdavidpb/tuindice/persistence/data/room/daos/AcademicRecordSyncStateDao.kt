package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordSyncStateEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicRecordSyncStateTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicRecordSyncStateDao : UpsertDao<AcademicRecordSyncStateEntity>() {
	@Query(
		"SELECT * FROM ${AcademicRecordSyncStateTable.TABLE_NAME} " +
			"WHERE ${AcademicRecordSyncStateTable.KEY} = :key " +
			"LIMIT 1"
	)
	abstract fun observeSyncState(key: String = AcademicRecordSyncStateTable.DEFAULT_KEY): Flow<AcademicRecordSyncStateEntity?>

	@Query(
		"SELECT * FROM ${AcademicRecordSyncStateTable.TABLE_NAME} " +
			"WHERE ${AcademicRecordSyncStateTable.KEY} = :key " +
			"LIMIT 1"
	)
	abstract suspend fun getSyncState(key: String = AcademicRecordSyncStateTable.DEFAULT_KEY): AcademicRecordSyncStateEntity?

	@Query("DELETE FROM ${AcademicRecordSyncStateTable.TABLE_NAME}")
	abstract suspend fun deleteAll()
}
