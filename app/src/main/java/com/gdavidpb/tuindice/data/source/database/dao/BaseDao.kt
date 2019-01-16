package com.gdavidpb.tuindice.data.source.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Update

interface BaseDao<T> {
    @Insert(onConflict = REPLACE)
    fun add(vararg obj: T): List<Long>

    @Delete
    fun remove(vararg obj: T)

    @Update(onConflict = REPLACE)
    fun update(vararg obj: T)
}