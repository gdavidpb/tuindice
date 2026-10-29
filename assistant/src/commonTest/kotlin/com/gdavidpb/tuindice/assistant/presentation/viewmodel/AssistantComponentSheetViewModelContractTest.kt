package com.gdavidpb.tuindice.assistant.presentation.viewmodel

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantComponentSheet
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationListItem
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantComponentSheetFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.componentSheetTest
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantEvaluationCardRepository
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationRow
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.testkit.coroutines.withUnconfinedTestDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AssistantComponentSheetViewModelContractTest {
	@Test
	fun initialState_isIdle_untilTheSheetOpens() = runTest {
		withUnconfinedTestDispatchers { dispatchers ->
			val viewModel = AssistantComponentSheetFixture().viewModel(dispatchers)

			assertEquals(AssistantComponentSheet.State.Idle, viewModel.state.value)
		}
	}

	@Test
	fun when_theSheetOpens_then_itShowsTheComponentNamed() = componentSheetTest(
		initial = assistantConversation(
			items = listOf(
				agentMessage("message-1", Component(component = UnknownChatComponent(id = "component-1", fallbackText = "x")))
			)
		)
	) { _, viewModel ->
		viewModel.openAction(messageId = "message-1", componentId = "component-1")

		val content = assertIs<AssistantComponentSheet.State.Content>(viewModel.state.value)
		assertEquals("message-1", content.messageId)
	}

	@Test
	fun when_theSheetOfAListOpens_then_theEvaluationsTheDeviceHasAreDrawnWithTheTabsCards() = componentSheetTest(
		initial = assistantConversation(items = listOf(agentMessage("message-1", Component(component = LIST)))),
		evaluationCards = FakeAssistantEvaluationCardRepository(cards = mapOf("ev-1" to TAB_CARD))
	) { fixture, viewModel ->
		viewModel.openAction(messageId = "message-1", componentId = "list-1")

		val content = assertIs<AssistantComponentSheet.State.Content>(viewModel.state.value)
		val list = assertIs<EvaluationListItem>(content.item)

		assertEquals(TAB_CARD, list.rows[0].deviceCard)
		assertNull(list.rows[1].deviceCard)
		assertEquals(listOf(setOf("ev-1", "ev-2")), fixture.evaluationCards.reads)
	}

	private companion object {
		val LIST = EvaluationListComponent(
			id = "list-1",
			fallbackText = "Tus evaluaciones del trimestre",
			display = ComponentDisplay.SHEET,
			items = listOf(
				row(evaluationId = "ev-1", subjectCode = "CI-2611", title = "Quiz 1", state = "scheduled"),
				row(evaluationId = "ev-2", subjectCode = "IS-101", title = "Quiz 2", state = "pending")
			)
		)

		fun row(evaluationId: String, subjectCode: String, title: String, state: String) = EvaluationRow(
			evaluationId = evaluationId,
			subjectCode = subjectCode,
			title = title,
			scheduleMode = "dated",
			maxGrade = 20.0,
			state = state
		)

		val TAB_CARD = EvaluationCardItem(
			name = "QUIZ 1",
			subjectCode = "CI-2611",
			dateText = UiText.Raw("Miércoles 21/01/26"),
			highlightTone = EvaluationTone.Error,
			gradeText = "-- / 20",
			statusText = "Sin nota",
			statusTone = EvaluationTone.Error
		)
	}
}
