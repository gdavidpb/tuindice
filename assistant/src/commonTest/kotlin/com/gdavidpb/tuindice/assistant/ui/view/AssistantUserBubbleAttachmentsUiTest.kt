package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatUserAttachmentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AssistantUserBubbleAttachmentsUiTest {
	@Test
	fun when_aSentMessageHasOnlyAPdf_then_ItsCardShowsAndNoTextBubbleDoes() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantUserBubbleView(
				item = ChatItem.User(
					id = "u1",
					text = "",
					isFailed = false,
					attachments = listOf(
						ChatUserAttachmentItem(
							kind = AssistantAttachmentKind.PDF,
							name = "programa.pdf",
							detail = "12 páginas",
							thumbnail = null,
							image = null
						)
					)
				)
			)
		}

		onNodeWithText("programa.pdf").assertIsDisplayed()
		assertNodeHidden(AssistantUiTags.Message.bubble("u1"))
	}
}
