package com.gdavidpb.tuindice.persistence.data.source.room.utils.extensions

import androidx.room.RoomDatabase
import androidx.room.withTransaction

suspend fun <T, R : RoomDatabase> R.withTransaction(block: suspend R.() -> T): T =
	withTransaction { block(this) }