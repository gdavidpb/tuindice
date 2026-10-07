package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextRange
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.performTextInputPerCharacter
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

	// The view model answers each keystroke a frame or more later, off the main thread. An answer
	// that is already stale when it lands must not take back what was typed after it was sent.
	@Test
	fun when_staleEchoLandsBetweenKeystrokes_then_keepsEveryTypedCharacter() = runTuIndiceUiTest {
		val echoedPassword = mutableStateOf("")
		val emittedPasswords = mutableListOf<String>()

		setTuIndiceTestContent {
			PasswordTextField(
				labelText = "Clave",
				password = echoedPassword.value,
				isPasswordVisible = true,
				onPasswordChange = { value -> emittedPasswords += value },
				onPasswordVisibilityToggle = {}
			)
		}

		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("a")
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("b")

		// The answer to "a" lands only now, after "b" is already in the field.
		runOnIdle { echoedPassword.value = emittedPasswords.first() }
		waitForIdle()

		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("c")

		// The remaining answers land in the order the view model would publish them.
		emittedPasswords.drop(1).forEach { emitted ->
			runOnIdle { echoedPassword.value = emitted }
			waitForIdle()
		}

		assertEquals("abc", emittedPasswords.last())
		onNodeWithTag(AuthUiTags.PasswordTextField).assertTextContains("abc")
	}

	// The stale answer lands after "b", then the answer to "b", and only then comes "c".
	@Test
	fun when_staleEchoRestoresTheTextBeforeTheNextKey_then_keepsTheTypingOrder() = runTuIndiceUiTest {
		val echoedPassword = mutableStateOf("")
		val emittedPasswords = mutableListOf<String>()

		setTuIndiceTestContent {
			PasswordTextField(
				labelText = "Clave",
				password = echoedPassword.value,
				isPasswordVisible = true,
				onPasswordChange = { value -> emittedPasswords += value },
				onPasswordVisibilityToggle = {}
			)
		}

		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("a")
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("b")

		runOnIdle { echoedPassword.value = emittedPasswords[0] }
		waitForIdle()

		// The stale answer must not pull the field back: without this the echo of "ab" would hide it.
		onNodeWithTag(AuthUiTags.PasswordTextField).assertTextContains("ab")

		runOnIdle { echoedPassword.value = emittedPasswords[1] }
		waitForIdle()

		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("c")

		assertEquals("abc", emittedPasswords.last())
		onNodeWithTag(AuthUiTags.PasswordTextField).assertTextContains("abc")
	}

	@Test
	fun when_charactersAreTypedOneByOneWithALaggingEcho_then_emitsThePassword() = runTuIndiceUiTest {
		val input = "record-retry-pass"
		val echoedPassword = mutableStateOf("")
		val emittedPasswords = mutableListOf<String>()

		setTuIndiceTestContent {
			PasswordTextField(
				labelText = "Clave",
				password = echoedPassword.value,
				isPasswordVisible = true,
				onPasswordChange = { value -> emittedPasswords += value },
				onPasswordVisibilityToggle = {}
			)
		}

		performTextInputPerCharacter(AuthUiTags.PasswordTextField, input) { index ->
			// The answer to the keystroke from two keys ago lands just before this one.
			if (index >= 2) {
				runOnIdle { echoedPassword.value = emittedPasswords[index - 2] }
				waitForIdle()
			}
		}

		runOnIdle { echoedPassword.value = emittedPasswords.last() }
		waitForIdle()

		assertEquals(input, emittedPasswords.last())
		onNodeWithTag(AuthUiTags.PasswordTextField).assertTextContains(input)
	}

	@Test
	fun when_passwordFieldIsDisabled_then_disablesInputAndVisibilityToggle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PasswordTextField(
				labelText = "Clave",
				password = "secreto",
				isPasswordVisible = true,
				enabled = false,
				onPasswordChange = {},
				onPasswordVisibilityToggle = {}
			)
		}

		assertNodeDisabled(AuthUiTags.PasswordTextField)
		assertNodeDisabled(AuthUiTags.PasswordToggle)
	}

	// A key typed while the owner is entering its wait is shown by the field, but the view model
	// drops it. The field must show exactly the state's text on entering and on leaving the wait.
	@Test
	fun when_theOwnerWaitsAndDropsAnEdit_then_theFieldShowsTheStateTextOnEnteringAndLeavingTheWait() = runTuIndiceUiTest {
		val echoedPassword = mutableStateOf("")
		val isWaiting = mutableStateOf(false)
		var viewModelWaiting = false

		setTuIndiceTestContent {
			PasswordTextField(
				labelText = "Clave",
				password = echoedPassword.value,
				isPasswordVisible = true,
				isWaiting = isWaiting.value,
				onPasswordChange = { value ->
					if (!viewModelWaiting) echoedPassword.value = value
				},
				onPasswordVisibilityToggle = {}
			)
		}

		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("a")
		waitForIdle()

		// The view model is already waiting and drops this key; the field has not heard yet.
		viewModelWaiting = true
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("b")
		runOnIdle { isWaiting.value = true }
		waitForIdle()

		// The wait reached the field after the key did: the field comes back to the state's text.
		assertEquals("a", onNodeWithTag(AuthUiTags.PasswordTextField).editableText())

		// A key that sneaks in during the wait is dropped as well.
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("c")
		runOnIdle { isWaiting.value = false }
		viewModelWaiting = false
		waitForIdle()

		assertEquals("a", onNodeWithTag(AuthUiTags.PasswordTextField).editableText())
	}

	// The normal flow must not change: typing, the echoes and a wait that drops nothing leave the
	// text and the caret where the person put them.
	@Test
	fun when_theOwnerWaitsWithoutDroppingAnything_then_theFieldKeepsWhatWasTyped() = runTuIndiceUiTest {
		val echoedPassword = mutableStateOf("")
		val isWaiting = mutableStateOf(false)

		setTuIndiceTestContent {
			PasswordTextField(
				labelText = "Clave",
				password = echoedPassword.value,
				isPasswordVisible = true,
				isWaiting = isWaiting.value,
				onPasswordChange = { value -> echoedPassword.value = value },
				onPasswordVisibilityToggle = {}
			)
		}

		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("abc")
		runOnIdle { isWaiting.value = true }
		waitForIdle()
		runOnIdle { isWaiting.value = false }
		waitForIdle()

		assertEquals("abc", onNodeWithTag(AuthUiTags.PasswordTextField).editableText())
		assertEquals(TextRange(3), onNodeWithTag(AuthUiTags.PasswordTextField).selectionRange())
	}
}

private fun SemanticsNodeInteraction.editableText() =
	fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.text

private fun SemanticsNodeInteraction.selectionRange() =
	fetchSemanticsNode().config.getOrNull(SemanticsProperties.TextSelectionRange)
