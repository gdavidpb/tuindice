package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationListItem
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantChatFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantEvaluationCardRepository
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationRow
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

// A list of evaluations in the chat draws each evaluation the device has with the card of the Evaluations tab, read for
// the evaluations the conversation's lists name, and the ones the device lacks from what the chat carries.
class AssistantChatEvaluationCardsBehaviorTest {
	@Test
	fun aListOfEvaluations_drawsTheOnesTheDeviceHasWithTheTabsCards() = chatTest(withCards()) { fixture, viewModel ->
		open(fixture, viewModel)

		val list = viewModel.evaluationList()

		assertEquals(TAB_CARD, list.rows[0].deviceCard)
		assertNull(list.rows[1].deviceCard)
		assertEquals(listOf(setOf("ev-1", "ev-2")), fixture.evaluationCards.reads)
	}

	@Test
	fun theCardsAreNotReadAgain_whileTheListsStayTheSame() = chatTest(withCards()) { fixture, viewModel ->
		open(fixture, viewModel)

		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", Text("Aquí van."), Component(LIST)), agentMessage("a2", Text("¿Algo más?")))
		)

		assertEquals(TAB_CARD, viewModel.evaluationList().rows[0].deviceCard)
		assertEquals(1, fixture.evaluationCards.reads.size)
	}

	@Test
	fun aConversationWithoutListsOfEvaluations_readsNoCard() = chatTest(withCards()) { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("Hola"))))
		viewModel.openAction(null)

		assertEquals(emptyList(), fixture.evaluationCards.reads)
	}

	private fun withCards() = AssistantChatFixture().apply {
		evaluationCards = FakeAssistantEvaluationCardRepository(cards = mapOf("ev-1" to TAB_CARD))
	}

	private suspend fun open(fixture: AssistantChatFixture, viewModel: AssistantChatViewModel) {
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", Text("Aquí van."), Component(LIST)))
		)
		viewModel.openAction(null)
	}

	private fun AssistantChatViewModel.evaluationList(): EvaluationListItem {
		val agent = assertIs<ChatItem.Agent>(content().items.single { item -> item.id == "a1" })

		return assertIs<EvaluationListItem>(agent.parts.filterIsInstance<ChatPartItem.Component>().single().item)
	}

	private companion object {
		val LIST = EvaluationListComponent(
			id = "list-1",
			fallbackText = "Tus evaluaciones del trimestre",
			items = listOf(
				EvaluationRow(
					evaluationId = "ev-1",
					subjectCode = "CI-2611",
					title = "Quiz 1",
					scheduleMode = "dated",
					maxGrade = 20.0,
					state = "scheduled"
				),
				EvaluationRow(
					evaluationId = "ev-2",
					subjectCode = "IS-101",
					title = "Quiz 2",
					scheduleMode = "continuous",
					maxGrade = 20.0,
					state = "pending"
				)
			)
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
