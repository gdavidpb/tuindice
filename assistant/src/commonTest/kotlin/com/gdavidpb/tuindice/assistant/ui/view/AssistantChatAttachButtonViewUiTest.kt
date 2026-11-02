package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class AssistantChatAttachButtonViewUiTest {
	@Test
	fun when_theButtonIsOn_then_itOpensTheMenuWithTheCameraAndTheGallery() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatAttachButtonView(
				isEnabled = true,
				isCameraAvailable = true,
				onAction = {},
				modifier = Modifier
			)
		}

		assertNodeHidden(AssistantUiTags.AttachTakePhoto)
		onNodeWithTag(AssistantUiTags.AttachButton).performClick()

		assertNodeVisible(AssistantUiTags.AttachTakePhoto)
		assertNodeVisible(AssistantUiTags.AttachPickPhotos)
	}

	@Test
	fun when_theDeviceHasNoCamera_then_theMenuOffersOnlyTheGallery() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatAttachButtonView(
				isEnabled = true,
				isCameraAvailable = false,
				onAction = {},
				modifier = Modifier
			)
		}

		onNodeWithTag(AssistantUiTags.AttachButton).performClick()

		assertNodeHidden(AssistantUiTags.AttachTakePhoto)
		assertNodeVisible(AssistantUiTags.AttachPickPhotos)
	}

	@Test
	fun when_theGalleryIsPicked_then_itRequestsAPhotoFromTheGallery() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatAttachButtonView(
				isEnabled = true,
				isCameraAvailable = true,
				onAction = { actions.add(it) },
				modifier = Modifier
			)
		}

		onNodeWithTag(AssistantUiTags.AttachButton).performClick()
		onNodeWithTag(AssistantUiTags.AttachPickPhotos).performClick()

		val request = assertIs<AssistantChat.Action.RequestAttachment>(actions.single())
		assertEquals(AssistantChat.AttachmentSource.GALLERY, request.source)
	}

	@Test
	fun when_aTurnOrAPhotoIsBeingRead_then_theButtonIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatAttachButtonView(
				isEnabled = false,
				isCameraAvailable = true,
				onAction = {},
				modifier = Modifier
			)
		}

		assertNodeVisible(AssistantUiTags.AttachButton)
		assertNodeDisabled(AssistantUiTags.AttachButton)
	}
}
