package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryDialogScreenUiTest {
	@Test
	fun when_removeConfirmationAccepted_then_invokesConfirmCallback() = runTuIndiceUiTest {
		var confirmClicks = 0

		setTuIndiceTestContent {
			RemoveProfilePictureConfirmationContentDialog(
				onConfirmClick = { confirmClicks++ },
				onDismissRequest = {}
			)
		}

		assertNodeVisible(SummaryUiTags.RemoveProfilePictureMessage)
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			confirmClicks == 1
		}

		assertEquals(1, confirmClicks)
	}

	@Test
	fun when_removeConfirmationCancelled_then_invokesDismissCallback() = runTuIndiceUiTest {
		var dismissClicks = 0

		setTuIndiceTestContent {
			RemoveProfilePictureConfirmationContentDialog(
				onConfirmClick = {},
				onDismissRequest = { dismissClicks++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogNegativeButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			dismissClicks > 0
		}

		assertTrue(dismissClicks > 0)
	}

	@Test
	fun when_profilePictureSettingsShownWithAllOptions_then_displaysAllActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfilePictureSettingsContentDialog(
				showRemove = true,
				isCameraAvailable = true,
				onPickPictureClick = {},
				onTakePictureClick = {},
				onRemovePictureClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePicturePickAction)
		assertNodeVisible(SummaryUiTags.ProfilePictureTakeAction)
		assertNodeVisible(SummaryUiTags.ProfilePictureRemoveAction)
	}

	@Test
	fun when_pickActionTappedFromContentDialog_then_invokesPickCallback() = runTuIndiceUiTest {
		var pickClicks = 0
		var dismissClicks = 0

		setTuIndiceTestContent {
			ProfilePictureSettingsContentDialog(
				showRemove = true,
				isCameraAvailable = true,
				onPickPictureClick = { pickClicks++ },
				onTakePictureClick = {},
				onRemovePictureClick = {},
				onDismissRequest = { dismissClicks++ }
			)
		}

		onNodeWithText("Subir foto").performClick()

		waitUntil(timeoutMillis = 2_000) {
			pickClicks == 1 && dismissClicks == 1
		}

		assertEquals(1, pickClicks)
		assertEquals(1, dismissClicks)
	}

	@Test
	fun when_removeActionTappedFromContentDialog_then_invokesRemoveCallback() = runTuIndiceUiTest {
		var removeClicks = 0
		var dismissClicks = 0

		setTuIndiceTestContent {
			ProfilePictureSettingsContentDialog(
				showRemove = true,
				isCameraAvailable = true,
				onPickPictureClick = {},
				onTakePictureClick = {},
				onRemovePictureClick = { removeClicks++ },
				onDismissRequest = { dismissClicks++ }
			)
		}

		onNodeWithText("Remover foto").performClick()

		waitUntil(timeoutMillis = 2_000) {
			removeClicks == 1 && dismissClicks == 1
		}

		assertEquals(1, removeClicks)
		assertEquals(1, dismissClicks)
	}

	@Test
	fun when_takeActionTappedFromContentDialog_then_invokesTakeCallback() = runTuIndiceUiTest {
		var takeClicks = 0
		var dismissClicks = 0

		setTuIndiceTestContent {
			ProfilePictureSettingsContentDialog(
				showRemove = true,
				isCameraAvailable = true,
				onPickPictureClick = {},
				onTakePictureClick = { takeClicks++ },
				onRemovePictureClick = {},
				onDismissRequest = { dismissClicks++ }
			)
		}

		onNodeWithText("Tomar foto").performClick()

		waitUntil(timeoutMillis = 2_000) {
			takeClicks == 1 && dismissClicks == 1
		}

		assertEquals(1, takeClicks)
		assertEquals(1, dismissClicks)
	}

	@Test
	fun when_cameraUnavailableAndRemoveHidden_then_onlyPickActionIsVisible() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfilePictureSettingsContentDialog(
				showRemove = false,
				isCameraAvailable = false,
				onPickPictureClick = {},
				onTakePictureClick = {},
				onRemovePictureClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePicturePickAction)
		assertNodeHidden(SummaryUiTags.ProfilePictureTakeAction)
		assertNodeHidden(SummaryUiTags.ProfilePictureRemoveAction)
	}

	@Test
	fun when_cameraAvailableAndRemoveHidden_then_showsPickAndTakeOnly() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfilePictureSettingsContentDialog(
				showRemove = false,
				isCameraAvailable = true,
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
