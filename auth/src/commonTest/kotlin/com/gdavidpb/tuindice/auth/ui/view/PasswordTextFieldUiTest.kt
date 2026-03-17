package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PasswordTextFieldUiTest {
	@Test
	fun when_userTypesPassword_then_emitsChangedValue() = runTuIndiceUiTest {
		var latestPassword = ""

		setTuIndiceTestContent {
			PasswordTextField(
				labelText = "Clave",
				password = "",
				isPasswordVisible = false,
				onPasswordChange = { value -> latestPassword = value },
				onPasswordVisibilityToggle = {}
			)
		}

		assertNodeVisible(AuthUiTags.PasswordTextField)
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("abc123")

		assertEquals("abc123", latestPassword)
	}

	@Test
	fun when_togglePasswordVisibilityTapped_then_keepsFieldVisible() = runTuIndiceUiTest {
		var isPasswordVisible by mutableStateOf(false)

		setTuIndiceTestContent {
			PasswordTextField(
				labelText = "Clave",
				password = "secreto",
				isPasswordVisible = isPasswordVisible,
				onPasswordChange = {},
				onPasswordVisibilityToggle = {
					isPasswordVisible = !isPasswordVisible
				}
			)
		}

		assertNodeVisible(AuthUiTags.PasswordToggle)
		onNodeWithContentDescription("Mostrar contraseña").assertIsDisplayed()
		onNodeWithTag(AuthUiTags.PasswordToggle).performClick()
		onNodeWithContentDescription("Ocultar contraseña").assertIsDisplayed()
		assertNodeVisible(AuthUiTags.PasswordTextField)
	}

	@Test
	fun when_passwordChangesExternally_then_updatesDisplayedValue() = runTuIndiceUiTest {
		val password = mutableStateOf("secreto")

		setTuIndiceTestContent {
			PasswordTextField(
				labelText = "Clave",
				password = password.value,
				isPasswordVisible = true,
				onPasswordChange = { value -> password.value = value },
				onPasswordVisibilityToggle = {}
			)
		}

		onNodeWithTag(AuthUiTags.PasswordTextField).assertTextContains("secreto")

		runOnIdle {
			password.value = "nueva-clave"
		}

		onNodeWithTag(AuthUiTags.PasswordTextField).assertTextContains("nueva-clave")
	}
}
