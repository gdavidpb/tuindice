package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantInputLimits
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.bubbles
import com.gdavidpb.tuindice.assistant.presentation.testing.cancelTurnAction
import com.gdavidpb.tuindice.assistant.presentation.testing.changeTextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.pickSuggestionAction
import com.gdavidpb.tuindice.assistant.presentation.testing.removeContextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.retryTurnAction
import com.gdavidpb.tuindice.assistant.presentation.testing.sendMessageAction
import com.gdavidpb.tuindice.assistant.testing.assistantStatus
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.message.SuggestedReply
import com.gdavidpb.tuindice.chatcontract.message.Suggestions
import com.gdavidpb.tuindice.chatcontract.message.Text
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_restricted_cooldown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AssistantChatTurnBehaviorTest {
	@Test
	fun send_startsTheTurnWithItsContext_clearsTheDraft_andFollowsTheTurn() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 4)
		fixture.turns.events = listOf(RunFinished(seq = 5, turnId = "t1"))
		val context = AssistantScreenContext(AssistantScreen.EVALUATIONS, visibleFields = mapOf("subject" to "CI4325"))
		viewModel.openAction(context)

		viewModel.changeTextAction("  ¿Cuál es mi promedio?  ")
		viewModel.sendMessageAction()

		val request = fixture.turns.requests.single()
		assertEquals("¿Cuál es mi promedio?", request.message.text)
		assertEquals(mapOf("subject" to "CI4325"), request.screenContext?.visibleFields)
		assertEquals(listOf("t1" to 4L), fixture.turns.followed)
		assertEquals("", viewModel.content().composer.text)
		assertNull(viewModel.content().notice)
	}

	@Test
	fun quotaExhausted_marksTheMessageFailed_keepsTheText_andSaysWhenItReturns() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.QUOTA_EXHAUSTED, 90, "quota")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertEquals(ChatNoticeItem.QuotaExhausted(message = "quota:90"), content.notice)
		assertEquals(listOf(true), content.bubbles().map { bubble -> bubble.isFailed })
		assertEquals("hola", content.composer.text)
	}

	@Test
	fun theStudentsEdits_keepTheTextRevision_andTheSendThatClearsTheText_movesIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		val opened = viewModel.content().composer.textRevision

		viewModel.changeTextAction("hola")
		assertEquals(opened, viewModel.content().composer.textRevision)

		viewModel.sendMessageAction()
		val cleared = viewModel.content().composer

		assertEquals("", cleared.text)
		assertTrue(cleared.textRevision > opened)
	}

	@Test
	fun aRefusedSend_givesTheTextBack_underANewTextRevision() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.QUOTA_EXHAUSTED, 90, "quota")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		val typed = viewModel.content().composer.textRevision

		viewModel.sendMessageAction()
		val restored = viewModel.content().composer

		// The send cleared it and the refusal gave it back: two replacements, so the field adopts «hola» again even
		// though the student typed it before.
		assertEquals("hola", restored.text)
		assertEquals(typed + 2, restored.textRevision)
	}

	@Test
	fun otherFailure_offersARetry_andRetryResendsTheFailedText() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException("unknown", null, "unknown")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertEquals(ChatNoticeItem.TurnFailed(message = "send-failed", canRetry = true), viewModel.content().notice)

		// The retry is answered by a turn that finishes: its final event comes after the accepted turn.
		fixture.turns.sendError = null
		fixture.turns.events = listOf(RunFinished(seq = 1, turnId = "t1"))
		viewModel.retryTurnAction()

		assertEquals(listOf("hola", "hola"), fixture.turns.requests.map { request -> request.message.text })
		assertNull(viewModel.content().notice)
	}

	@Test
	fun retryOfAFailedSend_resendsTheSameClientTurnId_andLeavesOneBubble() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException("unknown", null, "unknown")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		fixture.turns.sendError = null
		viewModel.retryTurnAction()

		val ids = fixture.turns.requests.map { request -> request.clientTurnId }
		assertEquals(2, ids.size)
		assertEquals(ids.first(), ids.last())
		assertEquals(listOf(false), viewModel.content().bubbles().map { bubble -> bubble.isFailed })
	}

	@Test
	fun anUnconfirmedTurn_offersARetryThatResendsTheSameClientTurnId() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				AssistantConversationItem.User(
					AssistantUserMessage(id = "client-1", text = "hola", createdAtMillis = 1L)
				)
			),
			activeTurn = AssistantActiveTurn(
				turnId = "t1",
				lastSeq = 3,
				progressLabel = null,
				status = AssistantTurnPhase.UNCONFIRMED
			)
		)
		viewModel.openAction(null)

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		viewModel.retryTurnAction()

		val request = fixture.turns.requests.single()
		assertEquals("client-1", request.clientTurnId)
		assertEquals("hola", request.message.text)
	}

	// The retry of an unconfirmed turn sends the message that turn answers, as it was sent. A message the student writes
	// while the turn is still unconfirmed is stored as the last message, and the retry does not send that one.
	@Test
	fun aRetryOfAnUnconfirmedTurn_resendsItsOwnMessage_notTheOneStoredAfterIt() = chatTest { fixture, viewModel ->
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
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		viewModel.pickSuggestionAction("s1")
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		fixture.conversation.addUserMessage(AssistantUserMessage(id = "client-2", text = "adiós", createdAtMillis = 2L))
		viewModel.retryTurnAction()

		assertEquals(2, fixture.turns.requests.size)
		val sent = fixture.turns.requests.first()
		val retried = fixture.turns.requests.last()
		assertEquals("Sí, quiero verlo", retried.message.text)
		assertEquals("s1", retried.message.suggestionId)
		assertEquals(sent.clientTurnId, retried.clientTurnId)
	}

	// A message the server refused keeps its own retry: the conversation observed again does not hand the retry to the
	// answer before it, so the retry sends the refused message with its suggestion. A new message waits for the answer
	// before it to be recovered, so that answer is confirmed before the message is refused.
	@Test
	fun aRefusedMessage_keepsItsRetry_afterTheUnconfirmedAnswerIsRecovered() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				agentMessage(
					"a1",
					Text("¿Quieres ver tu promedio?"),
					Suggestions(
						listOf(
							SuggestedReply(id = "s1", label = "Sí", text = "Sí, quiero verlo"),
							SuggestedReply(id = "s2", label = "No", text = "No, gracias")
						)
					)
				)
			)
		)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		viewModel.pickSuggestionAction("s1")

		// The answer to «Sí, quiero verlo» is recovered first: its final event comes, and «No, gracias» is sent after it.
		fixture.turns.scriptedFollows.add(listOf(RunFinished(seq = 1, turnId = "t1")))
		fixture.turns.sendError = AssistantRequestRefusedException("unknown", null, "unknown")
		viewModel.pickSuggestionAction("s2")

		fixture.conversation.conversation.value = fixture.conversation.conversation.value.copy(conversationId = "c2")
		fixture.turns.sendError = null
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t2", conversationId = "c1", lastSeq = 0)
		viewModel.retryTurnAction()

		val retried = fixture.turns.requests.last()
		assertEquals("No, gracias", retried.message.text)
		assertEquals("s2", retried.message.suggestionId)
	}

	// While an unconfirmed turn is retried, its notice is not shown and its retry is not offered: the send is on its way.
	@Test
	fun aRetryOfAnUnconfirmedTurn_offersNoNotice_whileTheSendIsOnItsWay() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				AssistantConversationItem.User(
					AssistantUserMessage(id = "client-1", text = "hola", createdAtMillis = 1L)
				)
			),
			activeTurn = AssistantActiveTurn(
				turnId = "t1",
				lastSeq = 3,
				progressLabel = null,
				status = AssistantTurnPhase.UNCONFIRMED
			)
		)
		viewModel.openAction(null)
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		fixture.gatedTurns.gated = true
		viewModel.retryTurnAction()

		val during = viewModel.content()
		assertTrue(during.isSending)
		assertNull(during.notice)

		fixture.gatedTurns.release.complete(Unit)

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)
	}

	@Test
	fun aRunErrorKeepsTheSuggestionOfTheTurn_forItsRetry() = chatTest { fixture, viewModel ->
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
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.openAction(null)

		viewModel.pickSuggestionAction("s1")
		viewModel.retryTurnAction()

		assertEquals(listOf("s1", "s1"), fixture.turns.requests.map { request -> request.message.suggestionId })
	}

	@Test
	fun anOtherRunErrorThatCannotBeRetried_saysSoWithoutInvitingARetry() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.OTHER, retryable = false))
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:OTHER:final", canRetry = false), viewModel.content().notice)
	}

	@Test
	fun guidedOnly_onSend_keepsTheStudentsDraftInTheComposer() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.GUIDED_ONLY, null, "guided")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals("hola", viewModel.content().composer.text)
	}

	@Test
	fun aSentTurnWithoutItsFirstEvent_isSending_andTheSendAndTheSuggestionsWait() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		fixture.turns.holdEvents = true
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				agentMessage(
					"a1",
					Text("¿Quieres ver tu promedio?"),
					Suggestions(listOf(SuggestedReply(id = "s1", label = "Sí", text = "Sí, quiero verlo")))
				)
			)
		)
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertTrue(content.isSending)
		assertFalse(content.composer.isSendEnabled)
		assertFalse(content.composer.isPhotoEnabled)
		assertTrue(content.suggestions.isEmpty())
	}

	@Test
	fun theFirstEventOfTheSentTurn_endsTheSending_andTheTurnCanBeStopped() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		fixture.turns.holdEvents = true
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertTrue(viewModel.content().isSending)

		fixture.conversation.conversation.value = assistantConversation(
			activeTurn = AssistantActiveTurn(
				turnId = "t1",
				lastSeq = 1,
				progressLabel = null,
				status = AssistantTurnPhase.RUNNING
			)
		)

		val content = viewModel.content()
		assertFalse(content.isSending)
		assertTrue(content.composer.isStoppable)
	}

	@Test
	fun stopIsOfferedWhileATurnRuns_andStoppingItAsksTheServerToCancelIt() = chatTest { fixture, viewModel ->
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
		viewModel.openAction(null)
		assertTrue(viewModel.content().composer.isStoppable)

		viewModel.cancelTurnAction()

		assertEquals(listOf("t1"), fixture.turns.cancelled)
	}

	@Test
	fun stopIsNotOfferedWhenNoTurnRuns_andCancellingDoesNothing() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		assertFalse(viewModel.content().composer.isStoppable)

		viewModel.cancelTurnAction()

		assertTrue(fixture.turns.cancelled.isEmpty())
	}

	@Test
	fun turnInProgress_keepsTheDraft_andShowsNoNotice() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.TURN_IN_PROGRESS, null, "busy")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertNull(content.notice)
		assertEquals("hola", content.composer.text)
		assertTrue(content.bubbles().single().isFailed)
	}

	@Test
	fun guidedOnly_onSend_pausesTheComposer() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.standing.answer = assistantStatus().copy(mode = "guided")
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.GUIDED_ONLY, null, "guided")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertTrue(content.isGuided)
		assertFalse(content.composer.isEnabled)
		assertEquals(ChatNoticeItem.Guided(message = "guided"), content.notice)
	}

	@Test
	fun suspension_onSend_pausesTheComposer_andOffersTheAppealWhenTheStandingAllowsIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.standing.answer = assistantStatus().copy(
			standing = AbuseStanding(level = StandingLevel.COOLDOWN, appealable = true)
		)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.SUSPENDED, null, "suspended")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertEquals(
			ChatNoticeItem.Restricted(
				message = UiText.Resource(Res.string.assistant_chat_restricted_cooldown),
				canAppeal = true
			),
			content.notice
		)
		assertFalse(content.composer.isEnabled)
	}

	@Test
	fun assistantDisabled_onSend_closesTheSheet() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.ASSISTANT_DISABLED, 60, "off")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals(AssistantChat.State.Unavailable, viewModel.state.value)
		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.Dismiss), fixture.effects)
	}

	@Test
	fun consentRequired_onSend_asksForConsentAgain() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.CONSENT_REQUIRED, null, "consent")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)
	}

	@Test
	fun messageTooLong_isNotSent_andKeepsTheTextInTheComposer() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		val tooLong = "a".repeat(AssistantInputLimits.MESSAGE_MAX_LENGTH + 1)
		viewModel.changeTextAction(tooLong)

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertEquals(ChatNoticeItem.TurnFailed(message = "too-long:2000", canRetry = false), content.notice)
		assertEquals(tooLong, content.composer.text)
		assertTrue(fixture.turns.requests.isEmpty())
		assertTrue(content.bubbles().isEmpty())
	}

	@Test
	fun runError_isShownWithItsRetryFlag() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)
		assertTrue(viewModel.content().bubbles().single().isFailed)
	}

	@Test
	fun nonRetryableRunError_offersNoRetry() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.REFUSED, retryable = false))
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:REFUSED", canRetry = false), viewModel.content().notice)
	}

	@Test
	fun suggestionsOfTheLastAgentMessage_areOffered_andSentWithTheirText() = chatTest { fixture, viewModel ->
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
		viewModel.openAction(null)

		val content = viewModel.content()
		assertEquals(listOf(ChatSuggestionItem(id = "s1", label = "Sí")), content.suggestions)
		val agent = content.items.single() as ChatItem.Agent
		assertEquals(listOf<ChatPartItem>(ChatPartItem.Text("¿Quieres ver tu promedio?")), agent.parts)

		viewModel.pickSuggestionAction("s1")

		val request = fixture.turns.requests.single()
		assertEquals("Sí, quiero verlo", request.message.text)
		assertEquals("s1", request.message.suggestionId)
		assertEquals("Sí, quiero verlo", viewModel.content().bubbles().single().text)
	}

	@Test
	fun progressLabel_isTheLabelOfTheRunningTurn_andClearsWhenItEnds() = chatTest { fixture, viewModel ->
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
		viewModel.openAction(null)

		assertEquals("Revisando tu pensum…", viewModel.content().progressLabel)

		fixture.conversation.conversation.value = assistantConversation(
			activeTurn = AssistantActiveTurn(
				turnId = "t1",
				lastSeq = 4,
				progressLabel = null,
				status = AssistantTurnPhase.FINISHED
			)
		)

		assertNull(viewModel.content().progressLabel)
	}

	@Test
	fun removingTheContext_sendsTheTurnWithoutIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(AssistantScreenContext(AssistantScreen.RECORD))

		viewModel.removeContextAction()
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		assertNull(viewModel.content().context)
		assertNull(fixture.turns.requests.single().screenContext)
	}

	// A send that another message's acceptance does not answer keeps its failure, and the retry it can get.
	@Test
	fun aFailedSend_keepsItsMark_whenAnotherMessageIsAccepted() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException("unknown", null, "unknown")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		fixture.turns.sendError = null
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t2", conversationId = "c1", lastSeq = 0)
		fixture.turns.events = listOf(RunFinished(seq = 1, turnId = "t2"))
		viewModel.changeTextAction("adiós")
		viewModel.sendMessageAction()

		val bubbles = viewModel.content().bubbles()
		assertEquals(listOf("hola", "adiós"), bubbles.map { bubble -> bubble.text })
		assertEquals(listOf(true, false), bubbles.map { bubble -> bubble.isFailed })
	}

	// The text a refused send gives back is not written over what the student types while the send waits.
	@Test
	fun aRefusedSend_doesNotOverwriteWhatTheStudentTypesWhileItWaits() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.TURN_IN_PROGRESS, null, "busy")
		fixture.gatedTurns.gated = true
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		viewModel.changeTextAction("nuevo")
		fixture.gatedTurns.release.complete(Unit)

		assertEquals("nuevo", viewModel.content().composer.text)
	}

	@Test
	fun aPickedSuggestion_isNotSent_whileAnotherSendIsOnItsWay() = chatTest { fixture, viewModel ->
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
		fixture.gatedTurns.gated = true
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		viewModel.pickSuggestionAction("s1")
		fixture.gatedTurns.release.complete(Unit)

		assertEquals(listOf("hola"), fixture.turns.requests.map { request -> request.message.text })
	}

	@Test
	fun aRetry_isNotSent_whileAnotherSendIsOnItsWay() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.sendError = AssistantRequestRefusedException("unknown", null, "unknown")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		fixture.turns.sendError = null
		fixture.gatedTurns.gated = true
		viewModel.changeTextAction("otra")
		viewModel.sendMessageAction()

		viewModel.retryTurnAction()
		fixture.gatedTurns.release.complete(Unit)

		assertEquals(listOf("hola", "otra"), fixture.turns.requests.map { request -> request.message.text })
	}

	// A stream that closes before the turn's final event leaves the turn's result unknown: the send ends with the notice
	// of an unconfirmed turn, and its retry stays allowed, as many times as the student asks.
	@Test
	fun aStreamThatClosesBeforeItsFinalEvent_failsTheSend_andAllowsARetry() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		viewModel.retryTurnAction()

		assertFalse(viewModel.content().isSending)
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		viewModel.retryTurnAction()

		assertEquals(listOf("hola", "hola", "hola"), fixture.turns.requests.map { request -> request.message.text })
	}

	// A connection that breaks before the turn's final event ends the send the same way.
	@Test
	fun aBrokenConnectionBeforeTheFinalEvent_failsTheSend_andAllowsARetry() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		fixture.turns.streamError = IllegalStateException("connection lost")
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		viewModel.retryTurnAction()

		assertFalse(viewModel.content().isSending)
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		viewModel.retryTurnAction()

		assertEquals(listOf("hola", "hola", "hola"), fixture.turns.requests.map { request -> request.message.text })
	}
}
