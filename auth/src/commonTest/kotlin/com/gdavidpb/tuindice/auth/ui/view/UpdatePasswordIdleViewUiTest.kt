package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
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
				onPasswordVisibilityToggle = {},
				onConfirmClick = {},
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave"
			)
		}

		assertNodeVisible(AuthUiTags.UpdatePasswordIdleContainer)
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("nueva-clave")

		assertEquals("nueva-clave", latestPassword)
	}

	@Test
	fun when_imeDoneIsPressedWithPassword_then_dispatchesConfirmCallback() = runTuIndiceUiTest {
		var confirmClicks = 0

		setTuIndiceTestContent {
			var state by remember { mutableStateOf(UpdatePassword.State.Idle()) }

			UpdatePasswordIdleView(
				state = state,
				onPasswordChange = { value -> state = state.copy(password = value) },
				onPasswordVisibilityToggle = {},
				onConfirmClick = { confirmClicks++ },
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave"
			)
		}

		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("clave-ime")
		onNodeWithTag(AuthUiTags.PasswordTextField).performImeAction()

		assertEquals(1, confirmClicks)
	}

	@Test
	fun when_passwordToggleTapped_then_dispatchesVisibilityToggle() = runTuIndiceUiTest {
		var isPasswordVisible by mutableStateOf(false)

		setTuIndiceTestContent {
			var state by remember {
				mutableStateOf(UpdatePassword.State.Idle(isPasswordVisible = isPasswordVisible))
			}

			UpdatePasswordIdleView(
				state = state,
				onPasswordChange = {},
				onPasswordVisibilityToggle = {
					isPasswordVisible = !isPasswordVisible
					state = state.copy(isPasswordVisible = isPasswordVisible)
				},
				onConfirmClick = {},
				appNameText = "TuIndice",
				messageText = "Debes actualizar la clave de TuIndice",
				passwordLabelText = "Clave"
			)
		}

		onNodeWithContentDescription("Mostrar contraseña")
			.performClick()
		onNodeWithContentDescription("Ocultar contraseña")
			.assertExists()
	}
}
