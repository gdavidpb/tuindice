package com.gdavidpb.tuindice.record.domain.service

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.data.model.quarter.LocalQuarter
import com.gdavidpb.tuindice.record.data.model.quarter.LocalSubject
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.math.floor
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexComputationEngineTest {
	private val engine = IndexComputationEngine()

	@Test
	fun recompute_calculatesQuarterCreditsIgnoringZeroGrades() {
		val quarter = createQuarter(
			id = "quarter-1",
			subjects = listOf(
				createSubject(id = "subject-1", quarterId = "quarter-1", grade = 5, credits = 4),
				createSubject(id = "subject-2", quarterId = "quarter-1", grade = 0, credits = 3),
				createSubject(id = "subject-3", quarterId = "quarter-1", grade = 2, credits = 2)
			)
		)

		val actualCredits = recompute(quarter).single().credits

		assertEquals(6, actualCredits)
	}

	@Test
	fun recompute_calculatesQuarterGradeUsingWeightedAverage() {
		val quarter = createQuarter(
			id = "quarter-1",
			subjects = listOf(
				createSubject(id = "subject-1", quarterId = "quarter-1", grade = 5, credits = 4),
				createSubject(id = "subject-2", quarterId = "quarter-1", grade = 0, credits = 3),
				createSubject(id = "subject-3", quarterId = "quarter-1", grade = 2, credits = 2)
			)
		)

		val creditsSum = (4 + 2).toDouble()
		val weightedSum = ((5 * 4) + (2 * 2)).toDouble()
		val expectedGrade = floor(weightedSum / creditsSum * 10000.0) / 10000.0

		val actualGrade = recompute(quarter).single().grade

		assertEquals(expectedGrade, actualGrade)
	}

	@Test
	fun recompute_replacesOnlyThePreviousAttempt_whenLatestAttemptPassed() {
		val oldestQuarter = createQuarter(
			id = "quarter-1",
			startDate = date(2019, 1),
			endDate = date(2019, 3),
			subjects = listOf(
				createSubject(
					id = "subject-1",
					quarterId = "quarter-1",
					code = "MA1111",
					grade = 1,
					credits = 4
				)
			)
		)
		val middleQuarter = createQuarter(
			id = "quarter-2",
			startDate = date(2019, 7),
			endDate = date(2019, 8),
			subjects = listOf(
				createSubject(
					id = "subject-2",
					quarterId = "quarter-2",
					code = "MA1111",
					grade = 2,
					credits = 4
				)
			)
		)
		val latestQuarter = createQuarter(
			id = "quarter-3",
			startDate = date(2019, 9),
			endDate = date(2019, 12),
			subjects = listOf(
				createSubject(
					id = "subject-3",
					quarterId = "quarter-3",
					code = "MA1111",
					grade = 5,
					credits = 4
				)
			)
		)

		val recomputedQuarter = recompute(
			latestQuarter,
			middleQuarter,
			oldestQuarter
		).first { quarter -> quarter.id == latestQuarter.id }

		assertEquals(8, recomputedQuarter.creditsSum)
		assertEquals(3.0, recomputedQuarter.gradeSum)
	}

	@Test
	fun recompute_calculatesGradeSumAcrossQuartersRespectingRetakes() {
		val quarter1 = createQuarter(
			id = "quarter-1",
			startDate = date(2019, 1),
			endDate = date(2019, 3),
			isCurrent = false,
			isReadOnly = true,
			subjects = listOf(
				createSubject(id = "subject-11", quarterId = "quarter-1", code = "MA1111", grade = 2, credits = 4),
				createSubject(id = "subject-12", quarterId = "quarter-1", code = "ID1111", grade = 3, credits = 3),
				createSubject(id = "subject-13", quarterId = "quarter-1", code = "CSA211", grade = 3, credits = 3)
			)
		)
		val quarter2 = createQuarter(
			id = "quarter-2",
			startDate = date(2019, 7),
			endDate = date(2019, 8),
			isCurrent = false,
			isReadOnly = true,
			subjects = listOf(
				createSubject(id = "subject-21", quarterId = "quarter-2", code = "MA1111", grade = 2, credits = 4)
			)
		)
		val quarter3 = createQuarter(
			id = "quarter-3",
			startDate = date(2019, 9),
			endDate = date(2019, 12),
			isCurrent = false,
			isReadOnly = true,
			subjects = listOf(
				createSubject(id = "subject-31", quarterId = "quarter-3", code = "MA1111", grade = 5, credits = 4),
				createSubject(id = "subject-32", quarterId = "quarter-3", code = "ID1112", grade = 5, credits = 3)
			)
		)
		val quarter4 = createQuarter(
			id = "quarter-4",
			startDate = date(2020, 1),
			endDate = date(2020, 3),
			isCurrent = false,
			isReadOnly = true,
			subjects = listOf(
				createSubject(id = "subject-41", quarterId = "quarter-4", code = "MA1112", grade = 0, credits = 4),
				createSubject(id = "subject-42", quarterId = "quarter-4", code = "ID1113", grade = 0, credits = 3)
			)
		)
		val quarter5 = createQuarter(
			id = "quarter-5",
			startDate = date(2020, 9),
			endDate = date(2020, 12),
			isCurrent = true,
			isReadOnly = false,
			subjects = listOf(
				createSubject(id = "subject-51", quarterId = "quarter-5", code = "MA1112", grade = 5, credits = 4),
				createSubject(id = "subject-52", quarterId = "quarter-5", code = "ID1113", grade = 0, credits = 3)
			)
		)

		val creditsSum = (4 + 3 + 3 + 4 + 3 + 4).toDouble()
		val weightedSum = ((2 * 4) + (3 * 3) + (3 * 3) + (5 * 4) + (5 * 3) + (5 * 4)).toDouble()
		val expectedGradeSum = floor(weightedSum / creditsSum * 10000.0) / 10000.0

		val actualGradeSum = recompute(quarter5, quarter4, quarter3, quarter2, quarter1)
			.first { quarter -> quarter.id == quarter5.id }
			.gradeSum

		assertEquals(expectedGradeSum, actualGradeSum)
	}

	@Test
	fun recompute_keepsOfficialWithoutEffectGradesVisibleInTheHistoricalQuarter_whileExcludingThemFromFutureCumulativeAggregates() {
		val historicalQuarter = createQuarter(
			id = "quarter-1",
			startDate = date(2016, 7),
			endDate = date(2016, 8),
			subjects = listOf(
				createSubject(
					id = "subject-1",
					quarterId = "quarter-1",
					code = "MA1111",
					grade = 2,
					credits = 4,
					status = SubjectStatus.WITHOUT_EFFECT
				)
			)
		)
		val approvalQuarter = createQuarter(
			id = "quarter-2",
			startDate = date(2016, 9),
			endDate = date(2016, 12),
			subjects = listOf(
				createSubject(
					id = "subject-2",
					quarterId = "quarter-2",
					code = "MA1111",
					grade = 3,
					credits = 4
				)
			)
		)

		val result = recompute(approvalQuarter, historicalQuarter)
		val recomputedHistorical = result.first { quarter -> quarter.id == historicalQuarter.id }
		val recomputedApproval = result.first { quarter -> quarter.id == approvalQuarter.id }

		assertEquals(4, recomputedHistorical.credits)
		assertEquals(2.0, recomputedHistorical.grade)
		assertEquals(4, recomputedHistorical.creditsSum)
		assertEquals(2.0, recomputedHistorical.gradeSum)

		assertEquals(4, recomputedApproval.credits)
		assertEquals(3.0, recomputedApproval.grade)
		assertEquals(4, recomputedApproval.creditsSum)
		assertEquals(3.0, recomputedApproval.gradeSum)
	}

	@Test
	fun recompute_includesUnreportedSubjectsInQuarterGradeOnly() {
		val quarter = createQuarter(
			id = "quarter-1",
			subjects = listOf(
				createSubject(
					id = "subject-1",
					quarterId = "quarter-1",
					code = "EC5751",
					grade = 0,
					credits = 3,
					status = SubjectStatus.UNREPORTED
				),
				createSubject(
					id = "subject-2",
					quarterId = "quarter-1",
					code = "EC5811",
					grade = 2,
					credits = 4
				),
				createSubject(
					id = "subject-3",
					quarterId = "quarter-1",
					code = "PS2323",
					grade = 2,
					credits = 4
				)
			)
		)

		val recomputedQuarter = recompute(quarter).single()

		assertEquals(11, recomputedQuarter.credits)
		assertEquals(1.4545, recomputedQuarter.grade)
		assertEquals(8, recomputedQuarter.creditsSum)
		assertEquals(2.0, recomputedQuarter.gradeSum)
	}

	private fun date(year: Int, month: Int): Long {
		return LocalDate(year, month, 1)
			.atStartOfDayIn(TimeZone.currentSystemDefault())
			.toEpochMilliseconds()
	}

	private fun createQuarter(
		id: String = "",
		startDate: Long = 0L,
		endDate: Long = 0L,
		isCurrent: Boolean = false,
		isReadOnly: Boolean = false,
		subjects: List<LocalSubject> = emptyList()
	) = LocalQuarter(
		id = id,
		name = "",
		startDate = startDate,
		endDate = endDate,
		grade = 0.0,
		gradeSum = 0.0,
		credits = 0,
		creditsSum = 0,
		isCurrent = isCurrent,
		isReadOnly = isReadOnly,
		revision = 0L,
		subjects = subjects
	)

	private fun createSubject(
		id: String = "",
		quarterId: String = "",
		code: String = "",
		name: String = "",
		grade: Int = 0,
		credits: Int = 0,
		status: SubjectStatus? = null
	) = LocalSubject(
		id = id,
		quarterId = quarterId,
		code = code,
		name = name,
		credits = credits,
		grade = grade,
		status = status,
		revision = 0L
	)

	private fun recompute(vararg quarters: LocalQuarter): List<LocalQuarter> {
		return engine.recompute(
			quarters = quarters.toList(),
			affectedStartDate = Long.MIN_VALUE
		).quarters
	}
}
