package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatAttachmentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class AssistantChatAttachmentTrayViewUiTest {
	@Test
	fun when_theXOfAPhotoIsPressed_then_ThePhotoIsTakenOut() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(attachments = listOf(photo()), onAction = { actions += it })
		}

		onNodeWithTag(AssistantUiTags.AttachmentRemove).performClick()

		assertEquals("l1", assertIs<AssistantChat.Action.RemoveAttachment>(actions.single()).localId)
	}

	@Test
	fun when_aPhotoFailedAndCanRetry_then_ItsRetryIsOffered() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(
				attachments = listOf(photo(isFailed = true, canRetry = true, failureText = "No se pudo adjuntar.")),
				onAction = { actions += it }
			)
		}

		onNodeWithTag(AssistantUiTags.AttachmentRetry).performClick()

		assertEquals("l1", assertIs<AssistantChat.Action.RetryAttachment>(actions.single()).localId)
	}

	// The × of a failed photo that can retry is pressed where its touch target covers the retry's. The × is drawn
	// over the veil, so such a tap is the ×'s: it takes the photo out, and never retries it.
	@Test
	fun when_theXOfAFailedPhotoIsPressedWhereItCoversTheRetry_then_ThePhotoIsTakenOut() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(
				attachments = listOf(photo(isFailed = true, canRetry = true, failureText = "No se pudo adjuntar.")),
				onAction = { actions += it }
			)
		}

		// The lower-left part of the × is the part the retry's touch target covers too.
		onNodeWithTag(AssistantUiTags.AttachmentRemove).performTouchInput { click(percentOffset(0.2f, 0.8f)) }

		assertEquals("l1", assertIs<AssistantChat.Action.RemoveAttachment>(actions.single()).localId)
	}

	@Test
	fun when_aPhotoFailedAndCannotRetry_then_ItsXTakesItOut_andNoRetryIsOffered() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(
				attachments = listOf(
					photo(isFailed = true, canRetry = false, failureText = "Ya usaste todos los adjuntos de hoy.")
				),
				onAction = { actions += it }
			)
		}

		assertEquals(0, onAllNodesWithTag(AssistantUiTags.AttachmentRetry).fetchSemanticsNodes().size)
		onNodeWithTag(AssistantUiTags.AttachmentRemove).performClick()

		assertEquals("l1", assertIs<AssistantChat.Action.RemoveAttachment>(actions.single()).localId)
	}

	@Test
	fun when_aPhotoIsSent_then_ItsVeilSaysItIsBeingSent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(
				attachments = listOf(photo(isUploading = true)),
				onAction = {}
			)
		}

		onNodeWithContentDescription("Subiendo foto.jpg").assertIsDisplayed()
	}

	@Test
	fun when_aPhotoFailed_then_ItsVeilSaysItWasNotSentWithoutTheReason() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(
				attachments = listOf(photo(isFailed = true, canRetry = true, failureText = "No se pudo adjuntar.")),
				onAction = {}
			)
		}

		onNodeWithContentDescription("foto.jpg: no se pudo subir").assertIsDisplayed()
	}

	@Test
	fun when_aPhotoFailed_then_ItsReasonShowsUnderTheTray() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(
				attachments = listOf(photo(isFailed = true, canRetry = true, failureText = "No se pudo adjuntar.")),
				onAction = {}
			)
		}

		onNodeWithTag(AssistantUiTags.AttachmentFailure).assertIsDisplayed()
		onNodeWithText("No se pudo adjuntar.").assertIsDisplayed()
	}

	private fun photo(
		isUploading: Boolean = false,
		isFailed: Boolean = false,
		canRetry: Boolean = false,
		failureText: String? = null
	) = ChatAttachmentItem(
		localId = "l1",
		name = "foto.jpg",
		thumbnail = null,
		isUploading = isUploading,
		isFailed = isFailed,
		canRetry = canRetry,
		failureText = failureText
	)
}
