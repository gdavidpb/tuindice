package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EvaluationDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: EvaluationDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.evaluations
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getEvaluation_whenMissing_returnsNull() = runTest {
		assertNull(dao.getEvaluation(eid = "missing"))
	}

	@Test
	fun upsertEntity_thenGetEvaluation_returnsRoundTrippedEntity() = runTest {
		val evaluation = evaluation(id = "evaluation-1")

		dao.upsertEntity(evaluation)

		assertEquals(evaluation, dao.getEvaluation(eid = evaluation.id))
	}

	@Test
	fun upsertEntity_withExistingId_replacesRow() = runTest {
		val original = evaluation(id = "evaluation-1", grade = null, isDone = false)
		val updated = original.copy(
			grade = 15.5,
			isDone = true,
			revision = original.revision + 1
		)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(updated, dao.getEvaluation(eid = original.id))
	}

	@Test
	fun observeEvaluationsFlow_emitsAllRowsOrderedByDateAsc() = runTest {
		assertEquals(emptyList(), dao.observeEvaluationsFlow().first())

		val second = evaluation(id = "evaluation-2", date = 2L)
		val first = evaluation(id = "evaluation-1", date = 1L)
		val third = evaluation(id = "evaluation-3", attemptId = "attempt-2", date = 3L)

		dao.upsertEntities(listOf(second, third, first))

		assertEquals(
			listOf(first, second, third),
			dao.observeEvaluationsFlow().first()
		)
	}

	@Test
	fun observeEvaluationsFlow_sortsUndatedEvaluationsFirst() = runTest {
		val undated = evaluation(
			id = "evaluation-undated",
			date = null,
			scheduleMode = EvaluationScheduleMode.CONTINUOUS
		)
		val dated = evaluation(id = "evaluation-dated", date = 1L)

		dao.upsertEntities(listOf(dated, undated))

		assertEquals(
			listOf(undated, dated),
			dao.observeEvaluationsFlow().first()
		)
	}

	@Test
	fun getAttemptEvaluations_returnsOnlyAttemptRowsOrderedByDateAsc() = runTest {
		val second = evaluation(id = "evaluation-2", attemptId = "attempt-1", date = 2L)
		val first = evaluation(id = "evaluation-1", attemptId = "attempt-1", date = 1L)
		val otherAttempt = evaluation(id = "evaluation-3", attemptId = "attempt-2", date = 3L)

		dao.upsertEntities(listOf(second, first, otherAttempt))

		assertEquals(
			listOf(first, second),
			dao.getAttemptEvaluations(attemptId = "attempt-1").first()
		)
	}

	@Test
	fun deleteEvaluation_removesOnlyMatchingRow() = runTest {
		val target = evaluation(id = "evaluation-1", date = 1L)
		val other = evaluation(id = "evaluation-2", date = 2L)

		dao.upsertEntities(listOf(target, other))

		val deleted = dao.deleteEvaluation(eid = target.id)

		assertEquals(1, deleted)
		assertNull(dao.getEvaluation(eid = target.id))
		assertEquals(other, dao.getEvaluation(eid = other.id))
	}

	@Test
	fun deleteEvaluation_whenMissing_deletesNothing() = runTest {
		dao.upsertEntity(evaluation(id = "evaluation-1"))

		assertEquals(0, dao.deleteEvaluation(eid = "missing"))
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				evaluation(id = "evaluation-1", date = 1L),
				evaluation(id = "evaluation-2", date = 2L)
			)
		)

		val deleted = dao.deleteAll()

		assertEquals(2, deleted)
		assertEquals(emptyList(), dao.observeEvaluationsFlow().first())
	}

	private fun evaluation(
		id: String,
		attemptId: String = "attempt-1",
		date: Long? = 1L,
		grade: Double? = null,
		isDone: Boolean = false,
		scheduleMode: EvaluationScheduleMode = EvaluationScheduleMode.DATED,
		revision: Long = 1L
	) = EvaluationEntity(
		id = id,
		referenceId = "reference-$id",
		attemptId = attemptId,
		subjectCode = "ma1111",
		termId = "term-1",
		revision = revision,
		scheduleMode = scheduleMode,
		grade = grade,
		maxGrade = 20.0,
		date = date,
		type = 1,
		isDone = isDone
	)
}
