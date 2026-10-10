package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
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
class SyncStatusInfoDialogUiTest {
	@Test
	fun when_dismissTextIsOmitted_then_showsTitleMessageAndOnlyTheConfirmButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusInfoDialog(
				titleText = "No pudimos sincronizar",
				messageText = "Mantenemos tus datos anteriores.",
				confirmText = "Entendido",
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(SummaryUiTags.SyncStatusMessage)
		onNodeWithTag(BaseUiTags.ConfirmationDialogTitle).assertTextEquals("No pudimos sincronizar")
		onNodeWithTag(SummaryUiTags.SyncStatusMessage).assertTextEquals("Mantenemos tus datos anteriores.")
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).assertTextEquals("Entendido")
		assertNodeHidden(BaseUiTags.ConfirmationDialogNegativeButton)
	}

	@Test
	fun when_confirmButtonIsTapped_then_confirmsOnceAndThenDismisses() = runTuIndiceUiTest {
		val calls = mutableListOf<String>()

		setTuIndiceTestContent {
			SyncStatusInfoDialog(
				titleText = "Actualiza tu contraseña",
				messageText = "Tu contraseña cambió en la universidad.",
				confirmText = "Actualizar contraseña",
				dismissText = "Cerrar",
				onConfirmClick = { calls += "confirm" },
				onDismissRequest = { calls += "dismiss" }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		waitUntil(timeoutMillis = 2_000) { "dismiss" in calls }

		// Confirming is reported first; the dismissal follows once the sheet has hidden.
		assertEquals("confirm", calls.first())
		assertEquals(1, calls.count { call -> call == "confirm" })
	}

	@Test
	fun when_dismissButtonIsTapped_then_dismissesWithoutConfirming() = runTuIndiceUiTest {
		var confirmClicks = 0
		var dismissals = 0

		setTuIndiceTestContent {
			SyncStatusInfoDialog(
				titleText = "Actualiza tu contraseña",
				messageText = "Tu contraseña cambió en la universidad.",
				confirmText = "Actualizar contraseña",
				dismissText = "Cerrar",
				onConfirmClick = { confirmClicks++ },
				onDismissRequest = { dismissals++ }
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogNegativeButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).assertTextEquals("Cerrar")
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

		waitUntil(timeoutMillis = 2_000) { dismissals > 0 }

		assertTrue(dismissals > 0)
		assertEquals(0, confirmClicks)
	}
}
