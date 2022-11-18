package com.gdavidpb.tuindice.data.source.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gdavidpb.tuindice.data.source.room.daos.AccountDao
import com.gdavidpb.tuindice.data.source.room.daos.EvaluationDao
import com.gdavidpb.tuindice.data.source.room.daos.QuarterDao
import com.gdavidpb.tuindice.data.source.room.daos.SubjectDao
import com.gdavidpb.tuindice.data.source.room.entities.AccountEntity
import com.gdavidpb.tuindice.data.source.room.entities.EvaluationEntity
import com.gdavidpb.tuindice.data.source.room.entities.QuarterEntity
import com.gdavidpb.tuindice.data.source.room.entities.SubjectEntity
import com.gdavidpb.tuindice.data.source.room.utils.DatabaseConverters
import com.gdavidpb.tuindice.data.source.room.utils.DatabaseModel

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