package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantMemoryCategoryKey
import com.gdavidpb.tuindice.assistant.presentation.testing.answerAction
import com.gdavidpb.tuindice.assistant.presentation.testing.clearAllAction
import com.gdavidpb.tuindice.assistant.presentation.testing.confirmClearAllAction
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.deleteAction
import com.gdavidpb.tuindice.assistant.presentation.testing.memoryItem
import com.gdavidpb.tuindice.assistant.presentation.testing.memoryTest
import com.gdavidpb.tuindice.assistant.presentation.testing.retryAction
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The memory screen's behavior through its view model, over the fake server: loads, deletes, answers and the clearing.
class AssistantMemoryBehaviorTest {
	@Test
	fun when_loadSucceeds_then_listsOnlyTheMemoriesStillInForce() = memoryTest(
		items = listOf(
			memoryItem(id = "m1", state = "ACTIVE"),
			memoryItem(id = "m2", state = "PENDING_CONFIRMATION"),
			memoryItem(id = "m3", state = "ARCHIVED"),
			memoryItem(id = "m4", state = "REPLACED"),
			memoryItem(id = "m5", state = "DELETED")
		)
	) { _, viewModel ->
		assertEquals(listOf("m1", "m2"), viewModel.content().entries.map { entry -> entry.id })
	}

	@Test
	fun when_loadHasNothingInForce_then_isEmpty() = memoryTest(
		items = listOf(memoryItem(id = "m1", state = "ARCHIVED"))
	) { _, viewModel ->
		assertEquals(AssistantMemory.State.Empty, viewModel.state.value)
	}

	@Test
	fun when_loadFails_then_showsTheFailureText() = memoryTest(
		configure = { listFails = true }
	) { _, viewModel ->
		assertEquals(AssistantMemory.State.Failed(message = "load-failed"), viewModel.state.value)
	}

	@Test
	fun when_retryFollowsAFailedLoad_then_loadsTheList() = memoryTest(
		items = listOf(memoryItem(id = "m1")),
		configure = { listFails = true }
	) { fixture, viewModel ->
		fixture.repository.listFails = false
		viewModel.retryAction()

		assertEquals(listOf("m1"), viewModel.content().entries.map { entry -> entry.id })
	}

	@Test
	fun when_anEntryIsDeleted_then_itsRowLeavesTheList_andTheStudentIsTold() = memoryTest(
		items = listOf(memoryItem(id = "m1"), memoryItem(id = "m2"))
	) { fixture, viewModel ->
		viewModel.deleteAction(memoryId = "m1")

		assertEquals(listOf("m2"), viewModel.content().entries.map { entry -> entry.id })
		assertEquals(listOf("m1"), fixture.repository.deleted)
		assertEquals(listOf("deleted"), fixture.snackBars())
	}

	@Test
	fun when_theLastEntryIsDeleted_then_isEmpty() = memoryTest(
		items = listOf(memoryItem(id = "m1"))
	) { _, viewModel ->
		viewModel.deleteAction(memoryId = "m1")

		assertEquals(AssistantMemory.State.Empty, viewModel.state.value)
	}

	// Two deletes in flight at once: each reads the list before the other is gone; when both end, the list is empty.
	@Test
	fun when_twoEntriesAreDeletedAtOnce_then_isEmpty() = memoryTest(
		items = listOf(memoryItem(id = "m1"), memoryItem(id = "m2")),
		configure = { deleteGate = CompletableDeferred() }
	) { fixture, viewModel ->
		viewModel.deleteAction(memoryId = "m1")
		viewModel.deleteAction(memoryId = "m2")

		fixture.repository.deleteGate?.complete(Unit)

		assertEquals(AssistantMemory.State.Empty, viewModel.state.value)
	}

	// A second tap on an entry whose delete is on its way asks the server nothing more, so the student is told once.
	@Test
	fun when_theSameEntryIsDeletedTwice_then_itIsAskedOnceAndToldOnce() = memoryTest(
		items = listOf(memoryItem(id = "m1"), memoryItem(id = "m2")),
		configure = { deleteGate = CompletableDeferred() }
	) { fixture, viewModel ->
		viewModel.deleteAction(memoryId = "m1")
		viewModel.deleteAction(memoryId = "m1")

		fixture.repository.deleteGate?.complete(Unit)

		assertEquals(listOf("m1"), fixture.repository.deleted)
		assertEquals(listOf("deleted"), fixture.snackBars())
	}

