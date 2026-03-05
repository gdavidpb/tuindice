package com.gdavidpb.tuindice.login.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.ui.LoginUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class UpdatePasswordScreenUiTest {
	@Test
	fun when_idleStateAndConfirmTapped_then_invokesConfirmCallback() = runTuIndiceUiTest {
		var confirmedPassword = ""

		setTuIndiceTestContent {
			UpdatePasswordScreen(
				state = UpdatePassword.State.Idle(password = "abcd"),
				onPasswordChange = {},
				onConfirmClick = { password -> confirmedPassword = password },
				onDismissRequest = {}
			)
		}

		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		assertEquals("abcd", confirmedPassword)
	}

	@Test
	fun when_updatingState_then_displaysUpdatingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			UpdatePasswordScreen(
				state = UpdatePassword.State.Updating(password = "abcd"),
				onPasswordChange = {},
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(LoginUiTags.UpdatePasswordUpdatingIndicator)
	}

	@Test
	fun when_idleStateAndLaterTapped_then_invokesDismissCallback() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			UpdatePasswordScreen(
				state = UpdatePassword.State.Idle(password = "abcd"),
				onPasswordChange = {},
				onConfirmClick = {},
				onDismissRequest = { dismissCalls++ }
			)
		}

		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()
		assertEquals(1, dismissCalls)
	}
}
