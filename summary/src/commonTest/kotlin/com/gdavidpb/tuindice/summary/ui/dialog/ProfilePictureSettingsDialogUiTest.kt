package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ProfilePictureSettingsDialogUiTest {
	@Test
	fun when_pickActionTapped_then_invokesPickCallbackAndDismiss() = runTuIndiceUiTest {
		var pickClicks = 0
		var dismissClicks = 0

		setTuIndiceTestContent {
			ProfilePictureSettingsDialog(
				showRemove = true,
				isCameraAvailable = true,
				titleText = "Mi foto de perfil",
				pickPictureLabel = "Subir foto",
				takePictureLabel = "Tomar foto",
				removePictureLabel = "Remover foto",
				onPickPictureClick = { pickClicks++ },
				onTakePictureClick = {},
				onRemovePictureClick = {},
				onDismissRequest = { dismissClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePicturePickAction)

		onNodeWithText("Subir foto").performClick()

		waitUntil(timeoutMillis = 2_000) {
			pickClicks == 1 && dismissClicks == 1
		}

		assertEquals(1, pickClicks)
		assertEquals(1, dismissClicks)
	}

	@Test
	fun when_pickActionTapped_then_dismissesBeforeInvokingPickCallback() = runTuIndiceUiTest {
		val callbackOrder = mutableListOf<String>()

		setTuIndiceTestContent {
			ProfilePictureSettingsDialog(
				showRemove = true,
				isCameraAvailable = true,
				titleText = "Mi foto de perfil",
				pickPictureLabel = "Subir foto",
				takePictureLabel = "Tomar foto",
				removePictureLabel = "Remover foto",
				onPickPictureClick = { callbackOrder += "pick" },
				onTakePictureClick = {},
				onRemovePictureClick = {},
				onDismissRequest = { callbackOrder += "dismiss" }
			)
		}

		onNodeWithText("Subir foto").performClick()

		waitUntil(timeoutMillis = 2_000) {
			callbackOrder.size == 2
		}

		assertEquals(listOf("dismiss", "pick"), callbackOrder)
	}

	@Test
	fun when_cameraIsUnavailable_then_hidesTakeAction() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfilePictureSettingsDialog(
				showRemove = true,
				isCameraAvailable = false,
				titleText = "Mi foto de perfil",
				pickPictureLabel = "Subir foto",
				takePictureLabel = "Tomar foto",
				removePictureLabel = "Remover foto",
				onPickPictureClick = {},
				onTakePictureClick = {},
				onRemovePictureClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePicturePickAction)
		assertNodeHidden(SummaryUiTags.ProfilePictureTakeAction)
		assertNodeVisible(SummaryUiTags.ProfilePictureRemoveAction)
	}

	@Test
	fun when_takeActionTapped_then_invokesTakeCallbackAndDismiss() = runTuIndiceUiTest {
		var takeClicks = 0
		var dismissClicks = 0

		setTuIndiceTestContent {
			ProfilePictureSettingsDialog(
				showRemove = true,
				isCameraAvailable = true,
				titleText = "Mi foto de perfil",
				pickPictureLabel = "Subir foto",
				takePictureLabel = "Tomar foto",
				removePictureLabel = "Remover foto",
				onPickPictureClick = {},
				onTakePictureClick = { takeClicks++ },
				onRemovePictureClick = {},
				onDismissRequest = { dismissClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePictureTakeAction)
		onNodeWithText("Tomar foto").performClick()

		waitUntil(timeoutMillis = 2_000) {
			takeClicks == 1 && dismissClicks == 1
		}

		assertEquals(1, takeClicks)
		assertEquals(1, dismissClicks)
	}

	@Test
	fun when_removeActionTapped_then_invokesRemoveCallbackAndDismiss() = runTuIndiceUiTest {
		var removeClicks = 0
		var dismissClicks = 0

		setTuIndiceTestContent {
			ProfilePictureSettingsDialog(
				showRemove = true,
				isCameraAvailable = true,
				titleText = "Mi foto de perfil",
				pickPictureLabel = "Subir foto",
				takePictureLabel = "Tomar foto",
				removePictureLabel = "Remover foto",
				onPickPictureClick = {},
				onTakePictureClick = {},
				onRemovePictureClick = { removeClicks++ },
				onDismissRequest = { dismissClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePictureRemoveAction)
		onNodeWithText("Remover foto").performClick()

		waitUntil(timeoutMillis = 2_000) {
			removeClicks == 1 && dismissClicks == 1
		}

		assertEquals(1, removeClicks)
		assertEquals(1, dismissClicks)
	}

	@Test
	fun when_removeOptionIsDisabled_then_hidesRemoveAction() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfilePictureSettingsDialog(
				showRemove = false,
				isCameraAvailable = true,
				titleText = "Mi foto de perfil",
				pickPictureLabel = "Subir foto",
				takePictureLabel = "Tomar foto",
				removePictureLabel = "Remover foto",
				onPickPictureClick = {},
				onTakePictureClick = {},
				onRemovePictureClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePicturePickAction)
		assertNodeVisible(SummaryUiTags.ProfilePictureTakeAction)
		assertNodeHidden(SummaryUiTags.ProfilePictureRemoveAction)
	}
}
