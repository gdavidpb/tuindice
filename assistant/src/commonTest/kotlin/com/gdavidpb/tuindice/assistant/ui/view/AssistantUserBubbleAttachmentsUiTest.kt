package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatUserAttachmentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AssistantUserBubbleAttachmentsUiTest {
	@Test
	fun when_aSentMessageHasOnlyPhotos_then_ThePhotosShow_andNoTextBubbleDoes() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantUserBubbleView(item = message(photos = 2))
		}

		assertNodeVisible(AssistantUiTags.Message.attachment("u1", 0))
		assertNodeVisible(AssistantUiTags.Message.attachment("u1", 1))
		assertNodeHidden(AssistantUiTags.Message.bubble("u1"))
	}

	// A photo is tagged by its message and its place among the message's attachments, so a test or a reader can find it.
	@Test
	fun when_aSentMessageHasPhotos_then_EachIsTaggedByItsMessageAndItsPlace() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantUserBubbleView(item = message(photos = 2))
		}

		assertNodeVisible(AssistantUiTags.Message.attachment("u1", 1))
		assertNodeHidden(AssistantUiTags.Message.attachment("u1", 2))
	}

	private fun message(photos: Int) = ChatItem.User(
		id = "u1",
		text = "",
		isFailed = false,
		attachments = List(photos) { index ->
			ChatUserAttachmentItem(
				name = "foto-$index.jpg",
				thumbnail = null,
				image = null
			)
		}
	)
}
