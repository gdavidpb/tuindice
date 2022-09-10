package com.gdavidpb.tuindice.data.source.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gdavidpb.tuindice.data.source.room.daos.QuarterDao
import com.gdavidpb.tuindice.data.source.room.entities.QuarterEntity
import com.gdavidpb.tuindice.data.source.room.utils.DatabaseConverters
import com.gdavidpb.tuindice.data.source.room.utils.DatabaseModel

@Database(
	entities = [QuarterEntity::class],
	version = DatabaseModel.VERSION,
	exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class TuIndiceDatabase : RoomDatabase() {
	abstract val quarters: QuarterDao
}