package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantChatFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.acceptConsentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.changeTextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.intentCard
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.pickSuggestionAction
import com.gdavidpb.tuindice.assistant.presentation.testing.retryTurnAction
import com.gdavidpb.tuindice.assistant.presentation.testing.sendMessageAction
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.assistant.testing.assistantStatus
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import com.gdavidpb.tuindice.chatcontract.message.SuggestedReply
import com.gdavidpb.tuindice.chatcontract.message.Suggestions
import com.gdavidpb.tuindice.chatcontract.message.Text
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AssistantChatPhotoBehaviorTest {
	@Test
	fun when_aPhotoIsRead_then_theProposalAndItsIntentReachTheChat() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		val intent = assistantIntent(
			id = "i-photo",
			args = RememberArgs(statement = "Parcial 1", category = "evaluaciones", quote = "parcial")
		)
		fixture.extraction.result = ExtractionResult(message = "Encontré una evaluación.", intent = intent)

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))

		val content = viewModel.content()
		assertEquals(listOf("Encontré una evaluación."), content.agentTexts())
		assertEquals(IntentCardStatus.Open, content.intentCard("i-photo").status)
		assertTrue(content.composer.isPhotoEnabled)
	}

	@Test
	fun when_aPhotoIsBeingRead_then_thePhotoButtonIsOffAndNothingOpens() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		val gate = CompletableDeferred<Unit>()
		fixture.extraction.gate = gate

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))

		val reading = viewModel.content()
		assertTrue(reading.isExtracting)
		assertFalse(reading.composer.isPhotoEnabled)

		viewModel.dispatch(AssistantChat.Action.RequestPhoto(fromCamera = true))
		assertFalse(fixture.effects.contains(AssistantChat.Effect.OpenCamera))

		gate.complete(Unit)

		val read = viewModel.content()
		assertEquals(null, read.progressLabel)
		assertFalse(read.isExtracting)
		assertTrue(read.composer.isPhotoEnabled)
	}

	@Test
	fun when_theGalleryIsRequested_then_thePickerOpens() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		viewModel.dispatch(AssistantChat.Action.RequestPhoto(fromCamera = false))

		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.OpenPhotoPicker), fixture.effects)
	}

	@Test
	fun when_theQuotaIsExhaustedByAPhoto_then_theQuotaNoticeShows() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.extraction.failure = AssistantRequestRefusedException(
			code = AssistantErrorCodes.QUOTA_EXHAUSTED,
			retryAfterSeconds = 60,
			message = "quota"
		)

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))

		assertEquals(ChatNoticeItem.QuotaExhausted(message = "quota:60"), viewModel.content().notice)
	}

	@Test
	fun when_theNetworkFailsOnAPhoto_then_theSendFailedNoticeShowsWithoutARetry() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.extraction.failure = IllegalStateException("offline")

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))

		assertEquals(
			ChatNoticeItem.TurnFailed(message = "send-failed", canRetry = false),
			viewModel.content().notice
		)
	}

	@Test
	fun when_theServiceSaysGuidedOnly_then_theChatIsGuidedAndNoPhotoIsRequested() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		// The service that refuses the photo as guided-only says so when the status is read again.
		fixture.standing.answer = assistantStatus().copy(mode = "guided")
		fixture.extraction.failure = AssistantRequestRefusedException(
			code = AssistantErrorCodes.GUIDED_ONLY,
			retryAfterSeconds = null,
			message = "guided"
		)

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		viewModel.dispatch(AssistantChat.Action.RequestPhoto(fromCamera = false))

		val content = viewModel.content()
		assertTrue(content.isGuided)
		assertFalse(content.composer.isPhotoEnabled)
		assertEquals(ChatNoticeItem.Guided(message = "guided"), content.notice)
		assertFalse(fixture.effects.contains(AssistantChat.Effect.OpenPhotoPicker))
	}

	// The sheet left for the consent keeps no reading of the photo: the reading ended when the sheet was left.
	@Test
	fun when_aPhotoNeedsConsent_then_comingBackShowsNoReadingThatEnded() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.extraction.failure = AssistantRequestRefusedException(
			code = AssistantErrorCodes.CONSENT_REQUIRED,
			retryAfterSeconds = null,
			message = "consent"
		)

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)

		viewModel.acceptConsentAction()

		val content = viewModel.content()
		assertFalse(content.isExtracting)
		assertTrue(content.composer.isPhotoEnabled)
	}

	@Test
	fun when_theServiceSaysGuidedOnlyForAPhoto_then_theStatusIsReadAgain() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.extraction.failure = AssistantRequestRefusedException(
			code = AssistantErrorCodes.GUIDED_ONLY,
			retryAfterSeconds = null,
			message = "guided"
		)
		val readsBefore = fixture.standing.statusReads

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))

		assertEquals(readsBefore + 1, fixture.standing.statusReads)
	}

	// The send waits for the reading of a photo: the student's text stays in the composer, and it is sent once the
	// photo is read.
	@Test
	fun aPhotoBeingRead_offersNoSend_andTheSendIsOnAgainWhenTheReadingEnds() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		viewModel.changeTextAction("hola")
		val reading = CompletableDeferred<Unit>()
		fixture.extraction.gate = reading

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		assertFalse(viewModel.content().composer.isSendEnabled)
		viewModel.sendMessageAction()
		assertTrue(fixture.turns.requests.isEmpty())

		reading.complete(Unit)
		assertTrue(viewModel.content().composer.isSendEnabled)
	}

	// A photo that is not read is not the failure of a text turn: the failure of that turn stays, with its retry.
	@Test
	fun aPhotoNotRead_leavesTheFailureOfATextTurnAndItsRetry() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		fixture.extraction.failure = IllegalStateException("offline")
		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)
	}

	// A suggestion is not sent while a photo is read, as a message is not: the reply the student taps
	// waits for the reading.
	@Test
	fun aPickedSuggestion_isNotSent_whileAPhotoIsBeingRead() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(suggestingAgent()))
		viewModel.openAction(null)
		val reading = CompletableDeferred<Unit>()
		fixture.extraction.gate = reading

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		viewModel.pickSuggestionAction("s1")

		assertTrue(fixture.turns.requests.isEmpty())
		reading.complete(Unit)
	}

	// The suggestions are not offered while a photo is read: the send they start waits for the reading, and they come back
	// once it ends.
	@Test
	fun aPhotoBeingRead_offersNoSuggestions_andTheyComeBackWhenItEnds() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(suggestingAgent()))
		viewModel.openAction(null)
		val reading = CompletableDeferred<Unit>()
		// A reading that fails adds no answer to the conversation, so the suggestions of its last answer come back.
		fixture.extraction.failure = IllegalStateException("offline")
		fixture.extraction.gate = reading

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		assertTrue(viewModel.content().suggestions.isEmpty())

		reading.complete(Unit)
		assertTrue(viewModel.content().suggestions.isNotEmpty())
	}

	// The notice does not offer the retry while a photo is read: the retry waits for the reading, so its button would do
	// nothing. It is offered again once the reading ends.
	@Test
	fun aPhotoBeingRead_offersNoRetry_andItIsOfferedAgainWhenItEnds() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(suggestingAgent()))
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.openAction(null)
		viewModel.pickSuggestionAction("s1")
		assertTrue(assertIs<ChatNoticeItem.TurnFailed>(viewModel.content().notice).canRetry)
		val reading = CompletableDeferred<Unit>()
		fixture.extraction.gate = reading

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		assertFalse(assertIs<ChatNoticeItem.TurnFailed>(viewModel.content().notice).canRetry)

		reading.complete(Unit)
		assertTrue(assertIs<ChatNoticeItem.TurnFailed>(viewModel.content().notice).canRetry)
	}

	// A retry is not sent while a photo is read either: the failed message waits for the reading to end.
	@Test
	fun aRetry_isNotSent_whileAPhotoIsBeingRead() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.turns.sendError = AssistantRequestRefusedException("unknown", null, "unknown")
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		fixture.turns.sendError = null
		val reading = CompletableDeferred<Unit>()
		fixture.extraction.gate = reading

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		viewModel.retryTurnAction()

		assertEquals(1, fixture.turns.requests.size)
		reading.complete(Unit)
	}

	private suspend fun openChat(fixture: AssistantChatFixture, viewModel: AssistantChatViewModel) {
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
	}

	private fun photo() = PlatformFile("assistant-plan.jpg")

	private fun suggestingAgent() = agentMessage(
		"a1",
		Text("¿Quieres ver tu promedio?"),
		Suggestions(listOf(SuggestedReply(id = "s1", label = "Sí", text = "Sí, quiero verlo")))
	)

	private fun AssistantChat.State.Content.agentTexts(): List<String> {
		return items
			.filterIsInstance<ChatItem.Agent>()
			.flatMap { agent -> agent.parts }
			.filterIsInstance<ChatPartItem.Text>()
			.map { part -> part.text }
	}
}
