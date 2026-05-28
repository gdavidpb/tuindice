package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.PensumSelectionTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PensumSelectionDao : UpsertDao<PensumSelectionEntity>() {
	@Query(
		"SELECT * FROM ${PensumSelectionTable.TABLE_NAME} " +
			"WHERE ${PensumSelectionTable.ID} = :id " +
			"LIMIT 1"
	)
	abstract fun observeSelection(id: String = PensumSelectionTable.DEFAULT_ID): Flow<PensumSelectionEntity?>

	@Query(
		"SELECT * FROM ${PensumSelectionTable.TABLE_NAME} " +
			"WHERE ${PensumSelectionTable.ID} = :id " +
			"LIMIT 1"
	)
	abstract suspend fun getSelection(id: String = PensumSelectionTable.DEFAULT_ID): PensumSelectionEntity?

	@Query("DELETE FROM ${PensumSelectionTable.TABLE_NAME}")
	abstract suspend fun deleteAll(): Int
}
