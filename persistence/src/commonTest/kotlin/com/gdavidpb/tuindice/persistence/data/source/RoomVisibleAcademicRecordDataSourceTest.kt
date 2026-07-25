package com.gdavidpb.tuindice.persistence.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.toAcademicPensumSnapshot
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptOverrideDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptOverrideEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PendingMutationEntity
import com.gdavidpb.tuindice.persistence.data.room.mapper.toPendingMutationEntity
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_STORE_ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoomVisibleAcademicRecordDataSourceTest {
	@Test
	fun observeVisibleAcademicRecordFlow_whenSyntheticTermOnlyLivesInOutbox_projectsItForPensumConsumers() =
		runTest {
			val dataSource = createDataSource(
				mutations = listOf(pendingSyntheticTermMutation())
			)

			val snapshot = dataSource
				.observeVisibleAcademicRecordFlow()
				.first()
				.toAcademicPensumSnapshot()

			assertEquals(
				listOf("MAT1203" to TermKind.CURRENT, "MAT2205" to TermKind.SYNTHETIC),
				snapshot.attempts.map { attempt -> attempt.subjectCode to attempt.termKind }
			)
		}

	@Test
	fun observeVisibleAcademicRecordFlow_whenSyntheticTermMutationFailed_keepsProjectingIt() = runTest {
		val dataSource = createDataSource(
			mutations = listOf(
				pendingSyntheticTermMutation(status = PendingMutationStatus.FailedTerminal)
			)
		)

		val snapshot = dataSource
			.observeVisibleAcademicRecordFlow()
			.first()
			.toAcademicPensumSnapshot()

		assertEquals(
			listOf("MAT2205"),
			snapshot.attempts
				.filter { attempt -> attempt.termKind == TermKind.SYNTHETIC }
				.map(AcademicPensumSnapshot.Attempt::subjectCode)
		)
	}

	@Test
	fun observeVisibleAcademicRecordFlow_withoutConfirmedRecord_emitsNothingToProject() = runTest {
		val dataSource = createDataSource(
			record = null,
			terms = emptyList(),
			attempts = emptyList(),
			mutations = listOf(pendingSyntheticTermMutation())
		)

		assertNull(dataSource.observeVisibleAcademicRecordFlow().first())
	}
}

private fun createDataSource(
	record: AcademicRecordEntity? = AcademicRecordEntity(
		id = "record-1",
		revision = 1L,
		updatedAt = 0L
	),
	terms: List<AcademicTermEntity> = listOf(confirmedTermEntity()),
	attempts: List<AcademicAttemptEntity> = listOf(confirmedAttemptEntity()),
	overrides: List<AcademicAttemptOverrideEntity> = emptyList(),
	mutations: List<PendingMutationEntity> = emptyList()
) = RoomVisibleAcademicRecordDataSource(
	academicRecordDao = FakeAcademicRecordDao(record),
	academicTermDao = FakeAcademicTermDao(terms),
	academicAttemptDao = FakeAcademicAttemptDao(attempts),
	academicAttemptOverrideDao = FakeAcademicAttemptOverrideDao(overrides),
	pendingMutationDao = FakePendingMutationDao(mutations)
)

private fun confirmedTermEntity() = AcademicTermEntity(
	id = "term-confirmed",
	periodYear = 2026,
	periodCode = AcademicTermPeriod.JAN_MAR.name,
	termKey = "2026-JAN_MAR",
	termOrder = 20261,
	periodLabel = "Enero - Marzo 2026",
	kind = TermKind.CURRENT.name
)

private fun confirmedAttemptEntity() = AcademicAttemptEntity(
	id = "attempt-1",
	termId = "term-confirmed",
	subjectCode = "MAT1203",
	subjectName = "Algebra",
	credits = 4,
	positionInTerm = 0,
	gradingMode = AttemptGradingMode.NUMERIC.name,
	scoreKind = "EMPTY",
	academicOutcome = "PENDING",
	academicBadge = "NONE"
)

private fun pendingSyntheticTermMutation(
	status: PendingMutationStatus = PendingMutationStatus.Pending
) = MutationEnvelope(
	mutationId = "mutation-1",
	scopeKey = RECORD_MUTATION_SCOPE,
	command = AcademicRecordMutation.AddSyntheticTerm(
		termId = "term-synthetic",
		periodYear = 2027,
		periodCode = AcademicTermPeriod.JAN_MAR,
		attempts = listOf(
			AcademicRecordMutation.AddSyntheticTerm.SyntheticAttemptSeed(
				attemptId = "attempt-synthetic",
				subjectCode = "MAT2205",
				subjectName = "Ecuaciones Diferenciales",
				credits = 5,
				gradingMode = AttemptGradingMode.NUMERIC
			)
		)
	),
	precondition = MutationPrecondition.Revision(1L),
	status = status,
	createdAt = 1L,
	updatedAt = 1L,
	lastError = null
).toPendingMutationEntity(
	storeId = RECORD_MUTATION_STORE_ID,
	commandSerializer = AcademicRecordMutation.serializer(),
	json = Json
)

