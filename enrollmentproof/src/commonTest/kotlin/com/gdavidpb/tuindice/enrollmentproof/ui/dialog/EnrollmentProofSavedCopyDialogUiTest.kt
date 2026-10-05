package com.gdavidpb.tuindice.enrollmentproof.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EnrollmentProofSavedCopyDialogUiTest {
	@Test
	fun when_savedCopyDialogRendered_then_explainsTheFailedDownloadAndOffersBothAnswers() = runTuIndiceUiTest {
		var openSavedClicks = 0
		var dismissCalls = 0

		setTuIndiceTestContent {
			EnrollmentProofSavedCopyDialog(
				onOpenSavedClick = { openSavedClicks++ },
				onDismissRequest = { dismissCalls++ }
			)
		}

		assertNodeVisible(EnrollmentProofUiTags.SavedCopyMessage)
		onNodeWithText("No pudimos descargar tu comprobante").assertIsDisplayed()
		onNodeWithText(
			"Tenemos el que guardamos la última vez, que puede no estar al día. ¿Quieres abrirlo?"
		).assertIsDisplayed()
		onNodeWithText("Abrir guardado").assertIsDisplayed()
		onNodeWithText("Cancelar").assertIsDisplayed()
		// Nothing is answered for the user: the saved copy waits for a tap.
		assertEquals(0, openSavedClicks)
		assertEquals(0, dismissCalls)
	}

	@Test
	fun when_openSavedTapped_then_invokesOpenCallbackAndLeavesTheDismissToTheRoute() = runTuIndiceUiTest {
		var openSavedClicks = 0
		var dismissCalls = 0

		setTuIndiceTestContent {
			EnrollmentProofSavedCopyDialog(
				onOpenSavedClick = { openSavedClicks++ },
				onDismissRequest = { dismissCalls++ }
			)
		}

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		waitForIdle()

		assertEquals(1, openSavedClicks)
		// The route closes the dialog once the file is handed to the viewer.
		assertEquals(0, dismissCalls)
	}

	@Test
	fun when_cancelTapped_then_invokesDismissRequestWithoutOpening() = runTuIndiceUiTest {
		var openSavedClicks = 0
		var dismissCalls = 0

		setTuIndiceTestContent {
			EnrollmentProofSavedCopyDialog(
				onOpenSavedClick = { openSavedClicks++ },
				onDismissRequest = { dismissCalls++ }
			)
		}

		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			dismissCalls > 0
		}

		assertEquals(0, openSavedClicks)
	}
}
