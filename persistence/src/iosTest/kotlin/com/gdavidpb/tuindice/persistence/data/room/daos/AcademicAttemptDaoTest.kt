package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicAttemptDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: AcademicAttemptDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.academicAttempts
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getAttempts_whenEmpty_returnsEmptyList() = runTest {
		assertEquals(emptyList(), dao.getAttempts())
	}

	@Test
	fun upsertEntity_thenGetAttempts_returnsRoundTrippedEntity() = runTest {
		val attempt = attempt(id = "attempt-1")

		dao.upsertEntity(attempt)

		assertEquals(listOf(attempt), dao.getAttempts())
	}

	@Test
	fun upsertEntity_withExistingId_replacesRow() = runTest {
		val original = attempt(id = "attempt-1", scoreNumericValue = 10)
		val updated = original.copy(
			scoreNumericValue = 16,
			academicOutcome = "Approved"
		)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(listOf(updated), dao.getAttempts())
	}

	@Test
	fun getAttempts_returnsRowsOrderedByTermIdDescThenPositionAscThenIdAsc() = runTest {
		val newestTermFirstPosition = attempt(id = "attempt-3", termId = "term-2", positionInTerm = 0)
		val newestTermSecondPositionA = attempt(id = "attempt-a", termId = "term-2", positionInTerm = 1)
		val newestTermSecondPositionB = attempt(id = "attempt-b", termId = "term-2", positionInTerm = 1)
		val oldestTerm = attempt(id = "attempt-1", termId = "term-1", positionInTerm = 0)

		dao.upsertEntities(
			listOf(
				oldestTerm,
				newestTermSecondPositionB,
				newestTermFirstPosition,
				newestTermSecondPositionA
			)
		)

		assertEquals(
			listOf(
				newestTermFirstPosition,
				newestTermSecondPositionA,
				newestTermSecondPositionB,
				oldestTerm
			),
			dao.getAttempts()
		)
	}

	@Test
	fun observeAttemptsFlow_emitsRowsOrderedByTermIdDesc() = runTest {
		assertEquals(emptyList(), dao.observeAttemptsFlow().first())

		val oldest = attempt(id = "attempt-1", termId = "term-1")
		val newest = attempt(id = "attempt-2", termId = "term-2")

		dao.upsertEntities(listOf(oldest, newest))

		assertEquals(listOf(newest, oldest), dao.observeAttemptsFlow().first())
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				attempt(id = "attempt-1", termId = "term-1"),
				attempt(id = "attempt-2", termId = "term-2")
			)
		)

		dao.deleteAll()

		assertEquals(emptyList(), dao.getAttempts())
	}

	private fun attempt(
		id: String,
		termId: String = "term-1",
		subjectCode: String = "ma1111",
		positionInTerm: Int = 0,
		scoreNumericValue: Int? = 15
	) = AcademicAttemptEntity(
		id = id,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = "Matematicas I",
		credits = 4,
		positionInTerm = positionInTerm,
		gradingMode = "Numeric",
		scoreKind = "Numeric",
		scoreNumericValue = scoreNumericValue,
		scoreSymbolicValue = null,
		academicOutcome = "Approved",
		academicBadge = "None"
	)
}
