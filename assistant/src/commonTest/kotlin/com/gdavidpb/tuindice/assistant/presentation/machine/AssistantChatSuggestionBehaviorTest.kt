package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.pickSuggestionAction
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantSuggestionFacts
import com.gdavidpb.tuindice.chatcontract.context.ScreenId
import com.gdavidpb.tuindice.chatcontract.message.SuggestedReply
import com.gdavidpb.tuindice.chatcontract.message.Suggestions
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The questions of the shared catalog (SuggestedQuestions): the chat offers them when it starts and in guided mode.
private val generalQuestions = listOf(
	ChatSuggestionItem(id = "sq.about.what", label = "¿Qué puedes hacer por mí?")
)

private val summaryQuestions = listOf(
	ChatSuggestionItem(id = "sq.summary.term", label = "¿Cómo voy este trimestre?"),
	ChatSuggestionItem(id = "sq.summary.attention", label = "¿Qué necesita mi atención?"),
	ChatSuggestionItem(id = "sq.summary.pace", label = "¿Cuál es mi ritmo de créditos?")
)

private val summaryContext = AssistantScreenContext(
	screen = AssistantScreen.SUMMARY,
	suggestionFacts = AssistantSuggestionFacts(hasCurrentTerm = true, hasVisibleAttention = true, hasCreditPace = true)
)

class AssistantChatSuggestionBehaviorTest {
	@Test
	fun emptyConversation_offersTheThreeQuestionsOfItsScreen() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)

		viewModel.openAction(summaryContext)

		assertEquals(summaryQuestions, viewModel.content().suggestions)
	}

	@Test
	fun emptyConversationWithoutContext_offersTheGeneralQuestions() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)

		viewModel.openAction(null)

		assertEquals(generalQuestions, viewModel.content().suggestions)
	}

	@Test
	fun agentSuggestions_areNotOfferedInAnOpenChatWithMessages() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				agentMessage(
					"a1",
					Text("¿Quieres ver tu promedio?"),
					Suggestions(listOf(SuggestedReply(id = "s1", label = "Sí", text = "Sí, quiero verlo")))
				)
			)
		)

		viewModel.openAction(summaryContext)

		assertTrue(viewModel.content().suggestions.isEmpty())
	}

	@Test
	fun firstUserMessage_hidesTheQuestions_andAnEmptyConversationOffersThemAgain() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(summaryContext)
		assertEquals(summaryQuestions, viewModel.content().suggestions)

		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(AssistantConversationItem.User(AssistantUserMessage(id = "u1", text = "Hola", createdAtMillis = 0)))
		)
		assertTrue(viewModel.content().suggestions.isEmpty())

		fixture.conversation.conversation.value = assistantConversation()
		assertEquals(summaryQuestions, viewModel.content().suggestions)
	}

	@Test
	fun guidedAndEmptyConversation_offersTheQuestionsOfItsScreen() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.availability.availability.value =
			AssistantAvailability(mode = AssistantMode.GUIDED, serverDisabled = false)

		viewModel.openAction(summaryContext)

		val content = viewModel.content()
		assertTrue(content.isGuided)
		assertEquals(summaryQuestions, content.suggestions)
	}

	@Test
	fun guidedWithAgentMessageWithoutSuggestions_offersTheQuestionsOfItsScreen() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.availability.availability.value =
			AssistantAvailability(mode = AssistantMode.GUIDED, serverDisabled = false)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", Text("Elige una de estas preguntas.")))
		)

		viewModel.openAction(summaryContext)

		assertEquals(summaryQuestions, viewModel.content().suggestions)
	}

	@Test
	fun guidedWithAgentRepliesOutsideTheCatalog_offersTheQuestionsOfItsScreen() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.availability.availability.value =
			AssistantAvailability(mode = AssistantMode.GUIDED, serverDisabled = false)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				agentMessage(
					"a1",
					Text("¿Quieres ver tu promedio?"),
					Suggestions(listOf(SuggestedReply(id = "s1", label = "Sí", text = "Sí, quiero verlo")))
				)
			)
		)

		viewModel.openAction(summaryContext)

		assertEquals(summaryQuestions, viewModel.content().suggestions)
	}

	@Test
	fun guidedFollowUp_usesTheCatalogLabelAndTextEvenIfTheAgentReplyDiffers() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.availability.availability.value =
			AssistantAvailability(mode = AssistantMode.GUIDED, serverDisabled = false)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				agentMessage(
					"a1",
					Text("Listo."),
					Suggestions(listOf(SuggestedReply(id = "sq.summary.term", label = "Otra etiqueta", text = "Otro texto")))
				)
			)
		)

		viewModel.openAction(summaryContext)
		assertEquals(listOf(summaryQuestions.first()), viewModel.content().suggestions)
		viewModel.pickSuggestionAction("sq.summary.term")

		assertEquals("¿Cómo voy este trimestre?", fixture.turns.requests.single().message.text)
		assertEquals("sq.summary.term", fixture.turns.requests.single().message.suggestionId)
	}

	@Test
	fun agentMessageWithoutSuggestions_offersNoneWhenNotGuided() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", Text("Listo.")))
		)

		viewModel.openAction(summaryContext)

		assertEquals(emptyList(), viewModel.content().suggestions)
	}

	@Test
	fun runningTurn_hidesTheSuggestions() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		// The turn keeps running: its stream stays open, as a running turn's does, until its final event.
		fixture.turns.holdEvents = true
		fixture.conversation.conversation.value = assistantConversation(
			activeTurn = AssistantActiveTurn(
				turnId = "t1",
				lastSeq = 3,
				progressLabel = "Revisando tu pensum…",
				status = AssistantTurnPhase.RUNNING
			)
		)

		viewModel.openAction(summaryContext)

		assertEquals(emptyList(), viewModel.content().suggestions)
	}

	@Test
	fun pickSuggestion_ofTheScreen_sendsItsTextAndItsId() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(summaryContext)

		viewModel.pickSuggestionAction("sq.summary.pace")

		val request = fixture.turns.requests.single()
		assertEquals("¿Cuál es mi ritmo de créditos?", request.message.text)
		assertEquals("sq.summary.pace", request.message.suggestionId)
		assertEquals(ScreenId.SUMMARY, request.screenContext?.screen)
	}

	@Test
	fun pickSuggestion_fromTheCatalogButNotShown_sendsItsText() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(AssistantScreenContext(AssistantScreen.SUMMARY))

		viewModel.pickSuggestionAction("sq.about.what")

		val request = fixture.turns.requests.single()
		assertEquals("¿Qué puedes hacer por mí?", request.message.text)
		assertEquals("sq.about.what", request.message.suggestionId)
	}
}
