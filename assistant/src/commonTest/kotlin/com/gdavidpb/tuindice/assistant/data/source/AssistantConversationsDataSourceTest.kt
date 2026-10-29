package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConversationRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantTurnRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AssistantConversationsDataSourceTest {
	@Test
	fun when_conversationsAreDeleted_then_theThreadsGoOnTheServerBeforeTheDeviceForgetsThem() = runTest {
		val calls = mutableListOf<String>()
		val turns = RecordingAssistantTurnRepository(calls)
		val conversations = RecordingAssistantConversationRepository(calls)
		val dataSource = AssistantConversationsDataSource(
			turnRepository = turns,
			conversationRepository = conversations
		)

		dataSource.deleteAssistantConversations()

		assertEquals(listOf("deleteConversations", "clearConversation"), calls)
		assertEquals(1, turns.conversationsDeleted)
		assertEquals(1, conversations.cleared)
	}

	@Test
	fun when_theServerRefusesTheDeletion_then_theDeviceKeepsItsConversation() = runTest {
		val calls = mutableListOf<String>()
		val conversations = RecordingAssistantConversationRepository(calls)
		val dataSource = AssistantConversationsDataSource(
			turnRepository = RefusingAssistantTurnRepository(calls),
			conversationRepository = conversations
		)

		assertFailsWith<IllegalStateException> { dataSource.deleteAssistantConversations() }

		assertEquals(0, conversations.cleared)
	}

	/** A turn repository whose server refuses to delete the threads; every other call is recorded as usual. */
	private class RefusingAssistantTurnRepository(
		calls: MutableList<String>
	) : AssistantTurnRepository by RecordingAssistantTurnRepository(calls) {
		override suspend fun deleteConversations() {
			error("The server refused to delete the threads")
		}
	}
}
