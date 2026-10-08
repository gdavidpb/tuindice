package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.performTextInputPerCharacter
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SignInIdleViewUiTest {
	@Test
	fun when_serviceIsUnavailable_then_buttonIsDisabledAndTheFixedMessageIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInIdleView(
				isWaiting = false,
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
				isWaiting = false,
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
				isWaiting = false,
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
				isWaiting = false,
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
				isWaiting = false,
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
				isWaiting = false,
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

	// A wrong USB ID or password: both fields turn red and the one message sits under the password. The
	// text under a field is merged into the field's own node, so the tag is read from the unmerged tree.
	@Test
	fun when_theCredentialsWereRejected_then_bothFieldsAreInErrorAndTheMessageIsUnderThePassword() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInView(state = SignIn.State.Idle(usbId = "12-34567", password = "1234", rejection = invalidCredentials))
		}

		assertNodeVisible(AuthUiTags.SignInRejectedMarker, useUnmergedTree = true)
		onNodeWithTag(AuthUiTags.SignInRejectedMarker, useUnmergedTree = true)
			.assertTextEquals(invalidCredentials.message)
		onAllNodesWithText(invalidCredentials.message, useUnmergedTree = true).assertCountEquals(1)

		// What a screen reader reads: the password field carries the message, the identifier does not repeat it.
		assertTrue(onNodeWithTag(AuthUiTags.PasswordTextField).texts().contains(invalidCredentials.message))
		assertFalse(onNodeWithTag(AuthUiTags.UsbIdTextField).texts().contains(invalidCredentials.message))

		assertTrue(onNodeWithTag(AuthUiTags.UsbIdTextField).hasError(), "the identifier must be in error")
		assertTrue(onNodeWithTag(AuthUiTags.PasswordTextField).hasError(), "the password must be in error")

		val identifier = onNodeWithTag(AuthUiTags.UsbIdTextField).fetchSemanticsNode().boundsInRoot
		val password = onNodeWithTag(AuthUiTags.PasswordTextField).fetchSemanticsNode().boundsInRoot
		val message = onNodeWithTag(AuthUiTags.SignInRejectedMarker, useUnmergedTree = true)
			.fetchSemanticsNode().boundsInRoot
		val button = onNodeWithTag(AuthUiTags.SignInButton).fetchSemanticsNode().boundsInRoot
		assertTrue(message.top >= identifier.bottom, "the message is under the identifier, not on it")
		assertTrue(message.top > password.top && message.bottom <= password.bottom, "the message is the password's")
		assertTrue(message.bottom <= button.top, "the message sits above the button")

		assertEquals(
			LiveRegionMode.Polite,
			onNodeWithTag(AuthUiTags.SignInRejectedMarker, useUnmergedTree = true).liveRegion()
		)
		assertNodeHidden(AuthUiTags.ServiceUnavailableMessage)
	}

	// A disabled account or an unverified device: the fields stay as they are, the message is fixed under the button.
	@Test
	fun when_theAccountOrDeviceWasRejected_then_theMessageIsUnderTheButtonInTheErrorColorAndTheFieldsAreNotInError() =
		runTuIndiceUiTest {
			val verdicts = listOf(accountDisabled, untrusted)
			var state by mutableStateOf<SignIn.State.Idle>(SignIn.State.Idle(usbId = "12-34567", password = "1234"))
			var errorColor = Color.Unspecified

			setTuIndiceTestContent {
				errorColor = MaterialTheme.colorScheme.error

				SignInView(state = state)
			}

			for (verdict in verdicts) {
				runOnIdle { state = SignIn.State.Idle(usbId = "12-34567", password = "1234", rejection = verdict) }

				assertNodeVisible(AuthUiTags.SignInRejectedMarker)
				onNodeWithTag(AuthUiTags.SignInRejectedMarker).assertTextEquals(verdict.message)
				onAllNodesWithText(verdict.message).assertCountEquals(1)

				assertFalse(onNodeWithTag(AuthUiTags.UsbIdTextField).hasError(), "identifier must not be in error")
				assertFalse(onNodeWithTag(AuthUiTags.PasswordTextField).hasError(), "password must not be in error")

				val button = onNodeWithTag(AuthUiTags.SignInButton).fetchSemanticsNode().boundsInRoot
				val message = onNodeWithTag(AuthUiTags.SignInRejectedMarker).fetchSemanticsNode().boundsInRoot
				assertTrue(message.top >= button.bottom, "the message sits under the button")

				assertEquals(errorColor, onNodeWithTag(AuthUiTags.SignInRejectedMarker).textColor())
				assertEquals(LiveRegionMode.Polite, onNodeWithTag(AuthUiTags.SignInRejectedMarker).liveRegion())
				assertNodeHidden(AuthUiTags.ServiceUnavailableMessage)

				runOnIdle { state = SignIn.State.Idle(usbId = "12-34567", password = "1234") }
				waitUntil(timeoutMillis = 5_000L) {
					onAllNodesWithTag(AuthUiTags.SignInRejectedMarker).fetchSemanticsNodes().isEmpty()
				}
			}
		}

	// Anything that is not a verdict (no connection, timeout, throttling...) leaves the form untouched:
	// it only gets a snackbar.
	@Test
	fun when_nothingWasRejected_then_noMarkerNoMessageAndNoFieldInError() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInView(state = SignIn.State.Idle(usbId = "12-34567", password = "1234"))
		}

		assertNodeHidden(AuthUiTags.SignInRejectedMarker)
		assertNodeVisible(AuthUiTags.PasswordTextField)
		assertFalse(onNodeWithTag(AuthUiTags.UsbIdTextField).hasError(), "identifier must not be in error")
		assertFalse(onNodeWithTag(AuthUiTags.PasswordTextField).hasError(), "password must not be in error")
	}

	// The view model answers each key late and the rejection leaves with the first answer: what was typed keeps its
	// order and the signal is gone, however the answers interleave.
	@Test
	fun when_theUserTypesAfterARejectionWithALaggingEcho_then_theSignalLeavesWithoutLosingOrReorderingWhatWasTyped() =
		runTuIndiceUiTest {
			val input = "otra-clave-9"
			val echoed = mutableStateOf(
				SignIn.State.Idle(usbId = "12-34567", password = "", rejection = invalidCredentials)
			)
			val emitted = mutableListOf<String>()

			setTuIndiceTestContent {
				SignInView(
					state = echoed.value.copy(isPasswordVisible = true),
					onPasswordChange = { value -> emitted += value }
				)
			}

			assertNodeVisible(AuthUiTags.SignInRejectedMarker, useUnmergedTree = true)

			performTextInputPerCharacter(AuthUiTags.PasswordTextField, input) { index ->
				// The answer to the key from two keys ago lands just before this one.
				if (index >= 2) {
					runOnIdle {
						echoed.value = echoed.value.copy(password = emitted[index - 2], rejection = null)
					}
					waitForIdle()
				}
			}

			runOnIdle { echoed.value = echoed.value.copy(password = emitted.last(), rejection = null) }
			waitForIdle()

			assertEquals(input, emitted.last())
			assertEquals(input, onNodeWithTag(AuthUiTags.PasswordTextField).editableText())
			assertNodeHidden(AuthUiTags.SignInRejectedMarker, useUnmergedTree = true)
			assertFalse(onNodeWithTag(AuthUiTags.PasswordTextField).hasError(), "password must not be in error")
			assertFalse(onNodeWithTag(AuthUiTags.UsbIdTextField).hasError(), "identifier must not be in error")
		}

	// The same for the identifier: its border goes back to normal with the answer, and the text stays.
	@Test
	fun when_theUserTypesTheIdentifierAfterARejection_then_theSignalLeavesAndTheTextStays() = runTuIndiceUiTest {
		val echoed = mutableStateOf(
			SignIn.State.Idle(usbId = "12-34567", password = "1234", rejection = invalidCredentials)
		)

		setTuIndiceTestContent {
			SignInView(
				state = echoed.value,
				onUsbIdChange = { value -> echoed.value = echoed.value.copy(usbId = value, rejection = null) }
			)
		}

		assertNodeVisible(AuthUiTags.SignInRejectedMarker, useUnmergedTree = true)
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextReplacement("12-34568")
		waitForIdle()

		assertNodeHidden(AuthUiTags.SignInRejectedMarker, useUnmergedTree = true)
		assertEquals("12-34568", onNodeWithTag(AuthUiTags.UsbIdTextField).editableText())
		assertFalse(onNodeWithTag(AuthUiTags.UsbIdTextField).hasError(), "identifier must not be in error")
	}

	// The line under a field is reserved whether or not there is a message, so the form neither is tighter than it
	// was nor jumps when the message comes and goes.
	@Test
	fun when_thereIsNoError_then_bothFieldsStillReserveTheLineUnderThem() = runTuIndiceUiTest {
		var density: Density? = null

		setTuIndiceTestContent {
			density = LocalDensity.current

			SignInView(state = SignIn.State.Idle(usbId = "12-34567", password = "1234"))
		}

		val reserved = with(requireNotNull(density)) { (TextFieldDefaults.MinHeight + 16.dp).toPx() }

		for (tag in listOf(AuthUiTags.UsbIdTextField, AuthUiTags.PasswordTextField)) {
			val height = onNodeWithTag(tag).fetchSemanticsNode().size.height

			assertTrue(height >= reserved, "$tag is $height px tall, less than the field plus its reserved line ($reserved)")
		}
	}

	@Test
	fun when_theCredentialsWereRejected_then_theFieldsDoNotChangeHeightAndTheFormDoesNotJump() = runTuIndiceUiTest {
		var state by mutableStateOf(SignIn.State.Idle(usbId = "12-34567", password = "1234"))
		var density: Density? = null

		setTuIndiceTestContent {
			density = LocalDensity.current

			SignInView(state = state)
		}

		val identifierBefore = onNodeWithTag(AuthUiTags.UsbIdTextField).fetchSemanticsNode().boundsInRoot
		val passwordBefore = onNodeWithTag(AuthUiTags.PasswordTextField).fetchSemanticsNode().boundsInRoot

		runOnIdle { state = state.copy(rejection = invalidCredentials) }
		waitForIdle()

		val identifierAfter = onNodeWithTag(AuthUiTags.UsbIdTextField).fetchSemanticsNode().boundsInRoot
		val passwordAfter = onNodeWithTag(AuthUiTags.PasswordTextField).fetchSemanticsNode().boundsInRoot
		val tolerance = with(requireNotNull(density)) { 4.dp.toPx() }

		assertTrue(
			abs(identifierAfter.height - identifierBefore.height) <= tolerance,
			"the identifier changed height: ${identifierBefore.height} -> ${identifierAfter.height}"
		)
		assertTrue(
			abs(passwordAfter.height - passwordBefore.height) <= tolerance,
			"the password changed height: ${passwordBefore.height} -> ${passwordAfter.height}"
		)
		assertTrue(
			abs(passwordAfter.top - passwordBefore.top) <= tolerance,
			"the form moved: ${passwordBefore.top} -> ${passwordAfter.top}"
		)
	}

	// A field in error announces the reason of the rejection, not the default text of Material.
	@Test
	fun when_theCredentialsWereRejected_then_bothFieldsAnnounceTheMessageAsTheirError() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SignInView(state = SignIn.State.Idle(usbId = "12-34567", password = "1234", rejection = invalidCredentials))
		}

		assertEquals(invalidCredentials.message, onNodeWithTag(AuthUiTags.UsbIdTextField).errorDescription())
		assertEquals(invalidCredentials.message, onNodeWithTag(AuthUiTags.PasswordTextField).errorDescription())
	}

	@Composable
	private fun SignInView(
		state: SignIn.State.Idle,
		onUsbIdChange: (usbId: String) -> Unit = {},
		onPasswordChange: (password: String) -> Unit = {}
	) {
		SignInIdleView(
			isWaiting = false,
			state = state,
			onUsbIdChange = onUsbIdChange,
			onPasswordChange = onPasswordChange,
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

	private companion object {
		val invalidCredentials = SignIn.Rejection.InvalidCredentials(message = "Revisa tu USBID y contraseña")
		val accountDisabled = SignIn.Rejection.AccountDisabled(
			message = "Cuenta inhabilitada. Escríbenos a soporte@tuindice.app para recuperarla."
		)
		val untrusted = SignIn.Rejection.Untrusted(
			message = "No pudimos verificar la seguridad del dispositivo. Si persiste, escríbenos a soporte@tuindice.app."
		)
	}
}

private fun SemanticsNodeInteraction.errorDescription() =
	fetchSemanticsNode().config.getOrNull(SemanticsProperties.Error)

private fun SemanticsNodeInteraction.hasError() =
	fetchSemanticsNode().config.contains(SemanticsProperties.Error)

private fun SemanticsNodeInteraction.texts() =
	fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text).orEmpty().map { text -> text.text }

private fun SemanticsNodeInteraction.liveRegion() =
	fetchSemanticsNode().config.getOrNull(SemanticsProperties.LiveRegion)

private fun SemanticsNodeInteraction.editableText() =
	fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.text

private fun SemanticsNodeInteraction.textColor(): Color {
	val results = mutableListOf<TextLayoutResult>()
	val read = fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action

	assertTrue(read?.invoke(results) == true, "the node has no text layout")

	return results.first().layoutInput.style.color
}
