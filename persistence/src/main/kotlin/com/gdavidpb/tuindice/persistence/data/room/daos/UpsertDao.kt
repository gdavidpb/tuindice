package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery

abstract class UpsertDao<T> {
	@RawQuery
	@Transaction
	abstract suspend fun rawQuery(
		query: SupportSQLiteQuery
	): Int

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	abstract suspend fun insertEntity(
		entity: T
	): Long

	@Update
	abstract suspend fun updateEntity(
		entity: T
	)

	@Transaction
	open suspend fun upsertEntities(
		entities: List<T>
	) {
		entities.forEach { entity ->
			val index = insertEntity(entity)

			if (index == -1L) updateEntity(entity)
		}
	}
}