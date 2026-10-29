package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ClearAssistantMemoryDialogUiTest {
	@Test
	fun when_confirmIsTapped_then_confirmsTheClearing() = runTuIndiceUiTest {
		var isConfirmed = false
		setTuIndiceTestContent {
			ClearAssistantMemoryDialog(
				onConfirmClick = { isConfirmed = true },
				onDismissRequest = {}
			)
		}

		assertNodeVisible(AssistantUiTags.ClearAllConfirm)
		onNodeWithText(TITLE).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.ClearAllConfirm).assertTextEquals(CONFIRM_TEXT)
		onNodeWithTag(AssistantUiTags.ClearAllConfirm).performClick()

		assertTrue(isConfirmed)
	}

	@Test
	fun when_cancelIsTapped_then_dismissesWithoutClearing() = runTuIndiceUiTest {
		var isConfirmed = false
		var isDismissed = false
		setTuIndiceTestContent {
			ClearAssistantMemoryDialog(
				onConfirmClick = { isConfirmed = true },
				onDismissRequest = { isDismissed = true }
			)
		}

		assertNodeVisible(AssistantUiTags.ClearAllCancel)
		onNodeWithTag(AssistantUiTags.ClearAllCancel).performClick()

		assertTrue(isDismissed)
		assertFalse(isConfirmed)
	}

	private companion object {
		const val TITLE = "¿Eliminar todo lo que recuerda el asistente?"
		const val CONFIRM_TEXT = "Eliminar"
	}
}
