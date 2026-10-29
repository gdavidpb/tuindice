package com.gdavidpb.tuindice.assistant.data.model

import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.chatcontract.intent.EditEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AssistantConversationReducersTest {

	// A photo read again brings its intent again: the intent the student resolved stays resolved, and the photo keeps
	// one bubble.
	@Test
	fun aPhotoReadAgainKeepsItsResolvedIntentResolvedAndItsBubbleOnce() {
		val intent = assistantIntent(args = EditEvaluationArgs(mutationId = "m1", evaluationId = "e1"))
		val photo = AssistantMessage(id = "photo-1", parts = emptyList(), createdAtMillis = 1L)
		val resolved = AssistantConversationState()
			.withAssistantMessage(photo, intent)
			.copy(openIntents = emptyMap(), resolvedIntents = mapOf(intent.id to OutcomeKind.CONFIRMED))

		val readAgain = resolved.withAssistantMessage(photo, intent)

		assertFalse(intent.id in readAgain.openIntents)
		assertEquals(mapOf(intent.id to OutcomeKind.CONFIRMED), readAgain.resolvedIntents)
		assertEquals(listOf(photo), readAgain.messages)
		assertEquals(listOf(photo.id), readAgain.order)
	}
}
