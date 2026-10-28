package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.testing.bubbles
import com.gdavidpb.tuindice.assistant.presentation.testing.cancelTurnAction
import com.gdavidpb.tuindice.assistant.presentation.testing.changeTextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.retryTurnAction
import com.gdavidpb.tuindice.assistant.presentation.testing.sendMessageAction
import com.gdavidpb.tuindice.chatcontract.event.Progress
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The send path of the chat sheet: a new message waits for an unconfirmed answer to be recovered, and the retry
// of a run that failed takes the place of the failed question. The other sends and retries are in
// AssistantChatTurnBehaviorTest.
class AssistantChatSendPathBehaviorTest {
	// The answer to a message is unconfirmed. A new message waits for that answer to be recovered, and it is sent
	// after the answer's final event: the recovery follows the unconfirmed turn again, and the new message is then
	// sent as a turn.
	@Test
	fun aNewMessage_isSentOnlyAfterTheUnconfirmedAnswerIsRecovered() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t0", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		// The recovery of t0 gets its final event; the new message is then accepted as t1 and followed to its end.
		fixture.turns.scriptedFollows.add(listOf(RunFinished(seq = 1, turnId = "t0")))
		fixture.turns.scriptedFollows.add(listOf(RunFinished(seq = 1, turnId = "t1")))
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		viewModel.changeTextAction("adiós")
		viewModel.sendMessageAction()

