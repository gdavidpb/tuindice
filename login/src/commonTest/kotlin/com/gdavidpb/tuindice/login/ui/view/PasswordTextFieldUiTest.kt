package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.login.ui.LoginUiTags
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
				onPasswordChange = { value -> latestPassword = value }
			)
		}

		assertNodeVisible(LoginUiTags.PasswordTextField)
		onNodeWithTag(LoginUiTags.PasswordTextField).performTextInput("abc123")

		assertEquals("abc123", latestPassword)
	}

	@Test
	fun when_togglePasswordVisibilityTapped_then_keepsFieldVisible() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PasswordTextField(
				labelText = "Clave",
				password = "secreto",
				onPasswordChange = {}
			)
		}

		assertNodeVisible(LoginUiTags.PasswordToggle)
		onNodeWithContentDescription("Mostrar contraseña").assertIsDisplayed()
		onNodeWithTag(LoginUiTags.PasswordToggle).performClick()
		onNodeWithContentDescription("Ocultar contraseña").assertIsDisplayed()
		assertNodeVisible(LoginUiTags.PasswordTextField)
	}
}
