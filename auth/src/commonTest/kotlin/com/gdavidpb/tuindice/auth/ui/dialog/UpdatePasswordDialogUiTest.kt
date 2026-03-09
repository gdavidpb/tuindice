package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
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
				updatingTitleText = "Actualizando clave",
				confirmText = "Actualizar",
				laterText = "Luego",
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave",
				onPasswordChange = {},
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeDisabled(BaseUiTags.ConfirmationDialogPositiveButton)
	}

	@Test
	fun when_idleStateWithPasswordAndConfirmTapped_then_invokesConfirmCallback() = runTuIndiceUiTest {
		var confirmedPassword = ""

		setTuIndiceTestContent {
			UpdatePasswordDialog(
				state = UpdatePassword.State.Idle(password = "1234"),
				titleText = "Actualizar clave",
				updatingTitleText = "Actualizando clave",
				confirmText = "Actualizar",
				laterText = "Luego",
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave",
				onPasswordChange = {},
				onConfirmClick = { password -> confirmedPassword = password },
				onDismissRequest = {}
			)
		}

		assertNodeEnabled(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		assertEquals("1234", confirmedPassword)
	}

	@Test
	fun when_updatingState_then_displaysProgressContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			UpdatePasswordDialog(
				state = UpdatePassword.State.Updating(password = "1234"),
				titleText = "Actualizar clave",
				updatingTitleText = "Actualizando clave",
				confirmText = "Actualizar",
				laterText = "Luego",
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave",
				onPasswordChange = {},
				onConfirmClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeVisible(AuthUiTags.UpdatePasswordUpdatingIndicator)
		assertNodeDisabled(BaseUiTags.ConfirmationDialogPositiveButton)
		assertNodeDisabled(BaseUiTags.ConfirmationDialogNegativeButton)
	}
}
