package com.gdavidpb.tuindice.subjects.ui.model

import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubjectGradeChartSummaryTest {
	@Test
	fun from_resolvesMultimodalDistribution() {
		val summary = SubjectGradeChartSummary.from(
			latestGradeBins = listOf(
				SubjectGradeBin(grade = 1, count = 12),
				SubjectGradeBin(grade = 2, count = 28),
				SubjectGradeBin(grade = 3, count = 42),
				SubjectGradeBin(grade = 4, count = 42),
				SubjectGradeBin(grade = 5, count = 10)
			),
			medianGrade = 3.0,
			stddevGrade = 0.9
		)

		assertEquals(listOf(3, 4), summary.modalGrades)
		assertEquals("3·4", summary.modeLabel)
		assertEquals("3", summary.medianLabel)
		assertEquals("0.9", summary.stddevLabel)
	}

	@Test
	fun from_clampsStddevRangeToSupportedGradeDomain() {
		val summary = SubjectGradeChartSummary.from(
			latestGradeBins = listOf(
				SubjectGradeBin(grade = 1, count = 4),
				SubjectGradeBin(grade = 5, count = 8)
			),
			medianGrade = 4.8,
			stddevGrade = 1.4
		)

		assertEquals(3.4, summary.stddevRangeStart)
		assertEquals(5.0, summary.stddevRangeEnd)
	}

	@Test
	fun from_ignoresEmptyOrZeroDistributionsForMode() {
		val summary = SubjectGradeChartSummary.from(
			latestGradeBins = listOf(
				SubjectGradeBin(grade = 1, count = 0),
				SubjectGradeBin(grade = 2, count = 0)
			),
			medianGrade = null,
			stddevGrade = null
		)

		assertEquals(emptyList(), summary.modalGrades)
		assertNull(summary.modeLabel)
		assertNull(summary.medianLabel)
		assertNull(summary.stddevLabel)
	}
}
