package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.testing.currentTermOf
import com.gdavidpb.tuindice.summary.testing.evaluationTypeOf
import com.gdavidpb.tuindice.summary.testing.progressTermOf
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.testing.ungradedAttentionOf
import kotlinx.datetime.LocalDate
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_career_percent
import tuindice.summary.generated.resources.summary_percent_format
import tuindice.summary.generated.resources.summary_points_none
import tuindice.summary.generated.resources.summary_statistics_current_figure
import tuindice.summary.generated.resources.summary_statistics_current_graded_one
import tuindice.summary.generated.resources.summary_statistics_current_overdue_many
import tuindice.summary.generated.resources.summary_statistics_current_score
import tuindice.summary.generated.resources.summary_statistics_current_title
import tuindice.summary.generated.resources.summary_statistics_type_quiz
import tuindice.summary.generated.resources.summary_statistics_type_tooltip_quiz
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

// "Este trimestre": the rows of the evaluation types, the points evaluated and the evaluations that went by ungraded.
class SummaryStatisticsCurrentTermItemMapperTest {
	@Test
	fun when_theTermIsAbsent_then_thisTermInvitesToEnrollAndHasNoLink() {
		val current = statisticsHubOf(statistics = statisticsOf()).toSummaryStatisticsItem()!!.currentTerm

		assertEquals(UiText.Resource(Res.string.summary_statistics_current_title), current.title)
		assertNull(current.termLink)
		assertNull(current.termTarget)
	}

	@Test
	fun when_theEvaluationsAreNotDownloaded_then_thisTermInvitesToOpenThemThroughItsAction() {
		val statistics = statisticsOf().copy(currentTerm = currentTermOf(hasSynced = false))

		val current = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.currentTerm

		assertEquals(UiText.Raw("Sep - Dic 2026"), current.termLink)
		assertEquals(SummaryHubTarget.Record, current.termTarget)
		val content = assertIs<SummaryStatisticsCurrentTermItem.Content.NotSynced>(current.content)
		assertEquals(SummaryHubTarget.Evaluations, content.target)
	}

	@Test
	fun when_theEvaluationsAreDownloadedButTheTermHasNone_then_theBlockSaysSo() {
		val statistics = statisticsOf().copy(currentTerm = currentTermOf(hasSynced = true))

		val current = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.currentTerm

		assertIs<SummaryStatisticsCurrentTermItem.Content.NoEvaluations>(current.content)
	}

	@Test
	fun when_aTypeHasNoGrade_then_itShowsNoScore_andHasNoTooltip() {
		val statistics = statisticsOf().copy(
			currentTerm = currentTermOf(
				types = listOf(
					evaluationTypeOf(EvaluationType.TEST, earned = 0.0, evaluated = 0.0, graded = 0, total = 4)
				),
				fraction = 0.0
			)
		)

		val data = statisticsHubOf(statistics = statistics)
			.toSummaryStatisticsItem()!!
			.currentTerm
			.content as SummaryStatisticsCurrentTermItem.Content.Data
		val row = data.rows.single()

		assertEquals(UiText.Resource(Res.string.summary_points_none), row.scoreText)
		assertNull(row.tooltip)
	}

	@Test
	fun when_aTypeIsGraded_then_itsTooltipNamesItsPointsAndItsShare() {
		val statistics = statisticsOf().copy(
			currentTerm = currentTermOf(
				types = listOf(
					evaluationTypeOf(EvaluationType.QUIZ, earned = 8.0, evaluated = 10.0, graded = 1, total = 1)
				),
				fraction = 0.025
			)
		)

		val currentTerm = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.currentTerm
		val row = (currentTerm.content as SummaryStatisticsCurrentTermItem.Content.Data).rows.single()

		assertEquals(UiText.Resource(Res.string.summary_statistics_type_quiz), row.name)
		assertEquals(UiText.Resource(Res.string.summary_statistics_current_graded_one, listOf(1, 1)), row.gradedText)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_current_score, listOf("8", "10", UiText.Resource(Res.string.summary_percent_format, listOf(80)))),
			row.scoreText
		)
		assertEquals(
			UiText.Resource(Res.string.summary_statistics_type_tooltip_quiz, listOf("8", "10", UiText.Resource(Res.string.summary_percent_format, listOf(80)))),
			row.tooltip?.title
		)
	}

	@Test
	fun when_theTermHasSubjectsEvaluated_then_theFigureCountsThePointsOutOfTheirTotal() {
		val statistics = statisticsOf().copy(
			currentTerm = currentTermOf(
				types = listOf(
					evaluationTypeOf(EvaluationType.TEST, earned = 4.0, evaluated = 10.0, graded = 1, total = 2)
				),
				fraction = 0.025
			)
		)
		val hub = statisticsHubOf(
			statistics = statistics,
			term = progressTermOf(10.0, 0.0, null, null)
		)

		val content = hub.toSummaryStatisticsItem()!!.currentTerm.content
		val figure = (content as SummaryStatisticsCurrentTermItem.Content.Data).figure!!

		assertEquals("10", figure.value)
		assertEquals(UiText.Resource(Res.string.summary_statistics_current_figure, listOf(400)), figure.label)
		assertEquals(UiText.Resource(Res.string.summary_career_percent, listOf(3)), figure.trailing)
	}

	@Test
	fun when_evaluationsAreOverdue_then_thatLineCountsThemAndLeadsToTheOldestWeek() {
		val attention = ungradedAttentionOf(count = 2)
		val statistics = statisticsOf().copy(
			currentTerm = currentTermOf(types = oneGradelessType(), fraction = 0.0, overdue = 2)
		)
		val hub = statisticsHubOf(statistics = statistics, attention = listOf(attention))

		val content = hub.toSummaryStatisticsItem()!!.currentTerm.content
		val overdue = (content as SummaryStatisticsCurrentTermItem.Content.Data).overdue!!

		assertEquals(
			UiText.Resource(Res.string.summary_statistics_current_overdue_many, listOf(2)),
			overdue.text
		)
		assertEquals(
			SummaryHubTarget.EvaluationsWeek(
				weekStart = LocalDate(2026, 10, 12),
				evaluationId = attention.oldestEvaluationId
			),
			overdue.target
		)
	}

	@Test
	fun when_theHubHasNoOverdueLine_then_theOverdueLinkHasNoTarget() {
		val statistics = statisticsOf().copy(
			currentTerm = currentTermOf(types = oneGradelessType(), fraction = 0.0, overdue = 1)
		)

		val currentTerm = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.currentTerm
		val overdue = (currentTerm.content as SummaryStatisticsCurrentTermItem.Content.Data).overdue!!

		assertNull(overdue.target)
	}

	private fun oneGradelessType() = listOf(
		evaluationTypeOf(EvaluationType.TEST, earned = 0.0, evaluated = 0.0, graded = 0, total = 1)
	)
}
