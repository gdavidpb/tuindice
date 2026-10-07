package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextRange
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.performTextInputPerCharacter
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class UsbIdTextFieldUiTest {
	@Test
	fun when_userTypesUsbId_then_formatsAndEmitsValue() = runTuIndiceUiTest {
		var latestUsbId = ""

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = "",
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		assertNodeVisible(AuthUiTags.UsbIdTextField)
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("123")

		assertEquals("12-3", latestUsbId)
	}

	@Test
	fun when_userTypesNonNumericCharacters_then_emitsOnlyDigitsWithMask() = runTuIndiceUiTest {
		var latestUsbId = ""

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = "",
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("ab12c3")

		assertEquals("12-3", latestUsbId)
	}

	@Test
	fun when_userTypesMoreThanAllowed_then_keepsMaxFormattedLength() = runTuIndiceUiTest {
		var latestUsbId = ""

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = "",
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("1234567")
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("89")

		assertEquals("12-34567", latestUsbId)
	}

	@Test
	fun when_usbIdChangesExternally_then_updatesDisplayedValue() = runTuIndiceUiTest {
		val usbId = mutableStateOf("12-34567")

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = usbId.value,
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> usbId.value = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).assertTextContains("12-34567")

		runOnIdle {
			usbId.value = "20-26123"
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).assertTextContains("20-26123")
	}

	// Same race as the password field: an answer from the view model that is already stale when
	// it lands must not take back the digits typed after it was sent.
	@Test
	fun when_staleEchoLandsBetweenKeystrokes_then_keepsEveryTypedDigit() = runTuIndiceUiTest {
		val echoedUsbId = mutableStateOf("")
		val emittedUsbIds = mutableListOf<String>()

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = echoedUsbId.value,
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> emittedUsbIds += value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("1")
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("2")

		// The answer to "1" lands only now, after "2" is already in the field.
		runOnIdle { echoedUsbId.value = emittedUsbIds.first() }
		waitForIdle()

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("3")

		// The remaining answers land in the order the view model would publish them.
		emittedUsbIds.drop(1).forEach { emitted ->
			runOnIdle { echoedUsbId.value = emitted }
			waitForIdle()
		}

		assertEquals("12-3", emittedUsbIds.last())
		onNodeWithTag(AuthUiTags.UsbIdTextField).assertTextContains("12-3")
	}

	@Test
	fun when_digitsAreTypedOneByOneWithALaggingEcho_then_emitsTheMaskedIdentifier() = runTuIndiceUiTest {
		val echoedUsbId = mutableStateOf("")
		val emittedUsbIds = mutableListOf<String>()

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = echoedUsbId.value,
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> emittedUsbIds += value }
			)
		}

		performTextInputPerCharacter(AuthUiTags.UsbIdTextField, "1234567") { index ->
			// The answer to the keystroke from two keys ago lands just before this one.
			if (index >= 2) {
				runOnIdle { echoedUsbId.value = emittedUsbIds[index - 2] }
				waitForIdle()
			}
		}

		runOnIdle { echoedUsbId.value = emittedUsbIds.last() }
		waitForIdle()

		assertEquals("12-34567", emittedUsbIds.last())
		assertEquals("12-34567", onNodeWithTag(AuthUiTags.UsbIdTextField).editableText())
	}

	@Test
	fun when_moreThanSevenDigitsArePasted_then_keepsTheFirstSeven() = runTuIndiceUiTest {
		val emittedUsbIds = mutableListOf<String>()

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = "",
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> emittedUsbIds += value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextReplacement("1234567890")

		assertEquals("12-34567", emittedUsbIds.lastOrNull())
		assertEquals("12-34567", onNodeWithTag(AuthUiTags.UsbIdTextField).editableText())
	}

	@Test
	fun when_aDigitIsInsertedInTheMiddle_then_theCaretStaysAfterIt() = runTuIndiceUiTest {
		val usbId = mutableStateOf("12-345")

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = usbId.value,
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> usbId.value = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInputSelection(TextRange(4))
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("9")

		assertEquals("12-3945", onNodeWithTag(AuthUiTags.UsbIdTextField).editableText())
		assertEquals(TextRange(5), onNodeWithTag(AuthUiTags.UsbIdTextField).selectionRange())
	}

	@Test
	fun when_aDigitIsInsertedIntoAFullField_then_nothingChanges() = runTuIndiceUiTest {
		val emittedUsbIds = mutableListOf<String>()

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = "12-34567",
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> emittedUsbIds += value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInputSelection(TextRange(4))
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("9")

		assertTrue(emittedUsbIds.isEmpty(), "Nothing may be emitted, got $emittedUsbIds")
		assertEquals("12-34567", onNodeWithTag(AuthUiTags.UsbIdTextField).editableText())
	}

	@Test
	fun when_identifierModeChangesAndTheCallerClears_then_theFieldIsEmptied() = runTuIndiceUiTest {
		val usbId = mutableStateOf("")
		val identifierMode = mutableStateOf(SignInIdentifierMode.UsbEmail)

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "Correo USB",
				placeholderText = "correo@usb.ve",
				identifierMode = identifierMode.value,
				usbId = usbId.value,
				toggleContentDescription = "Usar USBID",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> usbId.value = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("ab")
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextClearance()
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("cd")

		runOnIdle {
			identifierMode.value = SignInIdentifierMode.UsbId
			usbId.value = ""
		}
		waitForIdle()

		assertEquals("", onNodeWithTag(AuthUiTags.UsbIdTextField).editableText())
	}

	@Test
	fun when_emailModeReceivesLetters_then_emitsTextWithoutMask() = runTuIndiceUiTest {
		var latestUsbId = ""

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "Correo USB",
				placeholderText = "correo@usb.ve",
				identifierMode = SignInIdentifierMode.UsbEmail,
				usbId = "",
				toggleContentDescription = "Usar USBID",
				showTogglePulse = false,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value -> latestUsbId = value }
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("mail@usb.ve")

		assertEquals("mail@usb.ve", latestUsbId)
	}

	@Test
	fun when_showTogglePulseIsTrueAndFieldIsEmpty_then_pulseIsVisible() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				UsbIdTextField(
					labelText = "USB ID",
					placeholderText = "12-34567",
					usbId = "",
					toggleContentDescription = "Iniciar con correo USB",
					showTogglePulse = true,
					onIdentifierModeToggle = {},
					onUsbIdChange = {}
				)
			}
		}

		waitForIdle()

		assertNodeVisible(AuthUiTags.IdentifierModeTogglePulse, useUnmergedTree = true)
	}

	@Test
	fun when_showTogglePulseIsTrueButFieldHasValue_then_pulseIsHidden() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = "12-34567",
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = true,
				onIdentifierModeToggle = {},
				onUsbIdChange = {}
			)
		}

		assertNodeHidden(AuthUiTags.IdentifierModeTogglePulse, useUnmergedTree = true)
	}

	@Test
	fun when_theOwnerWaitsAndDropsAnEdit_then_theFieldShowsTheStateTextOnEnteringAndLeavingTheWait() = runTuIndiceUiTest {
		val echoedUsbId = mutableStateOf("")
		val isWaiting = mutableStateOf(false)
		var viewModelWaiting = false

		setTuIndiceTestContent {
			UsbIdTextField(
				labelText = "USB ID",
				placeholderText = "12-34567",
				usbId = echoedUsbId.value,
				toggleContentDescription = "Iniciar con correo USB",
				showTogglePulse = false,
				isWaiting = isWaiting.value,
				onIdentifierModeToggle = {},
				onUsbIdChange = { value ->
					if (!viewModelWaiting) echoedUsbId.value = value
				}
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("123")
		waitForIdle()

		viewModelWaiting = true
		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("4")
		runOnIdle { isWaiting.value = true }
		waitForIdle()

		assertEquals("12-3", onNodeWithTag(AuthUiTags.UsbIdTextField).editableText())

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("5")
		runOnIdle { isWaiting.value = false }
		viewModelWaiting = false
		waitForIdle()

		assertEquals("12-3", onNodeWithTag(AuthUiTags.UsbIdTextField).editableText())
	}
}

private fun SemanticsNodeInteraction.editableText() =
	fetchSemanticsNode().config.getOrNull(SemanticsProperties.EditableText)?.text

private fun SemanticsNodeInteraction.selectionRange() =
	fetchSemanticsNode().config.getOrNull(SemanticsProperties.TextSelectionRange)
