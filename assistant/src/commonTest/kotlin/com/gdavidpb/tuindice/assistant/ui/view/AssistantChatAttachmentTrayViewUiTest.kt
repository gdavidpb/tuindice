package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
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
	fun when_anAttachmentFailed_then_ItsRetryIsOffered() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatAttachmentTrayView(
				attachments = listOf(pdf(isFailed = true, failureText = "No se pudo adjuntar.")),
				onAction = { actions += it }
			)
		}

		onNodeWithTag(AssistantUiTags.AttachmentRetry).performClick()

		assertEquals("l1", assertIs<AssistantChat.Action.RetryAttachment>(actions.single()).localId)
	}

	private fun pdf(
		detail: String? = null,
		isFailed: Boolean = false,
		failureText: String? = null
	) = ChatAttachmentItem(
		localId = "l1",
		kind = AssistantAttachmentKind.PDF,
		name = "programa.pdf",
		detail = detail,
		thumbnail = null,
		isUploading = false,
		isFailed = isFailed,
		failureText = failureText
	)
}
