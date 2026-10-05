package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SignInIdleViewUiTest {
	@Test
	fun when_serviceIsUnavailable_then_buttonIsDisabledAndTheFixedMessageIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInIdleView(
				state = SignIn.State.Idle(usbId = "12-34567", password = "1234", isServiceUnavailable = true),
				onUsbIdChange = {},
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onIdentifierModeToggle = {},
				onSignInClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				termsAndConditionsText = "Terminos",
				privacyPolicyText = "Privacidad",
				policiesText = "Acepto Terminos y Privacidad",
				usbIdLabelText = "USB",
				usbEmailLabelText = "Correo USB",
				usbIdPlaceholderText = "12-34567",
				usbEmailPlaceholderText = "correo@usb.ve",
				useUsbEmailContentDescription = "Iniciar con correo USB",
				useUsbIdContentDescription = "Usar USBID",
				passwordLabelText = "Clave",
				signInButtonText = "Entrar"
			)
		}

		assertNodeDisabled(AuthUiTags.SignInButton)
		assertNodeVisible(AuthUiTags.ServiceUnavailableMessage)
		onNodeWithText("Servicios de la universidad no disponibles. Vuelve a intentarlo en un momento.")
			.assertIsDisplayed()
	}

	@Test
	fun when_serviceIsAvailable_then_noUnavailableMessageIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInIdleView(
				state = SignIn.State.Idle(usbId = "12-34567", password = "1234"),
				onUsbIdChange = {},
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onIdentifierModeToggle = {},
				onSignInClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				termsAndConditionsText = "Terminos",
				privacyPolicyText = "Privacidad",
				policiesText = "Acepto Terminos y Privacidad",
				usbIdLabelText = "USB",
				usbEmailLabelText = "Correo USB",
				usbIdPlaceholderText = "12-34567",
				usbEmailPlaceholderText = "correo@usb.ve",
				useUsbEmailContentDescription = "Iniciar con correo USB",
				useUsbIdContentDescription = "Usar USBID",
				passwordLabelText = "Clave",
				signInButtonText = "Entrar"
			)
		}

		assertNodeEnabled(AuthUiTags.SignInButton)
		assertNodeHidden(AuthUiTags.ServiceUnavailableMessage)
	}

	@Test
	fun when_stateIsInvalid_then_signInButtonIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInIdleView(
				state = SignIn.State.Idle(usbId = "12-34", password = ""),
				onUsbIdChange = {},
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onIdentifierModeToggle = {},
				onSignInClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				termsAndConditionsText = "Terminos",
				privacyPolicyText = "Privacidad",
				policiesText = "Acepto Terminos y Privacidad",
				usbIdLabelText = "USB",
				usbEmailLabelText = "Correo USB",
				usbIdPlaceholderText = "12-34567",
				usbEmailPlaceholderText = "correo@usb.ve",
				useUsbEmailContentDescription = "Iniciar con correo USB",
				useUsbIdContentDescription = "Usar USBID",
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
				onIdentifierModeToggle = {},
				onSignInClick = { signInClicks++ },
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				termsAndConditionsText = "Terminos",
				privacyPolicyText = "Privacidad",
				policiesText = "Acepto Terminos y Privacidad",
				usbIdLabelText = "USB",
				usbEmailLabelText = "Correo USB",
				usbIdPlaceholderText = "12-34567",
				usbEmailPlaceholderText = "correo@usb.ve",
				useUsbEmailContentDescription = "Iniciar con correo USB",
				useUsbIdContentDescription = "Usar USBID",
				passwordLabelText = "Clave",
				signInButtonText = "Entrar"
			)
		}

		assertNodeEnabled(AuthUiTags.SignInButton)
		onNodeWithTag(AuthUiTags.SignInButton).performClick()

		assertEquals(1, signInClicks)
	}

	@Test
	fun when_stateHasValidUsbEmailAndPassword_then_signInButtonIsEnabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInIdleView(
				state = SignIn.State.Idle(
					usbId = "mail@usb.ve",
					password = "1234",
					identifierMode = SignInIdentifierMode.UsbEmail
				),
				onUsbIdChange = {},
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onIdentifierModeToggle = {},
				onSignInClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				termsAndConditionsText = "Terminos",
				privacyPolicyText = "Privacidad",
				policiesText = "Acepto Terminos y Privacidad",
				usbIdLabelText = "USB",
				usbEmailLabelText = "Correo USB",
				usbIdPlaceholderText = "12-34567",
				usbEmailPlaceholderText = "correo@usb.ve",
				useUsbEmailContentDescription = "Iniciar con correo USB",
				useUsbIdContentDescription = "Usar USBID",
				passwordLabelText = "Clave",
				signInButtonText = "Entrar"
			)
		}

		assertNodeEnabled(AuthUiTags.SignInButton)
	}

	@Test
	fun when_stateHasUsbIdInUsbEmailModeAndPassword_then_signInButtonIsEnabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInIdleView(
				state = SignIn.State.Idle(
					usbId = "12-34567@usb.ve",
					password = "1234",
					identifierMode = SignInIdentifierMode.UsbEmail
				),
				onUsbIdChange = {},
				onPasswordChange = {},
				onPasswordVisibilityToggle = {},
				onIdentifierModeToggle = {},
				onSignInClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				termsAndConditionsText = "Terminos",
				privacyPolicyText = "Privacidad",
				policiesText = "Acepto Terminos y Privacidad",
				usbIdLabelText = "USB",
				usbEmailLabelText = "Correo USB",
				usbIdPlaceholderText = "12-34567",
				usbEmailPlaceholderText = "correo@usb.ve",
				useUsbEmailContentDescription = "Iniciar con correo USB",
				useUsbIdContentDescription = "Usar USBID",
				passwordLabelText = "Clave",
				signInButtonText = "Entrar"
			)
		}

		assertNodeEnabled(AuthUiTags.SignInButton)
	}
}
