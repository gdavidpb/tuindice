package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toStillAppliesItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class StillAppliesComponentViewUiTest {
	@Test
	fun when_componentIsShown_then_asksTheQuestionAndShowsTheStatement() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StillAppliesComponentView(item = component(), onAction = {}, modifier = Modifier)
		}

		onNodeWithText(QUESTION).assertIsDisplayed()
		onNodeWithText(STATEMENT).assertIsDisplayed()
	}

	@Test
	fun when_noLongerApplies_then_dispatchesAnAnswerThatDoesNotApply() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			StillAppliesComponentView(item = component(), onAction = { actions.add(it) })
		}

		assertNodeVisible(AssistantUiTags.Memory.stillAppliesNo(MEMORY_ID))
		onNodeWithTag(AssistantUiTags.Memory.stillAppliesNo(MEMORY_ID)).performClick()

		val answer = assertIs<AssistantChat.Action.AnswerStillApplies>(actions.single())
		assertEquals(MEMORY_ID, answer.memoryId)
		assertFalse(answer.applies)
	}

	private fun component(): StillAppliesItem =
		StillAppliesComponent(
			id = "component-1",
			fallbackText = STATEMENT,
			memoryId = MEMORY_ID,
			statement = STATEMENT
		).toStillAppliesItem()

	private companion object {
		const val MEMORY_ID = "memory-1"
		const val STATEMENT = "Estudias de noche"
		const val QUESTION = "¿Sigue aplicando?"
	}
}
