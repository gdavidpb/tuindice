package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConversationRepository
import com.gdavidpb.tuindice.assistant.testing.dataValues
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAssistantConversationUseCaseTest {
	@Test
	fun emitsWhatTheLocalConversationHoldsWithoutAskingTheServer() = runTest {
		val conversation = RecordingAssistantConversationRepository(conversationId = "c1")

		val values = ObserveAssistantConversationUseCase(conversation, RecordingReportingRepository())
			.execute(Unit)
			.dataValues()

		assertEquals(listOf(conversation.currentConversation()), values)
	}
}
