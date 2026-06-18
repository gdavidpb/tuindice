package com.gdavidpb.tuindice.auth.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
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
}
