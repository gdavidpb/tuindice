package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantChatComposerViewUiTest {
	@Test
	fun when_aTurnCanBeStopped_then_theStopButtonTakesTheSendPlace_andStoppingCancelsTheTurn() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false, isStoppable = true),
				isCameraAvailable = false,
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.StopButton)
		onNodeWithContentDescription(STOP_DESCRIPTION).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.SendButton).assertDoesNotExist()

		onNodeWithTag(AssistantUiTags.StopButton).performClick()

		assertIs<AssistantChat.Action.CancelTurn>(actions.single())
	}

	@Test
	fun when_noTurnCanBeStopped_then_theSendButtonIsThere_andNoStopButton() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "hola", isEnabled = true, isSendEnabled = true),
				isCameraAvailable = false,
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.SendButton)
		onNodeWithTag(AssistantUiTags.StopButton).assertDoesNotExist()

		onNodeWithTag(AssistantUiTags.SendButton).performClick()

		assertIs<AssistantChat.Action.Send>(actions.single())
	}

	@Test
	fun when_theComposerIsOn_then_theClipIsInsideTheField_underTheText_andOpensThePhotoMenu() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false, isPhotoEnabled = true),
				isCameraAvailable = true,
				onAction = {}
			)
		}

		onNode(hasTestTag(AssistantUiTags.PhotoButton) and hasAnyAncestor(hasTestTag(AssistantUiTags.Composer)))
			.assertIsDisplayed()
		onNodeWithContentDescription(PHOTO_DESCRIPTION).assertIsDisplayed()
		val field = onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().boundsInRoot
		val clip = onNodeWithTag(AssistantUiTags.PhotoButton).fetchSemanticsNode().boundsInRoot
		val send = onNodeWithTag(AssistantUiTags.SendButton).fetchSemanticsNode().boundsInRoot
		assertTrue(clip.left >= field.left && clip.right <= field.right)
		assertTrue(clip.top >= field.top && clip.bottom <= field.bottom)
		// The clip's 48 dp touch target sits 4 dp in from the start of the field. The tag is on the 40 dp icon button,
		// centred in that target, so the button itself starts 4 dp further in.
		assertEquals(with(density) { 8.dp.toPx() }, clip.left - field.left, absoluteTolerance = PIXEL_TOLERANCE)
		// The send button is inside the field too, at the end of the clip's row.
		assertTrue(send.left >= clip.right && send.right <= field.right)
		assertEquals(clip.center.y, send.center.y, absoluteTolerance = PIXEL_TOLERANCE)
		// The text has the field's width to itself: it starts 12 dp in from the field, above the clip's row.
		val text = onNodeWithText(PLACEHOLDER, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		assertEquals(with(density) { 12.dp.toPx() }, text.left - field.left, absoluteTolerance = PIXEL_TOLERANCE)
		assertTrue(
			clip.top >= text.bottom,
			"the clip's row starts at ${clip.top}, above the text that ends at ${text.bottom}"
		)

		assertNodeEnabled(AssistantUiTags.PhotoButton)
		onNodeWithTag(AssistantUiTags.PhotoButton).performClick()
		assertNodeVisible(AssistantUiTags.PhotoTake)
		assertNodeVisible(AssistantUiTags.PhotoPick)
	}

	@Test
	fun when_aTurnOrAPhotoIsBeingRead_then_theClipIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false, isPhotoEnabled = false),
				isCameraAvailable = true,
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.PhotoButton)
		assertNodeDisabled(AssistantUiTags.PhotoButton)
	}

	@Test
	fun when_theFieldIsOff_then_theClipIsNotThere() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "", isEnabled = false, isSendEnabled = false, isPhotoEnabled = true),
				isCameraAvailable = true,
				onAction = {}
			)
		}

		assertNodeHidden(AssistantUiTags.PhotoButton)
		assertNodeVisible(AssistantUiTags.SendButton)
	}

	@Test
	fun when_theFieldIsOff_then_theTextDoesNotMove() = runTuIndiceUiTest {
		var composer by mutableStateOf(
			ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false, isPhotoEnabled = true)
		)
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = composer,
				isCameraAvailable = true,
				onAction = {}
			)
		}

		// The clip is there, and the text starts after it; then the field turns off and the clip goes.
		val textWithClip = onNodeWithText(PLACEHOLDER, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
		composer = composer.copy(isEnabled = false)
		waitForIdle()

		assertNodeHidden(AssistantUiTags.PhotoButton)
		val textWithoutClip = onNodeWithText(PLACEHOLDER, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
		assertEquals(textWithClip, textWithoutClip, absoluteTolerance = PIXEL_TOLERANCE)
	}

	@Test
	fun when_theFieldHasOneLine_then_theSendButtonSitsUnderTheText_atTheEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false, isPhotoEnabled = true),
				isCameraAvailable = false,
				onAction = {}
			)
		}

		val field = onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().boundsInRoot
		val send = onNodeWithTag(AssistantUiTags.SendButton).fetchSemanticsNode().boundsInRoot
		val text = onNodeWithText(PLACEHOLDER, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(
			send.top >= text.bottom,
			"the send button starts at ${send.top}, above the text that ends at ${text.bottom}"
		)
		assertTrue(send.left > field.center.x && send.right <= field.right)
	}

	@Test
	fun when_theFieldGrows_then_theSendButtonStaysInTheRowAtTheBottom() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(
					text = "una\ndos\ntres",
					isEnabled = true,
					isSendEnabled = true,
					isPhotoEnabled = true
				),
				isCameraAvailable = false,
				onAction = {}
			)
		}

		val field = onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().boundsInRoot
		val send = onNodeWithTag(AssistantUiTags.SendButton).fetchSemanticsNode().boundsInRoot
		val oneLineFieldHeight = with(density) { OutlinedTextFieldDefaults.MinHeight.toPx() }

		assertTrue(field.height > oneLineFieldHeight)
		assertEquals(field.bottom - oneLineFieldHeight / 2, send.center.y, absoluteTolerance = PIXEL_TOLERANCE)
	}

	@Test
	fun when_theSendClearsATextTheStudentHadEmptiedBefore_then_theFieldIsEmptied() = runTuIndiceUiTest {
		var composer by mutableStateOf(ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false))
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = composer,
				isCameraAvailable = false,
				// The machine echoes every edit back, as the view model does.
				onAction = { action ->
					if (action is AssistantChat.Action.ChangeText) composer = composer.copy(text = action.text)
				}
			)
		}

		// The student writes, empties the field and writes again, so the field has reported the empty text before.
		onNodeWithTag(AssistantUiTags.Composer).performTextInput("a")
		onNodeWithTag(AssistantUiTags.Composer).performTextClearance()
		onNodeWithTag(AssistantUiTags.Composer).performTextInput("hola")
		waitForIdle()
		assertEquals("hola", composerText())

		// The send clears the text under a new revision.
		composer = composer.copy(text = "", textRevision = composer.textRevision + 1)
		waitForIdle()

		assertEquals("", composerText())
	}

	@Test
	fun when_theTextIsEmpty_then_theSendButtonIsOff_andInsideTheField() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false),
				isCameraAvailable = false,
				onAction = {}
			)
		}

		assertNodeDisabled(AssistantUiTags.SendButton)
		val field = onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().boundsInRoot
		val send = onNodeWithTag(AssistantUiTags.SendButton).fetchSemanticsNode().boundsInRoot
		assertTrue(send.left >= field.left && send.right <= field.right)
		assertTrue(send.top >= field.top && send.bottom <= field.bottom)
	}

	@Test
	fun when_theTextIsThere_then_theSendButtonIsOn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "hola", isEnabled = true, isSendEnabled = true),
				isCameraAvailable = false,
				onAction = {}
			)
		}

		assertNodeEnabled(AssistantUiTags.SendButton)
	}

	@Test
	fun when_aTurnCanBeStopped_then_theStopButtonIsInsideTheField() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatComposerView(
				composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false, isStoppable = true),
				isCameraAvailable = false,
				onAction = {}
			)
		}

		val field = onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().boundsInRoot
		val stop = onNodeWithTag(AssistantUiTags.StopButton).fetchSemanticsNode().boundsInRoot
		assertTrue(stop.left >= field.left && stop.right <= field.right)
	}

	private fun ComposeUiTest.composerText(): String =
		onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().config[SemanticsProperties.EditableText].text

	private companion object {
		const val STOP_DESCRIPTION = "Detener respuesta"
		const val PHOTO_DESCRIPTION = "Agregar evaluaciones desde una foto"
		const val PLACEHOLDER = "Escribe tu pregunta"
		const val PIXEL_TOLERANCE = 1f
	}
}
