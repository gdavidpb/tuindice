package com.gdavidpb.tuindice.persistence.data.source

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
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RoomPersistenceMaintenanceDataSourceTest {
	@Test
	fun clearAll_deletesEveryPersistedTableInsideTransaction() = runTest {
		val calls = mutableListOf<String>()
		val dataSource = RoomPersistenceMaintenanceDataSource(
			userDao = RecordingUserDao(calls),
			academicRecordDao = RecordingAcademicRecordDao(calls),
			academicRecordSyncStateDao = RecordingAcademicRecordSyncStateDao(calls),
			academicTermDao = RecordingAcademicTermDao(calls),
			academicAttemptDao = RecordingAcademicAttemptDao(calls),
			academicAttemptOverrideDao = RecordingAcademicAttemptOverrideDao(calls),
			evaluationDao = RecordingEvaluationDao(calls),
			evaluationSyncStateDao = RecordingEvaluationSyncStateDao(calls),
			pendingMutationDao = RecordingPendingMutationDao(calls),
			subjectCatalogCacheDao = RecordingSubjectCatalogCacheDao(calls),
			subjectDetailDao = RecordingSubjectDetailDao(calls),
			subjectStatsSegmentDao = RecordingSubjectStatsSegmentDao(calls),
			subjectStatsGradeBinDao = RecordingSubjectStatsGradeBinDao(calls),
			subjectStatsAttemptBinDao = RecordingSubjectStatsAttemptBinDao(calls),
			pensumCacheDao = RecordingPensumCacheDao(calls),
			pensumSelectionDao = RecordingPensumSelectionDao(calls),
			syntheticTermLoadPreviewCacheDao = RecordingSyntheticTermLoadPreviewCacheDao(calls),
			transactionRunner = RecordingTransactionRunner(calls)
		)

		dataSource.clearAll()

		assertEquals(
			listOf(
				"transaction:start",
				"pending_mutations",
				"evaluation_sync_state",
				"evaluations",
				"academic_attempt_override",
				"academic_attempt",
				"academic_term",
				"academic_record_sync_state",
				"academic_record",
				"subject_stats_attempt_bin",
				"subject_stats_grade_bin",
				"subject_stats_segment",
				"subject_detail",
				"subject_catalog_cache",
				"synthetic_term_load_preview_cache",
				"pensum_selection",
				"pensum_cache",
				"users",
				"transaction:end"
			),
			calls
		)
	}
}

private class RecordingPensumCacheDao(
	private val calls: MutableList<String>
) : PensumCacheDao() {
	override fun observePensum(cacheKey: String): Flow<PensumCacheEntity?> = emptyFlow()

	override suspend fun getPensum(cacheKey: String): PensumCacheEntity? = null

	override suspend fun getPensum(
		year: Int,
		modalityId: String
	): PensumCacheEntity? = null

	override suspend fun deleteAll(): Int {
		calls += "pensum_cache"
		return 0
	}

	override suspend fun upsertEntity(entity: PensumCacheEntity) = Unit

	override suspend fun upsertEntities(entities: List<PensumCacheEntity>) = Unit
}

private class RecordingPensumSelectionDao(
	private val calls: MutableList<String>
) : PensumSelectionDao() {
	override fun observeSelection(id: String): Flow<PensumSelectionEntity?> = emptyFlow()

	override suspend fun getSelection(id: String): PensumSelectionEntity? = null

	override suspend fun deleteAll(): Int {
		calls += "pensum_selection"
		return 0
	}

	override suspend fun upsertEntity(entity: PensumSelectionEntity) = Unit

	override suspend fun upsertEntities(entities: List<PensumSelectionEntity>) = Unit
}

private class RecordingSyntheticTermLoadPreviewCacheDao(
	private val calls: MutableList<String>
) : SyntheticTermLoadPreviewCacheDao() {
	override suspend fun getByCacheKey(cacheKey: String): SyntheticTermLoadPreviewCacheEntity? = null

	override suspend fun getFresh(
		cacheKey: String,
		now: Long
	): SyntheticTermLoadPreviewCacheEntity? = null

	override suspend fun deleteExpired(now: Long): Int = 0

	override suspend fun deleteAll(): Int {
		calls += "synthetic_term_load_preview_cache"
		return 0
	}

	override suspend fun upsertEntity(entity: SyntheticTermLoadPreviewCacheEntity) = Unit

	override suspend fun upsertEntities(entities: List<SyntheticTermLoadPreviewCacheEntity>) = Unit
}

