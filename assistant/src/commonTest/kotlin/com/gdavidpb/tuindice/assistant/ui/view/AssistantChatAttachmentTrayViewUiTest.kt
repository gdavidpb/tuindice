package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatAttachmentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantChatAttachmentTrayViewUiTest {
	@Test
	fun when_aPdfIsInTheTray_then_ItsNameAndItsPagesShow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(attachments = listOf(pdf(detail = "12 páginas")), onAction = {})
		}

		onNodeWithText("programa.pdf").assertIsDisplayed()
		onNodeWithText("12 páginas").assertIsDisplayed()
	}

	@Test
	fun when_theXOfAnAttachmentIsPressed_then_TheAttachmentIsTakenOut() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(attachments = listOf(pdf()), onAction = { actions += it })
		}

		onNodeWithTag(AssistantUiTags.AttachmentRemove).performClick()

		assertEquals("l1", assertIs<AssistantChat.Action.RemoveAttachment>(actions.single()).localId)
	}

	@Test
	fun when_anAttachmentFailedAndCanRetry_then_ItsRetryIsOffered() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(
				attachments = listOf(pdf(isFailed = true, canRetry = true, failureText = "No se pudo adjuntar.")),
				onAction = { actions += it }
			)
		}

		onNodeWithTag(AssistantUiTags.AttachmentRetry).performClick()

		assertEquals("l1", assertIs<AssistantChat.Action.RetryAttachment>(actions.single()).localId)
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
	fun when_anAttachmentFailed_then_ItsReasonShowsUnderTheTray() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(
				attachments = listOf(pdf(isFailed = true, canRetry = true, failureText = "No se pudo adjuntar.")),
				onAction = {}
			)
		}

		onNodeWithTag(AssistantUiTags.AttachmentFailure).assertIsDisplayed()
		onNodeWithText("No se pudo adjuntar.").assertIsDisplayed()
	}

	@Test
	fun when_aPdfFailed_then_ItsXDoesNotCoverItsName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(
				attachments = listOf(pdf(detail = "12 páginas", isFailed = true, failureText = "Algo falló.")),
				onAction = {}
			)
		}

		val name = onNodeWithText("programa.pdf").fetchSemanticsNode().boundsInRoot
		val remove = onNodeWithTag(AssistantUiTags.AttachmentRemove).fetchSemanticsNode().boundsInRoot

		assertTrue(name.right <= remove.left, "the name ends at ${name.right}, the × starts at ${remove.left}")
	}

	private fun pdf(
		detail: String? = null,
		isFailed: Boolean = false,
		canRetry: Boolean = false,
		failureText: String? = null
	) = ChatAttachmentItem(
		localId = "l1",
		kind = AssistantAttachmentKind.PDF,
		name = "programa.pdf",
		detail = detail,
		thumbnail = null,
		isUploading = false,
		isFailed = isFailed,
		canRetry = canRetry,
		failureText = failureText
	)

	private fun photo(
		isUploading: Boolean = false,
		isFailed: Boolean = false,
		canRetry: Boolean = false,
		failureText: String? = null
	) = ChatAttachmentItem(
		localId = "l1",
		kind = AssistantAttachmentKind.PHOTO,
		name = "foto.jpg",
		detail = null,
		thumbnail = null,
		isUploading = isUploading,
		isFailed = isFailed,
		canRetry = canRetry,
		failureText = failureText
	)
}
