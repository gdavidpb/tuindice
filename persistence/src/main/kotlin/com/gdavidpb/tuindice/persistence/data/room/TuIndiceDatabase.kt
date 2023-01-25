package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gdavidpb.tuindice.persistence.data.room.daos.AccountDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.QuarterDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectDao
import com.gdavidpb.tuindice.persistence.data.room.entity.AccountEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.DatabaseModel
import com.gdavidpb.tuindice.persistence.data.room.converter.DatabaseConverters

@Database(
	entities = [
		AccountEntity::class,
		QuarterEntity::class,
		SubjectEntity::class,
		EvaluationEntity::class
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
}