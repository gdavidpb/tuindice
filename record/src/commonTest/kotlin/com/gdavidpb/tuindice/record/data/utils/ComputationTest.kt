package com.gdavidpb.tuindice.record.data.utils

import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import kotlin.test.Test
import kotlin.test.assertEquals

class ComputationTest {
	@Test
	fun computeCreditsAndGrade_ignoreSubjectsWithZeroGrade() {
		val subjects = listOf(
			subject(code = "MA1111", credits = 4, grade = 5),
			subject(code = "ID1111", credits = 3, grade = 0),
			subject(code = "FI1111", credits = 2, grade = 2)
		)

		assertEquals(6, subjects.computeCredits())
		assertEquals(4.0, subjects.computeGrade(), absoluteTolerance = 0.0)
	}

	@Test
	fun computeGradeSum_appliesNoEffectRuleForLatestApprovedAttempt() {
		val quarterOld = quarter(
			id = "q1",
			startDate = 1_000L,
			subjects = listOf(
				subject(id = "s1", quarterId = "q1", code = "MA1111", credits = 4, grade = 2)
			)
		)
		val quarterMiddle = quarter(
			id = "q2",
			startDate = 2_000L,
			subjects = listOf(
				subject(id = "s2", quarterId = "q2", code = "MA1111", credits = 4, grade = 1),
				subject(id = "s3", quarterId = "q2", code = "FI1111", credits = 2, grade = 3)
			)
		)
		val quarterCurrent = quarter(
			id = "q3",
			startDate = 3_000L,
			subjects = listOf(
				subject(id = "s4", quarterId = "q3", code = "MA1111", credits = 4, grade = 5)
			)
		)

		val gradeSum = listOf(quarterCurrent, quarterMiddle, quarterOld).computeGradeSum(until = quarterCurrent)

		assertEquals(3.4, gradeSum, absoluteTolerance = 0.0)
	}

	@Test
	fun recompute_engineReturnsDeterministicDescendingQuartersAndAffectedSubset() {
		val quarter1 = quarter(
			id = "q1",
			startDate = 100L,
			subjects = listOf(
				subject(id = "s1", quarterId = "q1", code = "A", credits = 2, grade = 4)
			)
		)
		val quarter2 = quarter(
			id = "q2",
			startDate = 200L,
			subjects = listOf(
				subject(id = "s2", quarterId = "q2", code = "B", credits = 3, grade = 2)
			)
		)
		val quarter3 = quarter(
			id = "q3",
			startDate = 300L,
			subjects = listOf(
				subject(id = "s3", quarterId = "q3", code = "C", credits = 4, grade = 0)
			)
		)

		val result = IndexComputationEngine().recompute(
			quarters = listOf(quarter3, quarter2, quarter1),
			affectedStartDate = 200L
		)

		assertEquals(listOf("q3", "q2", "q1"), result.quarters.map(LocalQuarter::id))
		assertEquals(listOf("q3", "q2"), result.affectedQuarters.map(LocalQuarter::id))

		val q1 = result.quarters.first { it.id == "q1" }
		val q2 = result.quarters.first { it.id == "q2" }
		val q3 = result.quarters.first { it.id == "q3" }

		assertEquals(4.0, q1.grade, absoluteTolerance = 0.0)
		assertEquals(4.0, q1.gradeSum, absoluteTolerance = 0.0)
		assertEquals(2, q1.credits)
		assertEquals(2, q1.creditsSum)

		assertEquals(2.0, q2.grade, absoluteTolerance = 0.0)
		assertEquals(2.8, q2.gradeSum, absoluteTolerance = 0.0)
		assertEquals(3, q2.credits)
		assertEquals(5, q2.creditsSum)

		assertEquals(0.0, q3.grade, absoluteTolerance = 0.0)
		assertEquals(2.8, q3.gradeSum, absoluteTolerance = 0.0)
		assertEquals(0, q3.credits)
		assertEquals(5, q3.creditsSum)
	}

	private fun quarter(
		id: String,
		startDate: Long,
		subjects: List<LocalSubject>
	): LocalQuarter {
		return LocalQuarter(
			id = id,
			name = id,
			startDate = startDate,
			endDate = startDate + 100,
			grade = 0.0,
			gradeSum = 0.0,
			credits = 0,
			creditsSum = 0,
			isCurrent = id == "q3",
			isReadOnly = false,
			subjects = subjects
		)
	}

	private fun subject(
		id: String = "s",
		quarterId: String = "q",
		code: String,
		credits: Int,
		grade: Int
	): LocalSubject {
		return LocalSubject(
			id = id,
			quarterId = quarterId,
			code = code,
			name = code,
			credits = credits,
			grade = grade
		)
	}
}
