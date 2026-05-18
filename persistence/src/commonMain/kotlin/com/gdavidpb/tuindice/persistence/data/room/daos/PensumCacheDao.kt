package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.PensumCacheTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PensumCacheDao : UpsertDao<PensumCacheEntity>() {
	@Query(
		"SELECT * FROM ${PensumCacheTable.TABLE_NAME} " +
			"WHERE ${PensumCacheTable.CACHE_KEY} = :cacheKey " +
			"LIMIT 1"
	)
	abstract fun observePensum(cacheKey: String): Flow<PensumCacheEntity?>

	@Query(
		"SELECT * FROM ${PensumCacheTable.TABLE_NAME} " +
			"WHERE ${PensumCacheTable.CACHE_KEY} = :cacheKey " +
			"LIMIT 1"
	)
	abstract suspend fun getPensum(cacheKey: String): PensumCacheEntity?

	@Query(
		"SELECT * FROM ${PensumCacheTable.TABLE_NAME} " +
			"WHERE ${PensumCacheTable.YEAR} = :year " +
			"AND ${PensumCacheTable.MODALITY_ID} = :modalityId " +
			"LIMIT 1"
	)
	abstract suspend fun getPensum(
		year: Int,
		modalityId: String
	): PensumCacheEntity?

	@Query("DELETE FROM ${PensumCacheTable.TABLE_NAME}")
	abstract suspend fun deleteAll(): Int
}
