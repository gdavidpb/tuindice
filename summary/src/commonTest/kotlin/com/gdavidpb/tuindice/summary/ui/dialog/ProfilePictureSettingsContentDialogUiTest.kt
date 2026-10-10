package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
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

/**
 * The content dialog only binds the module's strings to [ProfilePictureSettingsDialog]:
 * what is asserted here is that binding (each label on its own action, each callback on
 * its own row), not the sheet behaviour the stateless dialog already covers.
 */
@OptIn(ExperimentalTestApi::class)
class ProfilePictureSettingsContentDialogUiTest {
	@Test
	fun when_allActionsAreAvailable_then_showsTheModuleTitleAndOneLabelPerAction() = runTuIndiceUiTest {
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

		assertNodeVisible(BaseUiTags.ConfirmationDialogTitle)
		onNodeWithTag(BaseUiTags.ConfirmationDialogTitle).assertTextEquals("Mi foto de perfil")
		assertActionLabel(tag = SummaryUiTags.ProfilePicturePickAction, label = "Subir foto")
		assertActionLabel(tag = SummaryUiTags.ProfilePictureTakeAction, label = "Tomar foto")
		assertActionLabel(tag = SummaryUiTags.ProfilePictureRemoveAction, label = "Eliminar foto")
	}

	@Test
	fun when_cameraIsUnavailableAndThereIsNoPicture_then_onlyThePickActionRemains() = runTuIndiceUiTest {
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
		onNodeWithText("Tomar foto").assertDoesNotExist()
		onNodeWithText("Eliminar foto").assertDoesNotExist()
	}

	@Test
	fun when_eachActionLabelIsTapped_then_forwardsOnlyItsOwnCallback() = runTuIndiceUiTest {
		val calls = mutableListOf<String>()

		setTuIndiceTestContent {
			ProfilePictureSettingsContentDialog(
				showRemove = true,
				isCameraAvailable = true,
				onPickPictureClick = { calls += "pick" },
				onTakePictureClick = { calls += "take" },
				onRemovePictureClick = { calls += "remove" },
				onDismissRequest = { calls += "dismiss" }
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePictureRemoveAction)

		onNodeWithText("Tomar foto").performClick()
		waitUntil(timeoutMillis = 2_000) { calls.size == 1 }
		assertEquals(listOf("take"), calls)

		onNodeWithText("Eliminar foto").performClick()
		waitUntil(timeoutMillis = 2_000) { calls.size == 2 }
		assertEquals(listOf("take", "remove"), calls)

		onNodeWithText("Subir foto").performClick()
		waitUntil(timeoutMillis = 2_000) { calls.size == 3 }
		assertEquals(listOf("take", "remove", "pick"), calls)
	}

	// The tag sits on a box around the clickable row, so the label is one level below it.
	private fun ComposeUiTest.assertActionLabel(tag: String, label: String) {
		onNode(hasText(label) and hasAnyAncestor(hasTestTag(tag))).assertExists()
	}
}
