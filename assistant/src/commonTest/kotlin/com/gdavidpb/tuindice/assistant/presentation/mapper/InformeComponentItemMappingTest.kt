package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.IndexGoalComponent
import com.gdavidpb.tuindice.chatcontract.component.IndexPoint
import com.gdavidpb.tuindice.chatcontract.component.IndexSeriesComponent
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.RecordStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.TermComponent
import com.gdavidpb.tuindice.chatcontract.component.TermSubjectRow
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_goal_feasible
import tuindice.assistant.generated.resources.assistant_card_a_goal_not_feasible
import tuindice.assistant.generated.resources.assistant_card_a_link_record
import tuindice.assistant.generated.resources.assistant_card_a_series_best
import tuindice.assistant.generated.resources.assistant_card_a_series_worst
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InformeComponentItemMappingTest {
	@Test
	fun term_linksTheRecordOnlyWhenTheCardNamesTheTerm_andShowsTheResultOnlyWithoutAGrade() {
		val item = TermComponent(
			id = "t1",
			fallbackText = "Trimestre",
			entityRefs = listOf(EntityRef(EntityKind.TERM, "term-1")),
			termId = "term-1",
			termLabel = "2026-JAN_MAR",
			termIndex = 3.8125,
			cumulativeIndex = null,
			credits = 18,
			rows = listOf(
				TermSubjectRow("a1", "IS-101", "Cálculo", 4, grade = 15, outcome = "approved"),
				TermSubjectRow("a2", "IS-102", "Física", 4, grade = null, outcome = "pending", badge = "equivalence")
			)
		).toTermItem()

		assertEquals(
			CardLinkItem(
				target = NavTarget.RECORD,
				text = UiText.Resource(Res.string.assistant_card_a_link_record),
				entityRefs = listOf(EntityRef(EntityKind.TERM, "term-1"))
			),
			item.recordLink
		)
		assertEquals("15", item.rows[0].gradeText)
		assertNull(item.rows[0].outcomeText)
		assertNull(item.rows[1].gradeText)
		assertEquals(attemptOutcomeText("pending"), item.rows[1].outcomeText)
		assertEquals(attemptBadgeText("equivalence"), item.rows[1].badgeText)
	}

	@Test
	fun term_withoutItsOwnRef_hasNoLink_andTheCumulativeIndexIsTheFigureWhenThereIsNoTermIndex() {
		val item = TermComponent(
			id = "t1",
			fallbackText = "Trimestre",
			entityRefs = emptyList(),
			termId = "term-1",
			termLabel = "2026-JAN_MAR",
			termIndex = null,
			cumulativeIndex = 16.4,
			credits = 18,
			rows = emptyList()
		).toTermItem()

		assertNull(item.recordLink)
		assertNull(item.secondaryFigure)
		assertEquals(UiText.Raw(16.4.toCardIndexText()), item.primaryFigure?.value)
	}

	@Test
	fun indexSeries_marksTheBestAndTheWorstTerm_andTheFigureIsTheLatestCumulativeIndex() {
		val item = IndexSeriesComponent(
			id = "s1",
			fallbackText = "Serie",
			points = listOf(
				IndexPoint("t1", "2025-SEP_DIC", 15.0, 15.0),
				IndexPoint("t2", "2026-JAN_MAR", 17.5, 16.2),
				IndexPoint("t3", "2026-APR_JUN", null, null)
			),
			bestTermId = "t2",
			worstTermId = "t1"
		).toIndexSeriesItem()

		assertEquals(UiText.Resource(Res.string.assistant_card_a_series_worst), item.points[0].markText)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_series_best), item.points[1].markText)
		assertNull(item.points[2].markText)
		assertEquals(UiText.Raw(16.2.toCardIndexText()), item.currentFigure?.value)
	}

	@Test
	fun recordStats_fillsEachBarOfTheLongestCount_andLeavesAnEmptyHistogramBlank() {
		val item = RecordStatsComponent(
			id = "r1",
			fallbackText = "Estadísticas",
			approved = 10,
			failed = 1,
			retired = 0,
			approvedFirstTry = 8,
			repeated = 2,
			histogram = listOf(0, 2, 4)
		).toRecordStatsItem()

		assertEquals(listOf("0", "2", "4"), item.histogram.map { bar -> bar.count })
		assertEquals(listOf(0f, 0.5f, 1f), item.histogram.map { bar -> bar.fraction })
		assertEquals(4, item.counts.size)

		val empty = RecordStatsComponent(
			id = "r2",
			fallbackText = "Estadísticas",
			approved = 0,
			failed = 0,
			retired = 0,
			approvedFirstTry = 0,
			repeated = 0,
			histogram = listOf(0, 0)
		).toRecordStatsItem()

		assertEquals(listOf(0f, 0f), empty.histogram.map { bar -> bar.fraction })
	}

	@Test
	fun indexGoal_saysWhetherTheGoalIsReachable() {
		val reachable = IndexGoalComponent(
			id = "g1",
			fallbackText = "Meta",
			targetCumulative = 16.0,
			credits = 12,
			requiredTermIndex = 17.5,
			feasible = true
		).toIndexGoalItem()
		val unreachable = IndexGoalComponent(
			id = "g1",
			fallbackText = "Meta",
			targetCumulative = 16.0,
			credits = 12,
			requiredTermIndex = null,
			feasible = false
		).toIndexGoalItem()

		assertEquals(UiText.Resource(Res.string.assistant_card_a_goal_feasible), reachable.feasibilityText)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_goal_not_feasible), unreachable.feasibilityText)
	}
}