private class RecordingTransactionRunner(
	private val calls: MutableList<String>
) : PersistenceTransactionRunner {
	override suspend fun <R> immediate(block: suspend () -> R): R {
		calls += "transaction:start"
		return block().also {
			calls += "transaction:end"
		}
	}
}

private class RecordingUserDao(
	private val calls: MutableList<String>
) : UserDao() {
	override fun getUserFlow(): Flow<UserEntity?> = emptyFlow()

	override suspend fun updateProfilePicture(url: String) = Unit

	override suspend fun deleteAll() {
		calls += "users"
	}

	override suspend fun upsertEntity(entity: UserEntity) = Unit

	override suspend fun upsertEntities(entities: List<UserEntity>) = Unit
}

private class RecordingAcademicRecordDao(
	private val calls: MutableList<String>
) : AcademicRecordDao() {
	override fun observeRecordFlow(): Flow<AcademicRecordEntity?> = emptyFlow()

	override suspend fun getRecord(): AcademicRecordEntity? = null

	override suspend fun deleteAll() {
		calls += "academic_record"
	}

	override suspend fun upsertEntity(entity: AcademicRecordEntity) = Unit

	override suspend fun upsertEntities(entities: List<AcademicRecordEntity>) = Unit
}

private class RecordingAcademicRecordSyncStateDao(
	private val calls: MutableList<String>
) : AcademicRecordSyncStateDao() {
	override fun observeSyncState(key: String): Flow<AcademicRecordSyncStateEntity?> = emptyFlow()

	override suspend fun getSyncState(key: String): AcademicRecordSyncStateEntity? = null

	override suspend fun deleteAll() {
		calls += "academic_record_sync_state"
	}

	override suspend fun upsertEntity(entity: AcademicRecordSyncStateEntity) = Unit

	override suspend fun upsertEntities(entities: List<AcademicRecordSyncStateEntity>) = Unit
}

private class RecordingAcademicTermDao(
	private val calls: MutableList<String>
) : AcademicTermDao() {
	override fun observeTermsFlow(): Flow<List<AcademicTermEntity>> = emptyFlow()

	override suspend fun getTerms(): List<AcademicTermEntity> = emptyList()

	override suspend fun deleteAll() {
		calls += "academic_term"
	}

	override suspend fun upsertEntity(entity: AcademicTermEntity) = Unit

	override suspend fun upsertEntities(entities: List<AcademicTermEntity>) = Unit
}

private class RecordingAcademicAttemptDao(
	private val calls: MutableList<String>
) : AcademicAttemptDao() {
	override fun observeAttemptsFlow(): Flow<List<AcademicAttemptEntity>> = emptyFlow()

	override suspend fun getAttempts(): List<AcademicAttemptEntity> = emptyList()

	override suspend fun deleteAll() {
		calls += "academic_attempt"
	}

	override suspend fun upsertEntity(entity: AcademicAttemptEntity) = Unit

	override suspend fun upsertEntities(entities: List<AcademicAttemptEntity>) = Unit
}

private class RecordingAcademicAttemptOverrideDao(
	private val calls: MutableList<String>
) : AcademicAttemptOverrideDao() {
	override fun observeOverridesFlow(): Flow<List<AcademicAttemptOverrideEntity>> = emptyFlow()

	override suspend fun getOverrides(): List<AcademicAttemptOverrideEntity> = emptyList()

	override suspend fun deleteByAttemptId(attemptId: String) = Unit

	override suspend fun deleteAll() {
		calls += "academic_attempt_override"
	}

	override suspend fun upsertEntity(entity: AcademicAttemptOverrideEntity) = Unit

	override suspend fun upsertEntities(entities: List<AcademicAttemptOverrideEntity>) = Unit
}

private class RecordingEvaluationDao(
	private val calls: MutableList<String>
) : EvaluationDao() {
	override fun observeEvaluationsFlow(): Flow<List<EvaluationEntity>> = emptyFlow()

	override suspend fun getEvaluation(eid: String): EvaluationEntity? = null

	override fun getAttemptEvaluations(attemptId: String): Flow<List<EvaluationEntity>> = emptyFlow()

	override suspend fun deleteEvaluation(eid: String): Int = 0

	override suspend fun deleteAll(): Int {
		calls += "evaluations"
		return 0
	}

	override suspend fun upsertEntity(entity: EvaluationEntity) = Unit

	override suspend fun upsertEntities(entities: List<EvaluationEntity>) = Unit
}

