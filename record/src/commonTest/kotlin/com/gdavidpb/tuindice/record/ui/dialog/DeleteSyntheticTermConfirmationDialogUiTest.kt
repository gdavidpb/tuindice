package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
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
class DeleteSyntheticTermConfirmationDialogUiTest {
	@Test
	fun when_confirmButtonTapped_then_invokesConfirmCallbackWithoutDismissCallback() = runTuIndiceUiTest {
		var confirmClicks = 0
		var dismissClicks = 0

		setTuIndiceTestContent {
			DeleteSyntheticTermConfirmationDialog(
				titleText = "Eliminar trimestre",
				messageText = "Se eliminaran las materias y cambios de este trimestre de proyeccion.",
				confirmText = "Eliminar",
				cancelText = "Cancelar",
				onConfirmClick = { confirmClicks++ },
				onDismissRequest = { dismissClicks++ }
			)
		}

		assertNodeVisible(RecordUiTags.DeleteSyntheticTermMessage)
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			confirmClicks == 1
		}

		assertEquals(1, confirmClicks)
		assertEquals(0, dismissClicks)
	}

	@Test
	fun when_cancelButtonTapped_then_invokesDismissCallback() = runTuIndiceUiTest {
		var dismissClicks = 0

		setTuIndiceTestContent {
			DeleteSyntheticTermConfirmationDialog(
				titleText = "Eliminar trimestre",
				messageText = "Se eliminaran las materias y cambios de este trimestre de proyeccion.",
				confirmText = "Eliminar",
				cancelText = "Cancelar",
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
}
