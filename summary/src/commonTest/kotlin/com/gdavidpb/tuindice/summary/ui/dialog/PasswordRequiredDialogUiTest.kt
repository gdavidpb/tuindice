package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PasswordRequiredDialogUiTest {
	@Test
	fun when_credentialsAreOutdated_then_asksToUpdateThePasswordAndConfirms() = runTuIndiceUiTest {
		var updatePasswordClicks = 0

		setTuIndiceTestContent {
			PasswordRequiredDialog(
				syncStatus = SyncStatus.OutdatedCredentials,
				onUpdatePasswordClick = { updatePasswordClicks++ },
				onDismissRequest = {}
			)
		}

		onNodeWithText("Actualiza tu contraseña").assertExists()
		onNodeWithText("Actualizar contraseña").assertExists()
		onNodeWithText("Cerrar").assertExists()
		assertNodeVisible(SummaryUiTags.SyncStatusMessage)

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		waitUntil(timeoutMillis = 2_000) { updatePasswordClicks == 1 }
		assertEquals(1, updatePasswordClicks)
	}

	@Test
	fun when_credentialsAreMissing_then_asksToEnterThePasswordAgain() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PasswordRequiredDialog(
				syncStatus = SyncStatus.MissingCredentials,
				onUpdatePasswordClick = {},
				onDismissRequest = {}
			)
		}

		onNodeWithText("Vuelve a ingresar tu contraseña").assertExists()
		onNodeWithText("Ingresar contraseña").assertExists()
		assertNodeVisible(SummaryUiTags.SyncStatusMessage)
	}
}
