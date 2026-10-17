package com.gdavidpb.tuindice.about.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DeleteAssistantConversationsDialogUiTest {
	@Test
	fun when_confirmIsTapped_then_confirmsTheDeletion() = runTuIndiceUiTest {
		var isConfirmed = false
		setTuIndiceTestContent {
			DeleteAssistantConversationsDialog(
				onConfirmClick = { isConfirmed = true },
				onDismissRequest = {}
			)
		}

		assertNodeVisible(AboutUiTags.DeleteAssistantConversationsConfirm)
		onNodeWithText(TITLE).assertIsDisplayed()
		onNodeWithTag(AboutUiTags.DeleteAssistantConversationsConfirm).assertTextEquals(CONFIRM_TEXT)
		onNodeWithTag(AboutUiTags.DeleteAssistantConversationsConfirm).performClick()

		assertTrue(isConfirmed)
	}

	@Test
	fun when_cancelIsTapped_then_dismissesWithoutDeleting() = runTuIndiceUiTest {
		var isConfirmed = false
		var isDismissed = false
		setTuIndiceTestContent {
			DeleteAssistantConversationsDialog(
				onConfirmClick = { isConfirmed = true },
				onDismissRequest = { isDismissed = true }
			)
		}

		assertNodeVisible(AboutUiTags.DeleteAssistantConversationsCancel)
		onNodeWithTag(AboutUiTags.DeleteAssistantConversationsCancel).performClick()

		assertTrue(isDismissed)
		assertFalse(isConfirmed)
	}

	private companion object {
		const val TITLE = "¿Borrar tus conversaciones con el asistente?"
		const val CONFIRM_TEXT = "Borrar"
	}
}
