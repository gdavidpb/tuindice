package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import kotlin.test.Test
import kotlin.test.assertEquals

class SavedStillAppliesAnswerTest {
	private val question = ChatPartItem.Component(StillAppliesItem("c1", false, "m1", "Prefiere las mañanas"))

	@Test
	fun `a question the student answered keeps the answer saved for its memory, and only that one`() {
		val answered = question.withSavedAnswer(mapOf("m1" to true))

		assertEquals(ChatPartItem.Component(StillAppliesItem("c1", false, "m1", "Prefiere las mañanas", answer = true)), answered)
		assertEquals(question, question.withSavedAnswer(mapOf("other" to false)))
	}

	@Test
	fun `a part that is not a question is left as it is`() {
		val text = ChatPartItem.Text("Hola")

		assertEquals(text, text.withSavedAnswer(mapOf("m1" to true)))
	}
}
