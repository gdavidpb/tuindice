package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gdavidpb.tuindice.persistence.data.room.converter.DatabaseConverters
import com.gdavidpb.tuindice.persistence.data.room.daos.UserDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.QuarterDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectDao
import com.gdavidpb.tuindice.persistence.data.room.entity.UserEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity

@Database(
	entities = [
		UserEntity::class,
		QuarterEntity::class,
		SubjectEntity::class,
		EvaluationEntity::class
	],
	version = 2,
	exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class TuIndiceDatabase : RoomDatabase() {
	abstract val users: UserDao
	abstract val quarters: QuarterDao
	abstract val subjects: SubjectDao
	abstract val evaluations: EvaluationDao
}