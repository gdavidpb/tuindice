package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.gdavidpb.tuindice.persistence.data.room.converter.DatabaseConverters
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptOverrideDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordSyncStateDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationSyncStateDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectDetailDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectStatsAttemptBinDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectStatsGradeBinDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectStatsSegmentDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SyntheticTermLoadPreviewCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.UserDao
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptOverrideEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordSyncStateEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationSyncStateEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PendingMutationEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectDetailEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsAttemptBinEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsGradeBinEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsSegmentEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SyntheticTermLoadPreviewCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.UserEntity

@Database(
	entities = [
		UserEntity::class,
		AcademicRecordEntity::class,
		AcademicTermEntity::class,
		AcademicAttemptEntity::class,
		AcademicAttemptOverrideEntity::class,
		AcademicRecordSyncStateEntity::class,
		EvaluationEntity::class,
		EvaluationSyncStateEntity::class,
		PendingMutationEntity::class,
		SubjectCatalogCacheEntity::class,
		SubjectDetailEntity::class,
		SubjectStatsSegmentEntity::class,
		SubjectStatsGradeBinEntity::class,
		SubjectStatsAttemptBinEntity::class,
		PensumCacheEntity::class,
		PensumSelectionEntity::class,
		SyntheticTermLoadPreviewCacheEntity::class
	],
	version = 33,
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
	abstract val academicRecordSyncState: AcademicRecordSyncStateDao
	abstract val evaluations: EvaluationDao
	abstract val evaluationSyncState: EvaluationSyncStateDao
	abstract val pendingMutations: PendingMutationDao
	abstract val subjectCatalogCache: SubjectCatalogCacheDao
	abstract val subjectDetails: SubjectDetailDao
	abstract val subjectStatsSegments: SubjectStatsSegmentDao
	abstract val subjectStatsGradeBins: SubjectStatsGradeBinDao
	abstract val subjectStatsAttemptBins: SubjectStatsAttemptBinDao
	abstract val pensumCache: PensumCacheDao
	abstract val pensumSelection: PensumSelectionDao
	abstract val syntheticTermLoadPreviewCache: SyntheticTermLoadPreviewCacheDao
}

@Suppress("KotlinNoActualForExpect")
expect object TuIndiceDatabaseConstructor : RoomDatabaseConstructor<TuIndiceDatabase> {
	override fun initialize(): TuIndiceDatabase
}
