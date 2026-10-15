package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.GradeHistogram
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsGradesItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_percent_format
import tuindice.summary.generated.resources.summary_statistics_grades_share_one
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SummaryStatisticsGradesMapperTest {
	@Test
	fun when_noAttemptIsGraded_then_theBlockSaysWhatItLacks() {
		val item = gradesItemOf(GradeHistogram(countsByGrade = emptyMap(), unreported = 0))

		assertTrue(item.content is SummaryStatisticsGradesItem.Content.Empty)
	}

	@Test
	fun when_gradesAreGiven_then_thereIsABarForEachGradeAndTheUnreportedAreAFigure() {
		val data = dataOf(gradesItemOf(GradeHistogram(countsByGrade = mapOf(5 to 3), unreported = 2)))

		assertEquals(listOf(0f, 0f, 0f, 0f, 3f), data.bars.map { bar -> bar.value })
		assertEquals(UiText.Raw("5"), data.bars.last().axisLabel)
		assertEquals(UiText.Raw("3"), data.bars.last().valueLabel)
		assertEquals(listOf("2"), data.facts.map { fact -> fact.value })
	}

	@Test
	fun when_theOnlyGradeIsOneAttempt_then_ItsShareIsOfTheSingleGrade() {
		val data = dataOf(gradesItemOf(GradeHistogram(countsByGrade = mapOf(4 to 1), unreported = 0)))

		val lines = data.bars.single { bar -> bar.value == 1f }.tooltip.lines

		assertEquals(
			listOf(
				UiText.Resource(
					Res.string.summary_statistics_grades_share_one,
					listOf(UiText.Resource(Res.string.summary_percent_format, listOf(100)))
				)
			),
			lines
		)
	}

	private fun dataOf(item: SummaryStatisticsGradesItem): SummaryStatisticsGradesItem.Content.Data {
		return item.content as SummaryStatisticsGradesItem.Content.Data
	}
}
