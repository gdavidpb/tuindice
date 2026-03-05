package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.ui.LoginUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class UpdatePasswordIdleViewUiTest {
	@Test
	fun when_passwordTyped_then_dispatchesPasswordChange() = runTuIndiceUiTest {
		var latestPassword = ""

		setTuIndiceTestContent {
			UpdatePasswordIdleView(
				state = UpdatePassword.State.Idle(),
				onPasswordChange = { value -> latestPassword = value },
				onConfirmClick = {},
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave"
			)
		}

		assertNodeVisible(LoginUiTags.UpdatePasswordIdleContainer)
		onNodeWithTag(LoginUiTags.PasswordTextField).performTextInput("nueva-clave")

		assertEquals("nueva-clave", latestPassword)
	}

	@Test
	fun when_imeDoneIsPressedWithPassword_then_dispatchesConfirmCallback() = runTuIndiceUiTest {
		var confirmedPassword: String? = null

		setTuIndiceTestContent {
			var state by remember { mutableStateOf(UpdatePassword.State.Idle()) }

			UpdatePasswordIdleView(
				state = state,
				onPasswordChange = { value -> state = state.copy(password = value) },
				onConfirmClick = { password -> confirmedPassword = password },
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave"
			)
		}

		onNodeWithTag(LoginUiTags.PasswordTextField).performTextInput("clave-ime")
		onNodeWithTag(LoginUiTags.PasswordTextField).performImeAction()

		assertEquals("clave-ime", confirmedPassword)
	}
}
