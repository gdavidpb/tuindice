package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.*
import com.gdavidpb.tuindice.persistence.data.room.converter.DatabaseConverters
import com.gdavidpb.tuindice.persistence.data.room.daos.*
import com.gdavidpb.tuindice.persistence.data.room.entity.*

@Database(
	entities = [
		UserEntity::class,
		QuarterEntity::class,
		SubjectEntity::class,
		EvaluationEntity::class,
		EvaluationSyncStateEntity::class,
		PendingMutationEntity::class
	],
	version = 10,
	exportSchema = false
)
@ConstructedBy(TuIndiceDatabaseConstructor::class)
@TypeConverters(DatabaseConverters::class)
abstract class TuIndiceDatabase : RoomDatabase() {
	abstract val users: UserDao
	abstract val quarters: QuarterDao
	abstract val subjects: SubjectDao
	abstract val evaluations: EvaluationDao
	abstract val evaluationSyncState: EvaluationSyncStateDao
	abstract val pendingMutations: PendingMutationDao
}

expect object TuIndiceDatabaseConstructor : RoomDatabaseConstructor<TuIndiceDatabase> {
	override fun initialize(): TuIndiceDatabase
}
