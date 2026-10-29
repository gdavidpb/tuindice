package com.gdavidpb.tuindice.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.presentation.model.TopBarSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TopBarActionsArrangementTest {

	@Test
	fun when_evaluationsHasItsActionAndNoAssistant_then_theActionIsInView() {
		val arrangement = arrangeTopBarActions(
			actions = listOf(TopBarAction.EvaluationsScheduleAction),
			hasAssistant = false,
			hasViewModeSelector = false
		)

		assertEquals(listOf(action(TopBarAction.EvaluationsScheduleAction)), arrangement.first)
		assertEquals(emptyList(), arrangement.second)
	}

	@Test
	fun when_evaluationsHasItsActionAndTheAssistant_then_theAssistantLeadsAndTheActionFollows() {
		val arrangement = arrangeTopBarActions(
			actions = listOf(TopBarAction.EvaluationsScheduleAction),
			hasAssistant = true,
			hasViewModeSelector = false
		)

		assertEquals(
			listOf(TopBarSlot.Assistant, action(TopBarAction.EvaluationsScheduleAction)),
			arrangement.first
		)
		assertEquals(emptyList(), arrangement.second)
	}

	@Test
	fun when_pensumHasItsActionAndTheAssistant_then_bothAreInView() {
		val arrangement = arrangeTopBarActions(
			actions = listOf(TopBarAction.SearchPensumAction),
			hasAssistant = true,
			hasViewModeSelector = false
		)

		assertEquals(
			listOf(TopBarSlot.Assistant, action(TopBarAction.SearchPensumAction)),
			arrangement.first
		)
		assertEquals(emptyList(), arrangement.second)
	}

	@Test
	fun when_recordHasAScheduleAndTheAssistant_then_theSwitchFollowsTheAssistant_andTheTermAndScheduleOverflow() {
		val arrangement = arrangeTopBarActions(
			actions = listOf(TopBarAction.RecordTermSelectionAction, TopBarAction.RecordScheduleAction),
			hasAssistant = true,
			hasViewModeSelector = true
		)

		assertEquals(listOf(TopBarSlot.Assistant, TopBarSlot.ViewModeSelector), arrangement.first)
		assertEquals(
			listOf(TopBarAction.RecordTermSelectionAction, TopBarAction.RecordScheduleAction),
			arrangement.second
		)
	}

	@Test
	fun when_recordHasAScheduleAndTheAssistantIsOff_then_thePlacesAreAllInView() {
		val arrangement = arrangeTopBarActions(
			actions = listOf(TopBarAction.RecordTermSelectionAction, TopBarAction.RecordScheduleAction),
			hasAssistant = false,
			hasViewModeSelector = true
		)

		assertEquals(
			listOf(
				TopBarSlot.ViewModeSelector,
				action(TopBarAction.RecordTermSelectionAction),
				action(TopBarAction.RecordScheduleAction)
			),
			arrangement.first
		)
		assertEquals(emptyList(), arrangement.second)
	}

	@Test
	fun when_recordHasNoScheduleAndTheAssistant_then_threePlacesAreAllInView() {
		val arrangement = arrangeTopBarActions(
			actions = listOf(TopBarAction.RecordTermSelectionAction),
			hasAssistant = true,
			hasViewModeSelector = true
		)

		assertEquals(
			listOf(
				TopBarSlot.Assistant,
				TopBarSlot.ViewModeSelector,
				action(TopBarAction.RecordTermSelectionAction)
			),
			arrangement.first
		)
		assertEquals(emptyList(), arrangement.second)
	}

	@Test
	fun when_fourActionsAndNoAssistant_then_twoAreInViewAndTheRestOverflow() {
		val arrangement = arrangeTopBarActions(
			actions = listOf(
				TopBarAction.EvaluationsScheduleAction,
				TopBarAction.SearchPensumAction,
				TopBarAction.ChangePensumAction,
				TopBarAction.StatisticsShareAction
			),
			hasAssistant = false,
			hasViewModeSelector = false
		)

		assertEquals(
			listOf(action(TopBarAction.EvaluationsScheduleAction), action(TopBarAction.SearchPensumAction)),
			arrangement.first
		)
		assertEquals(
			listOf(TopBarAction.ChangePensumAction, TopBarAction.StatisticsShareAction),
			arrangement.second
		)
	}

	@Test
	fun when_theScreenHasNoActions_then_onlyTheAssistantIsInView() {
		val arrangement = arrangeTopBarActions(
			actions = emptyList(),
			hasAssistant = true,
			hasViewModeSelector = false
		)

		assertEquals(listOf(TopBarSlot.Assistant), arrangement.first)
		assertEquals(emptyList(), arrangement.second)
	}

	@Test
	fun when_eachTabIsArranged_then_theAssistantLeadsOnlyWhenItIsOn() {
		val tabs = listOf(
			TopBarConfig.Record,
			TopBarConfig.RecordWithSchedule,
			TopBarConfig.EvaluationsWithSchedule,
			TopBarConfig.Pensum,
			TopBarConfig.Statistics
		)

		tabs.forEach { config ->
			val withAssistant = arrangeTopBarActions(
				actions = config.actions,
				hasAssistant = true,
				hasViewModeSelector = false
			).first
			val withoutAssistant = arrangeTopBarActions(
				actions = config.actions,
				hasAssistant = false,
				hasViewModeSelector = false
			).first

			assertEquals(TopBarSlot.Assistant, withAssistant.first())
			assertFalse(withoutAssistant.contains(TopBarSlot.Assistant))
		}
	}

	private fun action(action: TopBarAction): TopBarSlot = TopBarSlot.Action(action)
}
