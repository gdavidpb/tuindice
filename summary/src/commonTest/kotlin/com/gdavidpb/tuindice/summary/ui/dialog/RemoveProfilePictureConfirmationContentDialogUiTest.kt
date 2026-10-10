package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The content dialog binds the module's strings to [RemoveProfilePictureConfirmationDialog]:
 * these cases pin which string lands on which slot and that the two callbacks are not swapped.
 */
@OptIn(ExperimentalTestApi::class)
class RemoveProfilePictureConfirmationContentDialogUiTest {
	@Test
	fun when_dialogIsShown_then_bindsTheModuleStringsToTitleMessageAndButtons() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RemoveProfilePictureConfirmationContentDialog(
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(SummaryUiTags.RemoveProfilePictureMessage)
		onNodeWithTag(BaseUiTags.ConfirmationDialogTitle).assertTextEquals("Eliminar foto")
		onNodeWithTag(SummaryUiTags.RemoveProfilePictureMessage)
			.assertTextEquals("¿Estás seguro de que deseas eliminar tu foto de perfil?")
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).assertTextEquals("Eliminar")
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).assertTextEquals("Cancelar")
	}

	@Test
	fun when_removeButtonIsTapped_then_confirmsWithoutDismissing() = runTuIndiceUiTest {
		var confirmClicks = 0
		var dismissals = 0

		setTuIndiceTestContent {
			RemoveProfilePictureConfirmationContentDialog(
				onConfirmClick = { confirmClicks++ },
				onDismissRequest = { dismissals++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		waitUntil(timeoutMillis = 2_000) { confirmClicks == 1 }
		waitForIdle()

		// The caller closes it once the removal is under way; the sheet must stay until then.
		assertEquals(1, confirmClicks)
		assertEquals(0, dismissals)
		assertNodeVisible(SummaryUiTags.RemoveProfilePictureMessage)
	}

	@Test
	fun when_cancelButtonIsTapped_then_dismissesWithoutConfirming() = runTuIndiceUiTest {
		var confirmClicks = 0
		var dismissals = 0

		setTuIndiceTestContent {
			RemoveProfilePictureConfirmationContentDialog(
				onConfirmClick = { confirmClicks++ },
				onDismissRequest = { dismissals++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogNegativeButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

		waitUntil(timeoutMillis = 2_000) { dismissals > 0 }

		assertTrue(dismissals > 0)
		assertEquals(0, confirmClicks)
	}
}