		assertEquals(
			listOf("send:hola", "follow:t0", "follow:t0", "follow:t0", "send:adiós", "follow:t1"),
			fixture.turns.order
		)
		assertNull(viewModel.content().notice)
		assertFalse(viewModel.content().isSending)
	}

	// The recovery of the unconfirmed answer does not confirm it: the new message is not sent, its text goes back to the
	// composer, and the notice of the answer shows again with its retry.
	@Test
	fun aNewMessage_isNotSent_whenTheUnconfirmedAnswerIsNotRecovered() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t0", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		// The stream of the recovery breaks before any final event: the answer stays unconfirmed.
		fixture.turns.streamError = IllegalStateException("connection lost")
		viewModel.changeTextAction("adiós")
		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertEquals(listOf("hola"), fixture.turns.requests.map { request -> request.message.text })
		assertEquals("adiós", content.composer.text)
		assertFalse(content.isSending)
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), content.notice)
	}

	// While the unconfirmed answer is recovered, the new message is not sent: the send is on its way, and the composer
	// does not send it again.
	@Test
	fun aNewMessage_waitsWhileTheUnconfirmedAnswerIsRecovered() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t0", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		// The recovery stays open, as a stream that has not delivered its final event does.
		fixture.turns.holdEvents = true
		viewModel.changeTextAction("adiós")
		viewModel.sendMessageAction()

		val waiting = viewModel.content()
		assertTrue(waiting.isSending)
		assertFalse(waiting.composer.isSendEnabled)
		assertEquals(listOf("hola"), fixture.turns.requests.map { request -> request.message.text })
	}

	// While the retry is sent, its question takes the place of the failed one, and the failure notice is not shown.
	@Test
	fun aRetryOfARunError_takesThePlaceOfTheFailedQuestion_whileItIsSent() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t2", conversationId = "c1", lastSeq = 0)
		fixture.turns.events = listOf(RunFinished(seq = 1, turnId = "t2"))
		fixture.gatedTurns.gated = true
		viewModel.retryTurnAction()

		val during = viewModel.content()
		assertTrue(during.isSending)
		assertNull(during.notice)
		assertEquals(listOf(false), during.bubbles().map { bubble -> bubble.isFailed })

		fixture.gatedTurns.release.complete(Unit)

		assertNull(viewModel.content().notice)
		assertFalse(viewModel.content().isSending)
	}

	// The question a retry replaces leaves the conversation: a sheet opened again on the same conversation shows the retry
	// and not the question it replaced.
	@Test
	fun aRetryOfARunError_leavesNoQuestionBehind_whenTheSheetIsOpenedAgain() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t2", conversationId = "c1", lastSeq = 0)
		fixture.turns.events = listOf(RunFinished(seq = 1, turnId = "t2"))
		viewModel.retryTurnAction()
		val retriedId = fixture.turns.requests.last().clientTurnId

		val reopened = fixture.reopenedViewModel()
		backgroundScope.launchStateCollector(reopened.state, testScheduler)
		reopened.openAction(null)

		assertEquals(listOf(retriedId), reopened.content().bubbles().map { bubble -> bubble.id })
	}

	// The answer a new message waited for ended with a run error. The new message is sent anyway, and the notice keeps the
	// retry of that answer: the retry sends the question again as a new request, and the question makes way for it.
	@Test
	fun aRunErrorOfTheRecoveredAnswer_keepsItsRetry_whileTheNewMessageIsSent() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t0", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		// The recovery of t0 gets a run error; the new message is then accepted as t1 and followed to its end.
		fixture.turns.scriptedFollows.add(
			listOf(RunError(seq = 1, turnId = "t0", code = RunErrorCode.TRANSIENT, retryable = true))
		)
		fixture.turns.scriptedFollows.add(listOf(RunFinished(seq = 1, turnId = "t1")))
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c1", lastSeq = 0)
		viewModel.changeTextAction("adiós")
		viewModel.sendMessageAction()

		assertEquals(listOf("hola", "adiós"), fixture.turns.requests.map { request -> request.message.text })
		assertFalse(viewModel.content().isSending)
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		// The retry sends the question of the answer again: a new request, and the question makes way for it.
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t2", conversationId = "c1", lastSeq = 0)
		fixture.turns.events = listOf(RunFinished(seq = 1, turnId = "t2"))
		viewModel.retryTurnAction()

		val requests = fixture.turns.requests
		assertEquals(listOf("hola", "adiós", "hola"), requests.map { request -> request.message.text })
		assertNotEquals(requests.first().clientTurnId, requests.last().clientTurnId)
		assertNull(viewModel.content().notice)
		assertEquals(
			listOf(requests[1].clientTurnId, requests.last().clientTurnId),
			viewModel.content().bubbles().map { bubble -> bubble.id }
		)
	}

	// A run that failed is retried as a new request: its answer is a new turn, with a client turn id of its own, and the
	// failed question makes way for the new one. The failure that turn gets is its own, with a retry of its own.
	@Test
	fun aRetryOfARunError_isANewTurn_andGivesTheFailureOfThatTurn() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t2", conversationId = "c1", lastSeq = 0)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t2", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.retryTurnAction()

		val requests = fixture.turns.requests
		val content = viewModel.content()
		assertEquals(listOf("hola", "hola"), requests.map { request -> request.message.text })
		assertNotEquals(requests.first().clientTurnId, requests.last().clientTurnId)
		assertFalse(content.isSending)
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), content.notice)
		assertEquals(listOf(requests.last().clientTurnId), content.bubbles().map { bubble -> bubble.id })
		assertEquals(listOf(true), content.bubbles().map { bubble -> bubble.isFailed })
	}

	// The retry is decided in the session the conversation shows, and the question it replaces leaves for that session.
	// A session that has ended since refuses the replacement, so the question is kept when its session is gone.
	@Test
	fun aRetryOfARunError_takesTheReplacedQuestionOut_forTheSessionItIsDecidedIn() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = fixture.conversation.conversation.value.copy(sessionMark = 3)
		fixture.turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		val failedId = fixture.turns.requests.single().clientTurnId

		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t2", conversationId = "c1", lastSeq = 0)
		fixture.turns.events = listOf(RunFinished(seq = 1, turnId = "t2"))
		viewModel.retryTurnAction()

		assertEquals(listOf(failedId to 3), fixture.conversation.replacements)
	}

	// A recovery resumes the unconfirmed turn: its first event runs the turn again, and while the turn runs, its stream
	// stays open. From then on the stop of the sheet reaches the turn.
	@Test
	fun aStopOfAResumedTurn_reachesTheTurn() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t0", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertEquals(ChatNoticeItem.TurnFailed(message = "failure:TRANSIENT", canRetry = true), viewModel.content().notice)

		// The recovery of t0 gets a progress event, which runs the turn again, and then its stream stays open.
		fixture.turns.scriptedFollows.add(
			listOf(Progress(seq = 1, turnId = "t0", tool = "buscar", label = "Buscando"))
		)
		fixture.turns.holdAfterEvents = true
		viewModel.changeTextAction("adiós")
		viewModel.sendMessageAction()

		assertTrue(viewModel.content().composer.isStoppable)
		viewModel.cancelTurnAction()
		assertEquals(listOf("t0"), fixture.turns.cancelled)
	}
}
