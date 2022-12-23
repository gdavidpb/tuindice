package com.gdavidpb.tuindice.data.source.room.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Update

interface BaseDao<T> {
	@Insert(onConflict = REPLACE)
	suspend fun insert(vararg obj: T)

	@Update(onConflict = REPLACE)
	suspend fun update(vararg obj: T)
}