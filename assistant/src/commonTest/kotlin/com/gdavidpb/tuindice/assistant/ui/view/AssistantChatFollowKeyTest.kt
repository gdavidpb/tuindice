package com.gdavidpb.tuindice.assistant.ui.view

import com.gdavidpb.tuindice.assistant.ui.agentMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

// The key the list follows its last item by. A change inside an item must not move the list, so the key holds while
// the last item changes inside; a new item at the end, or another item there, moves it.
class AssistantChatFollowKeyTest {
	@Test
	fun when_theLastItemChangesInside_then_theFollowKeyStaysTheSame() {
		val before = listOf(agentMessage(id = "message-1"), agentMessage(id = "message-2", useful = null))
		val after = listOf(agentMessage(id = "message-1"), agentMessage(id = "message-2", useful = true))

		assertEquals(followKeyOf(before), followKeyOf(after))
	}

	@Test
	fun when_anItemAboveTheLastChangesInside_then_theFollowKeyStaysTheSame() {
		val before = listOf(agentMessage(id = "message-1", useful = null), agentMessage(id = "message-2"))
		val after = listOf(agentMessage(id = "message-1", useful = true), agentMessage(id = "message-2"))

		assertEquals(followKeyOf(before), followKeyOf(after))
	}

	@Test
	fun when_aNewItemArrives_then_theFollowKeyChanges() {
		val before = listOf(agentMessage(id = "message-1"))
		val after = listOf(agentMessage(id = "message-1"), agentMessage(id = "message-2"))

		assertNotEquals(followKeyOf(before), followKeyOf(after))
	}

	@Test
	fun when_anotherItemIsTheLast_then_theFollowKeyChanges() {
		val before = listOf(agentMessage(id = "message-1"), agentMessage(id = "message-2"))
		val after = listOf(agentMessage(id = "message-1"), agentMessage(id = "message-3"))

		assertNotEquals(followKeyOf(before), followKeyOf(after))
	}
}
