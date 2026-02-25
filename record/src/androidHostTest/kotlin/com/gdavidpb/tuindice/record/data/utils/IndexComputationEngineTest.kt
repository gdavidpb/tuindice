package com.gdavidpb.tuindice.record.data.utils

import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class IndexComputationEngineTest {
	private val engine = IndexComputationEngine()

	@Test
	fun recompute_recomputesAffectedQuartersAndCumulativeFields() {
		val quarter1 = quarter(
			id = "q1",
			startDate = 1_000L,
			subjects = listOf(
				subject(id = "s1", quarterId = "q1", code = "MA1111", credits = 4, grade = 2),
				subject(id = "s2", quarterId = "q1", code = "FS1111", credits = 2, grade = 3)
			)
		)
		val quarter2 = quarter(
			id = "q2",
			startDate = 2_000L,
			subjects = listOf(
				subject(id = "s3", quarterId = "q2", code = "MA1111", credits = 4, grade = 5),
				subject(id = "s4", quarterId = "q2", code = "ID1111", credits = 3, grade = 4)
			)
		)
		val quarter3 = quarter(
			id = "q3",
			startDate = 3_000L,
			subjects = listOf(
				subject(id = "s5", quarterId = "q3", code = "ZZ1111", credits = 2, grade = 5)
			)
		)

		val result = engine.recompute(
			quarters = listOf(quarter3, quarter1, quarter2),
			affectedStartDate = quarter2.startDate
		)

		assertEquals(listOf("q3", "q2"), result.affectedQuarters.map { it.id })

		val recomputedQ1 = result.quarters.first { it.id == "q1" }
		val recomputedQ2 = result.quarters.first { it.id == "q2" }
		val recomputedQ3 = result.quarters.first { it.id == "q3" }

		assertEquals(2.3333, recomputedQ1.gradeSum, 0.0001)
		assertEquals(6, recomputedQ1.creditsSum)

		assertEquals(4.2222, recomputedQ2.gradeSum, 0.0001)
		assertEquals(9, recomputedQ2.creditsSum)

		assertEquals(4.3636, recomputedQ3.gradeSum, 0.0001)
		assertEquals(11, recomputedQ3.creditsSum)
	}

	@Test
	fun recompute_isDeterministicAcrossInputOrderAndTieBreakers() {
		val quarterA = quarter(
			id = "qa",
			startDate = 1_000L,
			subjects = listOf(
				subject(id = "sa", quarterId = "qa", code = "MA1111", credits = 4, grade = 5)
			)
		)
		val quarterB = quarter(
			id = "qb",
			startDate = 1_000L,
			subjects = listOf(
				subject(id = "sb", quarterId = "qb", code = "MA1111", credits = 4, grade = 2)
			)
		)
		val quarterC = quarter(
			id = "qc",
			startDate = 2_000L,
			subjects = listOf(
				subject(id = "sc", quarterId = "qc", code = "ID1111", credits = 3, grade = 4)
			)
		)
		val baseline = engine.recompute(
			quarters = listOf(quarterA, quarterB, quarterC),
			affectedStartDate = 0L
		).quarters

		repeat(50) { iteration ->
			val shuffled = listOf(quarterA, quarterB, quarterC)
				.shuffled(Random(iteration))
			val recomputed = engine.recompute(
				quarters = shuffled,
				affectedStartDate = 0L
			).quarters

			assertEquals(baseline, recomputed)
		}

		val quarterAfterTie = baseline.first { it.id == "qc" }

		/*
		 * "qa" is newer than "qb" on tie by startDate because quarterId ASC in recency.
		 * If tie handling is wrong, this value changes.
		 */
		assertEquals(4.5714, quarterAfterTie.gradeSum, 0.0001)
		assertTrue(quarterAfterTie.creditsSum == 7)
	}

	private fun quarter(
		id: String,
		startDate: Long,
		subjects: List<LocalSubject>
	) = LocalQuarter(
		id = id,
		name = id,
		startDate = startDate,
		endDate = startDate + 100L,
		grade = 0.0,
		gradeSum = 0.0,
		credits = 0,
		creditsSum = 0,
		isCurrent = false,
		isReadOnly = false,
		subjects = subjects
	)

	private fun subject(
		id: String,
		quarterId: String,
		code: String,
		credits: Int,
		grade: Int
	) = LocalSubject(
		id = id,
		quarterId = quarterId,
		code = code,
		name = id,
		credits = credits,
		grade = grade
	)
}