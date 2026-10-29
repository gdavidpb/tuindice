package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.assistant.presentation.machine.memoryAnswer
import com.gdavidpb.tuindice.assistant.presentation.machine.nextMemoryRead
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryVocabulary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SavedStillAppliesAnswerTest {
	private val question = ChatPartItem.Component(StillAppliesItem("c1", false, "m1", "Prefiere las mañanas"))

	private fun answered(answer: Boolean?, isPending: Boolean = false) =
		ChatPartItem.Component(StillAppliesItem("c1", false, "m1", "Prefiere las mañanas", answer, isPending))

	@Test
	fun `the question shows the answer its memory has`() {
		val result = question.withMemoryAnswer(answer = { memoryId -> memoryId == "m1" }, pending = { false })

		assertEquals(answered(answer = true), result)
	}

	@Test
	fun `a question whose answer is being saved is marked as pending`() {
		val result = question.withMemoryAnswer(answer = { null }, pending = { memoryId -> memoryId == "m1" })

		assertEquals(answered(answer = null, isPending = true), result)
	}

	@Test
	fun `a part that is not a question is left as it is`() {
		val text = ChatPartItem.Text("Hola")

		assertEquals(text, text.withMemoryAnswer(answer = { true }, pending = { memoryId -> memoryId == "m1" }))
	}

	@Test
	fun `a memory in an unknown state still asks`() {
		val session = AssistantChatSession()
		session.memoryStates = mapOf("m1" to "UNKNOWN_STATE")
		session.memoryStatesRead = session.nextMemoryRead()

		assertNull(session.memoryAnswer("m1"))
	}

	@Test
	fun `a memory the list does not have no longer applies`() {
		val session = AssistantChatSession()
		session.memoryStates = mapOf("other" to MemoryVocabulary.STATE_ACTIVE)
		session.memoryStatesRead = session.nextMemoryRead()

		assertEquals(false, session.memoryAnswer("m1"))
	}

	@Test
	fun `an answer saved after a read asked before it holds over that read`() {
		val session = AssistantChatSession()
		session.memoryStates = mapOf("m1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		session.memoryStatesRead = session.nextMemoryRead()
		session.answeredMemories["m1"] = AssistantChatSession.MemoryAnswer(
			applies = false,
			readsBefore = session.memoryReadsRequested
		)

		assertEquals(false, session.memoryAnswer("m1"))
	}

	@Test
	fun `an answer saved before a read asked after it loses to that read`() {
		val session = AssistantChatSession()
		session.memoryStates = mapOf("m1" to MemoryVocabulary.STATE_ACTIVE)
		session.memoryStatesRead = session.nextMemoryRead()
		session.answeredMemories["m1"] = AssistantChatSession.MemoryAnswer(
			applies = false,
			readsBefore = session.memoryReadsRequested
		)
		val later = session.nextMemoryRead()
		session.memoryStates = mapOf("m1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		session.memoryStatesRead = later

		assertNull(session.memoryAnswer("m1"))
	}
}
