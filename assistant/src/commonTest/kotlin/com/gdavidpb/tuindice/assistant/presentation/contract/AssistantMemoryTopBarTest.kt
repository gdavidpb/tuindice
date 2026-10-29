package com.gdavidpb.tuindice.assistant.presentation.contract

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_memory_title
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// The bar of the memory screen says its own title in every state, so it never keeps the title of the screen before.
class AssistantMemoryTopBarTest {
	@Test
	fun when_anyStateIsShown_then_theBarSaysTheMemoryTitle() {
		val states = listOf(
			AssistantMemory.State.Idle,
			AssistantMemory.State.Loading,
			AssistantMemory.State.Empty,
			AssistantMemory.State.Failed(message = "Sin conexión"),
			AssistantMemory.State.Content(entries = emptyList())
		)

		states.forEach { state ->
			assertEquals(UiText.Resource(Res.string.assistant_memory_title), state.topBarTitle)
			assertTrue(state.isTopBarVisible)
			assertFalse(state.isBottomBarVisible)
		}
	}
}
