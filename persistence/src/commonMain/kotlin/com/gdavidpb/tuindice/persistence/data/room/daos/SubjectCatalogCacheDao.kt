package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectCatalogCacheTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SubjectCatalogCacheDao : UpsertDao<SubjectCatalogCacheEntity>() {
	@Query(
		"SELECT * FROM ${SubjectCatalogCacheTable.TABLE_NAME} " +
			"WHERE ${SubjectCatalogCacheTable.NORMALIZED_CODE} LIKE '%' || :normalizedQuery || '%' " +
			"OR ${SubjectCatalogCacheTable.NORMALIZED_NAME} LIKE '%' || :normalizedQuery || '%' " +
			"ORDER BY CASE " +
			"WHEN ${SubjectCatalogCacheTable.NORMALIZED_CODE} = :normalizedQuery THEN 0 " +
			"WHEN ${SubjectCatalogCacheTable.NORMALIZED_CODE} LIKE :normalizedQuery || '%' THEN 1 " +
			"WHEN ${SubjectCatalogCacheTable.NORMALIZED_NAME} LIKE :normalizedQuery || '%' THEN 2 " +
			"ELSE 3 END, " +
			"${SubjectCatalogCacheTable.NORMALIZED_CODE}, " +
			"${SubjectCatalogCacheTable.NORMALIZED_NAME} " +
			"LIMIT :limit"
	)
	abstract fun observeSearch(
		normalizedQuery: String,
		limit: Int
	): Flow<List<SubjectCatalogCacheEntity>>

	@Query("DELETE FROM ${SubjectCatalogCacheTable.TABLE_NAME}")
	abstract suspend fun deleteAll(): Int
}
