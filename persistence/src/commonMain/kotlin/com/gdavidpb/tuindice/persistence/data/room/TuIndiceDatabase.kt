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
		EvaluationEntity::class,
		EvaluationSyncStateEntity::class,
		PendingMutationEntity::class,
		SubjectDetailEntity::class,
		SubjectStatsSegmentEntity::class,
		SubjectStatsGradeBinEntity::class,
		SubjectStatsAttemptBinEntity::class
	],
	version = 22,
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
	abstract val evaluations: EvaluationDao
	abstract val evaluationSyncState: EvaluationSyncStateDao
	abstract val pendingMutations: PendingMutationDao
	abstract val subjectDetails: SubjectDetailDao
	abstract val subjectStatsSegments: SubjectStatsSegmentDao
	abstract val subjectStatsGradeBins: SubjectStatsGradeBinDao
	abstract val subjectStatsAttemptBins: SubjectStatsAttemptBinDao
}

@Suppress("KotlinNoActualForExpect")
expect object TuIndiceDatabaseConstructor : RoomDatabaseConstructor<TuIndiceDatabase> {
	override fun initialize(): TuIndiceDatabase
}
