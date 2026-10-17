package com.gdavidpb.tuindice.assistant.presentation.viewmodel

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantMemoryFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.memoryItem
import com.gdavidpb.tuindice.assistant.presentation.testing.memoryTest
import com.gdavidpb.tuindice.testkit.coroutines.withUnconfinedTestDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantMemoryViewModelContractTest {
	@Test
	fun initialState_isIdle_untilTheScreenObservesIt() = runTest {
		withUnconfinedTestDispatchers { dispatchers ->
			val viewModel = AssistantMemoryFixture().viewModel(dispatchers)

			assertEquals(AssistantMemory.State.Idle, viewModel.state.value)
		}
	}

	@Test
	fun when_theScreenStarts_then_theFirstLoadListsTheEntries() = memoryTest(
		items = listOf(memoryItem(id = "m1"))
	) { _, viewModel ->
		assertEquals(listOf("m1"), viewModel.content().entries.map { entry -> entry.id })
	}
}
