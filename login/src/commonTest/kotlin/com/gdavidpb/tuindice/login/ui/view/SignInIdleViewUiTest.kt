package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.ui.LoginUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SignInIdleViewUiTest {
	@Test
	fun when_stateIsInvalid_then_signInButtonIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInIdleView(
				state = SignIn.State.Idle(usbId = "12-34", password = ""),
				onUsbIdChange = {},
				onPasswordChange = {},
				onSignInClick = { _, _ -> },
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				termsAndConditionsText = "Terminos",
				privacyPolicyText = "Privacidad",
				policiesText = "Acepto Terminos y Privacidad",
				usbIdLabelText = "USB",
				passwordLabelText = "Clave",
				signInButtonText = "Entrar"
			)
		}

		assertNodeVisible(LoginUiTags.SignInIdleContainer)
		assertNodeDisabled(LoginUiTags.SignInButton)
	}

	@Test
	fun when_stateIsValidAndButtonTapped_then_invokesSignInCallback() = runTuIndiceUiTest {
		var receivedUsbId = ""
		var receivedPassword = ""

		setTuIndiceTestContent {
			SignInIdleView(
				state = SignIn.State.Idle(usbId = "12-34567", password = "1234"),
				onUsbIdChange = {},
				onPasswordChange = {},
				onSignInClick = { usbId, password ->
					receivedUsbId = usbId
					receivedPassword = password
				},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				termsAndConditionsText = "Terminos",
				privacyPolicyText = "Privacidad",
				policiesText = "Acepto Terminos y Privacidad",
				usbIdLabelText = "USB",
				passwordLabelText = "Clave",
				signInButtonText = "Entrar"
			)
		}

		assertNodeEnabled(LoginUiTags.SignInButton)
		onNodeWithTag(LoginUiTags.SignInButton).performClick()

		assertEquals("12-34567", receivedUsbId)
		assertEquals("1234", receivedPassword)
	}
}

