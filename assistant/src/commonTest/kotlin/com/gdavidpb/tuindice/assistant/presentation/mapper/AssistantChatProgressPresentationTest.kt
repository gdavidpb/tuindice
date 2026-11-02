package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.presentation.testing.FakeAssistantChatTexts
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.testing.FixedClock
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// The progress as the chat presents it: the label that waits, and the item it goes under. The texts are the fake's, so
// each label is told apart by its value.
class AssistantChatProgressPresentationTest {
	@Test
	fun when_aTurnRuns_then_itsLabelShowsUnderTheStudentsQuestion() = runTest {
		val session = AssistantChatSession().apply {
			conversation = assistantConversation(
				items = listOf(question(QUESTION_ID)),
				activeTurn = AssistantActiveTurn(
					turnId = "t1",
					lastSeq = 3,
					progressLabel = TURN_LABEL,
					status = AssistantTurnPhase.RUNNING
				)
			)
		}

		val content = session.presented()

		assertEquals(TURN_LABEL, content.progressLabel)
		assertEquals(QUESTION_ID, content.progressAnchorId)
	}

	@Test
	fun when_aPhotoIsBeingRead_then_itsLabelShowsUnderTheLatestItem() = runTest {
		val session = AssistantChatSession().apply {
			conversation = assistantConversation(items = listOf(question(QUESTION_ID), answer()))
			photoExtracting = true
		}

		val content = session.presented()

		assertEquals(PHOTO_READING, content.progressLabel)
		assertEquals(ANSWER_ID, content.progressAnchorId)
	}

	@Test
	fun when_aSendHasNoEventYet_then_theSendingLabelShowsUnderTheQuestion() = runTest {
		val session = AssistantChatSession().apply {
			conversation = assistantConversation(items = listOf(question(QUESTION_ID)))
			sending = true
		}

		val content = session.presented()

		assertEquals(SENDING, content.progressLabel)
		assertEquals(QUESTION_ID, content.progressAnchorId)
	}

	@Test
	fun when_aSendComesAfterAnAnswer_then_theSendingLabelShowsUnderTheLatestItem() = runTest {
		val session = AssistantChatSession().apply {
			conversation = assistantConversation(items = listOf(question(QUESTION_ID), answer()))
			sending = true
		}

		val content = session.presented()

		assertEquals(SENDING, content.progressLabel)
		assertEquals(ANSWER_ID, content.progressAnchorId)
	}

	@Test
	fun when_nothingWaits_then_neitherLabelNorAnchorShow() = runTest {
		val content = AssistantChatSession().presented()

		assertNull(content.progressLabel)
		assertNull(content.progressAnchorId)
	}

	@Test
	fun when_theLabelIsNull_then_theAnchorStaysUnderTheStudentsQuestion() = runTest {
		val session = AssistantChatSession().apply {
			conversation = assistantConversation(items = listOf(question(QUESTION_ID), answer()))
		}

		val content = session.presented()

		assertNull(content.progressLabel)
		assertEquals(QUESTION_ID, content.progressAnchorId)
	}

	@Test
	fun when_aRunningTurnHasNoStepLabelYet_then_theSendingLabelKeepsTheWait() = runTest {
		val session = AssistantChatSession().apply {
			conversation = assistantConversation(
				items = listOf(question(QUESTION_ID)),
				activeTurn = AssistantActiveTurn(
					turnId = "t1",
					lastSeq = 1,
					progressLabel = null,
					status = AssistantTurnPhase.RUNNING
				)
			)
		}

		val content = session.presented()

		assertEquals(SENDING, content.progressLabel)
		assertEquals(QUESTION_ID, content.progressAnchorId)
	}

	@Test
	fun when_aTurnLabelIsSetWhileAPhotoIsReadAndASendWaits_then_theTurnLabelWins() = runTest {
		val session = AssistantChatSession().apply {
			conversation = assistantConversation(
				items = listOf(question(QUESTION_ID)),
				activeTurn = AssistantActiveTurn(
					turnId = "t1",
					lastSeq = 2,
					progressLabel = TURN_LABEL,
					status = AssistantTurnPhase.RUNNING
				)
			)
			photoExtracting = true
			sending = true
		}

		val content = session.presented()

		assertEquals(TURN_LABEL, content.progressLabel)
	}

	// The content the session presents from an open sheet with nothing in it, as the machine presents each change.
	private suspend fun AssistantChatSession.presented(): AssistantChat.State.Content = presentedContent(
		texts = FakeAssistantChatTexts(),
		draftText = "",
		draftRevision = 0,
		content = AssistantChat.State.Content(
			items = emptyList(),
			progressLabel = null,
			composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false),
			suggestions = emptyList(),
			notice = null,
			isGuided = false
		),
		clock = FixedClock(nowMillis = NOW_MILLIS)
	)

	private fun question(id: String): AssistantConversationItem =
		AssistantConversationItem.User(AssistantUserMessage(id = id, text = "¿Cómo voy?", createdAtMillis = 1L))

	private fun answer(): AssistantConversationItem = agentMessage(ANSWER_ID, Text("Respuesta"))

	private companion object {
		const val NOW_MILLIS = 1_000L
		const val QUESTION_ID = "question"
		const val ANSWER_ID = "answer"
		const val TURN_LABEL = "Revisando tu pensum…"

		// The labels the fake texts answer (FakeAssistantChatTexts).
		const val PHOTO_READING = "photo-reading"
		const val SENDING = "sending"
	}
}
