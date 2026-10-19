package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.PlanComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanEvaluationComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanListComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanOption
import com.gdavidpb.tuindice.chatcontract.component.PlanRow
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_count_subject_many
import tuindice.assistant.generated.resources.assistant_card_b_count_subject_one
import tuindice.assistant.generated.resources.assistant_card_b_credits
import tuindice.assistant.generated.resources.assistant_card_b_joined
import tuindice.assistant.generated.resources.assistant_card_b_plan_clashes
import tuindice.assistant.generated.resources.assistant_card_b_plan_clashes_unchecked
import tuindice.assistant.generated.resources.assistant_card_b_plan_unlocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlanComponentItemMappingTest {
	@Test
	fun planList_linksEachTermToItsRecord_withTheTermKey() {
		val item = PlanListComponent(
			id = "pl1",
			fallbackText = "Planes",
			plans = listOf(PlanRow("2026-JAN_MAR", "2026-JAN_MAR", listOf("IS-101", "IS-102"), credits = 8))
		).toPlanListItem()

		assertEquals(listOf(EntityRef(EntityKind.TERM, "2026-JAN_MAR")), item.rows.single().link.entityRefs)
		val subjects = chatCardCountText(2, Res.string.assistant_card_b_count_subject_one, Res.string.assistant_card_b_count_subject_many)
		assertEquals(
			UiText.Resource(
				Res.string.assistant_card_b_joined,
				listOf(subjects, UiText.Resource(Res.string.assistant_card_b_credits, listOf(8)))
			),
			item.rows.single().summaryText
		)
	}

	@Test
	fun planEvaluation_clashesNotCountedYetReadAsUnchecked_andTheBandIsTheFigure() {
		val unchecked = PlanEvaluationComponent(
			id = "pe1",
			fallbackText = "Plan",
			termLabel = "2026-JAN_MAR",
			credits = 8,
			loadBand = "Exigente",
			loadIndex = null,
			clashes = 0
		).toPlanEvaluationItem()
		val checked = PlanEvaluationComponent(
			id = "pe2",
			fallbackText = "Plan",
			termLabel = "2026-JAN_MAR",
			credits = 8,
			loadBand = "  ",
			loadIndex = 2.5,
			clashes = 1
		).toPlanEvaluationItem()

		assertEquals(UiText.Resource(Res.string.assistant_card_b_plan_clashes_unchecked), unchecked.clashesName)
		assertNull(unchecked.clashesValueText)
		assertEquals(UiText.Resource(Res.string.assistant_card_b_plan_clashes), checked.clashesName)
		assertEquals("1", checked.clashesValueText)
		assertEquals(UiText.Raw("Exigente"), unchecked.primaryFigure?.value)
		assertEquals(UiText.Raw(2.5.toCardIndexText()), checked.primaryFigure?.value)
		assertNull(checked.secondaryFigure)
	}

	@Test
	fun planComparison_writesTheLoadBeforeTheCredits_andTheUnlocksOnlyWhenThereAreSome() {
		val item = PlanComparisonComponent(
			id = "pc1",
			fallbackText = "Comparación",
			options = listOf(
				PlanOption("Plan A", credits = 12, loadBand = "Exigente", unlocks = listOf("IS-201", "IS-202"), clashes = 0),
				PlanOption("Plan B", credits = 9, loadBand = "", unlocks = emptyList(), clashes = 0)
			)
		).toPlanComparisonItem()

		assertEquals(UiText.Resource(Res.string.assistant_card_b_plan_unlocks, listOf("IS-201, IS-202")), item.options[0].unlocksText)
		assertNull(item.options[1].unlocksText)
		assertEquals(UiText.Resource(Res.string.assistant_card_b_credits, listOf(9)), item.options[1].summaryText)
	}
}
