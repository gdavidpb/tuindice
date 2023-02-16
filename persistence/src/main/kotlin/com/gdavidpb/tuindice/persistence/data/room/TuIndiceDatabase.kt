package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gdavidpb.tuindice.persistence.data.room.converter.DatabaseConverters
import com.gdavidpb.tuindice.persistence.data.room.daos.*
import com.gdavidpb.tuindice.persistence.data.room.entity.*
import com.gdavidpb.tuindice.persistence.data.room.schema.DatabaseModel

@Database(
	entities = [
		AccountEntity::class,
		QuarterEntity::class,
		SubjectEntity::class,
		EvaluationEntity::class,
		SyncEntity::class
	],
	version = DatabaseModel.VERSION,
	exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class TuIndiceDatabase : RoomDatabase() {
	abstract val accounts: AccountDao
	abstract val quarters: QuarterDao
	abstract val subjects: SubjectDao
	abstract val evaluations: EvaluationDao
	abstract val syncs: SyncDao
}