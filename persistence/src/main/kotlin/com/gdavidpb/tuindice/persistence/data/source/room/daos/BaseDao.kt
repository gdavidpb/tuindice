package com.gdavidpb.tuindice.persistence.data.source.room.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(vararg obj: T)

	@Update(onConflict = OnConflictStrategy.REPLACE)
	suspend fun update(vararg obj: T)
}