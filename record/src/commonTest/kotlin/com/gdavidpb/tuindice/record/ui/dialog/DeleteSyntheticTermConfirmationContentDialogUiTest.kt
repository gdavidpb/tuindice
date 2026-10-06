package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DeleteSyntheticTermConfirmationContentDialogUiTest {
	@Test
	fun when_theDialogOpens_then_itAsksInTheAppsOwnWords_whatDeletingTakesAway() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DeleteSyntheticTermConfirmationContentDialog(
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogSheet)
		onNodeWithTag(BaseUiTags.ConfirmationDialogTitle).assertTextEquals("Eliminar trimestre")
		onNodeWithTag(RecordUiTags.DeleteSyntheticTermMessage)
			.assertTextEquals("Se eliminarán las materias y cambios de este trimestre de proyección.")
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).assertTextEquals("Eliminar")
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).assertTextEquals("Cancelar")
	}

	@Test
	fun when_deleteIsTapped_then_theConfirmationIsReported_andTheSheetIsLeftToTheCaller() = runTuIndiceUiTest {
		var confirmClicks = 0
		var dismissRequests = 0

		setTuIndiceTestContent {
			DeleteSyntheticTermConfirmationContentDialog(
				onConfirmClick = { confirmClicks++ },
				onDismissRequest = { dismissRequests++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		waitForIdle()

		assertEquals(1, confirmClicks)
		// Deleting is what closes it: the dialog does not dismiss itself on the way.
		assertEquals(0, dismissRequests)
	}

	@Test
	fun when_cancelIsTapped_then_theSheetAsksToClose_andNothingIsConfirmed() = runTuIndiceUiTest {
		var confirmClicks = 0
		var dismissRequests = 0

		setTuIndiceTestContent {
			DeleteSyntheticTermConfirmationContentDialog(
				onConfirmClick = { confirmClicks++ },
				onDismissRequest = { dismissRequests++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogNegativeButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

		waitUntil(timeoutMillis = 5_000) { dismissRequests > 0 }

		assertTrue(dismissRequests > 0)
		assertEquals(0, confirmClicks)
	}
}
