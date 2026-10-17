package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.evaluationsWeekItemFixture
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluationsAssistantContextTest {
	private val weekOneStart = LocalDate(2026, 9, 28)

	@Test
	fun assistantContext_ofTheContentNamesTheSelectedWeekUnderItsSavedKey() {
		val content = Evaluations.State.Content(
			weekItem = evaluationsWeekItemFixture(),
			evaluationGroups = emptyList(),
			weekOneStart = weekOneStart
		)

		assertEquals(
			AssistantScreenContext(
				screen = AssistantScreen.EVALUATIONS,
				filters = mapOf("week" to "academic:2026-09-28:8")
			),
			content.assistantContext
		)
	}

	@Test
	fun assistantContext_ofTheContinuousWeekNamesItContinuous() {
		val content = Evaluations.State.Content(
			weekItem = evaluationsWeekItemFixture(),
			selectedWeekKey = EvaluationsWeekKey.Continuous,
			evaluationGroups = emptyList(),
			weekOneStart = weekOneStart
		)

		assertEquals(mapOf("week" to "continuous"), content.assistantContext.filters)
	}

	@Test
	fun assistantContext_ofAContentWithoutAnAnchorLeavesTheWeekOut() {
		val content = Evaluations.State.Content(
			weekItem = evaluationsWeekItemFixture(),
			evaluationGroups = emptyList()
		)

		assertEquals(AssistantScreenContext(screen = AssistantScreen.EVALUATIONS), content.assistantContext)
	}

	@Test
	fun assistantContext_ofTheStatesWithoutContentNamesTheScreenAlone() {
		val states = listOf(
			Evaluations.State.Idle,
			Evaluations.State.Loading,
			Evaluations.State.Empty(),
			Evaluations.State.Failed(message = "Error")
		)

		states.forEach { state ->
			assertEquals(AssistantScreenContext(screen = AssistantScreen.EVALUATIONS), state.assistantContext)
		}
	}
}
