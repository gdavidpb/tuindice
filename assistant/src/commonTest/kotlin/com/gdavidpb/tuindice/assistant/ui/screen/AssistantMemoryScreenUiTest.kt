package com.gdavidpb.tuindice.assistant.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantMemoryScreenUiTest {
	@Test
	fun when_stateIsLoading_then_showsTheLoadingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantMemoryScreen(state = AssistantMemory.State.Loading, onAction = {})
		}

		assertNodeVisible(AssistantUiTags.MemoryLoading)
	}

	@Test
	fun when_stateIsEmpty_then_saysTheAssistantRemembersNothing() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantMemoryScreen(state = AssistantMemory.State.Empty, onAction = {})
		}

		assertNodeVisible(AssistantUiTags.MemoryEmpty)
		onNodeWithText(EMPTY_TITLE).assertIsDisplayed()
	}

	@Test
	fun when_stateIsFailed_then_retryDispatchesRetry() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantMemory.Action>()
		setTuIndiceTestContent {
			AssistantMemoryScreen(
				state = AssistantMemory.State.Failed(message = FAILED_MESSAGE),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.MemoryFailed)
		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()

		assertIs<AssistantMemory.Action.Retry>(actions.single())
	}

	@Test
	fun when_contentHasAnEntryThatNeedsConfirmation_then_deleteAndAnswerDispatch() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantMemory.Action>()
		setTuIndiceTestContent {
			AssistantMemoryScreen(
				state = AssistantMemory.State.Content(entries = listOf(entry(needsConfirmation = true))),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.Memory.entry(MEMORY_ID))
		onNodeWithText(STILL_APPLIES_QUESTION).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.Memory.delete(MEMORY_ID)).assertContentDescriptionEquals(DELETE_DESCRIPTION)
		onNodeWithTag(AssistantUiTags.Memory.stillAppliesYes(MEMORY_ID)).performClick()
		onNodeWithTag(AssistantUiTags.Memory.delete(MEMORY_ID)).performClick()

		val answer = assertIs<AssistantMemory.Action.AnswerStillApplies>(actions[0])
		assertTrue(answer.applies)
		assertIs<AssistantMemory.Action.DeleteEntry>(actions[1])
	}

	@Test
	fun when_contentIsShown_then_clearAllDispatchesClearAll() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantMemory.Action>()
		setTuIndiceTestContent {
			AssistantMemoryScreen(
				state = AssistantMemory.State.Content(entries = listOf(entry(needsConfirmation = false))),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.MemoryClearAll)
		onNodeWithTag(AssistantUiTags.MemoryClearAll).assertTextEquals(CLEAR_ALL_TEXT)
		onNodeWithTag(AssistantUiTags.MemoryClearAll).performClick()

		assertIs<AssistantMemory.Action.ClearAll>(actions.single())
	}

	private fun entry(needsConfirmation: Boolean): MemoryEntryItem =
		MemoryEntryItem(
			id = MEMORY_ID,
			text = "Estudias de noche",
			categoryLabel = "Hábitos",
			anchorLabel = null,
			needsConfirmation = needsConfirmation
		)

	private companion object {
		const val MEMORY_ID = "memory-1"
		const val EMPTY_TITLE = "El asistente todavía no recuerda nada"
		const val FAILED_MESSAGE = "Sin conexión"
		const val STILL_APPLIES_QUESTION = "¿Sigue aplicando?"
		const val DELETE_DESCRIPTION = "Eliminar"
		const val CLEAR_ALL_TEXT = "Eliminar todo"
	}
}
