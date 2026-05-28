package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SignOutDialogUiTest {
	@Test
	fun when_idleStateAndConfirmTapped_then_invokesConfirmCallback() = runTuIndiceUiTest {
		var confirmClicks = 0

		setTuIndiceTestContent {
			SignOutDialog(
				state = SignOut.State.Plain,
				titleText = "Cerrar sesion",
				messageText = "Deseas cerrar sesion?",
				confirmText = "Salir",
				secondaryText = null,
				cancelText = "Cancelar",
				onConfirmClick = { confirmClicks++ },
				onSecondaryClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		assertEquals(1, confirmClicks)
	}

	@Test
	fun when_idleStateAndCancelTapped_then_invokesDismissCallback() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			SignOutDialog(
				state = SignOut.State.Plain,
				titleText = "Cerrar sesion",
				messageText = "Deseas cerrar sesion?",
				confirmText = "Salir",
				secondaryText = null,
				cancelText = "Cancelar",
				onConfirmClick = {},
				onSecondaryClick = {},
				onDismissRequest = { dismissCalls++ }
			)
		}

		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			dismissCalls > 0
		}

		assertTrue(dismissCalls >= 1)
	}

	@Test
	fun when_loggingOutState_then_disablesConfirmButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignOutDialog(
				state = SignOut.State.LoggingOut(),
				titleText = "Cerrar sesion",
				messageText = "Deseas cerrar sesion?",
				confirmText = "Salir",
				secondaryText = "Cerrar igualmente",
				cancelText = "Cancelar",
				onConfirmClick = {},
				onSecondaryClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeDisabled(BaseUiTags.ConfirmationDialogPositiveButton)
		assertNodeDisabled(BaseUiTags.ConfirmationDialogNegativeButton)
		assertNodeDisabled(AuthUiTags.SignOutSecondaryButton)
	}
}
