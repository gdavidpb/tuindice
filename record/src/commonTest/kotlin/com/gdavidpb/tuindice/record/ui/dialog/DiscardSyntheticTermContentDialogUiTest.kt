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
class DiscardSyntheticTermContentDialogUiTest {
	@Test
	fun when_theDialogOpens_then_itSaysWhatLeavingWouldLose() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DiscardSyntheticTermContentDialog(
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogSheet)
		onNodeWithTag(BaseUiTags.ConfirmationDialogTitle).assertTextEquals("¿Descartar cambios?")
		onNodeWithTag(RecordUiTags.DiscardSyntheticTermMessage)
			.assertTextEquals("Se perderán el periodo y las materias de este trimestre de proyección.")
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).assertTextEquals("Descartar")
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).assertTextEquals("Cancelar")
	}

	@Test
	fun when_discardIsTapped_then_itIsReportedOnce_andTheSheetIsLeftToTheCaller() = runTuIndiceUiTest {
		var confirmClicks = 0
		var dismissRequests = 0

		setTuIndiceTestContent {
			DiscardSyntheticTermContentDialog(
				onConfirmClick = { confirmClicks++ },
				onDismissRequest = { dismissRequests++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		waitForIdle()

		assertEquals(1, confirmClicks)
		// Whoever asked decides what closing means (leaving the form): the sheet does not hide itself.
		assertEquals(0, dismissRequests)
	}

	@Test
	fun when_cancelIsTapped_then_theSheetAsksToClose_andNothingIsDiscarded() = runTuIndiceUiTest {
		var confirmClicks = 0
		var dismissRequests = 0

		setTuIndiceTestContent {
			DiscardSyntheticTermContentDialog(
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
