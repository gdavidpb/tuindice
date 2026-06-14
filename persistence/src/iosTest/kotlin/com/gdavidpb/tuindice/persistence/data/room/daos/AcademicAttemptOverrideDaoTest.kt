package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptOverrideEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicAttemptOverrideDaoTest {
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
	fun getOverrides_whenEmpty_returnsEmptyList() = runTest {
		assertEquals(emptyList(), dao.getOverrides())
	}

	@Test
	fun upsertEntity_thenGetOverrides_returnsRoundTrippedEntity() = runTest {
		val overrideRow = attemptOverride(attemptId = "attempt-1")

		dao.upsertEntity(overrideRow)

		assertEquals(listOf(overrideRow), dao.getOverrides())
	}

	@Test
	fun upsertEntity_withExistingAttemptId_replacesRow() = runTest {
		val original = attemptOverride(attemptId = "attempt-1", scoreNumericValue = 10, updatedAt = 1L)
		val updated = original.copy(scoreNumericValue = 15, updatedAt = 2L)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(listOf(updated), dao.getOverrides())
	}

	@Test
	fun getOverrides_returnsRowsOrderedByUpdatedAtDesc() = runTest {
		val oldest = attemptOverride(attemptId = "attempt-oldest", updatedAt = 1L)
		val newest = attemptOverride(attemptId = "attempt-newest", updatedAt = 3L)
		val middle = attemptOverride(attemptId = "attempt-middle", updatedAt = 2L)

		dao.upsertEntities(listOf(oldest, newest, middle))

		assertEquals(listOf(newest, middle, oldest), dao.getOverrides())
	}

	@Test
	fun observeOverridesFlow_emitsRowsOrderedByUpdatedAtDesc() = runTest {
		assertEquals(emptyList(), dao.observeOverridesFlow().first())

		val oldest = attemptOverride(attemptId = "attempt-oldest", updatedAt = 1L)
		val newest = attemptOverride(attemptId = "attempt-newest", updatedAt = 2L)

		dao.upsertEntities(listOf(oldest, newest))

		assertEquals(listOf(newest, oldest), dao.observeOverridesFlow().first())
	}

	@Test
	fun deleteByAttemptId_removesOnlyMatchingRow() = runTest {
		val target = attemptOverride(attemptId = "attempt-1", updatedAt = 1L)
		val other = attemptOverride(attemptId = "attempt-2", updatedAt = 2L)

		dao.upsertEntities(listOf(target, other))

		dao.deleteByAttemptId(attemptId = target.attemptId)

		assertEquals(listOf(other), dao.getOverrides())
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				attemptOverride(attemptId = "attempt-1", updatedAt = 1L),
				attemptOverride(attemptId = "attempt-2", updatedAt = 2L)
			)
		)

		dao.deleteAll()

		assertEquals(emptyList(), dao.getOverrides())
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
