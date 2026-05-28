package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class UpdatePasswordContentDialogUiTest {
	@Test
	fun when_idleStateAndConfirmTapped_then_invokesConfirmCallback() = runTuIndiceUiTest {
		var confirmedPassword = ""

		setTuIndiceTestContent {
			UpdatePasswordContentDialog(
				state = UpdatePassword.State.Idle(password = "abcd"),
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onConfirmClick = { password -> confirmedPassword = password },
				onDismissRequest = {}
			)
		}

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		assertEquals("abcd", confirmedPassword)
	}

	@Test
	fun when_updatingState_then_displaysButtonLoadingAndDisablesPasswordField() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			UpdatePasswordContentDialog(
				state = UpdatePassword.State.Updating(password = "abcd"),
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(AuthUiTags.UpdatePasswordIdleContainer)
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveLoading)
		assertNodeDisabled(AuthUiTags.PasswordTextField)
		assertNodeDisabled(BaseUiTags.ConfirmationDialogPositiveButton)
	}

	@Test
	fun when_idleStateAndLaterTapped_then_invokesDismissCallback() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			UpdatePasswordContentDialog(
				state = UpdatePassword.State.Idle(password = "abcd"),
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onConfirmClick = {},
				onDismissRequest = { dismissCalls++ }
			)
		}

		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()
		assertEquals(1, dismissCalls)
	}
}
