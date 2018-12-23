package com.gdavidpb.tuindice.data.source.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Single

interface BaseDao<T> {
    @Insert(onConflict = REPLACE)
    fun add(vararg obj: T): Single<List<Long>>

    @Delete
    fun remove(vararg obj: T): Completable

    @Update(onConflict = REPLACE)
    fun update(vararg obj: T): Completable
}