private class RecordingEvaluationSyncStateDao(
	private val calls: MutableList<String>
) : EvaluationSyncStateDao() {
	override fun observeSyncState(key: String): Flow<EvaluationSyncStateEntity?> = emptyFlow()

	override suspend fun getSyncState(key: String): EvaluationSyncStateEntity? = null

	override suspend fun deleteAll() {
		calls += "evaluation_sync_state"
	}

	override suspend fun upsertEntity(entity: EvaluationSyncStateEntity) = Unit

	override suspend fun upsertEntities(entities: List<EvaluationSyncStateEntity>) = Unit
}

private class RecordingPendingMutationDao(
	private val calls: MutableList<String>
) : PendingMutationDao() {
	override fun observePendingMutations(
		storeId: String,
		scopeKey: String
	): Flow<List<PendingMutationEntity>> = emptyFlow()

	override suspend fun getPendingMutations(
		storeId: String,
		scopeKey: String
	): List<PendingMutationEntity> = emptyList()

	override fun observeMutations(
		storeId: String,
		scopeKey: String
	): Flow<List<PendingMutationEntity>> = emptyFlow()

	override suspend fun requeueFailedMutations(
		storeId: String,
		scopeKey: String,
		retryableBefore: Long,
		status: String,
		updatedAt: Long
	): Int = 0

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

	override suspend fun deleteAll(): Int {
		calls += "pending_mutations"
		return 0
	}

	override suspend fun upsertEntity(entity: PendingMutationEntity) = Unit

	override suspend fun upsertEntities(entities: List<PendingMutationEntity>) = Unit
}

private class RecordingSubjectDetailDao(
	private val calls: MutableList<String>
) : SubjectDetailDao() {
	override suspend fun getSubjectDetail(subjectCode: String): SubjectDetailEntity? = null

	override suspend fun deleteBySubjectCode(subjectCode: String): Int = 0

	override suspend fun deleteAll(): Int {
		calls += "subject_detail"
		return 0
	}

	override suspend fun upsertEntity(entity: SubjectDetailEntity) = Unit

	override suspend fun upsertEntities(entities: List<SubjectDetailEntity>) = Unit
}

private class RecordingSubjectCatalogCacheDao(
	private val calls: MutableList<String>
) : SubjectCatalogCacheDao() {
	override fun observeSearch(
		normalizedQuery: String,
		limit: Int
	): Flow<List<SubjectCatalogCacheEntity>> = emptyFlow()

	override suspend fun deleteAll(): Int {
		calls += "subject_catalog_cache"
		return 0
	}

	override suspend fun upsertEntity(entity: SubjectCatalogCacheEntity) = Unit

	override suspend fun upsertEntities(entities: List<SubjectCatalogCacheEntity>) = Unit
}

private class RecordingSubjectStatsSegmentDao(
	private val calls: MutableList<String>
) : SubjectStatsSegmentDao() {
	override suspend fun getSubjectSegments(subjectCode: String): List<SubjectStatsSegmentEntity> = emptyList()

	override suspend fun deleteBySubjectCode(subjectCode: String): Int = 0

	override suspend fun deleteAll(): Int {
		calls += "subject_stats_segment"
		return 0
	}

	override suspend fun upsertEntity(entity: SubjectStatsSegmentEntity) = Unit

	override suspend fun upsertEntities(entities: List<SubjectStatsSegmentEntity>) = Unit
}

private class RecordingSubjectStatsGradeBinDao(
	private val calls: MutableList<String>
) : SubjectStatsGradeBinDao() {
	override suspend fun getSubjectGradeBins(subjectCode: String): List<SubjectStatsGradeBinEntity> = emptyList()

	override suspend fun deleteBySubjectCode(subjectCode: String): Int = 0

	override suspend fun deleteAll(): Int {
		calls += "subject_stats_grade_bin"
		return 0
	}

	override suspend fun upsertEntity(entity: SubjectStatsGradeBinEntity) = Unit

	override suspend fun upsertEntities(entities: List<SubjectStatsGradeBinEntity>) = Unit
}

private class RecordingSubjectStatsAttemptBinDao(
	private val calls: MutableList<String>
) : SubjectStatsAttemptBinDao() {
	override suspend fun getSubjectAttemptBins(subjectCode: String): List<SubjectStatsAttemptBinEntity> = emptyList()

	override suspend fun deleteBySubjectCode(subjectCode: String): Int = 0

	override suspend fun deleteAll(): Int {
		calls += "subject_stats_attempt_bin"
		return 0
	}

	override suspend fun upsertEntity(entity: SubjectStatsAttemptBinEntity) = Unit

	override suspend fun upsertEntities(entities: List<SubjectStatsAttemptBinEntity>) = Unit
}
