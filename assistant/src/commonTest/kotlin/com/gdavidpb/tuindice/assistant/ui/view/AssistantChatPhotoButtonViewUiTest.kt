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
class AssistantChatPhotoButtonViewUiTest {
	@Test
	fun when_theButtonIsOn_then_itOpensTheMenuWithTheCameraAndTheGallery() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatPhotoButtonView(
				isEnabled = true,
				isCameraAvailable = true,
				onAction = {},
				modifier = Modifier
			)
		}

		assertNodeHidden(AssistantUiTags.PhotoTake)
		onNodeWithTag(AssistantUiTags.PhotoButton).performClick()

		assertNodeVisible(AssistantUiTags.PhotoTake)
		assertNodeVisible(AssistantUiTags.PhotoPick)
	}

	@Test
	fun when_theDeviceHasNoCamera_then_theMenuOffersOnlyTheGallery() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatPhotoButtonView(
				isEnabled = true,
				isCameraAvailable = false,
				onAction = {},
				modifier = Modifier
			)
		}

		onNodeWithTag(AssistantUiTags.PhotoButton).performClick()

		assertNodeHidden(AssistantUiTags.PhotoTake)
		assertNodeVisible(AssistantUiTags.PhotoPick)
	}

	@Test
	fun when_theGalleryIsPicked_then_itRequestsAPhotoFromTheGallery() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatPhotoButtonView(
				isEnabled = true,
				isCameraAvailable = true,
				onAction = { actions.add(it) },
				modifier = Modifier
			)
		}

		onNodeWithTag(AssistantUiTags.PhotoButton).performClick()
		onNodeWithTag(AssistantUiTags.PhotoPick).performClick()

		assertEquals(false, assertIs<AssistantChat.Action.RequestPhoto>(actions.single()).fromCamera)
	}

	@Test
	fun when_aTurnOrAPhotoIsBeingRead_then_theButtonIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatPhotoButtonView(
				isEnabled = false,
				isCameraAvailable = true,
				onAction = {},
				modifier = Modifier
			)
		}

		assertNodeVisible(AssistantUiTags.PhotoButton)
		assertNodeDisabled(AssistantUiTags.PhotoButton)
	}
}