private class FakeAcademicRecordDao(
	private val record: AcademicRecordEntity?
) : AcademicRecordDao() {
	override fun observeRecordFlow(): Flow<AcademicRecordEntity?> = flowOf(record)

	override suspend fun getRecord(): AcademicRecordEntity? = record

	override suspend fun deleteAll() = Unit

	override suspend fun upsertEntity(entity: AcademicRecordEntity) = Unit

	override suspend fun upsertEntities(entities: List<AcademicRecordEntity>) = Unit
}

private class FakeAcademicTermDao(
	private val terms: List<AcademicTermEntity>
) : AcademicTermDao() {
	override fun observeTermsFlow(): Flow<List<AcademicTermEntity>> = flowOf(terms)

	override suspend fun getTerms(): List<AcademicTermEntity> = terms

	override suspend fun deleteAll() = Unit

	override suspend fun upsertEntity(entity: AcademicTermEntity) = Unit

	override suspend fun upsertEntities(entities: List<AcademicTermEntity>) = Unit
}

private class FakeAcademicAttemptDao(
	private val attempts: List<AcademicAttemptEntity>
) : AcademicAttemptDao() {
	override fun observeAttemptsFlow(): Flow<List<AcademicAttemptEntity>> = flowOf(attempts)

	override suspend fun getAttempts(): List<AcademicAttemptEntity> = attempts

	override suspend fun deleteAll() = Unit

	override suspend fun upsertEntity(entity: AcademicAttemptEntity) = Unit

	override suspend fun upsertEntities(entities: List<AcademicAttemptEntity>) = Unit
}

private class FakeAcademicAttemptOverrideDao(
	private val overrides: List<AcademicAttemptOverrideEntity>
) : AcademicAttemptOverrideDao() {
	override fun observeOverridesFlow(): Flow<List<AcademicAttemptOverrideEntity>> = flowOf(overrides)

	override suspend fun getOverrides(): List<AcademicAttemptOverrideEntity> = overrides

	override suspend fun deleteByAttemptId(attemptId: String) = Unit

	override suspend fun deleteAll() = Unit

	override suspend fun upsertEntity(entity: AcademicAttemptOverrideEntity) = Unit

	override suspend fun upsertEntities(entities: List<AcademicAttemptOverrideEntity>) = Unit
}

private class FakePendingMutationDao(
	private val mutations: List<PendingMutationEntity>
) : PendingMutationDao() {
	override fun observePendingMutations(
		storeId: String,
		scopeKey: String
	): Flow<List<PendingMutationEntity>> = flowOf(pendingMutationsIn(storeId, scopeKey))

	override suspend fun getPendingMutations(
		storeId: String,
		scopeKey: String
	): List<PendingMutationEntity> = pendingMutationsIn(storeId, scopeKey)

	override fun observeMutations(
		storeId: String,
		scopeKey: String
	): Flow<List<PendingMutationEntity>> = flowOf(mutationsIn(storeId, scopeKey))

	override suspend fun getMutations(
		storeId: String,
		scopeKey: String
	): List<PendingMutationEntity> = mutationsIn(storeId, scopeKey)

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

	override suspend fun requeueFailedMutations(
		storeId: String,
		scopeKey: String,
		retryableBefore: Long,
		status: String,
		updatedAt: Long
	): Int = 0

	override suspend fun requeueTerminallyFailedMutations(
		storeId: String,
		scopeKey: String,
		status: String,
		updatedAt: Long
	): Int = 0

	override suspend fun deleteAll(): Int = 0

	override suspend fun upsertEntity(entity: PendingMutationEntity) = Unit

	override suspend fun upsertEntities(entities: List<PendingMutationEntity>) = Unit

	private fun mutationsIn(storeId: String, scopeKey: String) = mutations
		.filter { mutation -> mutation.storeId == storeId && mutation.scopeKey == scopeKey }
		.sortedBy(PendingMutationEntity::createdAt)

	private fun pendingMutationsIn(storeId: String, scopeKey: String) = mutationsIn(storeId, scopeKey)
		.filter { mutation -> mutation.status == PendingMutationStatus.Pending.name }
}