	@Test
	fun when_aDeleteFails_then_theListStays_andTheStudentIsTold() = memoryTest(
		items = listOf(memoryItem(id = "m1")),
		configure = { deleteFails = true }
	) { fixture, viewModel ->
		viewModel.deleteAction(memoryId = "m1")

		assertEquals(listOf("m1"), viewModel.content().entries.map { entry -> entry.id })
		assertEquals(listOf("delete-failed"), fixture.snackBars())
	}

	@Test
	fun when_anAnswerIsSaved_then_theListLoadsAgain() = memoryTest(
		items = listOf(memoryItem(id = "m1"))
	) { fixture, viewModel ->
		viewModel.answerAction(memoryId = "m1", applies = true)

		assertEquals(listOf("m1" to true), fixture.repository.answers)
		assertEquals(listOf("m1"), viewModel.content().entries.map { entry -> entry.id })
	}

	@Test
	fun when_anAnswerFails_then_theListStays_andTheStudentIsTold() = memoryTest(
		items = listOf(memoryItem(id = "m1")),
		configure = { answerFails = true }
	) { fixture, viewModel ->
		viewModel.answerAction(memoryId = "m1", applies = false)

		assertEquals(listOf("m1"), viewModel.content().entries.map { entry -> entry.id })
		assertEquals(listOf("answer-failed"), fixture.snackBars())
	}

	@Test
	fun when_clearAllIsTapped_then_itAsksForTheConfirmation_andClearsNothing() = memoryTest(
		items = listOf(memoryItem(id = "m1"))
	) { fixture, viewModel ->
		viewModel.clearAllAction()

		assertTrue(fixture.effects.any { effect -> effect is AssistantMemory.Effect.ShowClearAllConfirmation })
		assertEquals(0, fixture.repository.clears)
		assertEquals(listOf("m1"), viewModel.content().entries.map { entry -> entry.id })
	}

	@Test
	fun when_clearAllIsConfirmed_then_isEmpty_andTheStudentIsTold() = memoryTest(
		items = listOf(memoryItem(id = "m1"), memoryItem(id = "m2"))
	) { fixture, viewModel ->
		viewModel.confirmClearAllAction()

		assertEquals(1, fixture.repository.clears)
		assertEquals(AssistantMemory.State.Empty, viewModel.state.value)
		assertEquals(listOf("cleared"), fixture.snackBars())
	}

	@Test
	fun when_clearAllFails_then_theListStays_andTheStudentIsTold() = memoryTest(
		items = listOf(memoryItem(id = "m1")),
		configure = { clearFails = true }
	) { fixture, viewModel ->
		viewModel.confirmClearAllAction()

		assertEquals(listOf("m1"), viewModel.content().entries.map { entry -> entry.id })
		assertEquals(listOf("delete-failed"), fixture.snackBars())
	}

	@Test
	fun when_aCategoryIsKnown_then_itIsKept_andAnUnknownOneIsOther() = memoryTest(
		items = listOf(
			memoryItem(id = "m1", category = "PREFERENCE"),
			memoryItem(id = "m2", category = "A_KIND_THIS_BUILD_DOES_NOT_KNOW")
		)
	) { _, viewModel ->
		assertEquals(
			listOf(AssistantMemoryCategoryKey.PREFERENCE, AssistantMemoryCategoryKey.OTHER),
			viewModel.content().entries.map { entry -> entry.category }
		)
	}

	@Test
	fun when_aMemoryIsPendingConfirmation_then_itAsksForIt() = memoryTest(
		items = listOf(
			memoryItem(id = "m1", state = "ACTIVE"),
			memoryItem(id = "m2", state = "PENDING_CONFIRMATION")
		)
	) { _, viewModel ->
		assertEquals(
			listOf(false, true),
			viewModel.content().entries.map { entry -> entry.needsConfirmation }
		)
	}

	@Test
	fun when_aMemoryHasAnAnchor_then_itsLabelIsKept() = memoryTest(
		items = listOf(memoryItem(id = "m1", anchorLabel = "Periodo 2026-2"))
	) { _, viewModel ->
		assertEquals("Periodo 2026-2", viewModel.content().entries.single().anchorLabel)
	}
}
