package com.gdavidpb.tuindice.assistant.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantMemoryCategoryKey
import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.presentation.model.UiText
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
	fun when_contentIsShown_then_explainsWhatTheAssistantRemembers() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantMemoryScreen(
				state = AssistantMemory.State.Content(entries = listOf(entry())),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.MemoryExplanation)
		onNodeWithTag(AssistantUiTags.MemoryExplanation).assertTextEquals(EXPLANATION_TEXT)
	}

	@Test
	fun when_entriesAreShown_then_theyAreGroupedUnderTheTitleOfTheirCategory() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantMemoryScreen(
				state = AssistantMemory.State.Content(
					entries = listOf(
						entry(id = "memory-2", category = AssistantMemoryCategoryKey.CAREER_GOAL),
						entry(id = MEMORY_ID, category = AssistantMemoryCategoryKey.PREFERENCE)
					)
				),
				onAction = {}
			)
		}

		// The groups follow the order of the categories, not the order of the entries.
		onAllNodesWithTag(AssistantUiTags.MemorySectionTitle)[0].assertTextEquals(PREFERENCES_TITLE)
		onAllNodesWithTag(AssistantUiTags.MemorySectionTitle)[1].assertTextEquals(CAREER_GOAL_TITLE)
	}

	@Test
	fun when_anEntryIsShown_then_itSaysWhenItWasSaved() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantMemoryScreen(
				state = AssistantMemory.State.Content(entries = listOf(entry())),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.Memory.entry(MEMORY_ID))
		onNodeWithText(SINCE_TEXT).assertIsDisplayed()
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
		onNodeWithTag(AssistantUiTags.Memory.stillAppliesYes(MEMORY_ID)).assertTextEquals(YES_TEXT)
		onNodeWithTag(AssistantUiTags.Memory.stillAppliesNo(MEMORY_ID)).assertTextEquals(NO_TEXT)
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

	private fun entry(
		id: String = MEMORY_ID,
		category: AssistantMemoryCategoryKey = AssistantMemoryCategoryKey.PREFERENCE,
		needsConfirmation: Boolean = false
	): MemoryEntryItem =
		MemoryEntryItem(
			id = id,
			text = "Estudias de noche",
			category = category,
			sinceText = UiText.Raw(SINCE_TEXT),
			anchorLabel = null,
			needsConfirmation = needsConfirmation
		)

	private companion object {
		const val MEMORY_ID = "memory-1"
		const val EMPTY_TITLE = "El asistente todavía no recuerda nada"
		const val FAILED_MESSAGE = "Sin conexión"
		const val EXPLANATION_TEXT = "Esto es lo que el asistente recuerda de ti para responderte mejor. " +
			"Borra lo que no quieras que use."
		const val SINCE_TEXT = "Desde el 9 de octubre 2025"
		const val PREFERENCES_TITLE = "Preferencias"
		const val CAREER_GOAL_TITLE = "Metas de carrera"
		const val STILL_APPLIES_QUESTION = "¿Sigue aplicando?"
		const val YES_TEXT = "Sí"
		const val NO_TEXT = "Ya no"
		const val DELETE_DESCRIPTION = "Eliminar"
		const val CLEAR_ALL_TEXT = "Borrar todo lo que recuerda"
	}
}
