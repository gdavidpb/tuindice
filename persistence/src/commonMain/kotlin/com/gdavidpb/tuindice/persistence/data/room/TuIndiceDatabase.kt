package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.*
import com.gdavidpb.tuindice.persistence.data.room.converter.DatabaseConverters
import com.gdavidpb.tuindice.persistence.data.room.daos.*
import com.gdavidpb.tuindice.persistence.data.room.entity.*

@Database(
	entities = [
		UserEntity::class,
		AcademicRecordEntity::class,
		AcademicTermEntity::class,
		AcademicAttemptEntity::class,
		AcademicAttemptOverrideEntity::class,
		AcademicLocalTermEntity::class,
		AcademicLocalAttemptEntity::class,
		AcademicTermProjectionEntity::class,
		AcademicAttemptProjectionEntity::class,
		AcademicSummaryEntity::class,
		EvaluationEntity::class,
		EvaluationSyncStateEntity::class,
		PendingMutationEntity::class
	],
	version = 15,
	exportSchema = false
)
@ConstructedBy(TuIndiceDatabaseConstructor::class)
@TypeConverters(DatabaseConverters::class)
abstract class TuIndiceDatabase : RoomDatabase() {
	abstract val users: UserDao
	abstract val academicRecords: AcademicRecordDao
	abstract val academicTerms: AcademicTermDao
	abstract val academicAttempts: AcademicAttemptDao
	abstract val academicAttemptOverrides: AcademicAttemptOverrideDao
	abstract val academicLocalTerms: AcademicLocalTermDao
	abstract val academicLocalAttempts: AcademicLocalAttemptDao
	abstract val academicTermProjections: AcademicTermProjectionDao
	abstract val academicAttemptProjections: AcademicAttemptProjectionDao
	abstract val academicSummaries: AcademicSummaryDao
	abstract val evaluations: EvaluationDao
	abstract val evaluationSyncState: EvaluationSyncStateDao
	abstract val pendingMutations: PendingMutationDao
}

@Suppress("KotlinNoActualForExpect")
expect object TuIndiceDatabaseConstructor : RoomDatabaseConstructor<TuIndiceDatabase> {
	override fun initialize(): TuIndiceDatabase
}
