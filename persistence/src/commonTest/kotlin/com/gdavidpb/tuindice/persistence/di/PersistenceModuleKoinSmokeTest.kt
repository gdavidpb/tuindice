package com.gdavidpb.tuindice.persistence.di

import androidx.room.InvalidationTracker
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
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
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.dsl.module

class PersistenceModuleKoinSmokeTest {
	@Test
	fun resolvesPublicPersistenceContracts() = withKoinSmokeTest(
		persistenceModule,
		module {
			single<TuIndiceDatabase> { FakeTuIndiceDatabase() }
		}
	) {
		assertResolves(
			UserDao::class,
			AcademicRecordDao::class,
			AcademicTermDao::class,
			AcademicAttemptDao::class,
			AcademicAttemptOverrideDao::class,
			AcademicRecordSyncStateDao::class,
			EvaluationDao::class,
			EvaluationSyncStateDao::class,
			PendingMutationDao::class,
			SubjectCatalogCacheDao::class,
			SubjectDetailDao::class,
			SubjectStatsSegmentDao::class,
			SubjectStatsGradeBinDao::class,
			SubjectStatsAttemptBinDao::class,
			PensumCacheDao::class,
			PensumSelectionDao::class,
			SyntheticTermLoadPreviewCacheDao::class,
			PersistenceTransactionRunner::class,
			PersistenceMaintenanceRepository::class
		)
	}
}

private class FakeTuIndiceDatabase : TuIndiceDatabase() {
	override val users: UserDao = object : UserDao() {
		override fun getUserFlow(): Flow<UserEntity?> = emptyFlow()

		override suspend fun updateProfilePicture(url: String) = Unit

		override suspend fun deleteAll() = Unit

		override suspend fun upsertEntity(entity: UserEntity) = Unit

		override suspend fun upsertEntities(entities: List<UserEntity>) = Unit
	}

	override val academicRecords: AcademicRecordDao = object : AcademicRecordDao() {
		override fun observeRecordFlow(): Flow<AcademicRecordEntity?> = emptyFlow()

		override suspend fun getRecord(): AcademicRecordEntity? = null

		override suspend fun deleteAll() = Unit

		override suspend fun upsertEntity(entity: AcademicRecordEntity) = Unit

		override suspend fun upsertEntities(entities: List<AcademicRecordEntity>) = Unit
	}

	override val academicTerms: AcademicTermDao = object : AcademicTermDao() {
		override fun observeTermsFlow(): Flow<List<AcademicTermEntity>> = emptyFlow()

		override suspend fun getTerms(): List<AcademicTermEntity> = emptyList()

		override suspend fun deleteAll() = Unit

		override suspend fun upsertEntity(entity: AcademicTermEntity) = Unit

		override suspend fun upsertEntities(entities: List<AcademicTermEntity>) = Unit
	}

	override val academicAttempts: AcademicAttemptDao = object : AcademicAttemptDao() {
		override fun observeAttemptsFlow(): Flow<List<AcademicAttemptEntity>> = emptyFlow()

		override suspend fun getAttempts(): List<AcademicAttemptEntity> = emptyList()

		override suspend fun deleteAll() = Unit

		override suspend fun upsertEntity(entity: AcademicAttemptEntity) = Unit

		override suspend fun upsertEntities(entities: List<AcademicAttemptEntity>) = Unit
	}

	override val academicAttemptOverrides: AcademicAttemptOverrideDao = object : AcademicAttemptOverrideDao() {
		override fun observeOverridesFlow(): Flow<List<AcademicAttemptOverrideEntity>> = emptyFlow()

		override suspend fun getOverrides(): List<AcademicAttemptOverrideEntity> = emptyList()

		override suspend fun deleteByAttemptId(attemptId: String) = Unit

		override suspend fun deleteAll() = Unit

		override suspend fun upsertEntity(entity: AcademicAttemptOverrideEntity) = Unit

		override suspend fun upsertEntities(entities: List<AcademicAttemptOverrideEntity>) = Unit
	}

