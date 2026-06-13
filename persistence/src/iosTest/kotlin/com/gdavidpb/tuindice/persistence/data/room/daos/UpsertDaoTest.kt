package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptOverrideEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exercises the generic [UpsertDao] contract through a concrete table
 * ([AcademicAttemptOverrideDao]), since every DAO inherits this base.
 */
class UpsertDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: AcademicAttemptOverrideDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.academicAttemptOverrides
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun upsertEntity_whenRowIsNew_insertsRow() = runTest {
		val entity = attemptOverride(attemptId = "attempt-1")

		dao.upsertEntity(entity)

		assertEquals(listOf(entity), dao.getOverrides())
	}

	@Test
	fun upsertEntity_whenRowExists_updatesRow() = runTest {
		val original = attemptOverride(attemptId = "attempt-1", scoreNumericValue = 10)
		val updated = original.copy(scoreNumericValue = 15)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(listOf(updated), dao.getOverrides())
	}

	@Test
	fun upsertEntities_whenRowsAreNew_insertsAllRows() = runTest {
		val first = attemptOverride(attemptId = "attempt-1", updatedAt = 1L)
		val second = attemptOverride(attemptId = "attempt-2", updatedAt = 2L)

		dao.upsertEntities(listOf(first, second))

		assertEquals(listOf(second, first), dao.getOverrides())
	}

	@Test
	fun upsertEntities_withMixedNewAndExistingRows_insertsAndUpdates() = runTest {
		val existing = attemptOverride(attemptId = "attempt-1", scoreNumericValue = 10, updatedAt = 1L)

		dao.upsertEntity(existing)

		val updatedExisting = existing.copy(scoreNumericValue = 15, updatedAt = 3L)
		val brandNew = attemptOverride(attemptId = "attempt-2", updatedAt = 2L)

		dao.upsertEntities(listOf(updatedExisting, brandNew))

		assertEquals(listOf(updatedExisting, brandNew), dao.getOverrides())
	}

	@Test
	fun upsertEntities_withEmptyList_insertsNothing() = runTest {
		dao.upsertEntities(emptyList())

		assertEquals(emptyList(), dao.getOverrides())
	}

	@Test
	fun upsertEntities_withDuplicatedPrimaryKeyInBatch_lastEntityWins() = runTest {
		val first = attemptOverride(attemptId = "attempt-1", scoreNumericValue = 10, updatedAt = 1L)
		val second = attemptOverride(attemptId = "attempt-1", scoreNumericValue = 20, updatedAt = 2L)

		dao.upsertEntities(listOf(first, second))

		assertEquals(listOf(second), dao.getOverrides())
	}

	private fun attemptOverride(
		attemptId: String,
		scoreNumericValue: Int? = 12,
		updatedAt: Long = 1L
	) = AcademicAttemptOverrideEntity(
		attemptId = attemptId,
		scoreKind = "Numeric",
		scoreNumericValue = scoreNumericValue,
		scoreSymbolicValue = null,
		outcome = "Approved",
		updatedAt = updatedAt
	)
}
