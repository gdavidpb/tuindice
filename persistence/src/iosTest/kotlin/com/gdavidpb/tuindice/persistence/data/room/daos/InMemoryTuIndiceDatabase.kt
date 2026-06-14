package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import kotlinx.coroutines.Dispatchers

internal fun createInMemoryTuIndiceDatabase(): TuIndiceDatabase =
	Room.inMemoryDatabaseBuilder<TuIndiceDatabase>()
		.setDriver(BundledSQLiteDriver())
		.setQueryCoroutineContext(Dispatchers.Default)
		.build()
