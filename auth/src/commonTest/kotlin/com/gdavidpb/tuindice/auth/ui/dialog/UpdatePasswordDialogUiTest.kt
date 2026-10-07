package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class UpdatePasswordDialogUiTest {
	@Test
	fun when_idleStateWithoutPassword_then_disablesConfirmButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			UpdatePasswordDialog(
				state = UpdatePassword.State.Idle(password = ""),
				titleText = "Actualizar clave",
				confirmText = "Actualizar",
				laterText = "Luego",
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave",
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeDisabled(AuthUiTags.UpdatePasswordConfirmButton)
	}

	@Test
	fun when_idleStateWithPasswordAndConfirmTapped_then_invokesConfirmCallback() = runTuIndiceUiTest {
		var confirmClicks = 0

		setTuIndiceTestContent {
			UpdatePasswordDialog(
				state = UpdatePassword.State.Idle(password = "1234"),
				titleText = "Actualizar clave",
				confirmText = "Actualizar",
				laterText = "Luego",
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave",
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onConfirmClick = { confirmClicks++ },
				onDismissRequest = {}
			)
		}

		assertNodeEnabled(AuthUiTags.UpdatePasswordConfirmButton)
		onNodeWithTag(AuthUiTags.UpdatePasswordConfirmButton).performClick()

		assertEquals(1, confirmClicks)
	}

	@Test
	fun when_updatingState_then_displaysButtonLoadingAndDisablesFormActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			UpdatePasswordDialog(
				state = UpdatePassword.State.Updating(password = "1234", isPasswordVisible = true),
				titleText = "Actualizar clave",
				confirmText = "Actualizar",
				laterText = "Luego",
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave",
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(AuthUiTags.UpdatePasswordIdleContainer)
		assertNodeVisible(AuthUiTags.UpdatePasswordConfirmLoading)
		assertNodeDisabled(AuthUiTags.PasswordTextField)
		assertNodeDisabled(AuthUiTags.PasswordToggle)
		assertNodeDisabled(AuthUiTags.UpdatePasswordConfirmButton)
		assertNodeDisabled(BaseUiTags.ConfirmationDialogNegativeButton)
	}

	// The sheet keeps the field enabled until the `Updating` state reaches it. A key typed in that
	// window is dropped by the view model, so the field must come back showing the state's password.
	@Test
	fun when_aKeyIsDroppedAroundTheUpdate_then_theFieldShowsTheStatePasswordWhenBackToIdle() = runTuIndiceUiTest {
		var state: UpdatePassword.State by mutableStateOf(
			UpdatePassword.State.Idle(password = "", isPasswordVisible = true)
		)

		setTuIndiceTestContent {
			UpdatePasswordDialog(
				state = state,
				titleText = "Actualizar clave",
				confirmText = "Actualizar",
				laterText = "Luego",
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave",
				onPasswordChange = { password ->
					val current = state

					if (current is UpdatePassword.State.Idle) state = current.copy(password = password)
				},
				onPasswordVisibilityToggle = {},
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("a")
		waitForIdle()

		// The view model already left `Idle` and drops this key; the sheet has not heard yet.
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("b")
		runOnIdle { state = UpdatePassword.State.Updating(password = "a", isPasswordVisible = true) }
		waitForIdle()
		runOnIdle { state = UpdatePassword.State.Idle(password = "a", isPasswordVisible = true) }
		waitForIdle()

		assertEquals("a", onNodeWithTag(AuthUiTags.PasswordTextField).editableText())
	}
}

private fun SemanticsNodeInteraction.editableText() =
	fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.text
