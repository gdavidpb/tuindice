package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.CounterEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.CounterTable

@Dao
abstract class CounterDao : UpsertDao<CounterEntity>() {
	@Query(
		"SELECT ${CounterTable.VALUE} " +
				"FROM ${CounterTable.TABLE_NAME} " +
				"WHERE ${CounterTable.ID} = :uid"
	)
	abstract suspend fun getAndIncrement(
		uid: String
	): Long
}