package com.gdavidpb.tuindice.auth.ui.screen

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
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SignInScreenUiTest {
	@Test
	fun when_stateIsIdle_then_displaysIdleContentAndBackground() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInScreen(
				state = SignIn.State.Idle(),
				onUsbIdChange = {},
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onIdentifierModeToggle = {},
				onSignInClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {}
			)
		}

		assertNodeVisible(AuthUiTags.AnimatedPatternBackground)
		assertNodeVisible(AuthUiTags.SignInIdleContainer)
	}

	@Test
	fun when_stateIsLoggingIn_then_displaysLoggingInContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInScreen(
				state = SignIn.State.LoggingIn(
					usbId = "12-34567",
					password = "1234",
					messages = listOf("Validando")
				),
				onUsbIdChange = {},
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onIdentifierModeToggle = {},
				onSignInClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {}
			)
		}

		assertNodeVisible(AuthUiTags.AnimatedPatternBackground)
		assertNodeVisible(AuthUiTags.SignInLoggingInContainer)
	}

	@Test
	fun when_idleStateHasValidCredentialsAndButtonTapped_then_invokesSignInCallback() = runTuIndiceUiTest {
		var signInClicks = 0

		setTuIndiceTestContent {
			SignInScreen(
				state = SignIn.State.Idle(
					usbId = "12-34567",
					password = "1234"
				),
				onUsbIdChange = {},
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onIdentifierModeToggle = {},
				onSignInClick = { signInClicks++ },
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {}
			)
		}

		onNodeWithTag(AuthUiTags.SignInButton).performClick()

		assertEquals(1, signInClicks)
	}

	@Test
	fun when_idleInputsChange_then_dispatchesUsbIdAndPasswordCallbacks() = runTuIndiceUiTest {
		var latestUsbId = ""
		var latestPassword = ""

		setTuIndiceTestContent {
			SignInScreen(
				state = SignIn.State.Idle(),
				onUsbIdChange = { usbId -> latestUsbId = usbId },
				onPasswordChange = { password -> latestPassword = password },
				onPasswordVisibilityToggle = {},
				onIdentifierModeToggle = {},
				onSignInClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {}
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("123")
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("1234")

		assertEquals("12-3", latestUsbId)
		assertEquals("1234", latestPassword)
	}

	// A failure that lands while the form is still sliding out brings the same `Idle` content back,
	// so its fields must show the state's text and not a key the view model dropped meanwhile.
	@Test
	fun when_aKeyIsDroppedWhileSigningIn_then_theFieldsShowTheStateTextWhenBackToIdle() = runTuIndiceUiTest {
		var state: SignIn.State by mutableStateOf(
			SignIn.State.Idle(usbId = "12-34567", password = "a", isPasswordVisible = true)
		)

		setTuIndiceTestContent {
			SignInScreen(
				state = state,
				onUsbIdChange = {},
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onIdentifierModeToggle = {},
				onSignInClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {}
			)
		}

		mainClock.autoAdvance = false

		// The view model is already in `LoggingIn` and drops these keys; the screen has not heard yet.
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("b")
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("9")
		state = SignIn.State.LoggingIn(usbId = "12-34567", password = "a", messages = listOf("Validando"))
		mainClock.advanceTimeBy(50)
		state = SignIn.State.Idle(usbId = "12-34567", password = "a", isPasswordVisible = true)
		mainClock.advanceTimeBy(50)
		mainClock.autoAdvance = true
		waitForIdle()

		assertEquals("a", onNodeWithTag(AuthUiTags.PasswordTextField).editableText())
		assertEquals("12-34567", onNodeWithTag(AuthUiTags.UsbIdTextField).editableText())
	}
}

private fun SemanticsNodeInteraction.editableText() =
	fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.text
