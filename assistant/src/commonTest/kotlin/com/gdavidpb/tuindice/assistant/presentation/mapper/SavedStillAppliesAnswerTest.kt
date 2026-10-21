package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryVocabulary
import kotlin.test.Test
import kotlin.test.assertEquals

class SavedStillAppliesAnswerTest {
	private val question = ChatPartItem.Component(StillAppliesItem("c1", false, "m1", "Prefiere las mañanas"))

	private fun answered(answer: Boolean?, isPending: Boolean = false) =
		ChatPartItem.Component(StillAppliesItem("c1", false, "m1", "Prefiere las mañanas", answer, isPending))

	@Test
	fun `an answer the student gave on the sheet wins over the state of the memory`() {
		val result = question.withMemoryAnswer(
			answered = mapOf("m1" to false),
			states = mapOf("m1" to MemoryVocabulary.STATE_ACTIVE),
			pending = emptySet()
		)

		assertEquals(answered(answer = false), result)
	}

	@Test
	fun `an active memory is answered yes`() {
		val result = question.withMemoryAnswer(
			answered = emptyMap(),
			states = mapOf("m1" to MemoryVocabulary.STATE_ACTIVE),
			pending = emptySet()
		)

		assertEquals(answered(answer = true), result)
	}

	@Test
	fun `a memory that waits for confirmation still asks`() {
		val result = question.withMemoryAnswer(
			answered = emptyMap(),
			states = mapOf("m1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION),
			pending = emptySet()
		)

		assertEquals(answered(answer = null), result)
	}

	@Test
	fun `a memory missing from the loaded list is archived or deleted and no longer applies`() {
		val result = question.withMemoryAnswer(
			answered = emptyMap(),
			states = mapOf("other" to MemoryVocabulary.STATE_ACTIVE),
			pending = emptySet()
		)

		assertEquals(answered(answer = false), result)
	}

	@Test
	fun `without the list read the question still asks`() {
		val result = question.withMemoryAnswer(answered = emptyMap(), states = null, pending = emptySet())

		assertEquals(question, result)
	}

	@Test
	fun `a question whose answer is being saved is marked as pending`() {
		val result = question.withMemoryAnswer(answered = emptyMap(), states = null, pending = setOf("m1"))

		assertEquals(answered(answer = null, isPending = true), result)
	}

	@Test
	fun `a part that is not a question is left as it is`() {
		val text = ChatPartItem.Text("Hola")

		assertEquals(text, text.withMemoryAnswer(answered = mapOf("m1" to true), states = null, pending = setOf("m1")))
	}
}
