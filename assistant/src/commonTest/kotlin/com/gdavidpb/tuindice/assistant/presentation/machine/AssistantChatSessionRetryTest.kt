package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.presentation.mapper.presentedNotice
import com.gdavidpb.tuindice.assistant.presentation.mapper.runFailed
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.testing.FakeAssistantChatTexts
import com.gdavidpb.tuindice.assistant.presentation.testing.emptyAssistantConversation
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

class AssistantChatSessionRetryTest {
	// The session has sent a turn: a RunError of another turn is not the failure of that send, and shows nothing.
	@Test
	fun aRunErrorOfAnotherTurn_isNotTheFailureOfTheSentTurn() = runTest {
		val session = AssistantChatSession()
		session.sendingTurnId = "t1"

		session.runFailed(
			texts = FakeAssistantChatTexts(),
			event = AssistantChatInternalEvent.TurnRunFailed(turnId = "t0", code = RunErrorCode.TRANSIENT, retryable = true)
		)

		assertNull(session.turnFailure)
		assertNull(session.lastFailedSend)
	}

	// A RunError of the turn the session sent is the failure of that send.
	@Test
	fun aRunErrorOfTheSentTurn_isItsFailure() = runTest {
		val session = AssistantChatSession()
		session.sendingTurnId = "t1"

		session.runFailed(
			texts = FakeAssistantChatTexts(),
			event = AssistantChatInternalEvent.TurnRunFailed(turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true)
		)

		assertNotNull(session.turnFailure)
	}

	// The notice offers the retry of the answer a new message waited for only while no send is busy: RetryTurn is refused
	// meanwhile, so a retry offered then would do nothing.
	@Test
	fun aRetryOfTheRecoveredAnswer_isNotOffered_whileASendIsBusy() = runTest {
		val session = AssistantChatSession()
		val failure = AssistantChatSession.TurnFailure(message = "failure", retryable = true)
		session.lastFailedSend = AssistantChatSession.FailedSend(text = "hola", suggestionId = null, clientTurnId = "c1")
		session.recoveredFailure = AssistantChatSession.RecoveredFailure(clientTurnId = "c1", failure = failure)
		session.sending = true

		val notice = session.presentedNotice(
			texts = FakeAssistantChatTexts(),
			guided = false,
			restricted = null,
			clock = Clock.System
		)

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure", canRetry = false), notice)
	}

	// The same holds for the retry of a failed turn while another turn runs: it is refused until the running turn ends.
	@Test
	fun aRetryOfAFailedTurn_isNotOffered_whileATurnRuns() = runTest {
		val session = AssistantChatSession()
		session.lastFailedSend = AssistantChatSession.FailedSend(text = "hola", suggestionId = null, clientTurnId = "c1")
		session.turnFailure = AssistantChatSession.TurnFailure(message = "failure", retryable = true)
		session.observe(
			emptyAssistantConversation().copy(
				activeTurn = AssistantActiveTurn(
					turnId = "t2",
					lastSeq = 1,
					progressLabel = null,
					status = AssistantTurnPhase.RUNNING
				)
			)
		)

		val notice = session.presentedNotice(
			texts = FakeAssistantChatTexts(),
			guided = false,
			restricted = null,
			clock = Clock.System
		)

		assertEquals(ChatNoticeItem.TurnFailed(message = "failure", canRetry = false), notice)
	}
}
