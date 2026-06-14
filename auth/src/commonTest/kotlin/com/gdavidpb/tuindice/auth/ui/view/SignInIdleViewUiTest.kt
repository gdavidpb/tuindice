package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
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
				onPasswordVisibilityToggle = {},
				onSignInClick = {},
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

		assertNodeVisible(AuthUiTags.SignInIdleContainer)
		assertNodeDisabled(AuthUiTags.SignInButton)
	}

	@Test
	fun when_stateIsValidAndButtonTapped_then_invokesSignInCallback() = runTuIndiceUiTest {
		var signInClicks = 0

		setTuIndiceTestContent {
			SignInIdleView(
				state = SignIn.State.Idle(usbId = "12-34567", password = "1234"),
				onUsbIdChange = {},
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onSignInClick = { signInClicks++ },
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

		assertNodeEnabled(AuthUiTags.SignInButton)
		onNodeWithTag(AuthUiTags.SignInButton).performClick()

		assertEquals(1, signInClicks)
	}
}
