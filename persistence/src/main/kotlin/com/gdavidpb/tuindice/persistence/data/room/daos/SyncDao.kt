package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.SyncEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.SyncTable

@Dao
abstract class SyncDao : UpsertDao<SyncEntity>() {
	@Query(
		"SELECT * FROM ${SyncTable.TABLE_NAME} " +
				"ORDER BY ${SyncTable.TIMESTAMP} ASC"
	)
	abstract suspend fun getSyncsQueue(): List<SyncEntity>
}