package com.gdavidpb.tuindice.data.source.room.daos

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Update

interface BaseDao<T> {
	@Insert(onConflict = REPLACE)
	suspend fun add(vararg obj: T): List<Long>

	@Delete
	suspend fun remove(vararg obj: T)

	@Update(onConflict = REPLACE)
	suspend fun update(vararg obj: T)
}