	override val academicRecordSyncState: AcademicRecordSyncStateDao = object : AcademicRecordSyncStateDao() {
		override fun observeSyncState(key: String): Flow<AcademicRecordSyncStateEntity?> = emptyFlow()

		override suspend fun getSyncState(key: String): AcademicRecordSyncStateEntity? = null

		override suspend fun deleteAll() = Unit

		override suspend fun upsertEntity(entity: AcademicRecordSyncStateEntity) = Unit

		override suspend fun upsertEntities(entities: List<AcademicRecordSyncStateEntity>) = Unit
	}

	override val evaluations: EvaluationDao = object : EvaluationDao() {
		override fun observeEvaluationsFlow(): Flow<List<EvaluationEntity>> = emptyFlow()

		override suspend fun getEvaluation(eid: String): EvaluationEntity? = null

		override fun getAttemptEvaluations(attemptId: String): Flow<List<EvaluationEntity>> = emptyFlow()

		override suspend fun deleteEvaluation(eid: String): Int = 0

		override suspend fun deleteAll(): Int = 0

		override suspend fun upsertEntity(entity: EvaluationEntity) = Unit

		override suspend fun upsertEntities(entities: List<EvaluationEntity>) = Unit
	}

	override val evaluationSyncState: EvaluationSyncStateDao = object : EvaluationSyncStateDao() {
		override fun observeSyncState(key: String): Flow<EvaluationSyncStateEntity?> = emptyFlow()

		override suspend fun getSyncState(key: String): EvaluationSyncStateEntity? = null

		override suspend fun deleteAll() = Unit

		override suspend fun upsertEntity(entity: EvaluationSyncStateEntity) = Unit

		override suspend fun upsertEntities(entities: List<EvaluationSyncStateEntity>) = Unit
	}

	override val pendingMutations: PendingMutationDao = object : PendingMutationDao() {
		override fun observePendingMutations(
			storeId: String,
			scopeKey: String
		): Flow<List<PendingMutationEntity>> = emptyFlow()

		override suspend fun getPendingMutations(
			storeId: String,
			scopeKey: String
		): List<PendingMutationEntity> = emptyList()

		override suspend fun getMutations(
			storeId: String,
			scopeKey: String
		): List<PendingMutationEntity> = emptyList()

		override suspend fun getPendingMutation(
			storeId: String,
			scopeKey: String,
			mutationId: String
		): PendingMutationEntity? = null

		override suspend fun getPendingMutation(mutationId: String): PendingMutationEntity? = null

		override suspend fun deletePendingMutation(
			storeId: String,
			scopeKey: String,
			mutationId: String
		): Int = 0

		override suspend fun deletePendingMutationsByReplaceKey(
			storeId: String,
			replaceKey: String
		): Int = 0

		override suspend fun retryFailedMutations(
			storeId: String,
			scopeKey: String,
			status: String,
			updatedAt: Long
		): Int = 0

		override suspend fun deleteAll(): Int = 0

		override suspend fun upsertEntity(entity: PendingMutationEntity) = Unit

		override suspend fun upsertEntities(entities: List<PendingMutationEntity>) = Unit
	}

	override val subjectDetails: SubjectDetailDao = object : SubjectDetailDao() {
		override suspend fun getSubjectDetail(subjectCode: String): SubjectDetailEntity? = null

		override suspend fun deleteBySubjectCode(subjectCode: String): Int = 0

		override suspend fun deleteAll(): Int = 0

		override suspend fun upsertEntity(entity: SubjectDetailEntity) = Unit

		override suspend fun upsertEntities(entities: List<SubjectDetailEntity>) = Unit
	}

	override val subjectStatsSegments: SubjectStatsSegmentDao = object : SubjectStatsSegmentDao() {
		override suspend fun getSubjectSegments(subjectCode: String): List<SubjectStatsSegmentEntity> = emptyList()

		override suspend fun deleteBySubjectCode(subjectCode: String): Int = 0

		override suspend fun deleteAll(): Int = 0

		override suspend fun upsertEntity(entity: SubjectStatsSegmentEntity) = Unit

		override suspend fun upsertEntities(entities: List<SubjectStatsSegmentEntity>) = Unit
	}

