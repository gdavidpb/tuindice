package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.SyntheticTermLoadPreviewCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.SyntheticTermLoadPreviewCacheTable

@Dao
abstract class SyntheticTermLoadPreviewCacheDao : UpsertDao<SyntheticTermLoadPreviewCacheEntity>() {
	@Query(
		"SELECT * FROM ${SyntheticTermLoadPreviewCacheTable.TABLE_NAME} " +
			"WHERE ${SyntheticTermLoadPreviewCacheTable.CACHE_KEY} = :cacheKey " +
			"LIMIT 1"
	)
	abstract suspend fun getByCacheKey(cacheKey: String): SyntheticTermLoadPreviewCacheEntity?

	@Query(
		"SELECT * FROM ${SyntheticTermLoadPreviewCacheTable.TABLE_NAME} " +
			"WHERE ${SyntheticTermLoadPreviewCacheTable.CACHE_KEY} = :cacheKey " +
			"AND ${SyntheticTermLoadPreviewCacheTable.EXPIRES_AT} > :now " +
			"LIMIT 1"
	)
	abstract suspend fun getFresh(cacheKey: String, now: Long): SyntheticTermLoadPreviewCacheEntity?

	@Query(
		"DELETE FROM ${SyntheticTermLoadPreviewCacheTable.TABLE_NAME} " +
			"WHERE ${SyntheticTermLoadPreviewCacheTable.EXPIRES_AT} <= :now"
	)
	abstract suspend fun deleteExpired(now: Long): Int

	@Query("DELETE FROM ${SyntheticTermLoadPreviewCacheTable.TABLE_NAME}")
	abstract suspend fun deleteAll(): Int
}
