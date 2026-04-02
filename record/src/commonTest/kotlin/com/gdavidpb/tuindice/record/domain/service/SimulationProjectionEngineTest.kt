package com.gdavidpb.tuindice.record.domain.service

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.data.model.quarter.LocalQuarter
import com.gdavidpb.tuindice.record.data.model.quarter.LocalSubject
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SimulationProjectionEngineTest {
	private val engine = SimulationProjectionEngine()

	@Test
	fun recompute_excludesThePreviousFailedAttemptFromSimulatedCumulative_withoutRewritingTheHistoricalQuarter() {
		val historicalQuarter = createQuarter(
			id = "quarter-1",
			startDate = date(2019, 1),
			endDate = date(2019, 3),
			grade = 2.0,
			gradeSum = 2.0,
			credits = 4,
			creditsSum = 4,
			isReadOnly = true,
			subjects = listOf(
				createSubject(
					id = "subject-1",
					quarterId = "quarter-1",
					code = "MA1111",
					grade = 2,
					credits = 4
				)
			)
		)
		val latestQuarter = createQuarter(
			id = "quarter-2",
			startDate = date(2019, 7),
			endDate = date(2019, 9),
			grade = 4.0,
			gradeSum = 4.0,
			credits = 4,
			creditsSum = 4,
			isCurrent = true,
			isReadOnly = false,
			subjects = listOf(
				createSubject(
					id = "subject-2",
					quarterId = "quarter-2",
					code = "MA1111",
					grade = 4,
					credits = 4
				)
			)
		)

		val result = recompute(latestQuarter, historicalQuarter)
		val recomputedHistorical = result.first { quarter -> quarter.id == historicalQuarter.id }
		val recomputedLatest = result.first { quarter -> quarter.id == latestQuarter.id }

		assertEquals(2.0, recomputedHistorical.simulationGrade)
		assertEquals(2.0, recomputedHistorical.simulationGradeSum)
		assertEquals(4, recomputedHistorical.simulationCredits)
		assertEquals(4, recomputedHistorical.simulationCreditsSum)
		assertEquals(
			SubjectStatus.WITHOUT_EFFECT,
			recomputedHistorical.subjects.single().simulationStatus
		)

		assertEquals(4.0, recomputedLatest.simulationGrade)
		assertEquals(4.0, recomputedLatest.simulationGradeSum)
		assertEquals(4, recomputedLatest.simulationCredits)
		assertEquals(4, recomputedLatest.simulationCreditsSum)
		assertNull(recomputedLatest.subjects.single().simulationStatus)
	}

	@Test
	fun recompute_keepsOfficialWithoutEffectSubjectsUntouchedInSimulationStatus() {
		val quarter = createQuarter(
			id = "quarter-1",
			grade = 2.0,
			gradeSum = 2.0,
			credits = 4,
			creditsSum = 4,
			isReadOnly = true,
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

		val result = recompute(quarter).single()

		assertEquals(2.0, result.simulationGrade)
		assertEquals(2.0, result.simulationGradeSum)
		assertEquals(4, result.simulationCredits)
		assertEquals(4, result.simulationCreditsSum)
		assertNull(result.subjects.single().simulationStatus)
	}

	@Test
	fun recompute_keepsHistoricalQuarterSimulationAggregatesOfficial_evenWhenAFuturePassExcludesItCumulatively() {
		val historicalQuarter = createQuarter(
			id = "quarter-1",
			startDate = date(2016, 7),
			endDate = date(2016, 8),
			grade = 2.0,
			gradeSum = 3.0588,
			credits = 4,
			creditsSum = 17,
			isReadOnly = true,
			subjects = listOf(
				createSubject(
					id = "subject-1",
					quarterId = "quarter-1",
					code = "MA1111",
					grade = 2,
					credits = 4
				)
			)
		)
		val approvalQuarter = createQuarter(
			id = "quarter-2",
			startDate = date(2016, 9),
			endDate = date(2016, 12),
			grade = 3.0,
			gradeSum = 3.5384,
			credits = 4,
			creditsSum = 17,
			isCurrent = true,
			isReadOnly = false,
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

		assertEquals(2.0, recomputedHistorical.simulationGrade)
		assertEquals(3.0588, recomputedHistorical.simulationGradeSum)
		assertEquals(4, recomputedHistorical.simulationCredits)
		assertEquals(17, recomputedHistorical.simulationCreditsSum)
		assertEquals(SubjectStatus.WITHOUT_EFFECT, recomputedHistorical.subjects.single().simulationStatus)

		assertEquals(3.0, recomputedApproval.simulationGrade)
		assertEquals(3.0, recomputedApproval.simulationGradeSum)
		assertEquals(4, recomputedApproval.simulationCredits)
		assertEquals(4, recomputedApproval.simulationCreditsSum)
	}

	private fun createQuarter(
		id: String,
		startDate: Long = date(2020, 1),
		endDate: Long = date(2020, 3),
		grade: Double = 0.0,
		gradeSum: Double = 0.0,
		credits: Int = 0,
		creditsSum: Int = 0,
		isCurrent: Boolean = false,
		isReadOnly: Boolean = true,
		subjects: List<LocalSubject>
	) = LocalQuarter(
		id = id,
		name = "",
		startDate = startDate,
		endDate = endDate,
		grade = grade,
		gradeSum = gradeSum,
		credits = credits,
		creditsSum = creditsSum,
		isCurrent = isCurrent,
		isReadOnly = isReadOnly,
		revision = 1L,
		subjects = subjects
	)

	private fun createSubject(
		id: String,
		quarterId: String,
		code: String,
		grade: Int,
		credits: Int,
		status: SubjectStatus? = SubjectStatus.NORMAL
	) = LocalSubject(
		id = id,
		quarterId = quarterId,
		code = code,
		name = code,
		credits = credits,
		grade = grade,
		status = status,
		revision = 1L
	)

	private fun recompute(vararg quarters: LocalQuarter): List<LocalQuarter> {
		return engine.recompute(quarters.toList())
	}

	private fun date(year: Int, month: Int): Long {
		return LocalDate(year, month, 1)
			.atStartOfDayIn(TimeZone.currentSystemDefault())
			.toEpochMilliseconds()
	}
}