	override val subjectStatsGradeBins: SubjectStatsGradeBinDao = object : SubjectStatsGradeBinDao() {
		override suspend fun getSubjectGradeBins(subjectCode: String): List<SubjectStatsGradeBinEntity> = emptyList()

		override suspend fun deleteBySubjectCode(subjectCode: String): Int = 0

		override suspend fun deleteAll(): Int = 0

		override suspend fun upsertEntity(entity: SubjectStatsGradeBinEntity) = Unit

		override suspend fun upsertEntities(entities: List<SubjectStatsGradeBinEntity>) = Unit
	}

	override val subjectStatsAttemptBins: SubjectStatsAttemptBinDao = object : SubjectStatsAttemptBinDao() {
		override suspend fun getSubjectAttemptBins(subjectCode: String): List<SubjectStatsAttemptBinEntity> = emptyList()

		override suspend fun deleteBySubjectCode(subjectCode: String): Int = 0

		override suspend fun deleteAll(): Int = 0

		override suspend fun upsertEntity(entity: SubjectStatsAttemptBinEntity) = Unit

		override suspend fun upsertEntities(entities: List<SubjectStatsAttemptBinEntity>) = Unit
	}

	override val subjectCatalogCache: SubjectCatalogCacheDao = object : SubjectCatalogCacheDao() {
		override fun observeSearch(
			normalizedQuery: String,
			limit: Int
		): Flow<List<SubjectCatalogCacheEntity>> = emptyFlow()

		override suspend fun deleteAll(): Int = 0

		override suspend fun upsertEntity(entity: SubjectCatalogCacheEntity) = Unit

		override suspend fun upsertEntities(entities: List<SubjectCatalogCacheEntity>) = Unit
	}

	override val pensumCache: PensumCacheDao = object : PensumCacheDao() {
		override fun observePensum(cacheKey: String): Flow<PensumCacheEntity?> = emptyFlow()

		override suspend fun getPensum(cacheKey: String): PensumCacheEntity? = null

		override suspend fun getPensum(
			year: Int,
			modalityId: String
		): PensumCacheEntity? = null

		override suspend fun deleteAll(): Int = 0

		override suspend fun upsertEntity(entity: PensumCacheEntity) = Unit

		override suspend fun upsertEntities(entities: List<PensumCacheEntity>) = Unit
	}

	override val pensumSelection: PensumSelectionDao = object : PensumSelectionDao() {
		override fun observeSelection(id: String): Flow<PensumSelectionEntity?> = emptyFlow()

		override suspend fun getSelection(id: String): PensumSelectionEntity? = null

		override suspend fun deleteAll(): Int = 0

		override suspend fun upsertEntity(entity: PensumSelectionEntity) = Unit

		override suspend fun upsertEntities(entities: List<PensumSelectionEntity>) = Unit
	}

	override val syntheticTermLoadPreviewCache: SyntheticTermLoadPreviewCacheDao =
		object : SyntheticTermLoadPreviewCacheDao() {
			override suspend fun getByCacheKey(cacheKey: String): SyntheticTermLoadPreviewCacheEntity? = null

			override suspend fun getFresh(
				cacheKey: String,
				now: Long
			): SyntheticTermLoadPreviewCacheEntity? = null

			override suspend fun deleteExpired(now: Long): Int = 0

			override suspend fun deleteAll(): Int = 0

			override suspend fun upsertEntity(entity: SyntheticTermLoadPreviewCacheEntity) = Unit

			override suspend fun upsertEntities(entities: List<SyntheticTermLoadPreviewCacheEntity>) = Unit
		}

	override fun clearAllTables() = Unit

	override fun createInvalidationTracker(): InvalidationTracker {
		return InvalidationTracker(
			database = this,
			shadowTablesMap = mutableMapOf(),
			viewTables = mutableMapOf(),
			"users",
			"academic_record",
			"academic_term",
			"academic_attempt",
			"academic_attempt_override",
			"academic_record_sync_state",
			"evaluations",
			"evaluation_sync_state",
			"pending_mutations",
			"subject_catalog_cache",
			"subject_detail",
			"subject_stats_segment",
			"subject_stats_grade_bin",
			"subject_stats_attempt_bin",
			"pensum_cache",
			"pensum_selection",
			"synthetic_term_load_preview_cache"
		)
	}
}
